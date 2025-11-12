package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public class Scaffold extends Module {
    private final Setting<Boolean> tower = this.register(new Setting<>("Tower", true));
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", true));
    private final Setting<Boolean> swing = this.register(new Setting<>("Swing", true));

    private int oldSlot = -1;

    public Scaffold() {
        super("Scaffold", "Places blocks under you automatically", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        oldSlot = -1;
    }

    @Override
    public void onDisable() {
        if (oldSlot != -1) {
            // Use setSelectedSlot method instead of direct field access
            mc.player.getInventory().setSelectedSlot(oldSlot);
            oldSlot = -1;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        // Handle tower
        if (tower.getValue() && mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(0, 0.42, 0);
        }

        BlockPos belowPlayer = BlockPos.ofFloored(mc.player.getPos()).down();

        // Check if block is already placed
        if (!BlockUtil.isReplaceable(belowPlayer)) {
            return;
        }

        // Search blocks in hotbar
        int newSlot = -1;
        for (int i = 0; i < 9; i++) {
            // Filter out non-block items
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                continue;
            }

            // Filter out non-solid blocks
            Block block = Block.getBlockFromItem(stack.getItem());
            BlockState state = block.getDefaultState();
            if (!state.isSolid()) {
                continue;
            }

            // Filter out blocks that would fall
            if (block instanceof FallingBlock && FallingBlock.canFallThrough(mc.world.getBlockState(belowPlayer.down()))) {
                continue;
            }

            newSlot = i;
            break;
        }

        // Check if any blocks were found
        if (newSlot == -1) {
            return;
        }

        // Set slot using the proper method
        if (oldSlot == -1) {
            oldSlot = mc.player.getInventory().getSelectedSlot();
        }
        mc.player.getInventory().setSelectedSlot(newSlot);

        scaffoldTo(belowPlayer);
    }

    private void scaffoldTo(BlockPos belowPlayer) {
        // Tries to place a block directly under the player
        if (placeBlock(belowPlayer)) {
            return;
        }

        // If that doesn't work, tries to place a block next to the block that's under the player
        for (Direction side : Direction.values()) {
            BlockPos neighbor = belowPlayer.offset(side);
            if (placeBlock(neighbor)) {
                return;
            }
        }

        // If that doesn't work, tries to place a block next to a block that's next to the block that's under the player
        for (Direction side : Direction.values()) {
            for (Direction side2 : Arrays.copyOfRange(Direction.values(), side.ordinal(), 6)) {
                if (side.getOpposite() == side2) {
                    continue;
                }

                BlockPos neighbor = belowPlayer.offset(side).offset(side2);
                if (placeBlock(neighbor)) {
                    return;
                }
            }
        }
    }

    private boolean placeBlock(BlockPos pos) {
        Vec3d eyesPos = mc.player.getEyePos();

        for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.offset(side);
            Direction side2 = side.getOpposite();

            // Check if side is visible (facing away from player)
            if (eyesPos.squaredDistanceTo(Vec3d.ofCenter(pos)) >= eyesPos.squaredDistanceTo(Vec3d.ofCenter(neighbor))) {
                continue;
            }

            // Check if neighbor can be right clicked
            if (!BlockUtil.canClick(neighbor)) {
                continue;
            }

            Vec3d hitVec = Vec3d.ofCenter(neighbor).add(Vec3d.of(side2.getVector()).multiply(0.5));

            // Check if hitVec is within range (4.25 blocks)
            if (eyesPos.squaredDistanceTo(hitVec) > 18.0625) {
                continue;
            }

            // Rotate if enabled
            if (rotate.getValue()) {
                float[] rotations = RotationUtil.getRotations(hitVec);
                mc.player.setYaw(rotations[0]);
                mc.player.setPitch(rotations[1]);
            }

            // Place block
            BlockHitResult hitResult = new BlockHitResult(hitVec, side2, neighbor, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

            if (swing.getValue()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            return true;
        }

        return false;
    }
}