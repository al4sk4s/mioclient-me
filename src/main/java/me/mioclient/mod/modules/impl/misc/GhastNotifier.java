package me.mioclient.mod.modules.impl.misc;

import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.SoundEvents;

import java.util.HashSet;
import java.util.Set;

public class GhastNotifier extends Module {

    private final Setting<Boolean> chat =
            add(new Setting<>("Chat", true).setParent());
    private final Setting<Boolean> censorCoords =
            add(new Setting<>("CensorCoords", false, v -> chat.isOpen()));
    private final Setting<Boolean> sound =
            add(new Setting<>("Sound", true));

    private final Set<Entity> ghasts = new HashSet<>();

    public GhastNotifier() {
        super("GhastNotify", "Helps you find ghasts", Category.MISC);
    }

    @Override
    public void onEnable() {
        ghasts.clear();
    }

    @Override
    public void onUpdate() {
        for (Entity entity : GhastNotifier.mc.world.getLoadedEntityList()) {

            if (!(entity instanceof EntityGhast) || ghasts.contains(entity)) continue;

            if (chat.getValue()) {
                if (censorCoords.getValue()) {
                    Command.sendMessage("There is a ghast!");

                } else {
                    Command.sendMessage(
                            "There is a ghast at: "
                                    + entity.getPosition().getX()
                                    + "X, "
                                    + entity.getPosition().getY()
                                    + "Y, "
                                    + entity.getPosition().getZ()
                                    + "Z.");
                }
            }
            ghasts.add(entity);

            if (!sound.getValue()) continue;

            GhastNotifier.mc.player.playSound(SoundEvents.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
        }
    }
}