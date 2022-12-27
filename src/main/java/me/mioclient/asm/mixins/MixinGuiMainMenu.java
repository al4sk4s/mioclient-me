package me.mioclient.asm.mixins;

import me.mioclient.api.util.render.shader.GLSLShader;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiScreen {

    public GLSLShader shader;
    public long initTime;

    private boolean isGuiOpen;

    //Main menu ClickGui

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    protected void keyTyped(char typedChar, int keyCode, CallbackInfo info) {

        if (keyCode == ClickGui.INSTANCE.bind.getValue().getKey()) {
            ClickGui.INSTANCE.enable();
            isGuiOpen = true;
        }
        if (keyCode == 1) {
            ClickGui.INSTANCE.disable();
            isGuiOpen = false;
        }

        if (isGuiOpen) {
            try {
                MioClickGui.INSTANCE.keyTyped(typedChar, keyCode);

            } catch (Exception ignored) {

            }

            info.cancel();
        }
    }

    @Inject(method = {"drawScreen(IIF)V"}, at = {@At(value = "TAIL")})
    public void drawScreenTailHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

        if (isGuiOpen) {
            MioClickGui.INSTANCE.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Inject(method = {"mouseClicked"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {

        if (isGuiOpen) {
            MioClickGui.INSTANCE.mouseClicked(mouseX, mouseY, mouseButton);
            info.cancel();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {

        if (isGuiOpen) {
            MioClickGui.INSTANCE.mouseReleased(mouseX, mouseY, state);
        }
    }

    //Main menu shader

    @Inject(method = "drawPanorama", at = {@At(value = "TAIL")})
    public void drawPanoramaTailHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

        try {
            shader = new GLSLShader("/assets/minecraft/textures/mio/shader/fragment/fsh/shader.fsh");
        } catch (IOException ignored) {

        }

        GlStateManager.disableCull();

        shader.useShader(width * 2, height * 2, mouseX * 2, mouseY * 2, (System.currentTimeMillis() - initTime) / 1000f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }

    @Inject(method = "initGui", at = @At("HEAD"))
    private void initHook(CallbackInfo info) {
        initTime = System.currentTimeMillis();
    }
}