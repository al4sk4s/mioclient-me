package me.mioclient.mod.modules.impl.misc;

import me.mioclient.api.util.math.Timer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * @author t.me/asphyxia1337
 */

public class KillEffects extends Module {

    private final Setting<Lightning> lightning =
            add(new Setting<>("Lightning", Lightning.NORMAL));
    private final Setting<KillSound> killSound =
            add(new Setting<>("KillSound", KillSound.OFF));

    private final Timer timer = new Timer();

    public KillEffects() {
        super("KillEffects", "jajaja hypixel mode", Category.MISC);
    }

    private enum Lightning {
        NORMAL,
        SILENT,
        OFF
    }

    private enum KillSound {
        CS,
        NEVERLOSE,
        HYPIXEL,
        OFF
    }

    @Override
    public void onDeath(EntityPlayer player) {

        if (player == null
                || player == mc.player
                || player.getHealth() > 0.0f
                || mc.player.isDead
                || nullCheck()
                || fullNullCheck()) return;
        
        if (timer.passedMs(1500L)) {

            if (lightning.getValue() != Lightning.OFF) {

                mc.world.spawnEntity(new EntityLightningBolt(mc.world, player.posX, player.posY, player.posZ, true));

                if (lightning.getValue() == Lightning.NORMAL) {
                    mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.0f);
                }
            }

            if (killSound.getValue() != KillSound.OFF) {
                SoundEvent sound = getSound();

                if (sound != null) {
                    mc.player.playSound(sound, 1.0f, 1.0f);
                }
            }
            timer.reset();
        }
    }

    private SoundEvent getSound() {

        switch (killSound.getValue()) {

            case CS:
                return new SoundEvent(new ResourceLocation("mio", "kill_sound_cs"));

            case NEVERLOSE:
                return new SoundEvent(new ResourceLocation("mio", "kill_sound_nl"));

            case HYPIXEL:
                return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;

            default:
                return null;

        }
    }
}
