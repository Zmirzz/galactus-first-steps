package ro.galactus.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import ro.galactus.CosmicEntity;

public class CosmicModel extends EntityModel<CosmicEntity> {
    private final ModelPart root, head, leftArm, rightArm, leftLeg, rightLeg;
    public CosmicModel(ModelPart root) {
        this.root=root; head=root.getChild("head"); leftArm=root.getChild("left_arm"); rightArm=root.getChild("right_arm");
        leftLeg=root.getChild("left_leg"); rightLeg=root.getChild("right_leg");
    }
    public static TexturedModelData data() {
        var model=new ModelData(); var r=model.getRoot();
        r.addChild("body",ModelPartBuilder.create().uv(0,32).cuboid(-6,0,-3,12,12,6).uv(0,32).cuboid(-7,0,-3.5f,14,3,7),ModelTransform.NONE);
        r.addChild("belt",ModelPartBuilder.create().uv(32,0).cuboid(-6.5f,10,-3.5f,13,2,7),ModelTransform.NONE);
        r.addChild("head",ModelPartBuilder.create().uv(0,0).cuboid(-4,-8,-4,8,8,8)
            .uv(32,32).cuboid(-3,-5.5f,-4.1f,6,4,1)
            .uv(0,0).cuboid(-8,-13,-1.5f,3,11,4).cuboid(5,-13,-1.5f,3,11,4)
            .uv(0,0).cuboid(-10,-14,-1.5f,5,3,4).cuboid(5,-14,-1.5f,5,3,4)
            .uv(48,16).cuboid(-2.7f,-4.5f,-4.3f,1.8f,.6f,.3f).cuboid(.9f,-4.5f,-4.3f,1.8f,.6f,.3f),ModelTransform.NONE);
        r.addChild("left_arm",ModelPartBuilder.create().uv(0,32).cuboid(0,-1,-3,4,15,6).uv(32,0).cuboid(-.5f,8,-3.5f,5,3,7),ModelTransform.pivot(6,1,0));
        r.addChild("right_arm",ModelPartBuilder.create().uv(0,32).cuboid(-4,-1,-3,4,15,6).uv(32,0).cuboid(-4.5f,8,-3.5f,5,3,7),ModelTransform.pivot(-6,1,0));
        r.addChild("left_leg",ModelPartBuilder.create().uv(0,0).cuboid(-2.5f,0,-2.5f,5,12,5).uv(32,0).cuboid(-3,8,-3.5f,6,4,7),ModelTransform.pivot(3,12,0));
        r.addChild("right_leg",ModelPartBuilder.create().uv(0,0).cuboid(-2.5f,0,-2.5f,5,12,5).uv(32,0).cuboid(-3,8,-3.5f,6,4,7),ModelTransform.pivot(-3,12,0));
        return TexturedModelData.of(model,64,64);
    }
    public static TexturedModelData heraldData() {
        var model=new ModelData();var r=model.getRoot();
        r.addChild("body",ModelPartBuilder.create().uv(0,32).cuboid(-3,0,-2,6,12,4),ModelTransform.NONE);
        r.addChild("head",ModelPartBuilder.create().uv(0,0).cuboid(-3,-6,-3,6,6,6),ModelTransform.NONE);
        r.addChild("left_arm",ModelPartBuilder.create().uv(16,16).cuboid(-1,0,-1.5f,3,11,3),ModelTransform.pivot(4,0,0));
        r.addChild("right_arm",ModelPartBuilder.create().uv(16,16).cuboid(-2,0,-1.5f,3,11,3),ModelTransform.pivot(-4,0,0));
        r.addChild("left_leg",ModelPartBuilder.create().uv(32,0).cuboid(-1.5f,0,-1.5f,3,12,3),ModelTransform.pivot(1.5f,12,0));
        r.addChild("right_leg",ModelPartBuilder.create().uv(32,0).cuboid(-1.5f,0,-1.5f,3,12,3),ModelTransform.pivot(-1.5f,12,0));
        r.addChild("board",ModelPartBuilder.create().uv(0,32).cuboid(-5,24,-12,10,1,25).cuboid(-3,23,-14,6,1,4),ModelTransform.NONE);
        return TexturedModelData.of(model,64,64);
    }
    @Override public void setAngles(CosmicEntity e,float limb,float amount,float age,float yaw,float pitch) {
        head.yaw=yaw*.017453292f; head.pitch=pitch*.017453292f;
        leftArm.pitch=(float)Math.sin(age*.025)*.08f; rightArm.pitch=-leftArm.pitch;
        leftLeg.pitch=(float)Math.cos(limb*.4f)*amount*.25f; rightLeg.pitch=-leftLeg.pitch;
    }
    @Override public void render(MatrixStack matrices,VertexConsumer vertices,int light,int overlay,int color) { root.render(matrices,vertices,light,overlay,color); }
}
