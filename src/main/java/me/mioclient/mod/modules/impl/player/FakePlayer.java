package me.mioclient.mod.modules.impl.player;

import com.mojang.authlib.GameProfile;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.GameType;

import java.util.UUID;

public class FakePlayer extends Module {

    private final Setting<String> name =
            add(new Setting<>("Name", "Herobrine"));

    public FakePlayer() {
        super("FakePlayer", "Summons a client-side fake player.", Category.PLAYER, true);
    }

    @Override
    public String getInfo() {
        return name.getValue();
    }

    @Override
    public void onEnable() {

        Command.sendMessage("Spawned a fakeplayer with the name " + name.getValue() + ".");

        if (mc.player == null || mc.player.isDead) {
            disable();
            return;
        }

        EntityOtherPlayerMP player = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("0f75a81d-70e5-43c5-b892-f33c524284f2"), name.getValue()));

        player.copyLocationAndAnglesFrom(mc.player);
        player.rotationYawHead = mc.player.rotationYawHead;
        player.rotationYaw = mc.player.rotationYaw;
        player.rotationPitch = mc.player.rotationPitch;
        player.setGameType(GameType.SURVIVAL);
        player.inventory.copyInventory(FakePlayer.mc.player.inventory);
        player.setHealth(20);
        mc.world.addEntityToWorld(-12345, player);
        player.onLivingUpdate();
    }

    @Override
    public void onDisable() {
        if (mc.world != null) {
            mc.world.removeEntityFromWorld(-12345);
        }
    }

    @Override
    public void onLogout() {
        if (isOn()){
            disable();
        }
    }
}

