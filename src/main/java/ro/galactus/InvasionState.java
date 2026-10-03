package ro.galactus;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

public class InvasionState extends PersistentState {
    public static final int DORMANT=0, PREPARATION=1, ARRIVAL=2, INVASION=3, OCCUPIED=4, SAVED=5;
    public int stage=DORMANT, seconds=0, dormantSeconds=0, hunger=0, broken=0, portalCharge=0;
    public boolean ally=false, guideGiven=false;
    public String victory="";
    public BlockPos origin=BlockPos.ORIGIN, portal=null;
    public UUID boss=null, herald=null;
    public UUID[] anchors=new UUID[4];
    public static final Type<InvasionState> TYPE=new Type<>(InvasionState::new,InvasionState::read,null);
    public static InvasionState get(ServerWorld world) { return world.getPersistentStateManager().getOrCreate(TYPE,"galactus_invasion"); }
    public boolean active() { return stage==INVASION || stage==OCCUPIED; }
    public boolean exposed() { return broken==15; }
    public boolean canRedeem(float fraction) { return active() && exposed() && ally && fraction<=.35f; }
    public int anchorsLeft() { return 4-Integer.bitCount(broken & 15); }
    public static InvasionState read(NbtCompound n,RegistryWrapper.WrapperLookup registries) {
        var s=new InvasionState();
        s.stage=n.getInt("Stage"); s.seconds=n.getInt("Seconds"); s.dormantSeconds=n.getInt("DormantSeconds"); s.hunger=n.getInt("Hunger");
        s.broken=n.getInt("Broken"); s.ally=n.getBoolean("Ally"); s.guideGiven=n.getBoolean("GuideGiven"); s.victory=n.getString("Victory");
        s.origin=BlockPos.fromLong(n.getLong("Origin")); if(n.contains("Portal"))s.portal=BlockPos.fromLong(n.getLong("Portal"));
        s.portalCharge=n.getInt("PortalCharge"); if(n.containsUuid("Boss"))s.boss=n.getUuid("Boss"); if(n.containsUuid("Herald"))s.herald=n.getUuid("Herald");
        for(int i=0;i<4;i++) if(n.containsUuid("Anchor"+i))s.anchors[i]=n.getUuid("Anchor"+i);
        return s;
    }
    @Override public NbtCompound writeNbt(NbtCompound n,RegistryWrapper.WrapperLookup registries) {
        n.putInt("Stage",stage);n.putInt("Seconds",seconds);n.putInt("DormantSeconds",dormantSeconds);n.putInt("Hunger",hunger);n.putInt("Broken",broken);
        n.putBoolean("Ally",ally);n.putBoolean("GuideGiven",guideGiven);n.putString("Victory",victory);n.putLong("Origin",origin.asLong());
        n.putInt("PortalCharge",portalCharge);if(portal!=null)n.putLong("Portal",portal.asLong());
        if(boss!=null)n.putUuid("Boss",boss);if(herald!=null)n.putUuid("Herald",herald);
        for(int i=0;i<4;i++)if(anchors[i]!=null)n.putUuid("Anchor"+i,anchors[i]);return n;
    }
}
