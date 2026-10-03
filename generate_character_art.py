"""Original Minecraft character geometry with individually packed, painted UV islands.

No game assets or externally sourced textures. Rebuilds Java model data and three
512px atlases; material highlights are painted, not a shader/reflection claim.
"""
from pathlib import Path
from PIL import Image
import math

ROOT = Path(__file__).parent
OUT = ROOT / 'src/main/resources/assets/galactus/textures/entity'
OUT.mkdir(parents=True, exist_ok=True)
SIZE = 512
PALETTE = {'armor': (101, 49, 109), 'trim': (152, 78, 149),
           'dark': (37, 32, 51), 'steel': (85, 83, 113),
           'skin': (155, 128, 120), 'shadow': (78, 57, 60),
           'light': (203, 233, 255), 'silver': (235, 246, 255),
           'board': (239, 250, 255), 'mouth': (58, 70, 84)}

class Atlas:
    def __init__(self, name):
        self.name = name
        self.image = Image.new('RGBA', (SIZE, SIZE))
        self.glow = Image.new('RGBA', (SIZE, SIZE))
        self.x = self.y = self.row = 0

    def box(self, xyz, whd, material):
        w, h, d = [max(1, math.ceil(v)) for v in whd]
        tw, th = 2*(w+d), h+d
        if self.x + tw + 2 > SIZE:
            self.x = 0; self.y += self.row + 2; self.row = 0
        u, v = self.x, self.y
        assert v + th < SIZE
        self.x += tw+2; self.row = max(self.row, th)
        # Fractional geometry uses fractional UV extents in Minecraft. Fill the
        # island first so thin eyelids/rails cannot sample transparent gaps.
        base=PALETTE[material]+(255,)
        self.image.paste(base,(u,v,u+tw,v+th))
        if material=='light': self.glow.paste(base,(u,v,u+tw,v+th))
        # Vanilla box unfolding: top/bottom then left/front/right/back.
        faces = [(d,0,w,d,1.12), (d+w,0,w,d,.65),
                 (0,d,d,h,.66), (d,d,w,h,1),
                 (d+w,d,d,h,.83), (2*d+w,d,w,h,.72)]
        for fx, fy, fw, fh, shade in faces:
            for py in range(fh):
                for px in range(fw):
                    base = PALETTE[material]
                    edge = px in (0,fw-1) or py in (0,fh-1)
                    if material in ('silver','board'):
                        # Long bright highlights and dark reflected bands follow
                        # each anatomical surface, rather than random noise.
                        t = px / max(1,fw-1)
                        band = .87 + .30*math.exp(-((t-.26)/.17)**2) - .24*math.exp(-((t-.67)/.10)**2)
                        shade2 = max(.91,shade) * band * (1.04-.10*py/max(1,fh-1))
                        if edge: shade2 *= .95
                    elif material == 'light': shade2 = 1
                    else:
                        shade2 = shade*(1.08-.20*py/max(1,fh-1))
                        if edge: shade2 *= .70
                        if material in ('armor','steel') and fw>7 and fh>7 and px in (2,fw-3): shade2 *= 1.2
                    rgba = tuple(min(255,max(0,round(c*shade2))) for c in base)+(255,)
                    self.image.putpixel((u+fx+px,v+fy+py),rgba)
                    if material == 'light': self.glow.putpixel((u+fx+px,v+fy+py),rgba)
        vals = ','.join(f'{float(n):g}f' for n in (*xyz,*whd))
        return f'.uv({u},{v}).cuboid({vals})'

    def save(self):
        self.image.save(OUT / f'{self.name}.png')
        self.glow.save(OUT / f'{self.name}_glow.png')

