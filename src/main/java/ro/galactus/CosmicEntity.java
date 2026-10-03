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
        if(getWorld().isClient && getType()==GalactusMod.HERALD && age%3==0) {
            double a=Math.toRadians(getYaw());
            // Cosmetic wake follows the actual board. It never damages terrain.
            getWorld().addParticle(net.minecraft.particle.ParticleTypes.END_ROD,
                getX()+Math.sin(a)*.8,getY()+.08,getZ()-Math.cos(a)*.8,
                Math.sin(a)*.02,-.012,-Math.cos(a)*.02);
        }
        if(!getWorld().isClient && eventBound) {
            var s=InvasionState.get((net.minecraft.server.world.ServerWorld)getWorld());
            var id=getType()==GalactusMod.GALACTUS?s.boss:s.herald;
            if(!getUuid().equals(id)){discard();return;}
        }
        if(!getWorld().isClient && getType()==GalactusMod.HERALD) {
            var s=InvasionState.get((net.minecraft.server.world.ServerWorld)getWorld());
            if(getUuid().equals(s.herald)) {
                var player=getWorld().getClosestPlayer(s.origin.getX(),s.origin.getY(),s.origin.getZ(),80,false);
                double a=age*.007;
                net.minecraft.util.math.Vec3d target;
                if(player!=null) {
                    var direction=net.minecraft.util.math.Vec3d.ofCenter(s.origin).subtract(player.getPos());
                    direction=new net.minecraft.util.math.Vec3d(direction.x,0,direction.z);
                    if(direction.lengthSquared()<.01)direction=new net.minecraft.util.math.Vec3d(0,0,1);
                    target=player.getPos().add(direction.normalize().multiply(5)).add(0,.65+Math.sin(age*.035)*.15,0);
                    // Hold position within interaction reach instead of fleeing
                    // from a player who approaches with the redemption sigil.
                    if(squaredDistanceTo(player)<12.25)target=new net.minecraft.util.math.Vec3d(getX(),target.y,getZ());
                    var toward=player.getPos().subtract(getPos());
                    setYaw((float)(Math.atan2(-toward.x,toward.z)*180/Math.PI));
                } else {
                    target=new net.minecraft.util.math.Vec3d(s.origin.getX()+Math.sin(a)*18,s.origin.getY()+4+Math.sin(age*.025)*.5,s.origin.getZ()+Math.cos(a)*18);
                    setYaw((float)(-a*180/Math.PI));
                }
                var step=target.subtract(getPos()).multiply(.055);
                if(step.lengthSquared()>.36)step=step.normalize().multiply(.6);
                setPosition(getPos().add(step));bodyYaw=getYaw();headYaw=getYaw();
                if(age%100==0)setCustomName(net.minecraft.text.Text.translatable(s.ally?"galactus.surfer.ally":"galactus.surfer.name"));
            }
        }
    }
    @Override public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound n) {super.writeCustomDataToNbt(n);n.putBoolean("EventBound",eventBound);}
    @Override public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound n) {super.readCustomDataFromNbt(n);eventBound=n.getBoolean("EventBound");}
}
