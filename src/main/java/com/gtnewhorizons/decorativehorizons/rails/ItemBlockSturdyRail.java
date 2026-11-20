package com.gtnewhorizons.decorativehorizons.rails;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.gtnewhorizons.decorativehorizons.core.LineBuilder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Item for the sturdy rails. A click on the top of a sturdy rail extends the rails in the player's direction. */
public class ItemBlockSturdyRail extends ItemBlock {

    public ItemBlockSturdyRail(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (!isExtending(world, x, y, z, side))
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        if (!world.isRemote) {
            new LineBuilder(world, x, y, z, LineBuilder.horizontalFacing(player)).extend(player, stack);
        }
        return true;
    }

    private static boolean isExtending(World world, int x, int y, int z, int side) {
        return side == 1 && world.getBlock(x, y, z) instanceof ISturdyRail;
    }

    // see `ItemBlockLine.func_150936_a`
    @Override
    @SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
        return isExtending(world, x, y, z, side) || super.func_150936_a(world, x, y, z, side, player, stack);
    }
}
