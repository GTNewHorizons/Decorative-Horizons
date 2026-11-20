package com.gtnewhorizons.decorativehorizons.catwalks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.api.OutlineShape;
import com.gtnewhorizons.decorativehorizons.core.Boxes;
import com.gtnewhorizons.decorativehorizons.core.Footsteps;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** A catwalk: a floor and four railings. The player toggles the floor and each railing with a wrench. */
public class BlockCatwalk extends BlockFrame {

    private static final int OPEN = 0b0001111; // north 1, south 2, west 4, east 8
    public static final int NO_FLOOR = 0b0010000;
    public static final int LIT = 0b0100000;
    public static final int CAUTION = 0b1000000;
    private static final int STATE = OPEN | NO_FLOOR;

    public static final ForgeDirection[] SIDES = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST,
        ForgeDirection.EAST };

    private static final AxisAlignedBB FLOOR = Boxes.px(0, 0, 0, 16, 1, 16);
    private static final AxisAlignedBB RAILING = Boxes.px(0, 1, 0, 16, 16, 1);

    private static final AxisAlignedBB[][] PARTS = new AxisAlignedBB[32][];
    private static final OutlineShape[] SHAPES = new OutlineShape[32];

    public BlockCatwalk() {
        super("catwalk", "catwalk/grate", Integer.numberOfTrailingZeros(LIT));
    }

    @Override
    public int[] variants() {
        return new int[] { 0, 1, 2, 3 };
    }

    @Override
    public String variantName(int damage) {
        int meta = variantMeta(damage);
        return ((meta & LIT) != 0 ? "lit_" : "") + "catwalk" + ((meta & CAUTION) != 0 ? "_caution" : "");
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return world.getBlock(x, y, z) == this && (world.getBlockMetadata(x, y, z) & LIT) != 0 ? 12 : 0;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int open = 0;
        for (ForgeDirection side : SIDES) {
            int nx = x + side.offsetX, nz = z + side.offsetZ;
            if (connects(world, x, y, z, side)) {
                open |= bit(side);
                openRailing(world, nx, y, nz, side.getOpposite());
                BlockCagedLadder.linkCatwalk(world, nx, y, nz, side.getOpposite());
            } else if (world.isSideSolid(nx, y, nz, side.getOpposite(), false)) {
                open |= bit(side);
            }
        }
        setFloor(world, x, y + 1, z, false);
        if (world.getBlock(x, y - 1, z) instanceof BlockCatwalk) open |= NO_FLOOR;
        setMeta(world, x, y, z, variantBits(world.getBlockMetadata(x, y, z)) | open);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (isRemoved(world, x, y, z)) {
            for (ForgeDirection side : SIDES) {
                closeRailing(world, x + side.offsetX, y, z + side.offsetZ, side.getOpposite());
                BlockCagedLadder.unlinkCatwalk(world, x + side.offsetX, y, z + side.offsetZ, side.getOpposite());
            }
            setFloor(world, x, y + 1, z, true);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onWrench(World world, int x, int y, int z, WrenchClick click) {
        ForgeDirection target = click.target();
        if (target == ForgeDirection.UNKNOWN) return;
        int meta = world.getBlockMetadata(x, y, z);
        int newMeta = meta ^ (target.offsetY != 0 ? NO_FLOOR : bit(target));
        // with no floor and no railings, only the rims and posts joining neighbors are left to draw
        if ((newMeta & STATE) == STATE && !joinsNeighborRailings(world, x, y, z)) {
            world.setBlockToAir(x, y, z);
            dropBlockAsItem(world, x, y, z, new ItemStack(this, 1, damageDropped(meta)));
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.pop", 0.2F, 0.9F);
            Wrench.used(click.player(), world, x, y, z);
            return;
        }
        setMeta(world, x, y, z, newMeta);
        Wrench.used(click.player(), world, x, y, z);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (hasFloor(world.getBlockMetadata(x, y, z))) Footsteps.onEntityInside(y, entity, this);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> list,
        Entity entity) {
        for (AxisAlignedBB box : getParts(world.getBlockMetadata(x, y, z) & STATE)) {
            Boxes.addCollision(list, mask, box.getOffsetBoundingBox(x, y, z));
        }
    }

    @Override
    protected OutlineShape getPartsShape(World world, int x, int y, int z) {
        int key = world.getBlockMetadata(x, y, z) & STATE;
        OutlineShape shape = SHAPES[key];
        if (shape == null) SHAPES[key] = shape = OutlineShape.of(Arrays.asList(getParts(key)));
        return shape;
    }

    private static AxisAlignedBB[] getParts(int key) {
        AxisAlignedBB[] parts = PARTS[key];
        if (parts == null) PARTS[key] = parts = buildParts(key);
        return parts;
    }

    private static AxisAlignedBB[] buildParts(int meta) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        if (hasFloor(meta)) boxes.add(FLOOR);
        for (ForgeDirection side : SIDES) {
            if (!isOpen(meta, side)) boxes.add(Boxes.rotateY(RAILING, turns(side)));
        }
        return boxes.toArray(new AxisAlignedBB[0]);
    }

    public static String side(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (!isOpen(world.getBlockMetadata(x, y, z), side)) return "railing";
        return hasRim(world, x, y, z, side) ? "rim" : "open";
    }

    /** The model of {@code corner}: {@code none}, {@code post}, {@code foot} or {@code grate}. */
    public static String corner(IBlockAccess world, int x, int y, int z, Corner corner) {
        if (hasPost(world, x, y, z, corner)) return "post";
        if (!isFloorless(world, x, y, z)) return "none";
        int meta = world.getBlockMetadata(x, y, z);
        if (!isOpen(meta, corner.zSide) || !isOpen(meta, corner.xSide)) return "foot";
        return hasRim(world, x, y, z, corner.zSide) || hasRim(world, x, y, z, corner.xSide) ? "grate" : "none";
    }

    private static boolean connects(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int nx = x + side.offsetX, nz = z + side.offsetZ;
        Block neighbor = world.getBlock(nx, y, nz);
        if (neighbor instanceof BlockCatwalk) return true;
        return neighbor instanceof BlockCagedLadder
            && BlockCagedLadder.getBack(world.getBlockMetadata(nx, y, nz)) != side.getOpposite();
    }

    /** Does nothing when the block isn't a catwalk. */
    public static void openRailing(World world, int x, int y, int z, ForgeDirection side) {
        setMeta(world, x, y, z, world.getBlockMetadata(x, y, z) | bit(side));
    }

    /** Does nothing when the block isn't a catwalk. */
    public static void closeRailing(World world, int x, int y, int z, ForgeDirection side) {
        setMeta(world, x, y, z, world.getBlockMetadata(x, y, z) & ~bit(side));
    }

    private static void setFloor(World world, int x, int y, int z, boolean floor) {
        int meta = world.getBlockMetadata(x, y, z);
        setMeta(world, x, y, z, floor ? meta & ~NO_FLOOR : meta | NO_FLOOR);
    }

    private static void setMeta(World world, int x, int y, int z, int meta) {
        if (world.getBlock(x, y, z) instanceof BlockCatwalk catwalk) world.setBlock(x, y, z, catwalk, meta, 3);
    }

    private static boolean joinsNeighborRailings(IBlockAccess world, int x, int y, int z) {
        for (ForgeDirection side : SIDES) {
            if (capsRailing(world, x, y, z, side)) return true;
        }
        for (Corner corner : Corner.values()) {
            if (railingsMeet(world, x, y, z, corner)) return true;
        }
        return false;
    }

    private static boolean hasRim(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isFloorless(world, x, y, z) && isOpen(world.getBlockMetadata(x, y, z), side)
            && capsRailing(world, x, y, z, side);
    }

    /** Whether both railings of this catwalk at the corner are open, and the neighbors' railings meet there. */
    private static boolean hasPost(IBlockAccess world, int x, int y, int z, Corner corner) {
        int meta = world.getBlockMetadata(x, y, z);
        return isOpen(meta, corner.zSide) && isOpen(meta, corner.xSide) && railingsMeet(world, x, y, z, corner);
    }

    private static boolean capsRailing(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int nx = x + side.offsetX, nz = z + side.offsetZ;
        return world.getBlock(x, y - 1, z) instanceof BlockCatwalk && !isOpen(world.getBlockMetadata(x, y - 1, z), side)
            && (hasFloor(world, nx, y, nz) || BlockCagedLadder.hasFloor(world, nx, y, nz));
    }

    /**
     * Whether the neighbor on {@code zSide} has a closed railing or wall on {@code xSide}, and the neighbor on
     * {@code xSide} has a closed railing or wall on {@code zSide}.
     */
    private static boolean railingsMeet(IBlockAccess world, int x, int y, int z, Corner corner) {
        ForgeDirection a = corner.zSide, b = corner.xSide;
        return isWallClosed(world, x + a.offsetX, y, z + a.offsetZ, b)
            && isWallClosed(world, x + b.offsetX, y, z + b.offsetZ, a);
    }

    private static boolean isWallClosed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockCatwalk) return !isOpen(world.getBlockMetadata(x, y, z), side);
        return block instanceof BlockCagedLadder && !BlockCagedLadder.isWallOpen(world, x, y, z, side);
    }

    private static boolean isFloorless(IBlockAccess world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof BlockCatwalk && !hasFloor(world.getBlockMetadata(x, y, z));
    }

    private static boolean hasFloor(IBlockAccess world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof BlockCatwalk && hasFloor(world.getBlockMetadata(x, y, z));
    }

    private static boolean hasFloor(int meta) {
        return (meta & NO_FLOOR) == 0;
    }

    private static boolean isOpen(int meta, ForgeDirection side) {
        return (meta & bit(side)) != 0;
    }

    private static int bit(ForgeDirection side) {
        return 1 << (side.ordinal() - 2);
    }

    private static int turns(ForgeDirection side) {
        return switch (side) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            case DOWN, UP, UNKNOWN -> throw new IllegalArgumentException("no railing on side " + side);
        };
    }

    /** Same order as the y-rotations of the corner models in the blockstate. */
    public enum Corner {

        NORTH_WEST(ForgeDirection.NORTH, ForgeDirection.WEST),
        NORTH_EAST(ForgeDirection.NORTH, ForgeDirection.EAST),
        SOUTH_EAST(ForgeDirection.SOUTH, ForgeDirection.EAST),
        SOUTH_WEST(ForgeDirection.SOUTH, ForgeDirection.WEST);

        public final ForgeDirection zSide, xSide;

        Corner(ForgeDirection zSide, ForgeDirection xSide) {
            this.zSide = zSide;
            this.xSide = xSide;
        }
    }
}
