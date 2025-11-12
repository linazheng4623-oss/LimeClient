package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    private final Setting<Mode> mode = this.register(new Setting<>("Mode", Mode.VANILLA));
    private final Setting<Float> speed = this.register(new Setting<>("Speed", 1.0f, 0.1f, 5.0f));
    private final Setting<Boolean> antiKick = this.register(new Setting<>("AntiKick", true));
    private final Setting<Boolean> noClip = this.register(new Setting<>("NoClip", false));

    // Vulcan specific settings
    private final Setting<Boolean> vulcanTimer = this.register(new Setting<>("VulcanTimer", false));
    private final Setting<Float> vulcanTimerSpeed = this.register(new Setting<>("VulcanTimerSpeed", 1.05f, 0.1f, 2.0f));

    // AAC specific settings
    private final Setting<Boolean> aacGlide = this.register(new Setting<>("AACGlide", true));
    private final Setting<Float> aacGlideSpeed = this.register(new Setting<>("AACGlideSpeed", 0.5f, 0.1f, 2.0f));

    // NCP specific settings
    private final Setting<Boolean> ncpStrict = this.register(new Setting<>("NCPStrict", false));
    private final Setting<Boolean> ncpDamage = this.register(new Setting<>("NCPDamage", true));

    private int tickCounter = 0;
    private double startY = 0;
    private boolean wasFlying = false;

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        wasFlying = mc.player.getAbilities().flying;
        startY = mc.player.getY();

        if (ncpDamage.getValue() && mode.getValue() == Mode.NCP) {
            // Damage boost for NCP - using PositionAndOnGround like in Criticals
            boolean bl = mc.player.horizontalCollision;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY() + 0.1, mc.player.getZ(), false, bl
            ));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, bl
            ));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = wasFlying;
        mc.player.getAbilities().setFlySpeed(0.05f);

        if (noClip.getValue()) {
            mc.player.noClip = false;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        tickCounter++;

        switch (mode.getValue()) {
            case VANILLA:
                handleVanilla();
                break;
            case VULCAN:
                handleVulcan();
                break;
            case AAC:
                handleAAC();
                break;
            case NCP:
                handleNCP();
                break;
        }

        handleAntiKick();
    }

    private void handleVanilla() {
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 10f);

        if (noClip.getValue()) {
            mc.player.noClip = true;
        }
    }

    private void handleVulcan() {
        // Vulcan bypass - uses creative flight with small adjustments
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 12f);

        // Small vertical movement to bypass
        if (mc.player.age % 40 < 20) {
            mc.player.addVelocity(0, 0.02, 0);
        } else {
            mc.player.addVelocity(0, -0.02, 0);
        }

        mc.player.velocityDirty = true;
    }

    private void handleAAC() {
        // AAC bypass - mimics gliding behavior
        if (aacGlide.getValue()) {
            // Simulate elytra flight
            Vec3d motion = mc.player.getVelocity();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;

            if (mc.options.jumpKey.isPressed()) {
                motionY += aacGlideSpeed.getValue() * 0.1;
            } else if (mc.options.sneakKey.isPressed()) {
                motionY -= aacGlideSpeed.getValue() * 0.1;
            } else {
                // Gentle descent
                motionY = -0.01;
            }

            // Horizontal movement
            float yaw = mc.player.getYaw();
            float forward = mc.player.forwardSpeed;
            float strafe = mc.player.sidewaysSpeed;

            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (forward > 0 ? 45 : -45);
                }

                motionX = Math.cos(Math.toRadians(yaw + 90)) * speed.getValue() * 0.1;
                motionZ = Math.sin(Math.toRadians(yaw + 90)) * speed.getValue() * 0.1;
            }

            mc.player.setVelocity(motionX, motionY, motionZ);
        } else {
            // Fallback to vanilla with adjustments
            handleVanilla();
        }
    }

    private void handleNCP() {
        // NCP bypass - uses packet-based flight
        if (ncpStrict.getValue()) {
            // Strict mode - more conservative movement
            double motionX = 0;
            double motionY = 0;
            double motionZ = 0;

            if (mc.options.jumpKey.isPressed()) {
                motionY = speed.getValue() * 0.5;
            } else if (mc.options.sneakKey.isPressed()) {
                motionY = -speed.getValue() * 0.5;
            }

            // Horizontal movement with reduced speed for strict mode
            float yaw = mc.player.getYaw();
            double moveSpeed = speed.getValue() * 0.8;

            if (mc.player.forwardSpeed != 0) {
                motionX = Math.cos(Math.toRadians(yaw + 90)) * moveSpeed;
                motionZ = Math.sin(Math.toRadians(yaw + 90)) * moveSpeed;
            }

            mc.player.setVelocity(motionX, motionY, motionZ);

            // Send position packets for strict - using PositionAndOnGround like in Criticals
            if (tickCounter % 2 == 0) {
                boolean bl = mc.player.horizontalCollision;
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround(), bl
                ));
            }
        } else {
            // Normal NCP mode
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed(speed.getValue() / 15f);
        }
    }

    private void handleAntiKick() {
        if (antiKick.getValue() && tickCounter % 80 == 0) {
            // Small downward movement to prevent flight kick
            mc.player.setVelocity(mc.player.getVelocity().x, -0.04, mc.player.getVelocity().z);
        }
    }

    private enum Mode {
        VANILLA("Vanilla"),
        VULCAN("Vulcan"),
        AAC("AAC"),
        NCP("NCP");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}