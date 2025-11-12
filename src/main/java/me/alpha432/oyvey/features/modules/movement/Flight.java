package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    private final Setting<Float> hSpeed = this.register(new Setting<>("H-Speed", 1.0f, 0.05f, 10.0f));
    private final Setting<Float> vSpeed = this.register(new Setting<>("V-Speed", 1.0f, 0.05f, 5.0f));
    private final Setting<Boolean> antiKick = this.register(new Setting<>("AntiKick", true));
    private final Setting<Integer> antiKickInterval = this.register(new Setting<>("AntiKick Interval", 30, 5, 80));
    private final Setting<Float> antiKickDistance = this.register(new Setting<>("AntiKick Distance", 0.07f, 0.01f, 0.2f));

    private int tickCounter = 0;

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        // Get current velocity
        Vec3d velocity = mc.player.getVelocity();
        double motionX = 0;
        double motionY = 0;
        double motionZ = 0;

        // Vertical movement using V-Speed
        if (mc.options.jumpKey.isPressed()) {
            motionY = vSpeed.getValue() * 0.5;
        } else if (mc.options.sneakKey.isPressed()) {
            motionY = -vSpeed.getValue() * 0.5;
        }

        // Horizontal movement using H-Speed - increased multiplier for better speed
        float yaw = mc.player.getYaw();
        float moveSpeed = hSpeed.getValue() * 0.5f; // Changed from 0.1f to 0.5f to match vertical speed

        if (mc.options.forwardKey.isPressed()) {
            motionX -= Math.sin(Math.toRadians(yaw)) * moveSpeed;
            motionZ += Math.cos(Math.toRadians(yaw)) * moveSpeed;
        }
        if (mc.options.backKey.isPressed()) {
            motionX += Math.sin(Math.toRadians(yaw)) * moveSpeed;
            motionZ -= Math.cos(Math.toRadians(yaw)) * moveSpeed;
        }
        if (mc.options.leftKey.isPressed()) {
            motionX += Math.cos(Math.toRadians(yaw)) * moveSpeed;
            motionZ += Math.sin(Math.toRadians(yaw)) * moveSpeed;
        }
        if (mc.options.rightKey.isPressed()) {
            motionX -= Math.cos(Math.toRadians(yaw)) * moveSpeed;
            motionZ -= Math.sin(Math.toRadians(yaw)) * moveSpeed;
        }

        // Apply velocity
        mc.player.setVelocity(motionX, motionY, motionZ);

        // Handle anti-kick
        if (antiKick.getValue()) {
            handleAntiKick();
        }

        tickCounter++;
    }

    private void handleAntiKick() {
        if (tickCounter > antiKickInterval.getValue() + 2) {
            tickCounter = 0;
        }

        switch (tickCounter) {
            case 0:
                // Small downward movement
                if (!mc.options.sneakKey.isPressed()) {
                    Vec3d velocity = mc.player.getVelocity();
                    mc.player.setVelocity(velocity.x, -antiKickDistance.getValue(), velocity.z);
                }
                break;
            case 1:
                // Return to original position
                Vec3d velocity = mc.player.getVelocity();
                mc.player.setVelocity(velocity.x, antiKickDistance.getValue(), velocity.z);
                break;
        }
    }
}
