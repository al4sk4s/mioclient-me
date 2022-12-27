package me.mioclient.api.util.render.shader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public class GLSLShader {

    private final int programId;
    private final int timeUniform;
    private final int mouseUniform;
    private final int resolutionUniform;

    public GLSLShader(String fragmentShaderLocation) throws IOException {
        int program = glCreateProgram();

        glAttachShader(program, createShader("/assets/minecraft/textures/mio/shader/vertex/vsh/passthrough.vsh", GLSLShader.class.getResourceAsStream("/assets/minecraft/textures/mio/shader/vertex/vsh/passthrough.vsh"), GL_VERTEX_SHADER));
        glAttachShader(program, createShader(fragmentShaderLocation, GLSLShader.class.getResourceAsStream(fragmentShaderLocation), GL_FRAGMENT_SHADER));

        glLinkProgram(program);

        int linked = glGetProgrami(program, GL_LINK_STATUS);

        if (linked == 0) {
            throw new IllegalStateException("Shader failed to link");
        }

        programId = program;

        glUseProgram(program);

        timeUniform = glGetUniformLocation(program, "time");
        mouseUniform = glGetUniformLocation(program, "mouse");
        resolutionUniform = glGetUniformLocation(program, "resolution");

        glUseProgram(0);
    }

    public void useShader(int width, int height, float mouseX, float mouseY, float time) {
        glUseProgram(programId);

        glUniform2f(resolutionUniform, width, height);
        glUniform2f(mouseUniform, mouseX / width, 1.0f - mouseY / height);
        glUniform1f(timeUniform, time);
    }

    private int createShader(String check, InputStream inputStream, int shaderType) throws IOException {
        int shader = glCreateShader(shaderType);

        glShaderSource(shader, readStreamToString(inputStream));

        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);

        if (compiled == 0) {
            System.err.println(glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
            System.err.println("Caused by " + check);
            throw new IllegalStateException("Failed to compile shader: " + check);
        }

        return shader;
    }

    private String readStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[512];

        int read;

        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}