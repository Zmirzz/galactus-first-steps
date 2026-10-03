from lab_rcon import commands
from pathlib import Path
import json,time,sys,re
ROOT=Path(__file__).parent
REPORT=ROOT/'evidence/live-tests.txt'
def cmd(*args):
    out=commands(*args)
    with REPORT.open('a',encoding='utf8') as f:
        for a,b in zip(args,out):
            line=f'{a} => {b}';print(line,flush=True);f.write(line+'\n')
    return out
def bridge(action,**kw):
    rid=time.time_ns()//1_000_000
    (ROOT/'run-client/lab-control.json').write_text(json.dumps(dict(id=rid,action=action,**kw)),encoding='utf8')
    for _ in range(50):
        time.sleep(.1)
        try:
            result=json.loads((ROOT/'run-client/lab-result.json').read_text())
            if result['id']==rid:
                print('CLIENT',result,flush=True);return result
        except (FileNotFoundError,json.JSONDecodeError):pass
    raise RuntimeError('Client bridge did not process '+action)
def expect(text):
    status=cmd('galactus status')[0]
    assert text in status,(text,status)
    with REPORT.open('a',encoding='utf8') as f:f.write('PASS '+text+'\n')
if __name__=='__main__':
    phase=sys.argv[1]
    if phase=='start':
        cmd('galactus reset','kill @e[type=galactus:galactus]','tp @a 0 -60 60 -45 -5','clear @a','gamemode creative @a','gamerule doMobSpawning false','time set day','execute at @a run galactus start')
        time.sleep(7);expect('stage=1');cmd('galactus_lab assert')
        bridge('shot',name='01-herald');cmd('galactus_lab elapse 3598');time.sleep(3);expect('stage=2');bridge('shot',name='02-arrival')
        cmd('galactus_lab elapse 44');time.sleep(3);expect('stage=3');cmd('galactus_lab assert')
        bridge('shot',name='03-invasion')
    elif phase=='nullifier':
        # Genuine player-attributed damage breaks anchors and produces the real dropped shards.
        cmd('galactus_lab breakanchors');time.sleep(2);expect('broken=15')
        cmd('execute as @e[type=minecraft:item] run data get entity @s Item')
        cmd('clear @a','gamemode survival @a','effect give @a minecraft:resistance 300 4 true','execute at @e[type=galactus:galactus,limit=1,sort=nearest] run tp @a ~ ~ ~20','item replace entity @a weapon.mainhand with galactus:ultimate_nullifier')
        bridge('use',hold=110);time.sleep(7);expect('victory=NULLIFIER');bridge('shot',name='04-nullifier-victory')
    elif phase=='herald':
        cmd('galactus reset','gamemode creative @a','tp @a 0 -60 0','execute at @a run galactus start','item replace entity @a weapon.mainhand with galactus:redemption_sigil','execute at @e[type=galactus:herald,limit=1] run tp @a ~ ~ ~1')
        time.sleep(.4);bridge('herald');time.sleep(1);expect('ally=true')
        cmd('galactus invade');time.sleep(7);cmd('galactus_lab breakanchors','execute as @e[type=galactus:galactus] run damage @s 800 minecraft:player_attack by @a[limit=1]');time.sleep(3);expect('victory=HERALD');bridge('shot',name='05-herald-victory')
    elif phase=='portal':
        cmd('galactus reset','gamemode creative @a','tp @a 0 -60 0','execute at @a run galactus start','galactus invade');time.sleep(7)
        cmd('galactus_lab breakanchors',
            'setblock 5 -61 0 minecraft:beacon','setblock 5 -60 0 galactus:dimensional_core',
            'setblock 4 -60 0 minecraft:obsidian','setblock 6 -60 0 minecraft:obsidian','setblock 5 -60 1 minecraft:obsidian','setblock 5 -60 -1 minecraft:obsidian',
            'setblock 4 -60 1 minecraft:gold_block','setblock 4 -60 -1 minecraft:gold_block','setblock 6 -60 1 minecraft:gold_block','setblock 6 -60 -1 minecraft:gold_block',
            'tp @a 5 -59 3 180 15','clear @a','item replace entity @a weapon.mainhand with galactus:phase_conductor','give @a minecraft:ender_pearl 32','gamemode survival @a','effect give @a minecraft:resistance 300 4 true')
        bridge('core',x=5,y=-60,z=0);time.sleep(3)
        status=cmd('galactus status')[0];assert re.search(r'portal=[1-9]',status),status
        cmd('data get entity @a Inventory');bridge('shot',name='06-portal-charging')
    elif phase=='portalfinish':
        expect('victory=PORTAL');bridge('shot',name='07-portal-victory')
    elif phase=='occupied':
        cmd('galactus reset','gamemode creative @a','tp @a 0 -60 0','execute at @a run galactus start','galactus invade','galactus_lab elapse 1200')
        time.sleep(2);expect('stage=4');bridge('shot',name='08-occupied');cmd('galactus_lab assert','save-all flush','stop')
