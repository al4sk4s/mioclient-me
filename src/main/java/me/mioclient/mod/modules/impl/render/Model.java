package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class Model extends Module {

    public static Model INSTANCE;

    public Setting<Page> settings =
            add(new Setting<>("Settings", Page.OFFSETS));

    //Offsets

    public Setting<Double> mainScale =
            add(new Setting<>("MainScale" , 1.0 , 0.0 , 2.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Double> mainX =
            add(new Setting<>("MainX" , 0.0 , -1.0 , 1.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Double> mainY =
            add(new Setting<>("MainY" , 0.0 , -1.0 , 1.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Double> offScale =
            add(new Setting<>("OffScale" , 1.0 , 0.0 , 2.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Double> offX =
            add(new Setting<>("OffX" , 0.0 , -1.0 , 1.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Double> offY =
            add(new Setting<>("OffY" , 0.0 , -1.0 , 1.0 , v -> settings.getValue() == Page.OFFSETS));
    public Setting<Boolean> spinY =
            add(new Setting<>("SpinX", false, v -> settings.getValue() == Page.OFFSETS));
    public Setting<Boolean> spinX =
            add(new Setting<>("SpinY", false, v -> settings.getValue() == Page.OFFSETS));

    //All other mods

    public Setting<Boolean> customSwing =
            add(new Setting<>("CustomSwing", false, v -> settings.getValue() == Page.OTHERS).setParent());
    public Setting<Swing> swing =
            add(new Setting<>("Swing", Swing.MAINHAND, v -> settings.getValue() == Page.OTHERS && customSwing.isOpen()));
    public Setting<Boolean> slowSwing =
            add(new Setting<>("SlowSwing", false, v -> settings.getValue() == Page.OTHERS));
    public Setting<Boolean> noSway =
            add(new Setting<>("NoSway", false, v -> settings.getValue() == Page.OTHERS));
    public Setting<Boolean> instantSwap =
            add(new Setting<>("InstantSwap", false, v -> settings.getValue() == Page.OTHERS));
    public Setting<Boolean> swordChange =
            add(new Setting<>("SwordHandSwap", false, v -> settings.getValue() == Page.OTHERS));

    public Model() {
        super("Model", "Changes view model.", Category.RENDER);
        INSTANCE = this;
    }

    private enum Page {
        OTHERS,
        OFFSETS
    }

    public enum Swing {
        MAINHAND,
        OFFHAND,
        SERVER
    }

    @Override
    public void onUpdate(){
        if (instantSwap.getValue()) {

            if (mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9) {
                mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
                mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItemMainhand();
            }
        }

        if (customSwing.getValue()) {

            if (swing.getValue() == Model.Swing.OFFHAND) {
                mc.player.swingingHand = EnumHand.OFF_HAND;

            } else if (swing.getValue() == Model.Swing.MAINHAND) {
                mc.player.swingingHand = EnumHand.MAIN_HAND;
            }
        }

        if (swordChange.getValue()) {

            if (EntityUtil.isHoldingWeapon(mc.player)) {
                mc.player.setPrimaryHand(EnumHandSide.LEFT);

            } else {
                mc.player.setPrimaryHand(EnumHandSide.RIGHT);
            }
        }
    }
}

