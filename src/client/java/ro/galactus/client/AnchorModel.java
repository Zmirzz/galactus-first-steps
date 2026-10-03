package ro.galactus.client;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import ro.galactus.AnchorEntity;

public class AnchorModel extends EntityModel<AnchorEntity> {
    private final ModelPart root,crystal;
    public AnchorModel(ModelPart root) {this.root=root;this.crystal=root.getChild("crystal");}
    public static TexturedModelData data() {
        var data=new ModelData();var r=data.getRoot();
        r.addChild("crystal",ModelPartBuilder.create().uv(0,0).cuboid(-7,-7,-7,14,14,14),ModelTransform.of(0,0,0,.7854f,0,.7854f));
        r.addChild("ring",ModelPartBuilder.create().uv(0,32).cuboid(-12,12,-12,24,3,3).cuboid(-12,12,9,24,3,3).cuboid(-12,12,-9,3,3,18).cuboid(9,12,-9,3,3,18),ModelTransform.NONE);
        return TexturedModelData.of(data,64,64);
    }
    @Override public void setAngles(AnchorEntity e,float a,float b,float age,float yaw,float pitch) {crystal.yaw=age*.04f;crystal.pivotY=(float)Math.sin(age*.08)*2;}
    @Override public void render(MatrixStack m,VertexConsumer v,int l,int o,int c) {root.render(m,v,15728880,o,c);}
}
