package ro.galactus.client;

import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.entity.Entity;
import ro.galactus.GalactusMod;
import java.nio.file.Files;

/** Development-only harness. Never active in a normal installed mod. No mouse/keyboard injection. */
final class LabBridge {
    private static long request=-1;
    private static final long BOOT=System.currentTimeMillis();
    private static int hold=0,capture=0,frame=0,ticks=0,shotDelay=0;
    private static String shotName=null;
    static void tick(MinecraftClient c) {
        ticks++;c.options.pauseOnLostFocus=false;
        if(c.world==null||c.player==null)return;
        if(c.currentScreen instanceof net.minecraft.client.gui.screen.GameMenuScreen)c.setScreen(null);
        if(c.options.getGuiScale().getValue()!=2){c.options.getGuiScale().setValue(2);c.onResolutionChanged();}
        if(shotDelay>0 && --shotDelay==0)ScreenshotRecorder.saveScreenshot(c.runDirectory,shotName+".png",c.getFramebuffer(),text->{});
        if(hold>0 && --hold==0)c.options.useKey.setPressed(false);
        if(capture>0){capture--;if(ticks%4==0)ScreenshotRecorder.saveScreenshot(c.runDirectory,String.format("take-%05d.png",frame++),c.getFramebuffer(),text->{});}
        if(ticks%5!=0)return;
        var path=c.runDirectory.toPath().resolve("lab-control.json");
        try {
            if(!Files.exists(path))return;
            var json=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            long id=json.get("id").getAsLong();if(id<=BOOT||id==request)return;request=id;
            String action=json.get("action").getAsString();
            switch(action) {
                case "shot" -> {shotName=json.get("name").getAsString();shotDelay=10;}
                case "capture" -> {capture=json.get("ticks").getAsInt();frame=0;}
                case "presentation" -> {
                    c.options.hudHidden=json.has("clean")&&json.get("clean").getAsBoolean();
                    if(json.has("fov"))c.options.getFov().setValue(json.get("fov").getAsInt());
                    if(json.has("language")) {
                        c.options.language=json.get("language").getAsString();
                        c.getLanguageManager().setLanguage(c.options.language);c.reloadResources();
                    }
                }
                case "use" -> {c.options.useKey.setPressed(true);hold=json.has("hold")?json.get("hold").getAsInt():5;c.interactionManager.interactItem(c.player,Hand.MAIN_HAND);}
                case "herald" -> {for(Entity e:c.world.getEntities())if(e.getType()==GalactusMod.HERALD){c.interactionManager.interactEntity(c.player,e,Hand.MAIN_HAND);break;}}
                case "core" -> {var p=new BlockPos(json.get("x").getAsInt(),json.get("y").getAsInt(),json.get("z").getAsInt());c.interactionManager.interactBlock(c.player,Hand.MAIN_HAND,new BlockHitResult(Vec3d.ofCenter(p),Direction.UP,p,false));}
                case "close" -> c.scheduleStop();
            }
            Files.writeString(c.runDirectory.toPath().resolve("lab-result.json"),"{\"id\":"+id+",\"action\":\""+action+"\",\"held\":\""+c.player.getMainHandStack().getItem()+"\"}");
        } catch(Exception ex){GalactusMod.LOG.error("Lab bridge failed",ex);}
    }
}
