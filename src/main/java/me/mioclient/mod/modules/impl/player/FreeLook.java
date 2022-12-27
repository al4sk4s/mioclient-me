package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.events.impl.TurnEvent;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FreeLook extends Module {

    private float dYaw, dPitch;

    public FreeLook() {
        super("FreeLook", "Rotate your camera and not your player in 3rd person.", Category.PLAYER, true);
    }

    @Override
    public void onEnable() {
        dYaw = 0;
        dPitch = 0;

        mc.gameSettings.thirdPersonView = 1;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.thirdPersonView = 0;
    }

    @Override
    public void onTick() {
        if (mc.gameSettings.thirdPersonView != 1) {
            disable();
        }
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (mc.gameSettings.thirdPersonView > 0) {
            event.setYaw(event.getYaw() + dYaw);
            event.setPitch(event.getPitch() + dPitch);
        }
    }

    @SubscribeEvent
    public void onTurn(TurnEvent event) {
        if (mc.gameSettings.thirdPersonView > 0) {
            dYaw = (float) ((double) dYaw + (double) event.getYaw() * 0.15D);
            dPitch = (float) ((double) dPitch - (double) event.getPitch() * 0.15D);
            dPitch = MathHelper.clamp(dPitch, -90.0F, 90.0F);
            event.cancel();
        }
    }
}