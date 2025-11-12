package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "Makes you place/use all items faster", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        mc.itemUseCooldown = 0;
    }
}