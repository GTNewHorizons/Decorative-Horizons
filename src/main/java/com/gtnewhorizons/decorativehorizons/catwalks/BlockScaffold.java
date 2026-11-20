package com.gtnewhorizons.decorativehorizons.catwalks;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.decorativehorizons.core.ILineBlock;
import com.gtnewhorizons.decorativehorizons.core.LineBuilder;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A scaffold block in one of the {@link #STYLES}. The faces between two scaffolds aren't rendered. */
public class BlockScaffold extends BlockFrame implements ILineBlock {

    public static final String[] STYLES = { "smooth", "flat", "stepped" };
    public static final int STYLE = 0b011;
    public static final int CAUTION = 0b100;

    public BlockScaffold() {
        super("scaffold", "scaffold/frame", 0);
    }

    @Override
    public boolean continuesLine(Block block, int meta, int lineMeta) {
        return block == this && style(meta) == style(lineMeta);
    }

    @Override
    public int[] variants() {
        return new int[] { 0, 1, 2, CAUTION, CAUTION | 1, CAUTION | 2 };
    }

    @Override
    public String variantName(int damage) {
        int style = style(damage) % STYLES.length;
        return (style == 0 ? "" : STYLES[style] + "_") + "scaffold" + ((damage & CAUTION) != 0 ? "_caution" : "");
    }

    public static String side(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return world.getBlock(x + side.offsetX, y + side.offsetY, z + side.offsetZ) instanceof BlockScaffold ? "joined"
            : "none";
    }

    @Override
    public void onWrench(World world, int x, int y, int z, WrenchClick click) {
        if (!click.isSneaking()) return;
        LineBuilder line = new LineBuilder(
            world,
            x,
            y,
            z,
            click.face()
                .getOpposite());
        if (line.retract(click.player())) Wrench.used(click.player(), world, x, y, z);
    }

    @Override
    public boolean hasWrenchGrid() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return !(world.getBlock(x, y, z) instanceof BlockScaffold) && super.shouldSideBeRendered(world, x, y, z, side);
    }

    private static int style(int meta) {
        return meta & STYLE;
    }
}
