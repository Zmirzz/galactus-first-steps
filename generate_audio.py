"""Short original synthesized cosmic stingers. No sampled film audio."""
from pathlib import Path
import math, wave, struct, subprocess, json, os, shutil
ROOT=Path(__file__).parent
OUT=ROOT/'src/main/resources/assets/galactus/sounds'
OUT.mkdir(parents=True,exist_ok=True)
rate=44100
ffmpeg=os.environ.get('FFMPEG') or shutil.which('ffmpeg') or str(ROOT/'.tools/ffmpeg.exe')
for name,duration,tones in [('surfer_signal',3.8,[220,330,440,659.25]),('cosmic_arrival',5.6,[43.65,65.41,92.5,130.81])]:
    samples=[]
    for n in range(int(rate*duration)):
        t=n/rate; q=t/duration
        envelope=math.sin(math.pi*q)**1.4
        value=0
        for i,freq in enumerate(tones):
            phase=2*math.pi*freq*t+.35*math.sin(t*1.7+i)
            value += math.sin(phase)/(5+i*2)
            value += math.sin(phase*2)/(24+i*5)
        # Restrained shimmering harmonic swell, all deterministic synthesis.
        value += math.sin(2*math.pi*(1100*t+35*t*t))*.035*math.sin(math.pi*q)**3
        samples.append(struct.pack('<h',int(max(-.8,min(.8,value*envelope))*32767)))
    wav=ROOT/'evidence'/f'{name}.wav';wav.parent.mkdir(exist_ok=True)
    with wave.open(str(wav),'wb') as f:
        f.setnchannels(1);f.setsampwidth(2);f.setframerate(rate);f.writeframes(b''.join(samples))
    subprocess.run([ffmpeg,'-hide_banner','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(OUT/f'{name}.ogg')],check=True)
(OUT.parent/'sounds.json').write_text(json.dumps({n:{'subtitle':f'galactus.sound.{n}','sounds':[f'galactus:{n}']} for n in ['surfer_signal','cosmic_arrival']},indent=2),encoding='utf8')
print('Two original OGG cosmic stingers written')
