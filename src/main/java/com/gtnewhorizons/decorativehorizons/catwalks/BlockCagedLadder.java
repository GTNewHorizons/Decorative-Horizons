package com.gtnewhorizons.decorativehorizons.catwalks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.api.OutlineShape;
import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;
import com.gtnewhorizons.decorativehorizons.core.Boxes;
import com.gtnewhorizons.decorativehorizons.core.Footsteps;
import com.gtnewhorizons.decorativehorizons.core.LineBuilder;
import com.gtnewhorizons.decorativehorizons.core.wrench.Wrench;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** A ladder in a cage, with walls that open onto neighboring catwalks. */
public class BlockCagedLadder extends BlockFrame {

    private static final ForgeDirection[] FACINGS = { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH,
        ForgeDirection.WEST };
    private static final int FACING = 0b00000011;
    private static final int OPEN_WALLS = 0b00111100; // back, right, front, left
    public static final int FLOOR_OPEN = 0b01000000;
    public static final int CAUTION = 0b10000000;
    private static final int STATE = FACING | OPEN_WALLS | FLOOR_OPEN;

    private static final int BACK = 0, RIGHT = 1, FRONT = 2, LEFT = 3;

    private static final AxisAlignedBB WALL_BACK = Boxes.px(0, 0, 0, 16, 16, 1);
    private static final AxisAlignedBB WALL_LEFT = Boxes.px(0, 0, 0, 1, 16, 16);
    private static final AxisAlignedBB WALL_RIGHT = Boxes.px(15, 0, 0, 16, 16, 16);
    private static final AxisAlignedBB WALL_FRONT = Boxes.px(0, 0, 15, 16, 16, 16);
    private static final AxisAlignedBB RUNGS = Boxes.px(1, 0, 1, 15, 16, 2);
    private static final AxisAlignedBB FLOOR = Boxes.px(0, 0, 0, 16, 1, 16);

    // blocks walked between two footstep sounds in vanilla
    private static final double STEP_DISTANCE = 1 / 0.6;

    private static final AxisAlignedBB[][] PARTS = new AxisAlignedBB[128][];
    private static final OutlineShape[] SHAPES = new OutlineShape[128];

    public BlockCagedLadder() {
        super("caged_ladder", "caged_ladder/cage", Integer.numberOfTrailingZeros(CAUTION));
    }

    @Override
    public int[] variants() {
        return new int[] { 0, 1 };
    }

    @Override
    public String variantName(int damage) {
        return "caged_ladder" + (damage != 0 ? "_caution" : "");
    }

    @Override
    public boolean rotatesWithWrench() {
        return true;
    }

