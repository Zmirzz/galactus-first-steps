package ro.galactus;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class CosmicEntity extends PathAwareEntity {
    public boolean eventBound=false;
    public CosmicEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type,world); setPersistent();
        setNoGravity(true);
    }
    @Override protected void initGoals() {}
    @Override public boolean canImmediatelyDespawn(double distanceSquared) { return false; }
    @Override public boolean damage(net.minecraft.entity.damage.DamageSource source,float amount) {
        if(getWorld().isClient)return false;
        if(source.isOf(net.minecraft.entity.damage.DamageTypes.GENERIC_KILL))return super.damage(source,amount);
        if(getType()==GalactusMod.HERALD)return false;
        var s=InvasionState.get((net.minecraft.server.world.ServerWorld)getWorld());
        if(s.active()&&getUuid().equals(s.boss)&&!s.exposed()) {
            if(source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity p)p.sendMessage(net.minecraft.text.Text.literal("The cosmic anchors sustain his shield."),true);
            return false;
        }
        if(source.getAttacker()==null&&!source.isOf(net.minecraft.entity.damage.DamageTypes.MAGIC))return false;
        return super.damage(source,amount);
    }
    @Override public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        if(getWorld() instanceof net.minecraft.server.world.ServerWorld w) {
            var s=InvasionState.get(w);if(s.active()&&getUuid().equals(s.boss)&&s.exposed())InvasionController.win(w,"COMBAT");
        }
        super.onDeath(source);
    }
    @Override protected net.minecraft.util.ActionResult interactMob(net.minecraft.entity.player.PlayerEntity p,net.minecraft.util.Hand hand) {
        if(getType()==GalactusMod.HERALD && p.getStackInHand(hand).isOf(CosmicItems.SIGIL)) {
            if(getWorld().isClient)return net.minecraft.util.ActionResult.SUCCESS;
            if(InvasionController.redeem((net.minecraft.server.world.ServerWorld)getWorld(),p,this)) {
                p.getStackInHand(hand).decrementUnlessCreative(1,p);return net.minecraft.util.ActionResult.SUCCESS;
            }
        }
        return super.interactMob(p,hand);
    }
    @Override public void tick() {
        super.tick();
        if(!getWorld().isClient && eventBound) {
            var s=InvasionState.get((net.minecraft.server.world.ServerWorld)getWorld());
            var id=getType()==GalactusMod.GALACTUS?s.boss:s.herald;
            if(!getUuid().equals(id)){discard();return;}
        }
        if(!getWorld().isClient && getType()==GalactusMod.HERALD) {
            var s=InvasionState.get((net.minecraft.server.world.ServerWorld)getWorld());
            if(getUuid().equals(s.herald)) {
                double a=age*.012;setPosition(s.origin.getX()+Math.sin(a)*12,s.origin.getY()+3+Math.sin(age*.03)*.8,s.origin.getZ()+Math.cos(a)*12);
                setYaw((float)(-a*180/Math.PI));bodyYaw=getYaw();headYaw=getYaw();
            }
        }
    }
    @Override public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound n) {super.writeCustomDataToNbt(n);n.putBoolean("EventBound",eventBound);}
    @Override public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound n) {super.readCustomDataFromNbt(n);eventBound=n.getBoolean("EventBound");}
}
