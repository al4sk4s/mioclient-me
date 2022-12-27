package me.mioclient.asm.mixins;

import me.mioclient.Mio;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.impl.exploit.MultiTask;
import me.mioclient.mod.modules.impl.misc.UnfocusedCPU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.crash.CrashReport;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {Minecraft.class})
public abstract class MixinMinecraft {

    @Inject(method={"getLimitFramerate"}, at={@At(value="HEAD")}, cancellable=true)
    public void getLimitFramerateHook(CallbackInfoReturnable<Integer> info) {
        UnfocusedCPU mod = UnfocusedCPU.INSTANCE;

        try {
            if (mod.isOn() && !Display.isActive()) {
                info.setReturnValue(mod.unfocusedFps.getValue());
            }

        } catch (Exception ignored) {

        }
    }

    @Inject(method = {"shutdownMinecraftApplet"}, at = {@At(value = "HEAD")})
    private void stopClient(CallbackInfo callbackInfo) {
        unload();
    }

    @Redirect(method = {"run"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void displayCrashReport(Minecraft minecraft, CrashReport crashReport) {
        unload();
    }

    private void unload() {
        Mio.LOGGER.info("Initiated client shutdown.");

        Managers.onUnload();

        Mio.LOGGER.info("Finished client shutdown.");
    }

    @Redirect(method = {"sendClickBlockToController"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    private boolean isHandActiveWrapper(EntityPlayerSP playerSP) {
        return !MultiTask.INSTANCE.isOn() && playerSP.isHandActive();
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0))
    private boolean isHittingBlockHook(PlayerControllerMP playerControllerMP) {
        return !MultiTask.INSTANCE.isOn() && playerControllerMP.getIsHittingBlock();
    }
}

