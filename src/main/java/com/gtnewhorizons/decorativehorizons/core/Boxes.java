package com.gtnewhorizons.decorativehorizons.core;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;

/** Bounding boxes for block parts, in block-local coordinates. */
public final class Boxes {

    private Boxes() {}

    /** A box from pixel coordinates, 16 pixels to the block. */
    public static AxisAlignedBB px(double x0, double y0, double z0, double x1, double y1, double z1) {
        return AxisAlignedBB.getBoundingBox(x0 / 16, y0 / 16, z0 / 16, x1 / 16, y1 / 16, z1 / 16);
    }

    public static AxisAlignedBB rotateY(AxisAlignedBB box, int quarterTurns) {
        AxisAlignedBB b = box;
        for (int i = 0; i < (quarterTurns & 3); i++) {
            b = AxisAlignedBB.getBoundingBox(1 - b.maxZ, b.minY, b.minX, 1 - b.minZ, b.maxY, b.maxX);
        }
        return b;
    }

    /**
     * Adds {@code box}, in world coordinates, to {@code list} when the box intersects {@code mask}. Collision boxes go
     * through here, not {@code setBlockBounds}: GTNHLib reads the block bounds for face culling.
     */
    public static void addCollision(List<AxisAlignedBB> list, AxisAlignedBB mask, AxisAlignedBB box) {
        if (mask.intersectsWith(box)) list.add(box);
    }
}
