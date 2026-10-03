package ro.galactus;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class AnchorEntity extends PathAwareEntity {
    public int index=-1;
    public AnchorEntity(EntityType<? extends PathAwareEntity> type,World world) { super(type,world);setNoGravity(true);setPersistent(); }
    @Override protected void initGoals() {}
    @Override public boolean canImmediatelyDespawn(double d) { return false; }
    @Override public void tick() {
        super.tick();if(getWorld() instanceof ServerWorld w && index>=0 && index<4) {
            var s=InvasionState.get(w);
            if(!getUuid().equals(s.anchors[index])||s.stage==InvasionState.SAVED||(s.broken&(1<<index))!=0)discard();
        }
    }
    @Override public boolean damage(DamageSource source,float amount) {
        if(source.isOf(net.minecraft.entity.damage.DamageTypes.GENERIC_KILL))return super.damage(source,amount);
        if(!(source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity))return false;
        return super.damage(source,amount);
    }
    @Override public void onDeath(DamageSource source) {
        if(getWorld() instanceof ServerWorld w) InvasionController.anchorBroken(w,this);
        super.onDeath(source);
    }
    @Override public void writeCustomDataToNbt(NbtCompound n) { super.writeCustomDataToNbt(n);n.putInt("CosmicIndex",index); }
    @Override public void readCustomDataFromNbt(NbtCompound n) { super.readCustomDataFromNbt(n);index=n.getInt("CosmicIndex"); }
}
