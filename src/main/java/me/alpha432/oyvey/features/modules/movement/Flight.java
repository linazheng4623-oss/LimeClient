package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;

public class Flight extends Module {
    private final Setting<Float> speed = this.register(new Setting<>("Speed", 1.0f, 0.1f, 5.0f));
    private final Setting<Boolean> antiKick = this.register(new Setting<>("AntiKick", true));

    private int tickCounter = 0;
    private boolean wasFlying = false;

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        
        wasFlying = mc.player.getAbilities().flying;
        mc.player.getAbilities().flying = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = wasFlying;
        mc.player.getAbilities().setFlySpeed(0.05f);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        tickCounter++;
        
        // Set flight speed
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 10f);
        
        // Handle anti-kick
        handleAntiKick();
    }

    private void handleAntiKick() {
        if (antiKick.getValue() && tickCounter % 80 == 0 && !mc.player.isOnGround()) {
            // Small downward movement to prevent flight kick, but only when not on ground
            mc.player.setVelocity(mc.player.getVelocity().x, -0.04, mc.player.getVelocity().z);
        }
    }
}
