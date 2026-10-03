package ro.galactus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;

public final class InvasionConfig {
    public boolean automaticInvasion = true;
    public int heraldAfterDays = 2;
    public int preparationDays = 3;
    public int arrivalSeconds = 45;
    public int hungerSeconds = 1200;
    public boolean terrainDestruction = true;
    public int maxConsumedBlocksPerPulse = 24;
    public int maxInvasionRadius = 192;
    public int raidIntervalSeconds = 45;
    public int maxRaidMobs = 24;
    public int portalChargeSeconds = 90;
    public static InvasionConfig load() {
        var path=FabricLoader.getInstance().getConfigDir().resolve("galactus.json");
        var gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            InvasionConfig c=Files.exists(path)?gson.fromJson(Files.readString(path),InvasionConfig.class):new InvasionConfig();
            if(c==null)c=new InvasionConfig();
            c.heraldAfterDays=bound(c.heraldAfterDays,0,1000); c.preparationDays=bound(c.preparationDays,1,1000);
            c.arrivalSeconds=bound(c.arrivalSeconds,5,3600); c.hungerSeconds=bound(c.hungerSeconds,120,86400);
            c.maxConsumedBlocksPerPulse=bound(c.maxConsumedBlocksPerPulse,0,64); c.maxInvasionRadius=bound(c.maxInvasionRadius,64,512);
            c.raidIntervalSeconds=bound(c.raidIntervalSeconds,10,600); c.maxRaidMobs=bound(c.maxRaidMobs,0,64);
            c.portalChargeSeconds=bound(c.portalChargeSeconds,10,600);
            Files.createDirectories(path.getParent()); Files.writeString(path,gson.toJson(c)); return c;
        } catch(Exception ex) { GalactusMod.LOG.error("Cannot read galactus.json; using defaults",ex); return new InvasionConfig(); }
    }
    private static int bound(int n,int lo,int hi) { return Math.max(lo,Math.min(hi,n)); }
}
