package ro.galactus;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((d,r,e)->{d.register(CommandManager.literal("galactus")
            .then(CommandManager.literal("status").executes(ctx->{var p=ctx.getSource().getPlayer();if(p!=null)InvasionController.status(ctx.getSource().getServer().getOverworld(),p);else {var s=InvasionState.get(ctx.getSource().getServer().getOverworld());ctx.getSource().sendFeedback(()->Text.literal("stage="+s.stage+" seconds="+s.seconds+" hunger="+s.hunger+" broken="+s.broken+" ally="+s.ally+" victory="+s.victory+" portal="+s.portalCharge),false);}return 1;}))
            .then(CommandManager.literal("journal").executes(ctx->{var p=ctx.getSource().getPlayerOrThrow();p.getInventory().insertStack(CosmicItems.guide());return 1;}))
            .then(CommandManager.literal("start").requires(s->s.hasPermissionLevel(2)).executes(ctx->{var w=ctx.getSource().getServer().getOverworld();InvasionController.warn(w,net.minecraft.util.math.BlockPos.ofFloored(ctx.getSource().getPosition()));return 1;}))
            .then(CommandManager.literal("arrive").requires(s->s.hasPermissionLevel(2)).executes(ctx->{InvasionController.arrive(ctx.getSource().getServer().getOverworld());return 1;}))
            .then(CommandManager.literal("invade").requires(s->s.hasPermissionLevel(2)).executes(ctx->{var w=ctx.getSource().getServer().getOverworld();var s=InvasionState.get(w);if(s.stage==0)InvasionController.warn(w,net.minecraft.util.math.BlockPos.ofFloored(ctx.getSource().getPosition()));InvasionController.invade(w);return 1;}))
            .then(CommandManager.literal("reset").requires(s->s.hasPermissionLevel(2)).executes(ctx->{InvasionController.reset(ctx.getSource().getServer().getOverworld());return 1;}))
            .then(CommandManager.literal("kit").requires(s->s.hasPermissionLevel(2)).executes(ctx->{var p=ctx.getSource().getPlayerOrThrow();for(var item:new net.minecraft.item.Item[]{CosmicItems.RECEIVER,CosmicItems.NULLIFIER,CosmicItems.SIGIL,CosmicItems.PORTAL_CORE,CosmicItems.CONDUCTOR})p.getInventory().insertStack(new ItemStack(item));p.getInventory().insertStack(new ItemStack(net.minecraft.item.Items.ENDER_PEARL,32));p.getInventory().insertStack(CosmicItems.guide());return 1;}))
            .then(CommandManager.literal("reloadconfig").requires(s->s.hasPermissionLevel(2)).executes(ctx->{InvasionController.CONFIG=InvasionConfig.load();ctx.getSource().sendFeedback(()->Text.literal("galactus.json reloaded"),false);return 1;}))
        );
        if(net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment())d.register(CommandManager.literal("galactus_lab").requires(s->s.hasPermissionLevel(2))
            .then(CommandManager.literal("elapse").then(CommandManager.argument("seconds",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0,86400)).executes(ctx->{
                var s=InvasionState.get(ctx.getSource().getServer().getOverworld());int n=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx,"seconds");s.seconds+=n;if(s.active())s.hunger+=n;s.markDirty();return 1;})))
            .then(CommandManager.literal("breakanchors").executes(ctx->{var w=ctx.getSource().getServer().getOverworld();var p=w.getPlayers().getFirst();var s=InvasionState.get(w);for(var id:s.anchors)if(id!=null&&w.getEntity(id) instanceof AnchorEntity a)a.damage(w.getDamageSources().playerAttack(p),1000);return 1;}))
            .then(CommandManager.literal("weaken").executes(ctx->{var w=ctx.getSource().getServer().getOverworld();var s=InvasionState.get(w);if(s.boss!=null&&w.getEntity(s.boss) instanceof CosmicEntity b)b.setHealth(300);return 1;}))
            .then(CommandManager.literal("assert").executes(ctx->{var w=ctx.getSource().getServer().getOverworld();var s=InvasionState.get(w);var messages=new java.util.ArrayList<String>();
                int bosses=0,heralds=0,anchors=0;for(var entity:w.iterateEntities()){if(entity.getType()==GalactusMod.GALACTUS)bosses++;if(entity.getType()==GalactusMod.HERALD)heralds++;if(entity.getType()==GalactusMod.ANCHOR)anchors++;}
                messages.add("bosses="+bosses+" heralds="+heralds+" anchors="+anchors);
                if(s.active() && (bosses!=1||heralds!=1||anchors!=s.anchorsLeft()))throw new IllegalStateException("Duplicate or missing cosmic entities: "+messages);
                if(s.active()&&w.getEntity(s.boss)!=null&&w.getEntity(s.boss).getY()<w.getBottomY())throw new IllegalStateException("Boss below world floor");
                for(var id:new String[]{"cosmic_receiver","ultimate_nullifier","redemption_sigil","phase_conductor","dimensional_core"}){
                    if(w.getRecipeManager().get(GalactusMod.id(id)).isEmpty())throw new IllegalStateException("Missing recipe: "+id);
                }messages.add("recipes=5/5");
                var loaded=InvasionState.read(s.writeNbt(new net.minecraft.nbt.NbtCompound(),w.getRegistryManager()),w.getRegistryManager());
                if(loaded.stage!=s.stage||loaded.seconds!=s.seconds||loaded.broken!=s.broken||loaded.ally!=s.ally||loaded.portalCharge!=s.portalCharge)throw new IllegalStateException("Persistence mismatch");
                messages.add("persistence=PASS");
                if(s.active()&&s.boss!=null&&w.getEntity(s.boss) instanceof CosmicEntity boss&&!s.exposed()){
                    float hp=boss.getHealth();boss.damage(w.getDamageSources().playerAttack(w.getPlayers().getFirst()),10);if(hp!=boss.getHealth())throw new IllegalStateException("Shield failed");messages.add("shield=PASS");}
                String message=String.join(" | ",messages);ctx.getSource().sendFeedback(()->Text.literal(message),false);GalactusMod.LOG.info("LAB {}",message);return 1;
            }))
        );
        });
    }
}
