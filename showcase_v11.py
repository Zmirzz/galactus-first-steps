"""Native gameplay showcase in the isolated production world (no input injection)."""
from live_test import cmd,bridge,ROOT
import os,sys,time,subprocess
hwnd=sys.argv[1]
env=dict(os.environ)
env['UM_FFMPEG_WIN']=str(ROOT/'.tools/ffmpeg.exe')
env['PYTHONPATH']=str(ROOT/'.tools/python-libs')+';C:/Users/razva/.codex/plugins/cache/universal-modder/universal-modder/0.2.0'
cmd('galactus reset','gamemode creative @a','clear @a','tp @a 18.5 63 14.5 -90 2','time set 5000','weather clear')
bridge('presentation',clean=False,fov=50,language='ro_ro')
log=(ROOT/'evidence/recording-v11.txt').open('w')
recorder=subprocess.Popen([sys.executable,'-m','um','win','record','--hwnd',hwnd,'--out',str(ROOT/'evidence/showcase-v11'),'--seconds','38','--fps','30'],env=env,stdout=log,stderr=subprocess.STDOUT)
time.sleep(2)
cmd('execute at @a run galactus start')
time.sleep(9)
bridge('presentation',clean=True)
bridge('shot',name='surfer-v11-release')
time.sleep(3)
cmd('gamemode spectator @a','execute positioned 66 0 14 positioned over motion_blocking_no_leaves run tp @a ~ ~8 ~65 180 -14','galactus arrive')
bridge('presentation',clean=False,fov=55)
time.sleep(5)
bridge('presentation',clean=True)
time.sleep(2)
bridge('shot',name='rift-v11-release')
time.sleep(3)
cmd('galactus invade')
bridge('presentation',clean=False)
time.sleep(5)
bridge('presentation',clean=True)
cmd('execute at @e[type=galactus:galactus,limit=1] run tp @a ~18 ~7 ~64 164 -14')
time.sleep(2)
bridge('shot',name='galactus-v11-release')
try:recorder.wait(timeout=15)
except subprocess.TimeoutExpired:recorder.terminate();raise
assert recorder.returncode==0,recorder.returncode
cmd('galactus status','execute as @e[type=galactus:galactus] run data get entity @s Pos')
print('38-second native gameplay showcase complete',flush=True)
