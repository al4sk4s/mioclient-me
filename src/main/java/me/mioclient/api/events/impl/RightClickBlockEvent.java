package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RightClickBlockEvent extends Event {

    public BlockPos pos;
    public EnumHand hand;
    public ItemStack stack;

    public RightClickBlockEvent(BlockPos pos, EnumHand hand, ItemStack stack) {
        this.pos = pos;
        this.hand = hand;
        this.stack = stack;
    }
}

