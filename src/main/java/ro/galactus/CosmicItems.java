package ro.galactus;

import net.minecraft.item.*;
import net.minecraft.block.*;
import net.minecraft.registry.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.*;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.text.RawFilteredPair;
import java.util.List;

public final class CosmicItems {
    public static final Item SHARD=register("cosmic_shard",new Item(new Item.Settings()));
    public static final Item RECEIVER=register("cosmic_receiver",new Device("receiver"));
    public static final Item NULLIFIER=register("ultimate_nullifier",new Device("nullifier"));
    public static final Item SIGIL=register("redemption_sigil",new Device("sigil"));
    public static final Block PORTAL_BLOCK=Registry.register(Registries.BLOCK,GalactusMod.id("dimensional_core"),new Block(AbstractBlock.Settings.create().strength(5,1200).luminance(s->12)));
    public static final Item PORTAL_CORE=register("dimensional_core",new BlockItem(PORTAL_BLOCK,new Item.Settings()));
    public static final Item CONDUCTOR=register("phase_conductor",new Device("conductor"));
    public static final Item TROPHY=register("world_savior",new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
    private static Item register(String name,Item item) { return Registry.register(Registries.ITEM,GalactusMod.id(name),item); }
    public static void init() {
        Registry.register(Registries.ITEM_GROUP,GalactusMod.id("cosmic"),net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
            .icon(()->new ItemStack(NULLIFIER)).displayName(Text.translatable("itemGroup.galactus"))
            .entries((context,entries)->{for(var item:new Item[]{RECEIVER,SHARD,NULLIFIER,SIGIL,PORTAL_CORE,CONDUCTOR,TROPHY})entries.add(item);}).build());
    }
    public static ItemStack guide() {
        var book=new ItemStack(Items.WRITTEN_BOOK);
        var pages=List.of(
            "THE COMING HUNGER\n\nThe silver herald has marked this world. Galactus arrives in three Minecraft days (60 minutes while players are in the Overworld). Sleep does not consume the preparation timer.\n\nRight-click your Cosmic Receiver for status.",
            "PREPARE\n\nStock enchanted armor, bows, food and regeneration. Four floating cosmic anchors will feed his shield. Destroy all four; each drops one Cosmic Shard. Raid creatures defend them. Arrows can hit anchors and Galactus.",
            "PATH 1: NULLIFIER\n\nCraft Ultimate Nullifier with four Cosmic Shards, a Nether Star, two diamonds and two Echo Shards. Break all four anchors, then hold right-click for five seconds within 64 blocks of Galactus.\n\nThe device is consumed on success.",
            "PATH 2: BANISHMENT\n\nPlace a Dimensional Core on a beacon. Put obsidian in its four cardinal adjacent positions and gold blocks at its four corners, all level with the core.\n\nUse Phase Conductor on the core with 32 Ender Pearls in your inventory after breaking the anchors.",
            "DEFEND THE PORTAL\n\nStay within 48 blocks of the core for 90 seconds while fighting off raids. Charge pauses if you leave. The frame must remain intact. Finish charging to banish Galactus.\n\nCraft core: four obsidian, four Ender Eyes, diamond block. Conductor: four pearls, four amethyst shards, Nether Star.",
            "PATH 3: REDEEM\n\nCraft Redemption Sigil using a Nether Star, diamond block, two emeralds, two gold ingots and three books. Right-click the herald with it.\n\nBreak the anchors and bring Galactus below 35% health. The herald will force him through a cosmic rift.",
            "WHEN HUNGER WINS\n\nAt full hunger, the invasion becomes an occupation. Raids and terrain consumption continue, but all three solutions still work.\n\nFighting alone can also defeat his exposed form. He never deletes your save. Terrain consumption is permanent where enabled.",
            "SURVIVAL NOTES\n\nThe event pauses when the Overworld is empty. Its progress is saved. Stay near the invasion to fight it.\n\nA new world naturally receives its warning after two active days. Crafting and using a Cosmic Receiver can call the herald earlier.\n\nServer config: config/galactus.json"
        ).stream().map(t->RawFilteredPair.<Text>of(Text.literal(t))).toList();
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT,new WrittenBookContentComponent(RawFilteredPair.of("The Coming Hunger"),"Future Foundation",0,pages,true));
        return book;
    }
    private static class Device extends Item {
        private final String action;
        Device(String action) { super(new Item.Settings().maxCount(1).rarity(action.equals("receiver")?Rarity.UNCOMMON:Rarity.EPIC));this.action=action; }
        @Override public TypedActionResult<ItemStack> use(World w,PlayerEntity player,Hand hand) {
            var stack=player.getStackInHand(hand);
            if(w.isClient)return TypedActionResult.success(stack);
            var world=(net.minecraft.server.world.ServerWorld)w;
            if(!world.getRegistryKey().equals(World.OVERWORLD)) {player.sendMessage(Text.literal("Cosmic devices work in the Overworld."),true);return TypedActionResult.fail(stack);}
            if(action.equals("receiver")) {
                var s=InvasionState.get(world);
                if(s.stage==InvasionState.DORMANT)InvasionController.warn(world,player.getBlockPos());
                InvasionController.status(world,player);
                return TypedActionResult.success(stack);
            }
            if(action.equals("nullifier")) {
                if(InvasionController.canNullify(world,player)) {player.setCurrentHand(hand);return TypedActionResult.consume(stack);}
                return TypedActionResult.fail(stack);
            }
            player.sendMessage(Text.literal(action.equals("sigil")?"Use the sigil on the silver herald.":"Use Phase Conductor on a complete Dimensional Core frame."),true);
            return TypedActionResult.pass(stack);
        }
        @Override public int getMaxUseTime(ItemStack stack,net.minecraft.entity.LivingEntity user) { return 100; }
        @Override public UseAction getUseAction(ItemStack stack) { return action.equals("nullifier")?UseAction.BOW:UseAction.NONE; }
        @Override public ItemStack finishUsing(ItemStack stack,World w,net.minecraft.entity.LivingEntity user) {
            if(!w.isClient && user instanceof PlayerEntity p && action.equals("nullifier") && InvasionController.canNullify((net.minecraft.server.world.ServerWorld)w,p)) {
                InvasionController.win((net.minecraft.server.world.ServerWorld)w,"NULLIFIER");
                stack.decrementUnlessCreative(1,p);p.getItemCooldownManager().set(this,200);
            }return stack;
        }
        @Override public ActionResult useOnBlock(ItemUsageContext context) {
            if(!action.equals("conductor"))return super.useOnBlock(context);
            if(context.getWorld().isClient)return ActionResult.SUCCESS;
            return InvasionController.activatePortal((net.minecraft.server.world.ServerWorld)context.getWorld(),context.getPlayer(),context.getBlockPos())?ActionResult.SUCCESS:ActionResult.FAIL;
        }
    }
}
