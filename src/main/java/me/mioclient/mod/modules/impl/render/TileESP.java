package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.tileentity.*;

import java.awt.*;

/**
 * @author t.me/asphyxia1337
 */

public class TileESP extends Module {

    private final Setting<Boolean> beds =
            add(new Setting<>("Beds", true));
    private final Setting<Boolean> chests =
            add(new Setting<>("Chests", true));
    private final Setting<Boolean> eChests =
            add(new Setting<>("EChests", true));
    private final Setting<Boolean> shulkers =
            add(new Setting<>("Shulkers", true));
    private final Setting<Boolean> signs =
            add(new Setting<>("Signs", true));
    private final Setting<Boolean> dispensers =
            add(new Setting<>("Dispensers", true));
    private final Setting<Boolean> hoppers =
            add(new Setting<>("Hoppers", true));
    private final Setting<Boolean> furnaces =
            add(new Setting<>("Furnaces", true));

    private int count;

    public TileESP() {
        super("TileESP", "Highlights tile entities such as storages and signs.", Category.RENDER, true);
    }

    @Override
    public String getInfo() {
        return String.valueOf(count);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        count = 0;

        for (TileEntity entity : mc.world.loadedTileEntityList) {
            if (isValid(entity)) {
                RenderUtil.drawSelectionBoxESP(entity.getPos(), getColor(entity), false, new Color(-1), 1.0f, true, true, 100, false);
                count++;
            }
        }
    }

    private Color getColor(TileEntity entity) {
        if (entity instanceof TileEntityChest) return new Color(155, 127, 77, 100);

        if (entity instanceof TileEntityBed) return new Color(190, 49, 49, 100);

        if (entity instanceof TileEntityEnderChest) return new Color(124, 37, 196, 100);

        if (entity instanceof TileEntityShulkerBox) return new Color(255, 1, 175, 100);

        if (entity instanceof TileEntityFurnace || entity instanceof TileEntityDispenser || entity instanceof TileEntityHopper) return new Color(150, 150, 150, 100);

        return new Color(255, 255, 255, 100);
    }

    private boolean isValid(TileEntity entity) {
        return entity instanceof TileEntityChest && chests.getValue()
                || entity instanceof TileEntityBed && beds.getValue()
                || entity instanceof TileEntityEnderChest && eChests.getValue()
                || entity instanceof TileEntityShulkerBox && shulkers.getValue()
                || entity instanceof TileEntityFurnace && furnaces.getValue()
                || entity instanceof TileEntityDispenser && dispensers.getValue()
                || entity instanceof TileEntityHopper && hoppers.getValue()
                || entity instanceof TileEntitySign && signs.getValue();
    }
}
