package me.mioclient.api.managers.impl;

import me.mioclient.mod.Mod;

public class TimerManager extends Mod {

    public float timer = 1;

    public void set(float factor) {

        if (factor < 0.1f) factor = 0.1f;

        timer = factor;
    }

    public void reset() {
        timer = 1;
    }

    public float get() {
        return timer;
    }
}

