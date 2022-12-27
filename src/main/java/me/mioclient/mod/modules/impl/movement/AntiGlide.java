package me.mioclient.mod.modules.impl.movement;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovementInput;

/**
 * @author t.me/asphyxia1337
 */

public class AntiGlide extends Module {

    private final Setting<Boolean> onGround =
            add(new Setting<>("OnGround", true));
    private final Setting<Boolean> ice =
            add(new Setting<>("Ice", true));

    public AntiGlide() {
        super("AntiGlide", "Prevents inertial moving.", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        setIceSlipperiness(0.98f);
    }

    @Override
    public void onUpdate() {

        if (onGround.getValue() && !mc.player.onGround) return;

        MovementInput input = mc.player.movementInput;

        if (input.moveForward == 0.0 && input.moveStrafe == 0.0) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        }

        if (ice.getValue() && mc.player.getRidingEntity() == null) {
            setIceSlipperiness(0.6f);

        } else {
            setIceSlipperiness(0.98f);
        }
    }

    private void setIceSlipperiness(float in) {
        Blocks.ICE.setDefaultSlipperiness(in);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(in);
        Blocks.PACKED_ICE.setDefaultSlipperiness(in);
    }
}
