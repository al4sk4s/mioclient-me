package me.mioclient.api.managers.impl;

import java.util.LinkedList;

public final class FpsManager {

    private int fps;
    private final LinkedList<Long> frames = new LinkedList<>();

    public void update() {
        long time = System.nanoTime();

        frames.add(time);

        while (true) {
            long f = frames.getFirst();
            final long ONE_SECOND = 1000000L * 1000L;
            if (time - f > ONE_SECOND) frames.remove();
            else break;
        }

        fps = frames.size();
    }

    public int getFPS() {
        return fps;
    }

    public float getFrametime() {
        return 1.0f / 240;
    }
}