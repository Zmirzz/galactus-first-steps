from pathlib import Path
import struct,zlib,json
ROOT=Path(__file__).parent/'src/main/resources'
def png(path,w,h,pixel):
    def chunk(tag,data): return struct.pack('>I',len(data))+tag+data+struct.pack('>I',zlib.crc32(tag+data)&0xffffffff)
    rows=b''.join(b'\0'+b''.join(bytes(pixel(x,y)) for x in range(w)) for y in range(h))
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(rows))+chunk(b'IEND',b''))
for name in ['galactus','herald']:
    def color(x,y):
        base=(105,50,126) if x<32 and y<32 else (74,53,93) if x<32 else (155,91,163) if y<32 else (147,109,88)
        if x>=48 and 16<=y<20: base=(255,219,139)
        if name=='herald': base=(174,190,203)
        shade=12 if (x+y)%8==0 else -8 if x%8==7 else 0
        return (*[max(0,min(255,v+shade)) for v in base],255)
    png(ROOT/f'assets/galactus/textures/entity/{name}.png',64,64,color)
print('Original cosmic textures written')
def write(path,obj):
    p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2),encoding='utf-8')
png(ROOT/'assets/galactus/textures/entity/anchor.png',64,64,lambda x,y: (174+(x%8)*7,55+(y%8)*10,218,255) if y<32 else (40,20,58,255))
items={'cosmic_receiver':('Cosmic Receiver',(89,218,245)), 'cosmic_shard':('Cosmic Shard',(219,97,250)),
       'ultimate_nullifier':('Ultimate Nullifier',(241,191,83)), 'redemption_sigil':('Redemption Sigil',(139,239,224)),
       'phase_conductor':('Phase Conductor',(131,136,255)), 'world_savior':('World Savior',(246,198,92))}
for name,(label,col) in items.items():
    def ipix(x,y,col=col,name=name):
        # Distinct readable silhouettes, drawn directly on the native 16 px item grid.
        if name=='cosmic_shard': inside=abs(x-8)/4+abs(y-8)/7<1
        elif name=='ultimate_nullifier': inside=(3<=x<=12 and 5<=y<=11) or (5<=x<=7 and 12<=y<=14) or (x in [3,12] and 2<=y<=4)
        elif name=='phase_conductor': inside=abs(x-8)<=2 and 2<=y<=13 or (4<=x<=12 and y in [4,10])
        elif name=='world_savior': inside=(4<=x<=11 and 2<=y<=8) or (x in [7,8] and 8<=y<=12) or (4<=x<=11 and y==13)
        elif name=='cosmic_receiver': inside=abs(x-7.5)+abs(y-7.5)<7
        else: inside=3<=x<=12 and 3<=y<=12 and (x in [3,4,11,12] or y in [3,4,11,12] or abs(x-y)<2)
        if not inside:return (0,0,0,0)
        shade=40 if x+y<14 else -35
        if (x in [6,7,8,9] and y in [6,7,8,9]): return (243,238,220,255)
        return (*[min(255,max(0,c+shade)) for c in col],255)
    png(ROOT/f'assets/galactus/textures/item/{name}.png',16,16,ipix)
    write(f'assets/galactus/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'galactus:item/{name}'}})
def corePixel(x,y):
    if x in [0,1,14,15] or y in [0,1,14,15]:return (50,28,68,255)
    if abs(x-7.5)+abs(y-7.5)<4:return (198,241,252,255)
    return (84+x*5,52+y*4,157,255)
png(ROOT/'assets/galactus/textures/block/dimensional_core.png',16,16,corePixel)
write('assets/galactus/models/block/dimensional_core.json',{'parent':'minecraft:block/cube_all','textures':{'all':'galactus:block/dimensional_core'}})
write('assets/galactus/models/item/dimensional_core.json',{'parent':'galactus:block/dimensional_core'})
write('assets/galactus/blockstates/dimensional_core.json',{'variants':{'':{'model':'galactus:block/dimensional_core'}}})
write('data/galactus/loot_table/blocks/dimensional_core.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'galactus:dimensional_core'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
write('data/minecraft/tags/block/mineable/pickaxe.json',{'replace':False,'values':['galactus:dimensional_core']})
lang={f'item.galactus.{n}':v[0] for n,v in items.items()}
lang.update({'block.galactus.dimensional_core':'Dimensional Core','entity.galactus.galactus':'Galactus','entity.galactus.herald':'Shalla-Bal','entity.galactus.cosmic_anchor':'Cosmic Anchor','itemGroup.galactus':'Galactus: The Coming Hunger'})
write('assets/galactus/lang/en_us.json',lang)
ro=dict(lang);ro.update({'item.galactus.cosmic_receiver':'Receptor Cosmic','item.galactus.cosmic_shard':'Fragment Cosmic','item.galactus.ultimate_nullifier':'Anulatorul Suprem','item.galactus.redemption_sigil':'Sigiliul Mantuirii','item.galactus.phase_conductor':'Conductor de Faza','item.galactus.world_savior':'Salvatorul Lumii','block.galactus.dimensional_core':'Nucleu Dimensional','entity.galactus.cosmic_anchor':'Ancora Cosmica'})
write('assets/galactus/lang/ro_ro.json',ro)
recipes={
 'cosmic_receiver':([' I ','ACA',' I '],{'I':'minecraft:iron_ingot','A':'minecraft:amethyst_shard','C':'minecraft:compass'}),
 'ultimate_nullifier':(['SDS','ENE','SDS'],{'S':'galactus:cosmic_shard','D':'minecraft:diamond','E':'minecraft:echo_shard','N':'minecraft:nether_star'}),
 'redemption_sigil':(['BEB','GNG','BDE'],{'B':'minecraft:book','E':'minecraft:emerald','G':'minecraft:gold_ingot','N':'minecraft:nether_star','D':'minecraft:diamond_block'}),
 'phase_conductor':(['APA','PNP','APA'],{'A':'minecraft:amethyst_shard','P':'minecraft:ender_pearl','N':'minecraft:nether_star'}),
 'dimensional_core':(['OEO','EDE','OEO'],{'O':'minecraft:obsidian','E':'minecraft:ender_eye','D':'minecraft:diamond_block'})}
for name,(pattern,keys) in recipes.items():
    write(f'data/galactus/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':{k:{'item':v} for k,v in keys.items()},'result':{'id':f'galactus:{name}','count':1}})
write('data/galactus/advancement/recipes/cosmic_research.json',{'criteria':{'receiver':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['galactus:cosmic_receiver']}]}}},'rewards':{'recipes':[f'galactus:{r}' for r in recipes]}})
png(ROOT/'assets/galactus/icon.png',128,128,lambda x,y: (169,84,188,255) if (26<=x<=40 or 88<=x<=102) and 12<=y<=65 else (110,58,137,255) if 40<=x<=88 and 36<=y<=97 else (240,204,98,255) if 46<=x<=82 and 63<=y<=68 else (20,13,32,255))
print('Recipes, native models, translations, item art and icon written')
