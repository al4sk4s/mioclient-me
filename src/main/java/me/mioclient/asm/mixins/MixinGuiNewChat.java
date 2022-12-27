package me.mioclient.asm.mixins;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.impl.misc.BetterChat;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static me.mioclient.api.util.Wrapper.mc;

@Mixin(value={GuiNewChat.class})
public abstract class MixinGuiNewChat extends Gui {

    @Redirect(method={"drawChat"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectHook(int left, int top, int right, int bottom, int color) {
        BetterChat mod = BetterChat.INSTANCE;
        ClickGui gui = ClickGui.INSTANCE;
        
        int rectColor = mod.colorRect.getValue() ? gui.rainbow.getValue() ? ColorUtil.toARGB(ColorUtil.rainbow(gui.rainbowDelay.getValue()).getRed(), ColorUtil.rainbow(gui.rainbowDelay.getValue()).getGreen(), ColorUtil.rainbow(gui.rainbowDelay.getValue()).getBlue(), 45) : ColorUtil.toARGB(gui.color.getValue().getRed(), gui.color.getValue().getGreen(), gui.color.getValue().getBlue(), 45) : color;

        if (mod.isOn()) {
            if (mod.rect.getValue()) {
                Gui.drawRect(left, top, right, bottom, rectColor);

            } else {
                Gui.drawRect(left, top, right, bottom, 0);
            }

        } else {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

    @Redirect(method={"drawChat"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
        
        if (text.contains(Managers.TEXT.syncCode)) {
            mc.fontRenderer.drawStringWithShadow(text, x, y, Managers.COLORS.getCurrent().getRGB());
            
        } else {
            mc.fontRenderer.drawStringWithShadow(text, x, y, color);
        }

        return 0;
    }

    @Redirect(method={"setChatLine"}, at=@At(value="INVOKE", target="Ljava/util/List;size()I", ordinal=0, remap=false))
    public int drawnChatLinesSize(List<ChatLine> list) {
        return BetterChat.INSTANCE.isOn() && BetterChat.INSTANCE.infinite.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method={"setChatLine"}, at=@At(value="INVOKE", target="Ljava/util/List;size()I", ordinal=2, remap=false))
    public int chatLinesSize(List<ChatLine> list) {
        return BetterChat.INSTANCE.isOn() && BetterChat.INSTANCE.infinite.getValue() ? -2147483647 : list.size();
    }

    @Shadow
    public boolean isScrolled;
    private float percentComplete;
    private long prevMillis = System.currentTimeMillis();
    private boolean configuring;
    private float animationPercent;
    private int lineBeingDrawn;

    @Shadow
    public float getChatScale() {
        return mc.gameSettings.chatScale;
    }

    private void updatePercentage(long diff) {
        if (percentComplete < 1.0f) {
            percentComplete += 0.004f * (float) diff;
        }
        percentComplete = MathUtil.clamp(percentComplete, 0.0f, 1.0f);
    }

    @Inject(method = { "drawChat" }, at = { @At(value = "HEAD") }, cancellable = true)
    private void modifyChatRendering(CallbackInfo ci) {
        if (configuring) {
            ci.cancel();
            return;
        }

        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        prevMillis = current;
        updatePercentage(diff);
        float t = percentComplete;
        animationPercent = MathUtil.clamp(1.0f - (t -= 1.0f) * t * t * t, 0.0f, 1.0f);
    }

    @Inject(method = { "drawChat" }, at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", ordinal = 0, shift = At.Shift.AFTER) })
    private void translate(CallbackInfo ci) {
        float y = 1.0f;

        if (!isScrolled) {
            y += (9.0f - 9.0f * animationPercent) * getChatScale();
        }

        GlStateManager.translate(0.0f, y, 0.0f);
    }

    @ModifyArg(method = { "drawChat" }, at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0, remap = false), index = 0)
    private int getLineBeingDrawn(int line) {
        lineBeingDrawn = line;
        return line;
    }

    @Inject(method = { "printChatMessageWithOptionalDeletion" }, at = { @At(value = "HEAD") })
    private void resetPercentage(CallbackInfo ci) {
        percentComplete = 0.0f;
    }

    @ModifyVariable(method = { "setChatLine" }, at = @At(value = "STORE"), ordinal = 0)
    private List<ITextComponent> setNewLines(List<ITextComponent> original) {
        return original;
    }
}