def build(name, surfer=False):
    atlas = Atlas(name); lines = []
    def part(var, parent, child, pivot, boxes, rotation=(0,0,0)):
        shape = 'ModelPartBuilder.create()'+''.join(atlas.box(*b) for b in boxes)
        vals = ','.join(f'{float(n):g}f' for n in (*pivot,*rotation))
        lines.append(f'        var {var}={parent}.addChild("{child}",{shape},ModelTransform.of({vals}));')
    if surfer:
        part('body','r','body',(0,0,0),[
            ((-3.5,0,-2),(7,5,4),'silver'), # chest
            ((-2.5,5,-1.5),(5,6,3),'silver'), # waist
            ((-3,10,-2),(6,2,4),'silver'),
            ((-3.4,1,-2.5),(3,3,1),'silver'),((.4,1,-2.5),(3,3,1),'silver'),
            ((-2,5,-1.9),(4,2,1),'silver'),((-1.5,7,-1.8),(3,2,1),'silver')])
        part('head','r','head',(0,0,0),[
            ((-1,-1,-1),(2,2,2),'silver'),
            ((-2.5,-6,-2),(5,5,4),'silver'),
            ((-2,-2,-2.3),(4,1,1),'silver'),
            ((-.5,-4,-2.6),(1,2,1),'silver'),
            ((-1.8,-4.2,-2.2),(1.3,.45,.3),'light'),((.5,-4.2,-2.2),(1.3,.45,.3),'light'),
            ((-.9,-2.1,-2.5),(1.8,.25,.2),'mouth')])
        for side,sign in [('left',1),('right',-1)]:
            part(side+'Arm','r',side+'_arm',(sign*4,1,0),[
                ((-1.5,-1,-1.7),(3,3,3.4),'silver'),
                ((-1.2,2,-1.4),(2.4,4,2.8),'silver')])
            part(side+'Forearm',side+'Arm','forearm',(0,6,0),[
                ((-1.2,0,-1.2),(2.4,4,2.4),'silver'),
                ((-1,4,-1.1),(2,2,2.2),'silver'),
                ((sign*.7,4,-1.1),(1,1,1),'silver')])
            part(side+'Leg','r',side+'_leg',(sign*1.8,11,0),[
                ((-1.4,0,-1.5),(2.8,6,3),'silver')])
            part(side+'Shin',side+'Leg','shin',(0,6,0),[
                ((-1.1,0,-1.2),(2.2,6,2.4),'silver'),
                ((-1.2,5.5,-2.5),(2.4,1.2,4),'silver')])
        # Tapered nose/tail and rails: board is an independent, gently banking part.
        part('board','r','board',(0,24,0),[
            ((-4,-.25,-10),(8,.8,20),'board'),
            ((-3.5,-.45,-13),(7,.8,3),'board'),
            ((-2.5,-.7,-15),(5,.8,2),'board'),
            ((-1.5,-.9,-16),(3,.8,1),'board'),
            ((-3.5,-.25,10),(7,.8,3),'board'),
            ((-2.5,-.25,13),(5,.8,2),'board'),
            ((-1.5,-.25,15),(3,.8,1),'board'),
            ((-4,.1,-9),(.3,.4,18),'light'),((3.7,.1,-9),(.3,.4,18),'light'),
            ((-.2,-.35,-12),(.4,.1,24),'silver')])
    else:
        part('body','r','body',(0,0,0),[
            ((-5,0,-3),(10,10,6),'dark'),
            ((-5.5,0,-3.5),(11,4,7),'armor'),
            ((-4.5,4,-3.4),(9,5,6.8),'steel'),
            ((-1.5,1,-4),(3,5,1),'trim'),
            ((-1,2,-4.3),(2,2,.5),'steel'),
            ((-4,5,-3.7),(2,3,.6),'armor'),((2,5,-3.7),(2,3,.6),'armor'),
            ((-5.5,9,-3.5),(11,2,7),'trim'),
            ((-2,9,-3.8),(4,2,1),'steel'),
            ((-5,11,-3.4),(3,4,1),'armor'),((2,11,-3.4),(3,4,1),'armor'),
            ((-1.5,11,-3.5),(3,3,1),'steel'),
            ((-1,6,-3.75),(2,.35,.35),'light')])
        part('head','r','head',(0,-1,0),[
            ((-3.5,-6,-3),(7,6,6),'skin'),
            ((-3.5,-8,-3.4),(7,2,6.8),'armor'),
            ((-4,-7,-3.5),(1,7,7),'armor'),((3,-7,-3.5),(1,7,7),'armor'),
            ((-3,-1,-3.5),(6,1,1),'armor'),
            ((-3,-6,-3.5),(6,1.5,1),'armor'),
            ((-1,-8.5,-4),(2,4,1),'trim'),
            ((-2.6,-4.8,-3.35),(2,.65,.4),'shadow'),((.6,-4.8,-3.35),(2,.65,.4),'shadow'),
            ((-2.5,-4.6,-3.6),(1.8,.45,.4),'light'),((.7,-4.6,-3.6),(1.8,.45,.4),'light'),
            ((-.6,-4,-3.8),(1.2,2,1),'skin'),
            ((-1.6,-1.8,-3.2),(3.2,.4,.4),'shadow'),
            ((-2,-1.3,-3.25),(4,.8,.5),'skin'),
            ((-3,-3.8,-3.4),(1.2,2,.6),'skin'),((1.8,-3.8,-3.4),(1.2,2,.6),'skin')])
        for side,sign in [('left',1),('right',-1)]:
            # Wide angular helmet vanes, with layered channels and swept tips.
            part(side+'Vane','head',side+'_vane',(sign*3.8,-4,0),[
                ((-1,-7,-2),(2,9,4),'armor'),
                ((-1.3,-8,-2.5),(2.6,2,5),'trim'),
                ((-.45,-6,-2.3),(.9,7,.5),'steel'),
                ((-1.6,-9.5,-2.2),(3.2,2,4.4),'armor')],(0,0,sign*.27))
            part(side+'Arm','r',side+'_arm',(sign*6,1,0),[
                ((-2.5,-1.5,-3.5),(5,4,7),'armor'),
                ((-2.7,-1.8,-3.8),(5.4,1,7.6),'trim'),
                ((-1.7,2,-2.3),(3.4,5,4.6),'steel')])
            part(side+'Forearm',side+'Arm','forearm',(0,7,0),[
                ((-2,0,-2.5),(4,5,5),'armor'),
                ((-2.3,0,-2.8),(4.6,1.2,5.6),'trim'),
                ((-2.3,4,-2.8),(4.6,1.2,5.6),'trim'),
                ((-1.8,5,-2.3),(3.6,3,4.6),'steel'),
                ((-1.4,6,-2.7),(2.8,1.5,.7),'armor')])
            part(side+'Leg','r',side+'_leg',(sign*2.8,11,0),[
                ((-2.2,0,-2.3),(4.4,6,4.6),'dark'),
                ((-2.4,1,-2.6),(4.8,4,1),'armor')])
            part(side+'Shin',side+'Leg','shin',(0,6,0),[
                ((-2.4,0,-2.7),(4.8,2,5.4),'trim'),
                ((-2.2,2,-2.4),(4.4,4,4.8),'armor'),
                ((-.6,2,-2.6),(1.2,4,.5),'steel'),
                ((-2.5,5,-3.5),(5,2,7),'steel'),
                ((-2.6,6,-3.6),(5.2,1,7.2),'trim')])
    atlas.save()
    return '\n'.join(lines)

