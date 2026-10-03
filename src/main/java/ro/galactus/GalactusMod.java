package ro.galactus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalactusMod implements ModInitializer {
    public static final String ID = "galactus";
    public static final Logger LOG = LoggerFactory.getLogger(ID);
    public static Identifier id(String path) { return Identifier.of(ID, path); }
    public static final EntityType<CosmicEntity> GALACTUS = Registry.register(Registries.ENTITY_TYPE, id("galactus"),
        EntityType.Builder.<CosmicEntity>create(CosmicEntity::new, SpawnGroup.MONSTER).dimensions(9, 36).maxTrackingRange(16).trackingTickInterval(2).makeFireImmune().build());
    public static final EntityType<CosmicEntity> HERALD = Registry.register(Registries.ENTITY_TYPE, id("herald"),
        EntityType.Builder.<CosmicEntity>create(CosmicEntity::new, SpawnGroup.CREATURE).dimensions(.7f, 2).maxTrackingRange(12).trackingTickInterval(2).makeFireImmune().build());
    public static final EntityType<AnchorEntity> ANCHOR = Registry.register(Registries.ENTITY_TYPE,id("cosmic_anchor"),
        EntityType.Builder.<AnchorEntity>create(AnchorEntity::new,SpawnGroup.MONSTER).dimensions(1.5f,3).maxTrackingRange(12).makeFireImmune().build());
    @Override public void onInitialize() {
        FabricDefaultAttributeRegistry.register(GALACTUS, CosmicEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 1000).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,.06).add(EntityAttributes.GENERIC_FOLLOW_RANGE,128));
        FabricDefaultAttributeRegistry.register(HERALD, CosmicEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH,160).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,.3));
        FabricDefaultAttributeRegistry.register(ANCHOR,AnchorEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH,80).add(EntityAttributes.GENERIC_MOVEMENT_SPEED,0));
        CosmicItems.init();InvasionController.CONFIG=InvasionConfig.load();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(InvasionController::tick);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(InvasionController::started);
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->server.execute(()->InvasionController.onJoin(handler.player)));
        Commands.register();
        LOG.info("Galactus invasion initialized for Minecraft 1.21.1");
    }
}

