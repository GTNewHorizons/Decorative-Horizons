package com.gtnewhorizons.decorativehorizons.catwalks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.decorativehorizons.core.ILineBlock;
import com.gtnewhorizons.decorativehorizons.core.ItemBlockVariant;
import com.gtnewhorizons.decorativehorizons.core.LineBuilder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Item for an {@link ILineBlock}. A sneak-click on a block of the same line extends the line. */
public class ItemBlockLine extends ItemBlockVariant {

    public ItemBlockLine(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        LineBuilder line = createLine(world, x, y, z, side);
        // Minecraft skips Block.onBlockActivated for a sneak-click with an item in hand, so the item extends the line
        if (!isExtending(player, stack, line))
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        if (!world.isRemote) {
            ChunkCoordinates placed = line.extend(player, stack);
            if (placed != null && field_150939_a instanceof BlockSupportColumn) {
                ForgeDirection face = ForgeDirection.getOrientation(side);
                BlockSupportColumn.connect(world, placed.posX, placed.posY, placed.posZ, face);
            }
        }
        return true;
    }

    private static LineBuilder createLine(World world, int x, int y, int z, int side) {
        return new LineBuilder(
            world,
            x,
            y,
            z,
            ForgeDirection.getOrientation(side)
                .getOpposite());
    }

    /**
     * Whether a sneak-click with {@code stack} on the start of {@code line} extends the line. The new block then goes
     * at the end of the line, not next to the clicked face.
     */
    private static boolean isExtending(EntityPlayer player, ItemStack stack, LineBuilder line) {
        return player.isSneaking() && line.startsLineOf(stack);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        if (!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata)) return false;
        if (field_150939_a instanceof BlockSupportColumn) {
            BlockSupportColumn.connect(
                world,
                x,
                y,
                z,
                ForgeDirection.getOrientation(side)
                    .getOpposite());
        }
        return true;
    }

    // Minecraft runs this placement check on the client, before the click goes to the server
    @Override
    @SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
        return isExtending(player, stack, createLine(world, x, y, z, side))
            || super.func_150936_a(world, x, y, z, side, player, stack);
    }
}
