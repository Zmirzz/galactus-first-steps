from pathlib import Path
import json
ROOT=Path(__file__).parent
translations={
 'galactus.sound.surfer_signal': ('A cosmic signal resonates','Un semnal cosmic răsună'),
 'galactus.sound.cosmic_arrival': ('The dimensional rift trembles','Falia dimensională se cutremură'),
 'entity.galactus.herald': ('Silver Surfer','Silver Surfer'),
 'galactus.surfer.name': ('Silver Surfer • Herald of Galactus','Silver Surfer • Heraldul lui Galactus'),
 'galactus.surfer.ally': ('Silver Surfer • Your ally','Silver Surfer • Aliatul tău'),
 'galactus.bar.prepare': ('THE COMING HUNGER   •   %s to prepare','FOAMEA COSMICĂ   •   %s pentru pregătire'),
 'galactus.bar.arrival': ('THE RIFT IS OPENING   •   %ss','FALIA SE DESCHIDE   •   %ss'),
 'galactus.title.herald': ('SILVER SURFER','SILVER SURFER'),
 'galactus.subtitle.herald': ('The herald has found your world.','Heraldul ți-a găsit lumea.'),
 'galactus.title.arrival': ('THE SKY FRACTURES','CERUL SE FRACTUREAZĂ'),
 'galactus.subtitle.arrival': ('The Devourer of Worlds is coming.','Devoratorul de Lumi se apropie.'),
 'galactus.title.invasion': ('GALACTUS','GALACTUS'),
 'galactus.subtitle.invasion': ('Break the anchors. Save your world.','Distruge ancorele. Salvează-ți lumea.'),
 'galactus.title.saved': ('WORLD SAVED','LUMEA ESTE SALVATĂ'),
 'galactus.subtitle.saved': ('The hunger has ended.','Foamea cosmică s-a sfârșit.'),
 'galactus.dialogue.chosen': ('SILVER SURFER: Your world has been chosen. Galactus is coming.', 'SILVER SURFER: Lumea ta a fost aleasă. Galactus se apropie.'),
 'galactus.dialogue.prepare': ('Prepare while you can. Your journal explains the anchors and three ways to stop him.', 'Pregătește-te cât mai ai timp. Jurnalul explică ancorele și cele trei căi de a-l opri.'),
 'galactus.dialogue.redemption': ('SILVER SURFER: I will not sacrifice another world. Break his anchors and weaken him. I will open the rift.', 'SILVER SURFER: Nu voi sacrifica încă o lume. Distruge ancorele și slăbește-l. Eu voi deschide falia.')
}
for i,lang in enumerate(['en_us','ro_ro']):
    p=ROOT/f'src/main/resources/assets/galactus/lang/{lang}.json'
    data=json.loads(p.read_text(encoding='utf8'))
    data.update({k:v[i] for k,v in translations.items()})
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf8')
print('Silver Surfer identity and cinematic translations updated')
