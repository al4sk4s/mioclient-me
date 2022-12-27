package me.mioclient.api.managers.impl;

import me.mioclient.api.util.Wrapper;
import me.mioclient.api.util.math.Timer;
import me.mioclient.mod.Mod;
import me.mioclient.mod.modules.impl.client.HUD;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class ServerManager extends Mod {

    private final float[] tpsCounts = new float[10];
    private final DecimalFormat format = new DecimalFormat("##.00#");
    private final Timer timer = new Timer();
    private float TPS = 20.0f;
    private long lastUpdate = -1L;
    private String serverBrand = "";

    public void onPacketReceived() {
        timer.reset();
    }

    public boolean isServerNotResponding() {
        return timer.passedMs(HUD.getInstance().lagTime.getValue().intValue());
    }

    public long serverRespondingTime() {
        return timer.getPassedTimeMs();
    }

    public void update() {
        float tps;
        long currentTime = System.currentTimeMillis();
        if (lastUpdate == -1L) {
            lastUpdate = currentTime;
            return;
        }
        long timeDiff = currentTime - lastUpdate;
        float tickTime = (float) timeDiff / 20.0f;
        if (tickTime == 0.0f) {
            tickTime = 50.0f;
        }
        if ((tps = 1000.0f / tickTime) > 20.0f) {
            tps = 20.0f;
        }
        System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.length - 1);
        tpsCounts[0] = tps;
        double total = 0.0;
        for (float f : tpsCounts) {
            total += f;
        }
        if ((total /= tpsCounts.length) > 20.0) {
            total = 20.0;
        }
        TPS = Float.parseFloat(format.format(total));
        lastUpdate = currentTime;
    }

    public void reset() {
        Arrays.fill(tpsCounts, 20.0f);
        TPS = 20.0f;
    }

    public float getTpsFactor() {
        return 20.0f / TPS;
    }

    public float getTPS() {
        return TPS;
    }

    public String getServerBrand() {
        return serverBrand;
    }

    public void setServerBrand(String brand) {
        serverBrand = brand;
    }

    public int getPing() {
        if (fullNullCheck()) {
            return 0;
        }
        try {
            return Objects.requireNonNull(Wrapper.mc.getConnection()).getPlayerInfo(Wrapper.mc.getConnection().getGameProfile().getId()).getResponseTime();
        } catch (Exception e) {
            return 0;
        }
    }
}

