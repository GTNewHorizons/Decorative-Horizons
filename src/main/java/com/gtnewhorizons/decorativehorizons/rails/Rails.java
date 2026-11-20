package com.gtnewhorizons.decorativehorizons.rails;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;
import com.gtnewhorizons.decorativehorizons.core.Boxes;

/**
 * The sturdy rails, and the code that the rail classes share. The rail classes extend three different vanilla rails.
 * Each rail class has an {@code onNeighborBlockChange} without vanilla's support check, so the rail stays in place with
 * no block underneath.
 */
public final class Rails {

    private static final AxisAlignedBB WALKWAY = Boxes.px(0, 0, 0, 16, 1, 16);

    public static final Block STURDY_RAIL = new BlockSturdyRail();
    public static final Block STURDY_POWERED_RAIL = new BlockSturdyPoweredRail(false);
    public static final Block STURDY_DETECTOR_RAIL = new BlockSturdyDetectorRail();
    public static final Block STURDY_ACTIVATOR_RAIL = new BlockSturdyPoweredRail(true);

    private Rails() {}

    public static void register() {
        DecorativeHorizons.register(STURDY_RAIL, "sturdy_rail", ItemBlockSturdyRail.class);
        DecorativeHorizons.register(STURDY_POWERED_RAIL, "sturdy_powered_rail", ItemBlockSturdyRail.class);
        DecorativeHorizons.register(STURDY_DETECTOR_RAIL, "sturdy_detector_rail", ItemBlockSturdyRail.class);
        DecorativeHorizons.register(STURDY_ACTIVATOR_RAIL, "sturdy_activator_rail", ItemBlockSturdyRail.class);
    }

    public static void setup(Block rail, String name) {
        rail.setHardness(0.7F);
        rail.setStepSound(Block.soundTypeMetal);
        rail.setBlockName(DecorativeHorizons.MODID + "." + name);
        // TODO: Must be replaced. The rail textures are copies of vanilla's, as placeholders.
        rail.setBlockTextureName(DecorativeHorizons.MODID + ":rails/" + name);
    }

    public static boolean canPlaceAt(World world, int x, int y, int z) {
        return world.getBlock(x, y, z)
            .isReplaceable(world, x, y, z) && !BlockRailBase.func_150049_b_(world, x, y - 1, z);
    }

    /** Adds the 1 px floor at x, y, z to {@code list} when the floor intersects {@code mask}. */
    public static void addWalkway(List<AxisAlignedBB> list, AxisAlignedBB mask, int x, int y, int z) {
        Boxes.addCollision(list, mask, WALKWAY.getOffsetBoundingBox(x, y, z));
    }
}
