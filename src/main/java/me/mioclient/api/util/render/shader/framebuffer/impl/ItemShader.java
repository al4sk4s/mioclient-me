package me.mioclient.api.util.render.shader.framebuffer.impl;

import me.mioclient.api.util.render.shader.framebuffer.FramebufferShader;
import org.lwjgl.opengl.GL20;

import static me.mioclient.api.util.Wrapper.mc;

public class ItemShader extends FramebufferShader {

    public static ItemShader INSTANCE = new ItemShader();

    public float mix;
    public float alpha = 1.0f;
    public boolean model;

    public ItemShader() {
        super("glow.frag");
    }

    @Override
    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("color");
        setupUniform("divider");
        setupUniform("radius");
        setupUniform("maxSample");
        setupUniform("dimensions");
        setupUniform("mixFactor");
        setupUniform("minAlpha");
        setupUniform("inside");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0);
        GL20.glUniform1i(getUniform("inside"), model ? 1 : 0);
        GL20.glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth * (radius * quality), 1F / mc.displayHeight * (radius * quality));
        GL20.glUniform3f(getUniform("color"), red, green, blue);
        GL20.glUniform1f(getUniform("divider"), 140F);
        GL20.glUniform1f(getUniform("radius"), radius);
        GL20.glUniform1f(getUniform("maxSample"), 10F);
        GL20.glUniform2f(getUniform("dimensions"), mc.displayWidth, mc.displayHeight);
        GL20.glUniform1f(getUniform("mixFactor"), mix);
        GL20.glUniform1f(getUniform("minAlpha"), alpha);
    }
}