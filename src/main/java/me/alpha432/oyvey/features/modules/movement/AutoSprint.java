package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;

public class AutoSprint extends Module {
    // Settings
    public Setting<Boolean> multiDirection = register(new Setting<>("MultiDirection", false));
    public Setting<Boolean> requireFood = register(new Setting<>("RequireFood", false));
    public Setting<Integer> foodLevel = register(new Setting<>("FoodLevel", 6, 0, 20));

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints for you", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        // Check if player can sprint
        if (!canSprint()) return;

        // Set sprinting
        if (multiDirection.getValue()) {
            // Multi-directional sprint (any movement direction)
            if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
                mc.player.setSprinting(true);
            }
        } else {
            // Only sprint when moving forward
            if (mc.player.forwardSpeed > 0) {
                mc.player.setSprinting(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    private boolean canSprint() {
        // Check if player has enough food if required
        if (requireFood.getValue() && mc.player.getHungerManager().getFoodLevel() <= foodLevel.getValue()) {
            return false;
        }

        // Check if player is not using items (eating, blocking, etc.)
        if (mc.player.isUsingItem()) {
            return false;
        }

        // Check if player is not flying, elytra flying, or in liquid
        if (mc.player.isGliding() || mc.player.isTouchingWater() || mc.player.isInLava()) {
            return false;
        }

        // Check if player is not climbing
        if (mc.player.isClimbing()) {
            return false;
        }

        return true;
    }
}