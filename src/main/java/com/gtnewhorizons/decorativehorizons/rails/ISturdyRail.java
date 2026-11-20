package com.gtnewhorizons.decorativehorizons.rails;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.decorativehorizons.core.ILineBlock;
import com.gtnewhorizons.decorativehorizons.core.LineBuilder;
import com.gtnewhorizons.decorativehorizons.core.wrench.IWrenchable;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** Line and wrench behavior of the sturdy rails. Any two sturdy rails form one line. */
public interface ISturdyRail extends ILineBlock, IWrenchable {

    @Override
    default boolean continuesLine(Block block, int meta, int lineMeta) {
        return block instanceof ISturdyRail;
    }

    @Override
    default void onWrench(World world, int x, int y, int z, WrenchClick click) {
        if (!click.isSneaking()) return;
        ForgeDirection face = click.face();
        EntityPlayer player = click.player();
        ForgeDirection dir = face.offsetY == 0 ? face.getOpposite() : LineBuilder.horizontalFacing(player);
        if (new LineBuilder(world, x, y, z, dir).retract(player)) Wrench.used(player, world, x, y, z);
    }

    @Override
    default boolean hasWrenchGrid() {
        return false;
    }
}
