package me.mioclient.asm.mixins;

import me.mioclient.mod.modules.impl.render.Model;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityLivingBase.class})
public class MixinEntityLivingBase {

    @Inject(method={"getArmSwingAnimationEnd"}, at={@At(value="HEAD")}, cancellable=true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {

        Model mod = Model.INSTANCE;

        if (mod.isOn() && mod.slowSwing.getValue()) {
            info.setReturnValue(15);

        } else if (mod.isOn() && mod.customSwing.getValue() && mod.swing.getValue() == Model.Swing.SERVER) {
            info.setReturnValue(-1);
        }
    }
}

