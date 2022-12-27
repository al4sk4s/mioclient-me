package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class DamageBlockEvent extends Event {

    BlockPos pos;
    int progress;
    int breakerId;

    public DamageBlockEvent(BlockPos pos, int progress, int breakerId) {
        super();

        this.pos = pos;
        this.progress = progress;
        this.breakerId = breakerId;
    }

    public BlockPos getPosition() {
        return pos;
    }

    public int getProgress() {
        return progress;
    }

    public int getBreakerId() {
        return breakerId;
    }
}
