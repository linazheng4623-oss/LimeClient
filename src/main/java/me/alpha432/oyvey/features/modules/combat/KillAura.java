package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    // Settings
    public Setting<Float> range = register(new Setting<>("Range", 5.0f, 1.0f, 10.0f));
    public Setting<Boolean> players = register(new Setting<>("Players", true));
    public Setting<Boolean> monsters = register(new Setting<>("Monsters", true));
    public Setting<Boolean> animals = register(new Setting<>("Animals", false));
    public Setting<Boolean> rotate = register(new Setting<>("Rotate", false));
    public Setting<Integer> delay = register(new Setting<>("Delay", 10, 0, 20));

    private int tickCounter = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        tickCounter++;
        if (tickCounter < delay.getValue()) return;
        tickCounter = 0;

        // Get all valid targets using traditional loop instead of streams
        List<Entity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity) && getDistanceToEntity(entity) <= range.getValue()) {
                targets.add(entity);
            }
        }

        // Sort by distance
        targets.sort(Comparator.comparingDouble(this::getDistanceToEntity));

        if (!targets.isEmpty()) {
            Entity target = targets.get(0); // Closest target

            // Rotate to target if enabled
            if (rotate.getValue()) {
                rotateToTarget(target);
            }

            // Attack the target
            if (mc.player.getAttackCooldownProgress(0.0f) >= 1.0f) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (entity == mc.player) return false;
        if (entity == null) return false;
        if (!entity.isAlive()) return false;

        if (entity instanceof PlayerEntity) {
            return players.getValue();
        } else if (entity instanceof Monster) {
            return monsters.getValue();
        } else if (entity instanceof AnimalEntity) {
            return animals.getValue();
        }

        return false;
    }

    private double getDistanceToEntity(Entity entity) {
        return mc.player.getPos().distanceTo(entity.getPos());
    }

    private void rotateToTarget(Entity target) {
        if (target == null) return;

        double diffX = target.getX() - mc.player.getX();
        double diffY = (target.getY() + target.getEyeHeight(target.getPose())) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = target.getZ() - mc.player.getZ();

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / Math.PI));

        mc.player.setYaw(mc.player.getYaw() + wrapDegrees(yaw - mc.player.getYaw()));
        mc.player.setPitch(mc.player.getPitch() + wrapDegrees(pitch - mc.player.getPitch()));
    }

    private float wrapDegrees(float degrees) {
        float f = degrees % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }
        if (f < -180.0F) {
            f += 360.0F;
        }
        return f;
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }
}