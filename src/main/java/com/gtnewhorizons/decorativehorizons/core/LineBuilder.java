package com.gtnewhorizons.decorativehorizons.core;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

/** A straight line of blocks that starts at a clicked block and runs in direction {@code dir}. */
public final class LineBuilder {

    public static final int MAX_LENGTH = 64;

    // vanilla's yaw quadrants start at south and go clockwise
    private static final ForgeDirection[] BY_YAW = { ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.NORTH,
        ForgeDirection.EAST };

    private final World world;
    private final int x, y, z;
    private final ForgeDirection dir;

    public LineBuilder(World world, int x, int y, int z, ForgeDirection dir) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
    }

    public static ForgeDirection horizontalFacing(EntityLivingBase entity) {
        return BY_YAW[MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3];
    }

    /** Whether the clicked block continues a line of the block that {@code stack} places. */
    public boolean startsLineOf(ItemStack stack) {
        return continuesLine(x, y, z, getPlacedBlock(stack), getPlacedMeta(stack));
    }

    /**
     * Places the block of {@code stack} at the first position in direction {@code dir} that isn't part of the line, at
     * most {@link #MAX_LENGTH} blocks away. A player outside creative mode uses up one item of {@code stack}.
     *
     * @return the placed position, or {@code null} when nothing was placed
     */
    public ChunkCoordinates extend(EntityPlayer player, ItemStack stack) {
        Block block = getPlacedBlock(stack);
        int meta = getPlacedMeta(stack);
        int side = dir.getOpposite()
            .ordinal();
        for (int i = 1; i <= MAX_LENGTH; i++) {
            int px = x + dir.offsetX * i, py = y + dir.offsetY * i, pz = z + dir.offsetZ * i;
            if (!world.blockExists(px, py, pz)) return null;
            if (continuesLine(px, py, pz, block, meta)) continue;
            Block existing = world.getBlock(px, py, pz);
            if (!existing.isReplaceable(world, px, py, pz) || !player.canPlayerEdit(px, py, pz, side, stack)
                || !world.canPlaceEntityOnSide(block, px, py, pz, false, side, player, stack)) {
                return null;
            }

            BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, px, py, pz);
            if (!world.setBlock(px, py, pz, block, meta, 3)) return null;
            if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, dir.getOpposite())
                .isCanceled()) {
                snapshot.restore(true, false);
                return null;
            }

            Block.SoundType sound = block.stepSound;
            world.playSoundEffect(
                px + 0.5,
                py + 0.5,
                pz + 0.5,
                sound.func_150496_b(),
                (sound.getVolume() + 1.0F) / 2.0F,
                sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
            return new ChunkCoordinates(px, py, pz);
        }
        return null;
    }

    private static Block getPlacedBlock(ItemStack stack) {
        return ((ItemBlock) stack.getItem()).field_150939_a;
    }

    private static int getPlacedMeta(ItemStack stack) {
        return stack.getItem()
            .getMetadata(stack.getItemDamage());
    }

    /**
     * Removes the last block of the line. A player outside creative mode gets the block back as an item.
     *
     * @return false when the player may not remove the block
     */
    public boolean retract(EntityPlayer player) {
        Block line = world.getBlock(x, y, z);
        int lineMeta = world.getBlockMetadata(x, y, z);
        int length = 0;
        while (length < MAX_LENGTH && continuesLine(
            x + dir.offsetX * (length + 1),
            y + dir.offsetY * (length + 1),
            z + dir.offsetZ * (length + 1),
            line,
            lineMeta)) {
            length++;
        }
        return removeToPlayer(x + dir.offsetX * length, y + dir.offsetY * length, z + dir.offsetZ * length, player);
    }

    /**
     * Whether the block at px, py, pz continues {@code line}. A block in an unloaded chunk doesn't: reading the block
     * would load the chunk.
     */
    private boolean continuesLine(int px, int py, int pz, Block line, int meta) {
        if (!world.blockExists(px, py, pz)) return false;
        Block block = world.getBlock(px, py, pz);
        if (!(line instanceof ILineBlock lineBlock)) return block == line;
        return lineBlock.continuesLine(block, world.getBlockMetadata(px, py, pz), meta);
    }

    private boolean removeToPlayer(int px, int py, int pz, EntityPlayer player) {
        Block block = world.getBlock(px, py, pz);
        int meta = world.getBlockMetadata(px, py, pz);
        if (!player.canPlayerEdit(px, py, pz, 0, null)) return false;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(px, py, pz, world, block, meta, player);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;

        // 2001 is vanilla's block break effect (sound and particles)
        world.playAuxSFX(2001, px, py, pz, Block.getIdFromBlock(block) + (meta << 12));
        world.setBlockToAir(px, py, pz);
        if (!player.capabilities.isCreativeMode) {
            ItemStack drop = new ItemStack(block.getItemDropped(meta, world.rand, 0), 1, block.damageDropped(meta));
            // an item entity at the player's feet: with a full inventory, the item stays on the ground
            EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, drop);
            item.delayBeforeCanPickup = 0;
            world.spawnEntityInWorld(item);
        }
        return true;
    }
}
