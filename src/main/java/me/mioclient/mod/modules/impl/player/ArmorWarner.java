
package me.mioclient.mod.modules.impl.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorWarner extends Module {

    private final Setting<Integer> armorThreshold =
            add(new Setting<>("Armor%", 20, 1, 100));
    private final Setting<Boolean> notifySelf =
            add(new Setting<>("Self", true));
    private final Setting<Boolean> notification =
            add(new Setting<>("Friends", true));

    private final Map<EntityPlayer, Integer> entityArmorArraylist = new HashMap<>();

    public ArmorWarner() {
        super("ArmorWarner", "Notifies when your armor is low durability.", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player.isDead || !Managers.FRIENDS.isFriend(player.getName())) continue;

            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack == ItemStack.EMPTY) continue;

                int percent = EntityUtil.getDamagePercent(stack);

                if (percent <= armorThreshold.getValue() && !entityArmorArraylist.containsKey(player)) {

                    if (player == mc.player && notifySelf.getValue()) {
                        Command.sendMessage(ChatFormatting.RED + "Your " + getArmorPieceName(stack) + " low dura!");
                    }

                    if (Managers.FRIENDS.isFriend(player.getName()) && notification.getValue() && player != mc.player) {
                        mc.player.sendChatMessage("/msg " + player.getName() + " Yo, " + player.getName() + ", ur " + getArmorPieceName(stack) + " low dura!");
                    }

                    entityArmorArraylist.put(player, player.inventory.armorInventory.indexOf(stack));
                }
                if (!entityArmorArraylist.containsKey(player) || entityArmorArraylist.get(player) != player.inventory.armorInventory.indexOf(stack) || percent <= armorThreshold.getValue()) continue;

                entityArmorArraylist.remove(player);
            }
            if (!entityArmorArraylist.containsKey(player) || player.inventory.armorInventory.get(entityArmorArraylist.get(player)) != ItemStack.EMPTY) continue;

            entityArmorArraylist.remove(player);
        }
    }

    private String getArmorPieceName(ItemStack stack) {
        if (stack.getItem() == Items.DIAMOND_HELMET
                || stack.getItem() == Items.GOLDEN_HELMET
                || stack.getItem() == Items.IRON_HELMET
                || stack.getItem() == Items.CHAINMAIL_HELMET
                || stack.getItem() == Items.LEATHER_HELMET) {

            return "helmet is";
        }

        if (stack.getItem() == Items.DIAMOND_CHESTPLATE
                || stack.getItem() == Items.GOLDEN_CHESTPLATE
                || stack.getItem() == Items.IRON_CHESTPLATE
                || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.LEATHER_CHESTPLATE) {

            return "chest is";
        }

        if (stack.getItem() == Items.DIAMOND_LEGGINGS
                || stack.getItem() == Items.GOLDEN_LEGGINGS
                || stack.getItem() == Items.IRON_LEGGINGS
                || stack.getItem() == Items.CHAINMAIL_LEGGINGS
                || stack.getItem() == Items.LEATHER_LEGGINGS) {

            return "leggings are";
        }

        return "boots are";
    }
}
