package com.gtnewhorizons.decorativehorizons.core.wrench;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UNKNOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * The cell of the wrench grid that a click hits. The code is GT5-Unofficial's {@code GTUtility.determineWrenchingSide},
 * so the cells are the same as GregTech's.
 */
public final class WrenchSide {

    private WrenchSide() {}

    /**
     * The grid lines cross the clicked face at 1/4 and 3/4. Returns the face for the center cell, the side next to the
     * edge for an edge cell, and the opposite face for a corner cell. Returns {@link ForgeDirection#UNKNOWN} for an
     * invalid {@code side}.
     */
    public static ForgeDirection determineWrenchingSide(int side, Vec3 hit) {
        ForgeDirection face = ForgeDirection.getOrientation(side);
        float modX = ((float) hit.xCoord % 1.0f + 1.0f) % 1.0f;
        float modY = ((float) hit.yCoord % 1.0f + 1.0f) % 1.0f;
        float modZ = ((float) hit.zCoord % 1.0f + 1.0f) % 1.0f;
        ForgeDirection tBack = face.getOpposite();
        // in multiplayer, the hit vector has a precision of 1/16 and is rounded down, so 0.8 arrives as 0.75. Compare
        // with >= 0.75
        switch (face) {
            case DOWN, UP -> {
                if (modX < 0.25) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return WEST;
                }
                if (modX >= 0.75) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return EAST;
                }
                if (modZ < 0.25) return NORTH;
                if (modZ >= 0.75) return SOUTH;
                return face;
            }
            case NORTH, SOUTH -> {
                if (modX < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return WEST;
                }
                if (modX >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EAST;
                }
                if (modY < 0.25) return DOWN;
                if (modY >= 0.75) return UP;
                return face;
            }
            case WEST, EAST -> {
                if (modZ < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return NORTH;
                }
                if (modZ >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return SOUTH;
                }
                if (modY < 0.25) return DOWN;
                if (modY >= 0.75) return UP;
                return face;
            }
        }
        return UNKNOWN;
    }
}
