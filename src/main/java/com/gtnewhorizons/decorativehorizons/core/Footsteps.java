package com.gtnewhorizons.decorativehorizons.core;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Step sounds for 1 px floors over air. */
public final class Footsteps {

    // one map per side: Entity.equals() compares entity IDs, and in singleplayer both players have the same ID
    private static final Map<Entity, Integer> CLIENT = new WeakHashMap<>(), SERVER = new WeakHashMap<>();

    private Footsteps() {}

    /**
     * Plays the step sound of {@code floor} for a living entity that walks on {@code floor} over air. Riders and
     * sneaking players are skipped. Call from {@code onEntityCollidedWithBlock} of a block at height {@code y}.
     */
    public static void onEntityInside(int y, Entity entity, Block floor) {
        if (!(entity instanceof EntityLivingBase) || !entity.onGround || entity.ridingEntity != null) return;
        if (entity instanceof EntityPlayer && entity.isSneaking()) return;
        if (MathHelper.floor_double(entity.boundingBox.minY) != y) return;
        World world = entity.worldObj;
        // vanilla takes the step sound from the block under a 1 px floor, and air has no step sound
        int bx = MathHelper.floor_double(entity.posX), bz = MathHelper.floor_double(entity.posZ);
        int below = MathHelper.floor_double(entity.boundingBox.minY - 0.2);
        if (world.getBlock(bx, below, bz)
            .getMaterial() != Material.air) return;

        Map<Entity, Integer> nextStep = world.isRemote ? CLIENT : SERVER;
        float walked = entity.distanceWalkedOnStepModified;
        Integer next = nextStep.get(entity);
        if (next == null || walked < next - 2) {
            nextStep.put(entity, (int) walked + 1);
        } else if (walked > next) {
            nextStep.put(entity, (int) walked + 1);
            Block.SoundType sound = floor.stepSound;
            entity.playSound(sound.getStepResourcePath(), sound.getVolume() * 0.15F, sound.getPitch());
        }
    }
}
