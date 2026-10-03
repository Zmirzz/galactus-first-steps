package ro.galactus.client;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ro.galactus.*;
public class AnchorRenderer extends MobEntityRenderer<AnchorEntity,AnchorModel> {
    public AnchorRenderer(EntityRendererFactory.Context ctx) {super(ctx,new AnchorModel(ctx.getPart(GalactusClient.ANCHOR_LAYER)),.8f);}
    @Override public Identifier getTexture(AnchorEntity e) {return GalactusMod.id("textures/entity/anchor.png");}
    @Override protected void scale(AnchorEntity e,MatrixStack m,float delta) {m.scale(1.5f,1.5f,1.5f);}
}
