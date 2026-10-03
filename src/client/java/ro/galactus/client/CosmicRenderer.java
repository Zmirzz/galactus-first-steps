package ro.galactus.client;

import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ro.galactus.CosmicEntity;
import ro.galactus.GalactusMod;

public class CosmicRenderer extends MobEntityRenderer<CosmicEntity,CosmicModel> {
    private final boolean herald;
    public CosmicRenderer(EntityRendererFactory.Context ctx,boolean herald) {
        super(ctx,new CosmicModel(ctx.getPart(herald?GalactusClient.HERALD_LAYER:GalactusClient.LAYER),herald),herald?.65f:5);
        this.herald=herald;
        addFeature(new net.minecraft.client.render.entity.feature.EyesFeatureRenderer<CosmicEntity,CosmicModel>(this) {
            @Override public net.minecraft.client.render.RenderLayer getEyesTexture() {
                return net.minecraft.client.render.RenderLayer.getEyes(GalactusMod.id("textures/entity/"+(herald?"herald":"galactus")+"_glow.png"));
            }
        });
    }
    @Override public Identifier getTexture(CosmicEntity entity) { return GalactusMod.id("textures/entity/"+(herald?"herald":"galactus")+".png"); }
    @Override protected int getBlockLight(CosmicEntity e,net.minecraft.util.math.BlockPos pos) {
        return herald?15:super.getBlockLight(e,pos);
    }
    @Override protected boolean hasLabel(CosmicEntity e) {
        return !net.minecraft.client.MinecraftClient.getInstance().options.hudHidden && super.hasLabel(e);
    }
    @Override protected void scale(CosmicEntity e,MatrixStack m,float delta) { if(!herald)m.scale(16,16,16); }
}
