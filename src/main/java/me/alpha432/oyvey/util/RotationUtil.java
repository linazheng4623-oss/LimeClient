package me.alpha432.oyvey.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float[] getRotations(Vec3d target) {
        Vec3d eyesPos = mc.player.getEyePos();

        double diffX = target.x - eyesPos.x;
        double diffY = target.y - eyesPos.y;
        double diffZ = target.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{yaw, pitch};
    }
}