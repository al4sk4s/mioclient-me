package me.mioclient.mod.modules.impl.movement;

import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;

/**
 * @author t.me/asphyxia1337
 */

public class Sprint extends Module {

    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.RAGE));

    public Sprint() {
        super("Sprint", "Sprints.", Category.MOVEMENT);
    }

    private enum Mode {
        RAGE,
        LEGIT
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(mode.getValue());
    }

    @Override
    public void onTick() {
        if (mode.getValue() == Mode.RAGE && isMoving()) {
            mc.player.setSprinting(true);

        } else if (mode.getValue() == Mode.LEGIT
                && mc.gameSettings.keyBindForward.isKeyDown()
                && !mc.player.collidedHorizontally
                && !mc.player.isSneaking()) {

            mc.player.setSprinting(true);
        }
    }

    public static boolean isMoving() {
        return mc.gameSettings.keyBindForward.isKeyDown()
                || mc.gameSettings.keyBindBack.isKeyDown()
                || mc.gameSettings.keyBindLeft.isKeyDown()
                || mc.gameSettings.keyBindRight.isKeyDown();
    }
}