    @Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        ForgeDirection back = LineBuilder.horizontalFacing(placer);
        if (world.getBlock(x, y - 1, z) instanceof BlockCagedLadder) {
            back = getBack(world.getBlockMetadata(x, y - 1, z));
        } else if (world.getBlock(x, y + 1, z) instanceof BlockCagedLadder) {
            back = getBack(world.getBlockMetadata(x, y + 1, z));
        }
        setMeta(world, x, y, z, withFacing(variantBits(world.getBlockMetadata(x, y, z)), back));
        updateFloor(world, x, y, z);
        updateFloor(world, x, y + 1, z);
        linkCatwalks(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (isRemoved(world, x, y, z)) {
            for (ForgeDirection side : BlockCatwalk.SIDES) {
                BlockCatwalk.closeRailing(world, x + side.offsetX, y, z + side.offsetZ, side.getOpposite());
            }
            updateFloor(world, x, y + 1, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onWrench(World world, int x, int y, int z, WrenchClick click) {
        ForgeDirection target = click.target();
        int meta = world.getBlockMetadata(x, y, z);
        if (click.isSneaking()) {
            if (target != click.face()) return;
            setMeta(world, x, y, z, withFacing(meta, FACINGS[(meta + 1) & FACING]));
            linkCatwalks(world, x, y, z);
        } else if (target == ForgeDirection.DOWN) {
            setMeta(world, x, y, z, meta ^ FLOOR_OPEN);
        } else if (target.offsetY == 0 && target != ForgeDirection.UNKNOWN) {
            if (isWallOpen(world, x, y, z, target)) closeWall(world, x, y, z, target);
            else openWall(world, x, y, z, target);
        } else {
            return;
        }
        Wrench.used(click.player(), world, x, y, z);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (hasFloor(world.getBlockMetadata(x, y, z))) Footsteps.onEntityInside(y, entity, this);
        // vanilla plays climbing sounds only for Blocks.ladder
        if (!(entity instanceof EntityLivingBase) || entity.onGround) return;
        if (MathHelper.floor_double(entity.boundingBox.minY) != y) return;
        if (Math.floor(entity.posY / STEP_DISTANCE) != Math.floor(entity.prevPosY / STEP_DISTANCE)) {
            entity.playSound(DecorativeHorizons.MODID + ":caged_ladder.climb", 0.15F, 1.0F);
        }
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
        boxes.add(RUNGS);
        if (!isOpen(meta, BACK)) boxes.add(WALL_BACK);
        if (!isOpen(meta, LEFT)) boxes.add(WALL_LEFT);
        if (!isOpen(meta, RIGHT)) boxes.add(WALL_RIGHT);
        if (!isOpen(meta, FRONT)) boxes.add(WALL_FRONT);
        if (hasFloor(meta)) boxes.add(FLOOR);
        boxes.replaceAll(box -> Boxes.rotateY(box, meta & FACING));
        return boxes.toArray(new AxisAlignedBB[0]);
    }

    public static String side(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isWallOpen(world, x, y, z, side) ? "open" : "wall";
    }

    /**
     * Opens the wall on {@code side} for a catwalk there. The back wall stays closed, and a block that isn't a caged
     * ladder is skipped.
     */
    public static void linkCatwalk(World world, int x, int y, int z, ForgeDirection side) {
        if (isSideWall(world, x, y, z, side)) openWall(world, x, y, z, side);
    }

    /**
     * Closes the wall on {@code side} after a catwalk there is removed. The back wall, and a block that isn't a caged
     * ladder, are skipped.
     */
    public static void unlinkCatwalk(World world, int x, int y, int z, ForgeDirection side) {
        if (isSideWall(world, x, y, z, side)) closeWall(world, x, y, z, side);
    }

    private static boolean isSideWall(World world, int x, int y, int z, ForgeDirection side) {
        return world.getBlock(x, y, z) instanceof BlockCagedLadder && getBack(world.getBlockMetadata(x, y, z)) != side;
    }

    private static void linkCatwalks(World world, int x, int y, int z) {
        ForgeDirection back = getBack(world.getBlockMetadata(x, y, z));
        for (ForgeDirection side : BlockCatwalk.SIDES) {
            int nx = x + side.offsetX, nz = z + side.offsetZ;
            if (!(world.getBlock(nx, y, nz) instanceof BlockCatwalk)) continue;
            if (side == back) {
                BlockCatwalk.closeRailing(world, nx, y, nz, side.getOpposite());
                closeWall(world, x, y, z, side);
            } else {
                BlockCatwalk.openRailing(world, nx, y, nz, side.getOpposite());
                openWall(world, x, y, z, side);
            }
        }
    }

    private static void updateFloor(World world, int x, int y, int z) {
        setFloor(world, x, y, z, !(world.getBlock(x, y - 1, z) instanceof BlockCagedLadder));
    }

    private static void setFloor(World world, int x, int y, int z, boolean floor) {
        int meta = world.getBlockMetadata(x, y, z);
        setMeta(world, x, y, z, floor ? meta & ~FLOOR_OPEN : meta | FLOOR_OPEN);
    }

    /** Does nothing when the block isn't a caged ladder. */
    public static void openWall(World world, int x, int y, int z, ForgeDirection side) {
        int meta = world.getBlockMetadata(x, y, z);
        setMeta(world, x, y, z, meta | wallBit(meta, side));
    }

    /** Does nothing when the block isn't a caged ladder. */
    public static void closeWall(World world, int x, int y, int z, ForgeDirection side) {
        int meta = world.getBlockMetadata(x, y, z);
        setMeta(world, x, y, z, meta & ~wallBit(meta, side));
    }

    private static void setMeta(World world, int x, int y, int z, int meta) {
        if (world.getBlock(x, y, z) instanceof BlockCagedLadder ladder) world.setBlock(x, y, z, ladder, meta, 3);
    }

    public static boolean isWallOpen(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (!(world.getBlock(x, y, z) instanceof BlockCagedLadder)) return false;
        int meta = world.getBlockMetadata(x, y, z);
        return (meta & wallBit(meta, side)) != 0;
    }

    public static boolean hasFloor(IBlockAccess world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof BlockCagedLadder && hasFloor(world.getBlockMetadata(x, y, z));
    }

    private static boolean hasFloor(int meta) {
        return (meta & FLOOR_OPEN) == 0;
    }

    public static ForgeDirection getBack(int meta) {
        return FACINGS[meta & FACING];
    }

    public static int withFacing(int meta, ForgeDirection facing) {
        return (meta & ~FACING) | facingIndex(facing);
    }

    private static int wallBit(int meta, ForgeDirection side) {
        return relativeWallBit((facingIndex(side) - (meta & FACING)) & 3);
    }

    private static int facingIndex(ForgeDirection facing) {
        for (int i = 0; i < 4; i++) {
            if (FACINGS[i] == facing) return i;
        }
        return 0;
    }

    private static boolean isOpen(int meta, int relative) {
        return (meta & relativeWallBit(relative)) != 0;
    }

    private static int relativeWallBit(int relative) {
        return Integer.lowestOneBit(OPEN_WALLS) << relative;
    }
}
