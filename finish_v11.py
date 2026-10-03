from pathlib import Path
import subprocess,shutil
from PIL import Image,ImageDraw
ROOT=Path(__file__).parent;OUT=ROOT/'release-1.1.0';OUT.mkdir(exist_ok=True)
ffmpeg=ROOT/'.tools/ffmpeg.exe'
subprocess.run([str(ffmpeg),'-hide_banner','-loglevel','error','-y','-i',str(ROOT/'evidence/showcase-v11.mkv'),'-map','0:v:0','-c:v','copy','-movflags','+faststart',str(OUT/'Galactus-1.1.0-demo.mp4')],check=True)
for old,new in [('surfer-v11-release','Silver-Surfer'),('galactus-v11-release','Galactus'),('rift-v11-release','Falia-cosmica')]:
    shutil.copy2(ROOT/f'run-client/screenshots/{old}.png',OUT/f'{new}.png')
sheet=Image.new('RGB',(1280,720))
for i,t in enumerate([4,10,21,32]):
    path=ROOT/f'evidence/v11-frame-{i}.png'
    subprocess.run([str(ffmpeg),'-hide_banner','-loglevel','error','-y','-ss',str(t),'-i',str(OUT/'Galactus-1.1.0-demo.mp4'),'-frames:v','1',str(path)],check=True)
    frame=Image.open(path).convert('RGB').resize((640,360))
    sheet.paste(frame,(i%2*640,i//2*360))
sheet.save(ROOT/'evidence/v11-contact-sheet.png')
print('Native preview frames and MP4 packaged; contact sheet written for visual review')
