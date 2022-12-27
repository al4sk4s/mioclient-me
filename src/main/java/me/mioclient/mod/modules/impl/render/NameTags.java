package me.mioclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.InterpolationUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.impl.misc.PopNotify;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;


public class NameTags extends Module {

    public static NameTags INSTANCE;

    private final Setting<Page> page =
            add(new Setting<>("Settings", NameTags.Page.GLOBAL));
    private final Setting<Boolean> armor =
            add(new Setting<>("Armor", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> enchant =
            add(new Setting<>("Enchants", true, v -> armor.getValue() && page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> reversed =
            add(new Setting<>("Reversed", false, v -> armor.getValue() && page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> durability =
            add(new Setting<>("Durability", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> health =
            add(new Setting<>("Health", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> gameMode =
            add(new Setting<>("Gamemode", false, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> ping =
            add(new Setting<>("Ping", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> item =
            add(new Setting<>("ItemName", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> invisibles =
            add(new Setting<>("Invisibles", true, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> pops =
            add(new Setting<>("Pops", true, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Float> scaleFactor =
            add(new Setting<>("Scale", 1.0f, 1.0f, 3.0f, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> rect =
            add(new Setting<>("Rectangle", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> outline =
            add(new Setting<>("Outline", true, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Color> outlineColor =
            add(new Setting<>("Color", new Color(0x6C6CF3), v -> page.getValue() == Page.COLORS).hideAlpha());
    private final Setting<Color> secondColor =
            add(new Setting<>("FadeColor", new Color(-1), v -> page.getValue() == Page.COLORS).injectBoolean(false).hideAlpha());
    public Setting<Boolean> outlineRainbow =
            add(new Setting<>("Rainbow", false, v -> page.getValue() == Page.COLORS));

    private final Setting<Color> textColor =
            add(new Setting<>("TextColor", new Color(-1), v -> page.getValue() == Page.COLORS).hideAlpha());
    private final Setting<Boolean> textRainbow =
            add(new Setting<>("TextRainbow", false, v -> page.getValue() == Page.COLORS));

    private final ICamera camera = new Frustum();
    private final Map glCapMap = new HashMap<>();

    boolean shownItem;

    public static HashMap<String, Integer> totemPops = new HashMap<>();

    public NameTags() {
        super("NameTags", "Advanced name tags for players.", Category.RENDER);
        INSTANCE = this;
    }

    private enum Page {
        GLOBAL,
        COLORS
    }

    @Override
    public void onEnable() {
        totemPops.clear();
    }

    @Override
    public void onDeath(EntityPlayer player) {
        totemPops.remove(player.getName());
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        int popCount = 1;

        if (PopNotify.fullNullCheck() || mc.player.equals(player)) {
            return;
        }

        if (totemPops.containsKey(player.getName())) {
            popCount = totemPops.get(player.getName());
            totemPops.put(player.getName(), ++popCount);
        } else {
            totemPops.put(player.getName(), popCount);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player != null) {
            EntityPlayer renderPlayer = mc.getRenderViewEntity() == null ? mc.player : (EntityPlayer) mc.getRenderViewEntity();

            double posX = InterpolationUtil.getInterpolatedDouble(renderPlayer.lastTickPosX, renderPlayer.posX, event.getPartialTicks());
            double posY = InterpolationUtil.getInterpolatedDouble(renderPlayer.lastTickPosY, renderPlayer.posY, event.getPartialTicks());
            double posZ = InterpolationUtil.getInterpolatedDouble(renderPlayer.lastTickPosZ, renderPlayer.posZ, event.getPartialTicks());

            camera.setPosition(posX, posY, posZ);

            List players = new ArrayList(mc.world.playerEntities);
            players.sort(Comparator.comparing((entityPlayer) -> renderPlayer.getDistance((Entity)entityPlayer)).reversed());
            Iterator playerItr = players.iterator();

            while(true) {
                EntityPlayer player;
                NetworkPlayerInfo info;

                double playerX;
                double playerY;
                double playerZ;

                do {
                    do {
                        do {
                            do {
                                if (!playerItr.hasNext()) return;

                                player = (EntityPlayer)playerItr.next();
                                info = mc.player.connection.getPlayerInfo(player.getGameProfile().getId());
                            } while(!camera.isBoundingBoxInFrustum(player.getEntityBoundingBox()) && !camera.isBoundingBoxInFrustum(player.getEntityBoundingBox().offset(0.0D, 2.0D, 0.0D)));
                        } while(player == mc.getRenderViewEntity());
                    } while(!player.isEntityAlive());
                    playerX = InterpolationUtil.getInterpolatedDouble(player.lastTickPosX, player.posX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX;
                    playerY = InterpolationUtil.getInterpolatedDouble(player.lastTickPosY, player.posY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosY;
                    playerZ = InterpolationUtil.getInterpolatedDouble(player.lastTickPosZ, player.posZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosZ;
                } while(info != null && getGameModeShort(info.getGameType().getName()).equalsIgnoreCase("SP") && !(Boolean) invisibles.getValue());

                if (!player.getName().startsWith("Body #")) {
                    renderNameTag(player, playerX, playerY, playerZ);
                }
            }
        }
    }

    public void renderNameTag(EntityPlayer player, double x, double y, double z) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        shownItem = false;

        GlStateManager.pushMatrix();

        NetworkPlayerInfo info = mc.player.connection.getPlayerInfo(player.getGameProfile().getId());
        boolean isFriend = Managers.FRIENDS.isFriend(player.getName());
        boolean cFont = FontMod.INSTANCE.isOn();

        StringBuilder preNameTag = (new StringBuilder()).append(isFriend ? "§" + (isFriend ? "b" : "c") : (player.isSneaking() ? "§5" : "§r")).append(getName(player)).append(gameMode.getValue() && info != null ? " [" + getGameModeShort(info.getGameType().getName()) + "]" : "").append(ping.getValue() && info != null ? " " + ("") + info.getResponseTime() + "ms" : "").append(health.getValue() ? " §" + getHealthColor(player.getHealth() + player.getAbsorptionAmount()) + MathHelper.ceil(player.getHealth() + player.getAbsorptionAmount()) : "");

        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.sneak.getValue()) {
            preNameTag = (new StringBuilder()).append(isFriend ? "§" + (isFriend ? "b" : "c") : ("§r")).append(getName(player)).append(gameMode.getValue() && info != null ? " [" + getGameModeShort(info.getGameType().getName()) + "]" : "").append(ping.getValue() && info != null ? " " + ("") + info.getResponseTime() + "ms" : "").append(health.getValue() ? " §" + getHealthColor(player.getHealth() + player.getAbsorptionAmount()) + MathHelper.ceil(player.getHealth() + player.getAbsorptionAmount()) : "");
        }

        String totemPopKey;

        if (totemPops.get(player.getName()) != null && pops.getValue()) {
            StringBuilder totemPopKeyAppended = (new StringBuilder()).append(" ").append(ChatFormatting.DARK_RED).append("-");
            totemPopKey = totemPopKeyAppended.append(totemPops.get(player.getName())).toString();

        } else {
            totemPopKey = "";
        }

        String postNameTag = preNameTag.append(totemPopKey).toString();
        postNameTag = (Managers.FRIENDS.isCool(player.getName()) ? ChatFormatting.GOLD + "< > " + ChatFormatting.RESET : "") + postNameTag.replace(".0", "");

        EntityPlayer renderPlayer = mc.getRenderViewEntity() == null ? mc.player : (EntityPlayer) mc.getRenderViewEntity();
        float distance = renderPlayer.getDistance(player);

        float scale = (distance / 5.0F <= 2.0F ? 2.0F : distance / 5.0F * ((4.1F * scaleFactor.getValue()) / 100.0F * 10.0F + 1.0F)) * 2.5F * ((4.1F * scaleFactor.getValue()) / 100.0F / 10.0F);

        if ((double)distance <= 8.0D) {
            scale = 0.0245F;
        }

        GL11.glTranslated(((float)x), (double)((float)y + 2.45F) - (player.isSneaking() ? 0.4D : 0.0D) + (distance / 5.0F > 2.0F ? (double)(distance / 12.0F) - 0.7D : 0.0D), (float)z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        float var26 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;

        GL11.glRotatef(mc.getRenderManager().playerViewX, var26, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);

        disableGlCap(2896, 2929);
        enableGlCap(3042);

        GL11.glBlendFunc(770, 771);

        int width = Managers.TEXT.getStringWidth(postNameTag) / 2 + 1;

        int outlineColor = isFriend ? (new Color(0, 213, 255, 255)).getRGB() : (new Color(this.outlineColor.getValue().getRed(), this.outlineColor.getValue().getGreen(), this.outlineColor.getValue().getBlue(), 255)).getRGB();
        int fadeOutlineColor = (new Color(secondColor.getValue().getRed(), secondColor.getValue().getGreen(), secondColor.getValue().getBlue(), 255)).getRGB();

        GlStateManager.enableTexture2D();

        if (player.isSneaking()) {
            if (Chams.INSTANCE.isOn() && (Chams.INSTANCE.sneak.getValue().booleanValue())) {
                outlineColor = isFriend ? (new Color(0, 213, 255, 255)).getRGB() : (new Color(this.outlineColor.getValue().getRed(), this.outlineColor.getValue().getGreen(), this.outlineColor.getValue().getBlue(), 255)).getRGB();
            } else {
                outlineColor = isFriend ? (new Color(0, 213, 255, 255)).getRGB() : (new Color(170, 0, 170, 255)).getRGB();
            }
        }

        if (rect.getValue()) {
            Gui.drawRect(-width - 1, 8, width + 1, 19, ColorUtil.toRGBA(0, 0 ,0, 120));
        }

        if (outline.getValue()) {
            RenderUtil.drawNameTagOutline((float) (-width - 1), 8.0f, (float) (width + 1), 19.0f, 1.0f, outlineColor, secondColor.booleanValue ? fadeOutlineColor : outlineColor, (!isFriend && outlineRainbow.getValue()));
        }

        int textColor = textRainbow.getValue() ?
                Managers.COLORS.getRainbow().getRGB() :
                new Color(this.textColor.getValue().getRed(), this.textColor.getValue().getGreen(), this.textColor.getValue().getBlue()).getRGB();

        Managers.TEXT.drawStringWithShadow(postNameTag, (float) -width, cFont ? 8.65f : 9.2f, textColor);

        int xOffset;
        int index;
        ItemStack renderOffhand;

        if (armor.getValue()) {
            xOffset = -8;
            Item mainhand = player.getHeldItemMainhand().getItem();
            Item offhand = player.getHeldItemOffhand().getItem();

            if (mainhand != Items.AIR && offhand == Items.AIR) {
                xOffset = -16;
            } else if (mainhand == Items.AIR && offhand != Items.AIR) {
                xOffset = 0;
            }

            index = 0;
            Iterator var21 = player.inventory.armorInventory.iterator();

            while(var21.hasNext()) {
                renderOffhand = (ItemStack)var21.next();
                if (renderOffhand != null) {
                    xOffset -= 8;
                    if (renderOffhand.getItem() != Items.AIR) {
                        ++index;
                    }
                }
            }

            if (player.getHeldItemOffhand().getItem() != Items.AIR) {
                ++index;
            }

            int cacheX;
            label310: {
                label248: {
                    cacheX = xOffset - 8;
                    xOffset += 8 * (5 - index) - (index == 0 ? 4 : 0);
                    if (reversed.getValue()) {
                        if (player.getHeldItemOffhand().getItem() != Items.AIR) {
                            break label248;
                        }
                    } else if (player.getHeldItemMainhand().getItem() != Items.AIR) {
                        break label248;
                    }

                    if (!(Boolean) reversed.getValue()) {
                        shownItem = true;
                    }
                    break label310;
                }

                xOffset -= 10;
                if (reversed.getValue()) {
                    renderOffhand = player.getHeldItemOffhand().copy();
                    renderItem(player, renderOffhand, xOffset, 7, cacheX, false);
                } else {
                    renderOffhand = player.getHeldItemMainhand().copy();
                    renderItem(player, renderOffhand, xOffset, 7, cacheX, true);
                }

                xOffset += 18;
            }
            ItemStack armourStack2;
            ItemStack renderStack2;

            if (reversed.getValue()) {
                for(index = 0; index <= 3; ++index) {
                    armourStack2 = player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        renderStack2 = armourStack2.copy();
                        renderItem(player, renderStack2, xOffset, 7, cacheX, false);
                        xOffset += 16;
                    }
                }
            } else {
                for(index = 3; index >= 0; --index) {
                    armourStack2 = player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        renderStack2 = armourStack2.copy();
                        renderItem(player, renderStack2, xOffset, 7, cacheX, false);
                        xOffset += 16;
                    }
                }
            }
            label314: {
                if (reversed.getValue()) {
                    if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                        break label314;
                    }
                } else if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                    break label314;
                }

                xOffset += 0;
                if (reversed.getValue()) {
                    renderOffhand = player.getHeldItemMainhand().copy();
                    renderItem(player, renderOffhand, xOffset, 7, cacheX, true);
                } else {
                    renderOffhand = player.getHeldItemOffhand().copy();
                    renderItem(player, renderOffhand, xOffset, 7, cacheX, false);
                }

                xOffset += 8;
            }

            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
        } else if (durability.getValue()) {
            xOffset = 0;
            int count = 0;
            Iterator var28 = player.inventory.armorInventory.iterator();

            while(var28.hasNext()) {
                ItemStack armourStack = (ItemStack)var28.next();
                if (armourStack != null) {
                    xOffset -= 8;
                    if (armourStack.getItem() != Items.AIR) {
                        ++count;
                    }
                }
            }

            if (player.getHeldItemOffhand().getItem() != Items.AIR) {
                ++count;
            }

            int cacheX = xOffset - 8;
            xOffset += 8 * (5 - count) - (count == 0 ? 4 : 0);
            ItemStack armourStack2;
            if (reversed.getValue()) {
                for(index = 0; index <= 3; ++index) {
                    armourStack2 = player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        renderOffhand = armourStack2.copy();
                        renderDurability(player, renderOffhand, xOffset, 12);
                        xOffset += 16;
                    }
                }
            } else {
                for(index = 3; index >= 0; --index) {
                    armourStack2 = player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        renderOffhand = armourStack2.copy();
                        renderDurability(player, renderOffhand, xOffset, 12);
                        xOffset += 16;
                    }
                }
            }
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
        }
        GlStateManager.resetColor();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public void renderItem(EntityPlayer player, ItemStack stack, int x, int y, int nameX, boolean showHeldItemText) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -100.0F;
        GlStateManager.scale(1.0F, 1.0F, 0.01F);
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y / 2 - 12);

        if (durability.getValue()) {
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y / 2 - 12);
        }
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.disableDepth();

        renderEnchant(player, stack, x, y - 18);

        if (!shownItem && item.getValue() && showHeldItemText) {

            Managers.TEXT.drawStringWithShadow(stack.getDisplayName().equalsIgnoreCase("Air") ? "" : stack.getDisplayName(), (float) (nameX * 2 + 95 - Managers.TEXT.getStringWidth(stack.getDisplayName()) / 2), (float) (y - 37), Color.GRAY.getRGB());
            shownItem = true;
        }
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GL11.glPopMatrix();
    }

    private void renderDurability(EntityPlayer player, ItemStack stack, int x, int y) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.scale(1.0F, 1.0F, 0.01F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.disableDepth();

        if (stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
            float green = ((float)stack.getMaxDamage() - (float)stack.getItemDamage()) / (float)stack.getMaxDamage();
            float red = 1.0F - green;
            int damage = 100 - (int)(red * 100.0F);

            Managers.TEXT.drawStringWithShadow(damage + "%", (float)(x * 2 + 4), (float)(y - 10), ColorUtil.toHex((int)(red * 255.0F), (int)(green * 255.0F), 0));
        }
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GL11.glPopMatrix();
    }

    public void renderEnchant(EntityPlayer player, ItemStack stack, int x, int y) {
        int yCount = y;

        if ((stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) && durability.getValue()) {
            float green = ((float)stack.getMaxDamage() - (float)stack.getItemDamage()) / (float)stack.getMaxDamage();
            float red = 1.0F - green;
            int damage = 100 - (int)(red * 100.0F);

            Managers.TEXT.drawStringWithShadow(damage + "%", (float)(x * 2 + 4), (float)(y - 10), ColorUtil.toHex((int)(red * 255.0F), (int)(green * 255.0F), 0));
        }
        NBTTagList enchants;
        Enchantment ench;
        String enchName;
        short level;
        int index;
        short id;

        if (enchant.getValue()) {
            enchants = stack.getEnchantmentTagList();

            if (enchants != null) {
                for(index = 0; index < enchants.tagCount(); ++index) {
                    id = enchants.getCompoundTagAt(index).getShort("id");
                    level = enchants.getCompoundTagAt(index).getShort("lvl");
                    ench = Enchantment.getEnchantmentByID(id);

                    if (ench != null && !ench.isCurse()) {
                        enchName = level == 1 ? ench.getTranslatedName(level).substring(0, 3).toLowerCase() : ench.getTranslatedName(level).substring(0, 2).toLowerCase() + level;
                        enchName = enchName.substring(0, 1).toUpperCase() + enchName.substring(1);

                        if (enchName.contains("Pr") || enchName.contains("Bl")) {
                            GL11.glPushMatrix();
                            GL11.glScalef(1.0F, 1.0F, 0.0F);

                            Managers.TEXT.drawStringWithShadow(enchName, (float)(x * 2 + 3), (float)yCount, -1);

                            GL11.glScalef(1.0F, 1.0F, 1.0F);
                            GL11.glPopMatrix();
                            yCount += 8;
                        }
                    }
                }
            }
        }
    }

    public String getGameModeShort(String gameType) {
        if (gameType.equalsIgnoreCase("survival")) {
            return "S";
        } else if (gameType.equalsIgnoreCase("creative")) {
            return "C";
        } else if (gameType.equalsIgnoreCase("adventure")) {
            return "A";
        } else {
            return gameType.equalsIgnoreCase("spectator") ? "SP" : "NONE";
        }
    }

    public String getHealthColor(float health) {
        if (health > 18.0F) {
            return "a";
        } else if (health > 16.0F) {
            return "2";
        } else if (health > 12.0F) {
            return "e";
        } else if (health > 8.0F) {
            return "6";
        } else {
            return health > 5.0F ? "c" : "4";
        }
    }

    private String getName(EntityPlayer player) {
        return player.getName();
    }

    public void enableGlCap(int cap) {
        setGlCap(cap, true);
    }

    public void disableGlCap(int... caps) {
        int[] var2 = caps;
        int var3 = caps.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int cap = var2[var4];
            setGlCap(cap, false);
        }

    }

    public void setGlCap(int cap, boolean state) {
        glCapMap.put(cap, GL11.glGetBoolean(cap));
        setGlState(cap, state);
    }

    public void setGlState(int cap, boolean state) {
        if (state) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }

    }
}
