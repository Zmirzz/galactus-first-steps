package ro.galactus.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import ro.galactus.GalactusMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class GalactusClient implements ClientModInitializer {
    private int reconnectTicks=0;
    public static final EntityModelLayer LAYER = new EntityModelLayer(GalactusMod.id("cosmic"), "main");
    public static final EntityModelLayer HERALD_LAYER = new EntityModelLayer(GalactusMod.id("herald"), "main");
    public static final EntityModelLayer ANCHOR_LAYER = new EntityModelLayer(GalactusMod.id("anchor"), "main");
    @Override public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(LAYER, CosmicModel::data);
        EntityModelLayerRegistry.registerModelLayer(HERALD_LAYER, CosmicModel::heraldData);
        EntityModelLayerRegistry.registerModelLayer(ANCHOR_LAYER,AnchorModel::data);
        EntityRendererRegistry.register(GalactusMod.GALACTUS, ctx -> new CosmicRenderer(ctx,false));
        EntityRendererRegistry.register(GalactusMod.HERALD, ctx -> new CosmicRenderer(ctx,true));
        EntityRendererRegistry.register(GalactusMod.ANCHOR,AnchorRenderer::new);
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment() && Boolean.getBoolean("galactus.lab")) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.currentScreen instanceof net.minecraft.client.gui.screen.AccessibilityOnboardingScreen) {
                    client.options.onboardAccessibility = true;
                    client.options.write();
                    client.setScreen(new TitleScreen());
                }
                reconnectTicks++;
                if ((client.currentScreen instanceof TitleScreen || client.currentScreen instanceof net.minecraft.client.gui.screen.DisconnectedScreen && reconnectTicks%200==0) && client.world == null) {
                    ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse("127.0.0.1:25578"),
                        new ServerInfo("Galactus isolated lab","127.0.0.1:25578",ServerInfo.ServerType.OTHER),false,null);
                }
                LabBridge.tick(client);
            });
        }
    }
}
