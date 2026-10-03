from pathlib import Path
import shutil,zipfile,hashlib,json,re
ROOT=Path(__file__).parent
VERSION=re.search(r"version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
OUT=ROOT/f'release-{VERSION}';OUT.mkdir(exist_ok=True)
for name in [f'galactus-first-steps-{VERSION}.jar',f'galactus-first-steps-{VERSION}-sources.jar']:
    shutil.copy2(ROOT/'build/libs'/name,OUT/name)
for name in ['README.md','LICENSE','CHANGELOG.md']:
    shutil.copy2(ROOT/name,OUT/name)
shutil.copy2(ROOT/'run-prod/config/galactus.json',OUT/'galactus.example.json')
sources=list((ROOT/'src').rglob('*'))+list((ROOT/'gradle').rglob('*'))
sources += [ROOT/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew.bat','build.ps1','bootstrap.ps1','generate_assets.py','generate_character_art.py','generate_audio.py','revise_presentation.py','package_release.py','README.md','LICENSE','CHANGELOG.md','.gitignore']]
with zipfile.ZipFile(OUT/f'galactus-first-steps-{VERSION}-source-project.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted(sources):
        if p.is_file():z.write(p,'galactus-first-steps/'+p.relative_to(ROOT).as_posix())
with zipfile.ZipFile(OUT/f'galactus-first-steps-{VERSION}.jar') as z:
    entries=z.namelist();meta=json.loads(z.read('fabric.mod.json'))
    assert meta['depends']['minecraft']=='1.21.1'
    assert meta['version']==VERSION
    assert 'assets/galactus/textures/entity/herald_glow.png' in entries
    assert 'assets/galactus/sounds/surfer_signal.ogg' in entries
    assert not any(n.startswith('net/minecraft/') for n in entries)
    assert len([n for n in entries if n.startswith('data/galactus/recipe/') and n.endswith('.json')])==5
    assert 'LICENSE_galactus' in entries
with (OUT/'SHA256SUMS.txt').open('w') as f:
    for p in sorted(OUT.iterdir()):
        if p.is_file() and p.name!='SHA256SUMS.txt':f.write(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n')
for p in sorted(OUT.iterdir()):print(p.name,p.stat().st_size)
