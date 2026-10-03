package ro.galactus.client;

import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ro.galactus.CosmicEntity;
import ro.galactus.GalactusMod;

public class CosmicRenderer extends MobEntityRenderer<CosmicEntity,CosmicModel> {
    private final boolean herald;
    public CosmicRenderer(EntityRendererFactory.Context ctx,boolean herald) { super(ctx,new CosmicModel(ctx.getPart(herald?GalactusClient.HERALD_LAYER:GalactusClient.LAYER)),herald?.5f:5); this.herald=herald; }
    @Override public Identifier getTexture(CosmicEntity entity) { return GalactusMod.id("textures/entity/"+(herald?"herald":"galactus")+".png"); }
    @Override protected void scale(CosmicEntity e,MatrixStack m,float delta) { if(!herald)m.scale(16,16,16); }
}
