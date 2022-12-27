package me.mioclient.mod.modules.impl.client;

import me.mioclient.api.events.impl.PerspectiveEvent;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.MobEffects;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author t.me/asphyxia1337
 */

public class FovMod extends Module {

    private static FovMod INSTANCE = new FovMod();

    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.FOV));

    private final Setting<Boolean> customFov =
            add(new Setting<>("CustomFov", false, v -> page.getValue() == Page.FOV).setParent());
    private final Setting<Float> fov =
            add(new Setting<>("FOV", 120.0f, 10.0f, 180.0f, v -> page.getValue() == Page.FOV && customFov.isOpen()));

    private final Setting<Boolean> aspectRatio =
            add(new Setting<>("AspectRatio", false, v -> page.getValue() == Page.FOV).setParent());
    private final Setting<Float> aspectFactor =
            add(new Setting<>("AspectFactor", 1.8f, 0.1f, 3.0f, v -> page.getValue() == Page.FOV && aspectRatio.isOpen()));

    private final Setting<Boolean> defaults =
            add(new Setting<>("Defaults", false, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Float> sprint =
            add(new Setting<>("SprintAdd", 1.15f, 1.00f, 2.00f, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Float> speed =
            add(new Setting<>("SwiftnessAdd", 1.15f, 1.00f, 2.00f, v -> page.getValue() == Page.ADVANCED));

    public FovMod() {
        super("FovMod", "FOV modifier.", Category.CLIENT, true);
        setInstance();
    }

    public static FovMod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FovMod();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public enum Page {
        FOV,
        ADVANCED
    }

    @Override
    public void onUpdate() {
        if (customFov.getValue()) {
            FovMod.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, fov.getValue());
        }

        if (defaults.getValue()) {
            sprint.setValue(1.15f);
            speed.setValue(1.15f);

            defaults.setValue(false);
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        float fov = 1.0f;

        if (event.getEntity().isSprinting()) {

            fov = sprint.getValue();

            if (event.getEntity().isPotionActive(MobEffects.SPEED)) {
                fov = speed.getValue();
            }
        }

        event.setNewfov(fov);
    }

    @SubscribeEvent
    public void onPerspectiveUpdate(PerspectiveEvent event) {
        if (aspectRatio.getValue()) {
            event.setAngle(aspectFactor.getValue());
        }
    }
}