package me.mioclient.mod.modules.impl.misc;

import me.mioclient.api.events.impl.RenderToolTipEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.DecimalFormat;

/**
 * @author t.me/asphyxia1337
 */

public class ToolTips extends Module {

    public static ToolTips INSTANCE;

    public final Setting<Boolean> shulkerPreview =
            add(new Setting<>("ShulkerPreview", true));
    public final Setting<Boolean> wheelPeek =
            add(new Setting<>("WheelPeek", true));

    private final DecimalFormat format = new DecimalFormat("#");
    private float width;
    private float height;

    public ToolTips() {
        super("ToolTips", "Advanced tool tips.", Category.MISC, true);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderToolTip(RenderToolTipEvent event) {

        if (event.isCanceled() || nullCheck() || fullNullCheck()) return;

        if (!event.getItemStack().isEmpty()) {

            event.cancel();

            int x = event.getX();
            int y = event.getY();

            if (event.getItemStack().getItem() instanceof  ItemShulkerBox && shulkerPreview.getValue()) {
                drawShulkerPreview(event.getItemStack(), x + 3, y - 10);
            }

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            GlStateManager.translate(x + 10, y - 5, 0);

            String title = event.getItemStack().getDisplayName();

            RenderUtil.drawRect(0, -2, width, height, 0xE1080E20);

            float prevWidth = width;
            width = 0;

            int newY = drawString(title, 3, 1, getItemColor(event.getItemStack()));

            String itemNameDesc = getEnchants(event.getItemStack());

            if (itemNameDesc != null) {
                newY = drawString(itemNameDesc, 3, newY, 0xFF0000);
            }

            String typeString = null;
            String rightTypeString = null;

            if (event.getItemStack().getItem() instanceof ItemArmor) {

                ItemArmor armor = (ItemArmor) event.getItemStack().getItem();

                switch (armor.getEquipmentSlot()) {

                    case CHEST:
                        typeString = "Chest";
                        break;

                    case FEET:
                        typeString = "Feet";
                        break;

                    case HEAD:
                        typeString = "Head";
                        break;

                    case LEGS:
                        typeString = "Leggings";
                        break;

                    default:
                        break;
                }

                switch (armor.getArmorMaterial()) {

                    case CHAIN:
                        rightTypeString = "Chain";
                        break;

                    case DIAMOND:
                        rightTypeString = "Diamond";
                        break;

                    case GOLD:
                        rightTypeString = "Gold";
                        break;

                    case IRON:
                        rightTypeString = "Iron";
                        break;

                    case LEATHER:
                        rightTypeString = "Leather";
                        break;

                    default:
                        break;
                }
            }

            if (event.getItemStack().getItem() instanceof ItemElytra) {
                typeString = "Chest";
            }

            if (event.getItemStack().getItem() instanceof ItemSword) {
                typeString = "Mainhand";
                rightTypeString = "Sword";
            }

            if (typeString != null) {

                int prevY = newY;

                newY = drawString(typeString, 3, newY, -1);

                if (rightTypeString != null) {
                    drawString(rightTypeString, (int)(prevWidth - Managers.TEXT.getStringWidth(rightTypeString) - 3), prevY, -1);
                    width = Math.max(16 * 3, prevWidth);
                }
            }

            if (event.getItemStack().getItem() instanceof ItemSword) {

                ItemSword sword = (ItemSword)event.getItemStack().getItem();

                newY = drawString(sword.getAttackDamage() + " - " + sword.getAttackDamage() + " Damage", 3, newY, -1);
            }

            for (Enchantment enchant : EnchantmentHelper.getEnchantments(event.getItemStack()).keySet()) {

                String name = "+" + EnchantmentHelper.getEnchantmentLevel(enchant, event.getItemStack()) + " " + I18n.translateToLocal(enchant.getName());//enchant.getTranslatedName(EnchantmentHelper.getEnchantmentLevel(enchant, event.getItemStack()));

                if (name.contains("Vanish") || name.contains("Binding"))
                    continue;

                int color = -1;

                if (name.contains("Mending") || name.contains("Unbreaking"))
                    color = 0x00FF00;

                newY = drawString(name, 3, newY, color);
            }

            if (event.getItemStack().getMaxDamage() > 1) {

                float armorPct = ((float)(event.getItemStack().getMaxDamage()-event.getItemStack().getItemDamage()) /  (float)event.getItemStack().getMaxDamage())*100.0f;

                String durability = String.format("Durability %s %s / %s", format.format(armorPct) + "%", event.getItemStack().getMaxDamage()-event.getItemStack().getItemDamage(), event.getItemStack().getMaxDamage());

                newY = drawString(durability, 3, newY, -1);
            }

            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 150.0F;
            RenderHelper.enableGUIStandardItemLighting();

            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableLighting();

            GlStateManager.translate(-(x + 10), -(y - 5), 0);

            height = newY + 1;
        }
    }

    private void drawShulkerPreview(ItemStack stack, int x, int y) {
        NBTTagCompound blockEntityTag;
        NBTTagCompound tagCompound = stack.getTagCompound();

        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10) && (blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)) {
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            GlStateManager.disableDepth();

            RenderUtil.drawRect(x + 7, y + 17, x + 171, y + 57 + 16, 0xE1080E20);

            GlStateManager.enableDepth();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();

            GlStateManager.enableLighting();

            NonNullList nonnulllist = NonNullList.withSize(27, (Object) ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

            for (int i = 0; i < nonnulllist.size(); ++i) {
                int iX = x + i % 9 * 18 + 8;
                int iY = y + i / 9 * 18 + 18;
                ItemStack itemStack = (ItemStack) nonnulllist.get(i);
                Peek.mc.getItemRenderer().itemRenderer.zLevel = 501.0f;
                RenderUtil.itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
                RenderUtil.itemRender.renderItemOverlayIntoGUI(Peek.mc.fontRenderer, itemStack, iX, iY, null);
                Peek.mc.getItemRenderer().itemRenderer.zLevel = 0.0f;
            }

            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public static void drawShulkerGui(ItemStack stack, String name) {
        try {
            Item item = stack.getItem();
            TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            ItemShulkerBox shulker = (ItemShulkerBox) item;
            entityBox.blockType = shulker.getBlock();
            entityBox.setWorld(Peek.mc.world);
            ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("BlockEntityTag"), entityBox.items);
            entityBox.readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
            entityBox.setCustomName(name == null ? stack.getDisplayName() : name);
            new Thread(() -> {

                try {
                    Thread.sleep(200L);
                } catch (InterruptedException ignored) {

                }

                Peek.mc.player.displayGUIChest(entityBox);
            }).start();

        } catch (Exception ignored) {

        }
    }

    private int drawString(String string, int x, int y, int color) {
        Managers.TEXT.drawStringWithShadow(string, x, y, color);
        width = Math.max(width, Managers.TEXT.getStringWidth(string) + x + 3);
        return y + 9;
    }

    private int getItemColor(ItemStack stack) {

        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();

            switch (armor.getArmorMaterial()) {

                case CHAIN:
                    return 0x0070dd;

                case DIAMOND:
                    return EnchantmentHelper.getEnchantments(stack).keySet().isEmpty() ? 0x1eff00 : 0xa335ee;

                case GOLD:

                case IRON:
                    return 0x1eff00;

                case LEATHER:
                    return 0x9d9d9d;

                default:
                    break;
            }
        }

        else if (stack.getItem().equals(Items.GOLDEN_APPLE)) {

            if (stack.hasEffect()) return 0xa335ee;

            return 0x00CDFF;

        } else if (stack.getItem() instanceof ItemSword) {

            ItemSword sword = (ItemSword)stack.getItem();

            String material = sword.getToolMaterialName();

            if (material.equals("DIAMOND"))
                return 0xa335ee;
            if (material.equals("CHAIN"))
                return 0x0070dd;
            if (material.equals("GOLD"))
                return 0x1eff00;
            if (material.equals("IRON"))
                return 0x1eff00;
            if (material.equals("LEATHER"))
                return 0x9d9d9d;

            return -1;

        } else if (stack.getItem().equals(Items.TOTEM_OF_UNDYING))
            return 0xff8000;
        else if (stack.getItem().equals(Items.CHORUS_FRUIT))
            return 0x0070dd;
        else if (stack.getItem().equals(Items.ENDER_PEARL))
            return 0x0070dd;
        else if (stack.getItem().equals(Items.END_CRYSTAL))
            return 0xa335ee;
        else if (stack.getItem().equals(Items.EXPERIENCE_BOTTLE))
            return 0x1eff00;
        else if (stack.getItem().equals(Items.POTIONITEM))
            return 0x1eff00;
        else if (Item.getIdFromItem(stack.getItem()) == 130)
            return 0xa335ee;
        else if (stack.getItem() instanceof ItemShulkerBox)
            return 0xa335ee;

        return -1;
    }

    private String getEnchants(ItemStack stack) {
        String result = "";

        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (enchant == null) continue;

            String name = enchant.getTranslatedName(EnchantmentHelper.getEnchantmentLevel(enchant, stack));

            if (name.contains("Vanish")) {
                result += "Vanishing ";
            } else if (name.contains("Binding")) {
                result += "Binding ";
            }
        }

        if (stack.getItem().equals(Items.GOLDEN_APPLE) && stack.hasEffect())
            return "God";

        return result.isEmpty() ? null : result;
    }
}