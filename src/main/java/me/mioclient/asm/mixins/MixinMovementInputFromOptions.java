package me.mioclient.asm.mixins;

import me.mioclient.api.util.Wrapper;
import me.mioclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInputFromOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions implements Wrapper {

    @Redirect(method = "updatePlayerMoveState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    public boolean isKeyPressed(KeyBinding keyBinding) {

        return ClickGui.INSTANCE.guiMove.getValue() 
                && !(mc.currentScreen instanceof GuiChat)     
                ? Keyboard.isKeyDown(keyBinding.getKeyCode())
                : keyBinding.isKeyDown();
    }

}