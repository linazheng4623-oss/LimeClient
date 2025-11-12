package me.alpha432.oyvey.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).isReplaceable();
    }

    public static boolean canClick(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.isSolid();
    }
}