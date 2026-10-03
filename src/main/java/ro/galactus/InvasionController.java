package ro.galactus;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.sound.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;

public final class InvasionController {
    public static InvasionConfig CONFIG;
    private static final ServerBossBar BAR=new ServerBossBar(Text.literal("The Coming Hunger"),BossBar.Color.PURPLE,BossBar.Style.PROGRESS);
    private static final ServerBossBar HUNGER=new ServerBossBar(Text.literal("Planetary Hunger"),BossBar.Color.RED,BossBar.Style.NOTCHED_10);
    private static int ticks=0;
    private static int warmup=5;
    private static Vec3d strike=null;
    private static int strikeDelay=0;
    private static final int[][] OFFSETS={{32,0},{-32,0},{0,32},{0,-32}};
    public static void started(MinecraftServer server) {ticks=0;warmup=5;strike=null;strikeDelay=0;BAR.clearPlayers();HUNGER.clearPlayers();BAR.setDarkenSky(false);}
    public static void tick(MinecraftServer server) {
        if(++ticks%20!=0)return;
        var w=server.getOverworld();var s=InvasionState.get(w);var players=w.getPlayers();
        BAR.clearPlayers();HUNGER.clearPlayers();
        if(players.isEmpty()){warmup=5;return;}
        if(s.stage==InvasionState.DORMANT) {
            if(CONFIG.automaticInvasion && ++s.dormantSeconds>=CONFIG.heraldAfterDays*1200)warn(w,players.getFirst().getBlockPos());
            s.markDirty();return;
        }
        if(s.stage==InvasionState.SAVED)return;
        // Temporary tickets expire automatically. Never change the user's permanent force-loaded chunks.
        w.getChunkManager().addTicket(ChunkTicketType.PORTAL,new ChunkPos(s.origin),5,s.origin);
        if(warmup>0){warmup--;return;} // Let persisted entities load on server start and after an empty Overworld.
        for(var p:players) {BAR.addPlayer(p);if(s.active())HUNGER.addPlayer(p);}
        ensureHerald(w,s);
        s.seconds++;
        switch(s.stage) {
            case InvasionState.PREPARATION -> {
                int left=CONFIG.preparationDays*1200-s.seconds;
                BAR.setName(Text.literal("HERALD'S WARNING  •  "+clock(left)+" to prepare"));BAR.setColor(BossBar.Color.PURPLE);BAR.setPercent(clamp(left/(float)(CONFIG.preparationDays*1200)));
                if(left==600 || left==300 || left==60)message(w,"The herald: Galactus approaches. "+clock(left)+" remains.",Formatting.LIGHT_PURPLE);
                if(left<=0)arrive(w);
            }
            case InvasionState.ARRIVAL -> {
                int left=CONFIG.arrivalSeconds-s.seconds;
                BAR.setName(Text.literal("GALACTUS IS DESCENDING  •  "+Math.max(0,left)+"s"));BAR.setPercent(clamp(left/(float)CONFIG.arrivalSeconds));
                w.spawnParticles(ParticleTypes.PORTAL,s.origin.getX(),s.origin.getY()+35,s.origin.getZ(),160,10,14,10,.1);
                if(s.seconds%5==0)sound(w,s.origin,SoundEvents.ENTITY_WITHER_SPAWN,.45f,.5f);
                if(left<=0)invade(w);
            }
            case InvasionState.INVASION,InvasionState.OCCUPIED -> combat(w,s);
        }
        if(s.active()) {
            HUNGER.setPercent(clamp(s.hunger/(float)CONFIG.hungerSeconds));
            HUNGER.setName(Text.literal(s.stage==InvasionState.OCCUPIED?"WORLD OCCUPIED  •  You can still save it":"PLANETARY HUNGER  •  "+(100*s.hunger/CONFIG.hungerSeconds)+"%"));
            if(s.portal!=null)portalTick(w,s);
        }
        s.markDirty();
    }
    public static void warn(ServerWorld w,BlockPos requested) {
        var s=InvasionState.get(w);if(s.stage!=InvasionState.DORMANT)return;
        s.origin=surface(w,requested.getX()+48,requested.getZ());s.stage=InvasionState.PREPARATION;s.seconds=0;s.markDirty();warmup=5;
        ensureHerald(w,s);
        message(w,"SHALLA-BAL: This world has been chosen. Galactus is coming.",Formatting.LIGHT_PURPLE);
        message(w,"Prepare for "+CONFIG.preparationDays+" Minecraft days. Read The Coming Hunger journal; use Cosmic Receiver for coordinates.",Formatting.AQUA);
        for(var p:w.getPlayers()) {give(p,CosmicItems.guide());give(p,new ItemStack(CosmicItems.RECEIVER));p.addCommandTag("galactus_journal_received");}
        s.guideGiven=true;
        sound(w,s.origin,SoundEvents.ENTITY_ENDER_DRAGON_GROWL,.5f,.55f);
        GalactusMod.LOG.info("Herald warning at {}",s.origin);
    }
    public static void arrive(ServerWorld w) {
        var s=InvasionState.get(w);s.stage=InvasionState.ARRIVAL;s.seconds=0;s.markDirty();
        message(w,"The sky fractures. The Devourer of Worlds is descending.",Formatting.DARK_PURPLE);
        sound(w,s.origin,SoundEvents.ENTITY_WITHER_SPAWN,.6f,.5f);
    }
    public static void invade(ServerWorld w) {
        var s=InvasionState.get(w);s.stage=InvasionState.INVASION;s.seconds=0;s.hunger=0;s.markDirty();
        ensureBoss(w,s);
        for(int i=0;i<4;i++)if((s.broken&(1<<i))==0)ensureAnchor(w,s,i);
        message(w,"GALACTUS: Your world will sustain me. Destroy the four cosmic anchors to break his shield!",Formatting.RED);
        sound(w,s.origin,SoundEvents.ENTITY_ENDER_DRAGON_GROWL,.8f,.5f);
    }
    private static void ensureHerald(ServerWorld w,InvasionState s) {
        if(s.herald!=null && w.getEntity(s.herald)!=null)return;
        var e=GalactusMod.HERALD.create(w);if(e==null)return;
        e.eventBound=true;
        e.refreshPositionAndAngles(s.origin.getX()+.5,s.origin.getY()+6,s.origin.getZ()+12,180,0);
        e.setCustomName(Text.literal(s.ally?"Shalla-Bal • Your ally":"Shalla-Bal • Herald of Galactus"));
        e.setCustomNameVisible(true);w.spawnEntity(e);s.herald=e.getUuid();s.markDirty();
    }
    private static void ensureBoss(ServerWorld w,InvasionState s) {
        if(s.boss!=null && w.getEntity(s.boss)!=null)return;
        var e=GalactusMod.GALACTUS.create(w);if(e==null)return;
        e.eventBound=true;
        e.refreshPositionAndAngles(s.origin.getX()+.5,s.origin.getY(),s.origin.getZ()+.5,0,0);
        e.setCustomName(Text.literal("Galactus • Devourer of Worlds"));w.spawnEntity(e);s.boss=e.getUuid();s.markDirty();
    }
    private static void ensureAnchor(ServerWorld w,InvasionState s,int index) {
        if(s.anchors[index]!=null && w.getEntity(s.anchors[index])!=null)return;
        var e=GalactusMod.ANCHOR.create(w);if(e==null)return;
        var pos=surface(w,s.origin.getX()+OFFSETS[index][0],s.origin.getZ()+OFFSETS[index][1]);
        e.index=index;e.refreshPositionAndAngles(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5,0,0);
        e.setCustomName(Text.literal("Cosmic Anchor "+(index+1)));e.setCustomNameVisible(true);
        w.spawnEntity(e);s.anchors[index]=e.getUuid();s.markDirty();
    }
    private static void combat(ServerWorld w,InvasionState s) {
        ensureBoss(w,s);for(int i=0;i<4;i++)if((s.broken&(1<<i))==0)ensureAnchor(w,s,i);
        var raw=w.getEntity(s.boss);if(!(raw instanceof CosmicEntity boss))return;
        if(!boss.isAlive())return;
        // Colossal entities stand on the actual surface; terrain generation and gravity must not strand them below a new chunk.
        var ground=surface(w,boss.getBlockX(),boss.getBlockZ());boss.setPosition(boss.getX(),ground.getY(),boss.getZ());
        s.hunger=Math.min(CONFIG.hungerSeconds,s.hunger+1);
        if(s.stage==InvasionState.INVASION && s.hunger>=CONFIG.hungerSeconds) {
            s.stage=InvasionState.OCCUPIED;message(w,"The world is occupied. His hunger is endless, but your three paths remain open.",Formatting.RED);
        }
        BAR.setColor(s.exposed()?BossBar.Color.RED:BossBar.Color.PURPLE);
        BAR.setName(Text.literal(s.exposed()?"GALACTUS  •  EXPOSED":"GALACTUS  •  "+s.anchorsLeft()+" COSMIC ANCHORS"));BAR.setPercent(clamp(boss.getHealth()/boss.getMaxHealth()));
        BAR.setDarkenSky(true);
        var target=w.getPlayers().stream().filter(p->!p.isSpectator()).min(Comparator.comparingDouble(p->p.squaredDistanceTo(boss))).orElse(null);
        if(target!=null) {
            double dx=target.getX()-boss.getX(),dz=target.getZ()-boss.getZ();double length=Math.sqrt(dx*dx+dz*dz);
            boss.setYaw((float)(Math.atan2(dz,dx)*180/Math.PI-90));boss.bodyYaw=boss.getYaw();boss.headYaw=boss.getYaw();
            if(length>14 && length<128 && boss.getBlockPos().getSquaredDistance(s.origin)<64*64) {
                double speed=s.stage==InvasionState.OCCUPIED?1.2:.65;
                boss.setPosition(boss.getX()+dx/length*speed,boss.getY(),boss.getZ()+dz/length*speed);
            }
            if(strikeDelay>0) {
                ring(w,strike,5,ParticleTypes.FLAME);
                if(--strikeDelay==0) {
                    w.spawnParticles(ParticleTypes.EXPLOSION,strike.x,strike.y+1,strike.z,4,3,.3,3,0);
                    sound(w,BlockPos.ofFloored(strike),SoundEvents.ENTITY_GENERIC_EXPLODE.value(),.7f,.6f);
                    for(var p:w.getPlayers())if(!p.isCreative()&&!p.isSpectator()&&p.getPos().squaredDistanceTo(strike)<36) {
                        p.damage(w.getDamageSources().mobAttack(boss),s.ally?8:16);p.addVelocity(0,.7,0);
                    }
                }
            } else if(s.seconds%12==0) { strike=target.getPos();strikeDelay=4;target.sendMessage(Text.literal("COSMIC STRIKE — move out of the ring!"),true); }
        }
        if(s.seconds%CONFIG.raidIntervalSeconds==0)raids(w,s);
        if(s.seconds%6==0)consumeTerrain(w,s);
        if(s.ally && s.exposed() && s.seconds%10==0)boss.damage(w.getDamageSources().magic(),16);
        if(s.canRedeem(boss.getHealth()/boss.getMaxHealth()))win(w,"HERALD");
        for(int i=0;i<4;i++)if((s.broken&(1<<i))==0 && w.getEntity(s.anchors[i]) instanceof AnchorEntity a) {
            w.spawnParticles(ParticleTypes.REVERSE_PORTAL,a.getX(),a.getY()+1.5,a.getZ(),12,.6,1,.6,.02);
            Vec3d start=a.getPos().add(0,1,0),end=boss.getPos().add(0,20,0);
            for(int j=0;j<12;j++){var p=start.lerp(end,j/12.0);w.spawnParticles(ParticleTypes.WITCH,p.x,p.y,p.z,1,.1,.1,.1,0);}
        }
    }
    public static void anchorBroken(ServerWorld w,AnchorEntity e) {
        var s=InvasionState.get(w);
        if(!s.active()||e.index<0||e.index>=4||!e.getUuid().equals(s.anchors[e.index])||(s.broken&(1<<e.index))!=0)return;
        s.broken|=1<<e.index;s.markDirty();
        e.dropStack(new ItemStack(CosmicItems.SHARD));
        message(w,"Cosmic Anchor destroyed. "+s.anchorsLeft()+" remain."+(s.exposed()?" GALACTUS IS EXPOSED!":""),Formatting.AQUA);
        sound(w,e.getBlockPos(),SoundEvents.BLOCK_BEACON_DEACTIVATE,.8f,.6f);
    }
    public static boolean redeem(ServerWorld w,PlayerEntity player,CosmicEntity herald) {
        var s=InvasionState.get(w);
        if(!herald.getUuid().equals(s.herald)||s.stage==InvasionState.SAVED||s.ally)return false;
        s.ally=true;s.markDirty();herald.setCustomName(Text.literal("Shalla-Bal • Your ally"));
        message(w,"SHALLA-BAL: I will not sacrifice another world. Break his anchors; weaken him, and I will open the rift.",Formatting.AQUA);
        return true;
    }
    public static boolean canNullify(ServerWorld w,PlayerEntity p) {
        var s=InvasionState.get(w);var boss=s.boss==null?null:w.getEntity(s.boss);
        boolean allowed=s.active()&&s.exposed()&&boss!=null&&p.squaredDistanceTo(boss)<64*64;
        if(!allowed)p.sendMessage(Text.literal("Nullifier needs four broken anchors and Galactus within 64 blocks."),true);
        return allowed;
    }
    public static boolean portalFrame(ServerWorld w,BlockPos pos) {
        if(!w.getBlockState(pos).isOf(CosmicItems.PORTAL_BLOCK)||!w.getBlockState(pos.down()).isOf(Blocks.BEACON))return false;
        for(var o:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(!w.getBlockState(pos.add(o[0],0,o[1])).isOf(Blocks.OBSIDIAN))return false;
        for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})if(!w.getBlockState(pos.add(x,0,z)).isOf(Blocks.GOLD_BLOCK))return false;
        return true;
    }
    public static boolean activatePortal(ServerWorld w,PlayerEntity p,BlockPos pos) {
        if(p==null||!w.getRegistryKey().equals(World.OVERWORLD))return false;
        var s=InvasionState.get(w);
        if(s.portal!=null) {p.sendMessage(Text.literal("A dimensional core is already charging at "+coords(s.portal)+"."),true);return false;}
        if(!s.active()||!s.exposed()||!portalFrame(w,pos)||pos.getSquaredDistance(s.origin)>128*128) {
            p.sendMessage(Text.literal("Break all anchors. Core must be within 128 blocks of invasion, on a beacon, with obsidian on four sides and gold at four corners."),false);return false;
        }
        if(count(p,Items.ENDER_PEARL)<32) {p.sendMessage(Text.literal("The core needs 32 Ender Pearls."),true);return false;}
        take(p,Items.ENDER_PEARL,32);s.portal=pos.toImmutable();s.portalCharge=0;s.markDirty();
        message(w,"Dimensional core activated. Defend it within 48 blocks for "+CONFIG.portalChargeSeconds+" seconds!",Formatting.AQUA);
        return true;
    }
    private static void portalTick(ServerWorld w,InvasionState s) {
        // Keep the core loaded only while the invasion is running with players present.
        w.getChunkManager().addTicket(ChunkTicketType.PORTAL,new ChunkPos(s.portal),3,s.portal);
        if(!portalFrame(w,s.portal)) {
            message(w,"The portal frame broke. Rebuild and refuel it; the charge has been lost.",Formatting.RED);s.portal=null;s.portalCharge=0;return;
        }
        var defenders=w.getPlayers().stream().filter(p->!p.isSpectator()&&p.getBlockPos().getSquaredDistance(s.portal)<48*48).toList();
        if(!defenders.isEmpty())s.portalCharge++;
        ring(w,Vec3d.ofCenter(s.portal),6,ParticleTypes.PORTAL);
        for(var p:defenders)p.sendMessage(Text.literal("DIMENSIONAL CORE  "+(100*s.portalCharge/CONFIG.portalChargeSeconds)+"%  •  Defend the frame"),true);
        if(s.portalCharge>=CONFIG.portalChargeSeconds)win(w,"PORTAL");
    }
    public static void win(ServerWorld w,String method) {
        var s=InvasionState.get(w);if(s.stage==InvasionState.SAVED)return;
        s.stage=InvasionState.SAVED;s.victory=method;s.markDirty();strike=null;strikeDelay=0;
        if(s.boss!=null && w.getEntity(s.boss)!=null) {var e=w.getEntity(s.boss);w.spawnParticles(ParticleTypes.PORTAL,e.getX(),e.getY()+18,e.getZ(),400,10,14,10,.3);e.discard();}
        for(var id:s.anchors)if(id!=null&&w.getEntity(id)!=null)w.getEntity(id).discard();
        clearRaids(w);
        BAR.clearPlayers();HUNGER.clearPlayers();BAR.setDarkenSky(false);
        message(w,"WORLD SAVED — "+switch(method){case "NULLIFIER"->"The Ultimate Nullifier ended the hunger.";case "PORTAL"->"Galactus was banished beyond the dimensional rift.";case "HERALD"->"Shalla-Bal betrayed her master and saved your world.";default->"The Devourer of Worlds has fallen.";},Formatting.GOLD);
        for(var p:w.getPlayers())give(p,new ItemStack(CosmicItems.TROPHY));
        sound(w,s.origin,SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,.8f,1);
        GalactusMod.LOG.info("World saved via {}",method);
    }
    public static void reset(ServerWorld w) {
        var cosmic=new ArrayList<Entity>();for(var e:w.iterateEntities())if(e.getType()==GalactusMod.GALACTUS||e.getType()==GalactusMod.HERALD||e.getType()==GalactusMod.ANCHOR)cosmic.add(e);
        for(var e:cosmic)e.discard();
        var s=InvasionState.get(w);if(s.boss!=null&&w.getEntity(s.boss)!=null)w.getEntity(s.boss).discard();
        if(s.herald!=null&&w.getEntity(s.herald)!=null)w.getEntity(s.herald).discard();
        for(var id:s.anchors)if(id!=null&&w.getEntity(id)!=null)w.getEntity(id).discard();
        s.stage=0;s.seconds=0;s.dormantSeconds=0;s.hunger=0;s.broken=0;s.ally=false;s.boss=null;s.herald=null;s.anchors=new UUID[4];s.portal=null;s.portalCharge=0;s.victory="";s.markDirty();
        strike=null;strikeDelay=0;BAR.clearPlayers();HUNGER.clearPlayers();BAR.setDarkenSky(false);
        clearRaids(w);
    }
    private static void clearRaids(ServerWorld w) {var list=new ArrayList<Entity>();for(var e:w.iterateEntities())if(e.getCommandTags().contains("galactus_raid"))list.add(e);for(var e:list)e.discard();}
    private static void raids(ServerWorld w,InvasionState s) {
        int count=0;for(var entity:w.iterateEntities())if(entity.getCommandTags().contains("galactus_raid"))count++;
        if(count>=CONFIG.maxRaidMobs)return;
        for(var p:w.getPlayers()) {
            if(p.isSpectator()||(s.stage!=InvasionState.OCCUPIED&&p.getBlockPos().getSquaredDistance(s.origin)>192*192))continue;
            int number=Math.min(s.hunger>CONFIG.hungerSeconds/2?4:2,CONFIG.maxRaidMobs-count);
            for(int i=0;i<number;i++) {
                double angle=w.random.nextDouble()*Math.PI*2;var pos=surface(w,(int)(p.getX()+Math.cos(angle)*18),(int)(p.getZ()+Math.sin(angle)*18));
                HostileEntity mob=(i%2==0?EntityType.HUSK:EntityType.ENDERMITE).create(w);
                if(mob==null)continue;mob.refreshPositionAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
                mob.setCustomName(Text.literal("Cosmic Thrall"));mob.addCommandTag("galactus_raid");mob.setTarget(p);w.spawnEntity(mob);count++;
            }
        }
    }
    private static void consumeTerrain(ServerWorld w,InvasionState s) {
        if(!CONFIG.terrainDestruction || CONFIG.maxConsumedBlocksPerPulse==0 || !w.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING))return;
        BlockPos center=s.origin;
        if(s.stage==InvasionState.OCCUPIED&&!w.getPlayers().isEmpty())center=w.getPlayers().get(w.random.nextInt(w.getPlayers().size())).getBlockPos();
        int radius=24+(int)((CONFIG.maxInvasionRadius-24)*clamp(s.hunger/(float)CONFIG.hungerSeconds));
        for(int i=0;i<CONFIG.maxConsumedBlocksPerPulse;i++) {
            int x=center.getX()+w.random.nextInt(radius*2+1)-radius,z=center.getZ()+w.random.nextInt(radius*2+1)-radius;
            if(!w.isChunkLoaded(new BlockPos(x,center.getY(),z)))continue;
            var pos=new BlockPos(x,w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z)-1,z);
            if(!w.isChunkLoaded(pos)||pos.getY()<=w.getBottomY()+1)continue;
            if(s.portal!=null&&pos.getSquaredDistance(s.portal)<12*12)continue;
            var block=w.getBlockState(pos);
            if(block.isAir()||block.getHardness(w,pos)<0||block.hasBlockEntity()||block.isOf(Blocks.OBSIDIAN)||block.isOf(CosmicItems.PORTAL_BLOCK)||block.isOf(Blocks.GOLD_BLOCK))continue;
            if(w.random.nextInt(3)==0)w.breakBlock(pos,false);else w.setBlockState(pos,Blocks.CRYING_OBSIDIAN.getDefaultState(),3);
            w.spawnParticles(ParticleTypes.REVERSE_PORTAL,x+.5,pos.getY()+1,z+.5,2,.2,.5,.2,.05);
        }
    }
    public static void status(ServerWorld w,PlayerEntity p) {
        var s=InvasionState.get(w);
        p.sendMessage(Text.literal("[GALACTUS] "+switch(s.stage){case 0->"Dormant";case 1->"Preparing: "+clock(CONFIG.preparationDays*1200-s.seconds);case 2->"Arriving";case 3->"Invasion • hunger "+100*s.hunger/CONFIG.hungerSeconds+"%";case 4->"Occupied • still recoverable";default->"Saved via "+s.victory;}),false);
        if(s.stage!=0) p.sendMessage(Text.literal("Invasion: "+coords(s.origin)+" • Anchors left: "+s.anchorsLeft()+" • Herald allied: "+s.ally),false);
        if(s.active())for(int i=0;i<4;i++)if((s.broken&(1<<i))==0)p.sendMessage(Text.literal("Anchor "+(i+1)+": "+(s.origin.getX()+OFFSETS[i][0])+", ~, "+(s.origin.getZ()+OFFSETS[i][1])),false);
    }
    public static void onJoin(ServerPlayerEntity p) {
        var s=InvasionState.get(p.getServerWorld().getServer().getOverworld());
        if(s.stage>0 && s.stage<5){if(!p.getCommandTags().contains("galactus_journal_received")){give(p,CosmicItems.guide());give(p,new ItemStack(CosmicItems.RECEIVER));p.addCommandTag("galactus_journal_received");}p.sendMessage(Text.literal("A cosmic event is underway. Use a Cosmic Receiver or /galactus status."),false);}
    }
    static BlockPos surface(ServerWorld w,int x,int z) { w.getChunk(x>>4,z>>4);return new BlockPos(x,w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z),z); }
    private static void ring(ServerWorld w,Vec3d center,double radius,net.minecraft.particle.ParticleEffect type) {
        for(int i=0;i<24;i++){double a=i*Math.PI/12;w.spawnParticles(type,center.x+Math.cos(a)*radius,center.y+.1,center.z+Math.sin(a)*radius,1,0,.1,0,0);}
    }
    private static void sound(ServerWorld w,BlockPos pos,SoundEvent event,float volume,float pitch) {w.playSound(null,pos,event,SoundCategory.HOSTILE,volume,pitch);}
    private static void message(ServerWorld w,String message,Formatting color) {for(var p:w.getPlayers())p.sendMessage(Text.literal(message).formatted(color),false);}
    private static void give(PlayerEntity p,ItemStack stack) { if(!p.getInventory().insertStack(stack))p.dropItem(stack,false); }
    static int count(PlayerEntity p,Item item) {int n=0;for(int i=0;i<p.getInventory().size();i++){var stack=p.getInventory().getStack(i);if(stack.isOf(item))n+=stack.getCount();}return n;}
    private static void take(PlayerEntity p,Item item,int count) {for(int i=0;i<p.getInventory().size()&&count>0;i++){var stack=p.getInventory().getStack(i);if(stack.isOf(item)){int n=Math.min(count,stack.getCount());stack.decrement(n);count-=n;}}}
    static float clamp(float n) {return Math.max(0,Math.min(1,n));}
    private static String clock(int seconds) {seconds=Math.max(0,seconds);return String.format("%02d:%02d",seconds/60,seconds%60);}
    private static String coords(BlockPos pos) {return pos.getX()+", "+pos.getY()+", "+pos.getZ();}
}
