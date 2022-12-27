package me.mioclient.asm.mixins;

import me.mioclient.Mio;
import me.mioclient.api.events.impl.RenderToolTipEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.git.GitUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.click.items.other.Particle;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.impl.misc.ToolTips;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.mioclient.api.util.Wrapper.mc;

@Mixin(value = {GuiScreen.class})
public abstract class MixinGuiScreen extends Gui {

    private final Particle.Util particles = new Particle.Util(300);
    private boolean hoveringShulker;
    private ItemStack shulkerStack;
    private String shulkerName;

    @Inject(method = {"renderToolTip"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void renderToolTipHook(ItemStack stack, int x, int y, CallbackInfo info) {

        RenderToolTipEvent event = new RenderToolTipEvent(stack, x, y);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }

        if (stack.getItem() instanceof ItemShulkerBox) {
            hoveringShulker = true;
            shulkerStack = stack;
            shulkerName = stack.getDisplayName();
        } else {
            hoveringShulker = false;
        }
    }

    @Inject(method = {"mouseClicked"}, at = {@At(value = "HEAD")})
    public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {

        if (mouseButton == 2 && hoveringShulker && ToolTips.INSTANCE.wheelPeek.getValue() && ToolTips.INSTANCE.isOn()) {
            ToolTips.drawShulkerGui(shulkerStack, shulkerName);
        }
    }

    @Inject(method = {"drawScreen"}, at = {@At(value = "HEAD")})
    public void drawScreenHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

        if (mc.currentScreen != null) {
            if (ClickGui.INSTANCE.particles.getValue()) {
                particles.drawParticles();
            }

            if (ClickGui.INSTANCE.background.getValue() && mc.world != null) {
                RenderUtil.drawVGradientRect(0, 0, (float) Managers.TEXT.scaledWidth, (float) Managers.TEXT.scaledHeight, new Color(0, 0, 0, 0).getRGB(), Managers.COLORS.getCurrentWithAlpha(60));
            }

            if (mc.world == null) {
                Managers.TEXT.drawStringWithShadow("mioclient.me " + Mio.MODVER + " " + GitUtil.GIT_SHA + " " + GitUtil.BUILD_DATE, 2.0f, 2.0f, Managers.COLORS.getCurrent().getRGB());
            }
        }
    }

    @Inject(method = "drawWorldBackground(I)V", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackgroundHook(int tint, CallbackInfo info) {

        if (mc.world != null && ClickGui.INSTANCE.cleanGui.getValue() && !(mc.currentScreen instanceof MioClickGui)) {
            info.cancel();
        }
    }
}

