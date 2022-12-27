package me.mioclient.asm.mixins;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.mioclient.api.util.Wrapper.mc;
import static org.lwjgl.opengl.GL11.*;

@Mixin(value={GuiChat.class})
public abstract class MixinGuiChat {

    private boolean shouldDrawOutline;

    @Shadow
    protected GuiTextField inputField;

    @Inject(method = "keyTyped(CI)V", at = @At("RETURN"))
    public void keyTypedHook(char typedChar, int keyCode, CallbackInfo info) {

        if ((mc.currentScreen instanceof GuiChat)) {
            shouldDrawOutline = inputField.getText().startsWith(Managers.COMMANDS.getCommandPrefix());

        } else {
            shouldDrawOutline = false;
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void drawScreenHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

        if (shouldDrawOutline) {

            boolean blend = glIsEnabled(GL_BLEND);
            boolean texture2D = glIsEnabled(GL_TEXTURE_2D);

            glDisable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);

            RenderUtil.glColor(Managers.COLORS.getCurrent());

            glLineWidth(1.5f);
            glBegin(GL_LINES);

            int x = inputField.x - 2;
            int y = inputField.y - 2;
            int width = inputField.width;
            int height = inputField.height;

            glVertex2d(x, y);
            glVertex2d(x + width, y);
            glVertex2d(x + width, y);
            glVertex2d(x + width, y + height);
            glVertex2d(x + width, y + height);
            glVertex2d(x, y + height);
            glVertex2d(x, y + height);
            glVertex2d(x, y);

            glEnd();

            if (blend) {
                glEnable(GL_BLEND);
            }

            if (texture2D) {
                glEnable(GL_TEXTURE_2D);
            }
        }
    }
}