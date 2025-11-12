package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public Setting<Integer> health = this.register(new Setting<>("Health", 15, 0, 36));
    public Setting<Boolean> force = this.register(new Setting<>("Force", false));
    public Setting<Boolean> inventoryOnly = this.register(new Setting<>("InventoryOnly", false));

    private boolean moving = false;
    private boolean returnToHotbar = false;

    public AutoTotem() {
        super("AutoTotem", "Automatically places totems in offhand", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        // Don't operate in GUI if inventoryOnly is enabled
        if (inventoryOnly.getValue() && !(mc.currentScreen instanceof InventoryScreen)) {
            return;
        }

        // Check if we need a totem
        if (shouldPlaceTotem()) {
            // Find totem in inventory
            int totemSlot = findTotemSlot();

            if (totemSlot != -1) {
                // Move totem to offhand
                moveTotemToOffhand(totemSlot);
            }
        }
    }

    private boolean shouldPlaceTotem() {
        // Always place if force is enabled
        if (force.getValue()) {
            return true;
        }

        // Check if offhand is empty
        if (mc.player.getOffHandStack().isEmpty()) {
            return true;
        }

        // Check if offhand has totem
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return false; // Already has totem
        }

        // Check health threshold
        return mc.player.getHealth() <= health.getValue();
    }

    private int findTotemSlot() {
        // Search hotbar first (slots 0-8)
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        // Search rest of inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1; // No totem found
    }

    private void moveTotemToOffhand(int totemSlot) {
        // Offhand slot is 45 in container indexing
        int offhandSlot = 45;

        // Click on the totem slot to pick it up
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                totemSlot < 9 ? totemSlot + 36 : totemSlot, // Convert to container slots
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        // Click on offhand slot to place it
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                offhandSlot,
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        // If we picked up something else from offhand, put it back
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            // Put the item back in the original totem slot
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    totemSlot < 9 ? totemSlot + 36 : totemSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );
        }
    }

    @Override
    public void onEnable() {
        moving = false;
        returnToHotbar = false;
    }

    @Override
    public void onDisable() {
        moving = false;
        returnToHotbar = false;
    }

    public boolean isMoving() {
        return moving;
    }
}