boss = build('galactus'); surfer = build('herald', True)
source = '''package ro.galactus.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import ro.galactus.CosmicEntity;

/** Art data generated by generate_character_art.py; articulated animation below. */
public class CosmicModel extends EntityModel<CosmicEntity> {
    private final ModelPart root, head, body, leftArm, rightArm, leftLeg, rightLeg;
    private final boolean surfer;
    public CosmicModel(ModelPart root, boolean surfer) {
        this.root=root;this.surfer=surfer;head=root.getChild("head");body=root.getChild("body");
        leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");
        leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");
    }
    public static TexturedModelData data() {
        var model=new ModelData();var r=model.getRoot();
%BOSS%
        return TexturedModelData.of(model,512,512);
    }
    public static TexturedModelData heraldData() {
        var model=new ModelData();var r=model.getRoot();
%SURFER%
        return TexturedModelData.of(model,512,512);
    }
    @Override public void setAngles(CosmicEntity e,float limb,float amount,float age,float yaw,float pitch) {
        root.traverse().forEach(ModelPart::resetTransform);
        float breath=(float)Math.sin(age*.035);
        head.yaw=Math.max(-.45f,Math.min(.45f,yaw*.017453292f));
        head.pitch=Math.max(-.2f,Math.min(.2f,pitch*.017453292f));
        if(surfer) {
            root.roll=breath*.035f;
            body.yaw=.24f;head.yaw-=.15f;
            leftArm.roll=-1.0f-breath*.035f;rightArm.roll=.65f+breath*.035f;
            leftArm.pitch=-.35f;rightArm.pitch=.15f;
            leftArm.getChild("forearm").pitch=-.32f;rightArm.getChild("forearm").pitch=-.24f;
            leftLeg.pitch=-.28f;rightLeg.pitch=.17f;
            leftLeg.roll=-.26f;rightLeg.roll=.26f;
            leftLeg.getChild("shin").pitch=.25f;rightLeg.getChild("shin").pitch=.12f;
            root.getChild("board").pitch=breath*.025f;
            root.getChild("board").yaw=1.5707963f;
        } else {
            body.pitch=breath*.008f;
            leftArm.roll=-.08f;rightArm.roll=.08f;
            leftArm.pitch=breath*.025f;rightArm.pitch=-breath*.025f;
            leftArm.getChild("forearm").pitch=-.12f;rightArm.getChild("forearm").pitch=-.12f;
        }
    }
    @Override public void render(MatrixStack matrices,VertexConsumer vertices,int light,int overlay,int color) {
        root.render(matrices,vertices,light,overlay,color);
    }
}
'''.replace('%BOSS%',boss).replace('%SURFER%',surfer)
(ROOT/'src/client/java/ro/galactus/client/CosmicModel.java').write_text(source,encoding='utf8')
print('Galactus and Silver Surfer: articulated geometry + painted 512px UV atlases + emissive masks')
