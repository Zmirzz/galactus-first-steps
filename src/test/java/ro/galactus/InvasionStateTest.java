package ro.galactus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

class InvasionStateTest {
    @Test void reloadKeepsCountdownAnchorsAllegianceAndPortalProgress() {
        var s=new InvasionState();s.stage=InvasionState.INVASION;s.seconds=483;s.hunger=400;s.dormantSeconds=2400;
        s.broken=5;s.ally=true;s.portalCharge=37;s.origin=new BlockPos(100,72,-80);s.portal=new BlockPos(120,70,-60);
        s.boss=UUID.randomUUID();s.herald=UUID.randomUUID();for(int i=0;i<4;i++)s.anchors[i]=UUID.randomUUID();
        var loaded=InvasionState.read(s.writeNbt(new NbtCompound(),null),null);
        assertEquals(483,loaded.seconds);assertEquals(400,loaded.hunger);assertEquals(37,loaded.portalCharge);
        assertEquals(2400,loaded.dormantSeconds);assertEquals(s.origin,loaded.origin);assertEquals(s.portal,loaded.portal);
        assertEquals(s.boss,loaded.boss);assertEquals(s.herald,loaded.herald);assertArrayEquals(s.anchors,loaded.anchors);
        assertTrue(loaded.ally);assertEquals(2,loaded.anchorsLeft());assertFalse(loaded.exposed());
    }
    @Test void redemptionRequiresAllianceAllAnchorsAndWeakenedBoss() {
        var s=new InvasionState();s.stage=InvasionState.INVASION;s.broken=15;
        assertFalse(s.canRedeem(.3f));s.ally=true;assertFalse(s.canRedeem(.36f));assertTrue(s.canRedeem(.35f));
        s.broken=7;assertFalse(s.canRedeem(.1f));s.broken=15;s.stage=InvasionState.PREPARATION;assertFalse(s.canRedeem(.1f));
    }
    @Test void occupiedWorldRemainsRecoverableAndSavedWorldIsTerminal() {
        var s=new InvasionState();s.stage=InvasionState.OCCUPIED;s.ally=true;s.broken=15;
        assertTrue(s.active());assertTrue(s.canRedeem(.2f));s.stage=InvasionState.SAVED;s.victory="HERALD";
        assertFalse(s.active());assertFalse(s.canRedeem(.2f));var loaded=InvasionState.read(s.writeNbt(new NbtCompound(),null),null);
        assertEquals("HERALD",loaded.victory);assertEquals(InvasionState.SAVED,loaded.stage);
    }
}
