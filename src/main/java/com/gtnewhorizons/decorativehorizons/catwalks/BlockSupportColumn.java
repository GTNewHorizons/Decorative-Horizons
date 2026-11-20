package com.gtnewhorizons.decorativehorizons.catwalks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.api.OutlineShape;
import com.gtnewhorizons.decorativehorizons.core.Boxes;
import com.gtnewhorizons.decorativehorizons.core.ILineBlock;
import com.gtnewhorizons.decorativehorizons.core.LineBuilder;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** A column with an arm to each linked neighbor column, on any of the six sides. */
public class BlockSupportColumn extends BlockFrame implements ILineBlock {

    private static final int CONNECTIONS = 0b0111111; // by ForgeDirection ordinal
    public static final int CAUTION = 0b1000000;

    private static final double LO = 3.5, HI = 12.5;
    private static final AxisAlignedBB CORE = Boxes.px(LO, LO, LO, HI, HI, HI);
    private static final AxisAlignedBB[] ARMS = { Boxes.px(LO, 0, LO, HI, LO, HI), Boxes.px(LO, HI, LO, HI, 16, HI),
        Boxes.px(LO, LO, 0, HI, HI, LO), Boxes.px(LO, LO, HI, HI, HI, 16), Boxes.px(0, LO, LO, LO, HI, HI),
        Boxes.px(HI, LO, LO, 16, HI, HI) };
    private static final AxisAlignedBB[][] PARTS = new AxisAlignedBB[64][];
    private static final OutlineShape[] SHAPES = new OutlineShape[64];

    public BlockSupportColumn() {
        super("support_column", "support_column/frame", Integer.numberOfTrailingZeros(CAUTION));
    }

    @Override
    public int[] variants() {
        return new int[] { 0, 1 };
    }

    @Override
    public String variantName(int damage) {
        return "support_column" + (damage != 0 ? "_caution" : "");
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (isRemoved(world, x, y, z)) {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                removeArm(world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite());
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onWrench(World world, int x, int y, int z, WrenchClick click) {
        if (click.isSneaking()) {
            LineBuilder line = new LineBuilder(
                world,
                x,
                y,
                z,
                click.face()
                    .getOpposite());
            if (line.retract(click.player())) Wrench.used(click.player(), world, x, y, z);
            return;
        }
        ForgeDirection target = click.target();
        if (target == ForgeDirection.UNKNOWN) return;
        if (isConnected(world, x, y, z, target)) disconnect(world, x, y, z, target);
        else connect(world, x, y, z, target);
        Wrench.used(click.player(), world, x, y, z);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> list,
        Entity entity) {
        for (AxisAlignedBB box : getParts(connections(world, x, y, z)))
            Boxes.addCollision(list, mask, box.getOffsetBoundingBox(x, y, z));
    }

    @Override
    protected OutlineShape getPartsShape(World world, int x, int y, int z) {
        int key = connections(world, x, y, z);
        OutlineShape shape = SHAPES[key];
        if (shape == null) SHAPES[key] = shape = OutlineShape.of(Arrays.asList(getParts(key)));
        return shape;
    }

    private static AxisAlignedBB[] getParts(int key) {
        AxisAlignedBB[] parts = PARTS[key];
        if (parts == null) PARTS[key] = parts = buildParts(key);
        return parts;
    }

    private static AxisAlignedBB[] buildParts(int mask) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        boxes.add(CORE);
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if ((mask & 1 << side.ordinal()) != 0) boxes.add(ARMS[side.ordinal()]);
        }
        return boxes.toArray(new AxisAlignedBB[0]);
    }

    public static String side(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (!isConnected(world, x, y, z, side)) return "none";
        return isConnected(world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite()) ? "joined"
            : "end";
    }

    /** Links the column at x, y, z and the neighbor column on {@code side}. A position without a column is skipped. */
    public static void connect(World world, int x, int y, int z, ForgeDirection side) {
        addArm(world, x, y, z, side);
        addArm(world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite());
    }

    /** Unlinks the column at x, y, z and the neighbor column on {@code side}, the same way as {@link #connect}. */
    public static void disconnect(World world, int x, int y, int z, ForgeDirection side) {
        removeArm(world, x, y, z, side);
        removeArm(world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite());
    }

    private static void addArm(World world, int x, int y, int z, ForgeDirection side) {
        if (!(world.getBlock(x, y, z) instanceof BlockSupportColumn)) return;
        setConnections(world, x, y, z, connections(world, x, y, z) | 1 << side.ordinal());
    }

    private static void removeArm(World world, int x, int y, int z, ForgeDirection side) {
        if (!(world.getBlock(x, y, z) instanceof BlockSupportColumn)) return;
        setConnections(world, x, y, z, connections(world, x, y, z) & ~(1 << side.ordinal()));
    }

    private static boolean isConnected(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return (connections(world, x, y, z) & 1 << side.ordinal()) != 0;
    }

    private static void setConnections(World world, int x, int y, int z, int mask) {
        world.setBlock(x, y, z, Catwalks.SUPPORT_COLUMN, (world.getBlockMetadata(x, y, z) & ~CONNECTIONS) | mask, 3);
    }

    private static int connections(IBlockAccess world, int x, int y, int z) {
        if (!(world.getBlock(x, y, z) instanceof BlockSupportColumn)) return 0;
        return world.getBlockMetadata(x, y, z) & CONNECTIONS;
    }
}
