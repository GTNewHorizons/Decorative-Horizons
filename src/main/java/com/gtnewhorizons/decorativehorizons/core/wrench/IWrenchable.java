package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** A block with a wrench action. Call {@link #activateWrench} from {@code onBlockActivated}. */
public interface IWrenchable {

    /**
     * Runs {@link #onWrench} for a click with a wrench, sneaking or not. Returns false when the item in the player's
     * hand isn't a wrench, and Minecraft then uses that item as for a normal click.
     */
    default boolean activateWrench(World world, int x, int y, int z, WrenchClick click) {
        EntityPlayer player = click.player();
        if (!Wrench.isWrench(player.getHeldItem())) return false;
        // Minecraft calls onBlockActivated for a sneak-click too: doesSneakBypassUse() returns true for GT tools
        if (!world.isRemote) onWrench(world, x, y, z, click);
        return true;
    }

    /** Called on the server. */
    void onWrench(World world, int x, int y, int z, WrenchClick click);

    default boolean hasWrenchGrid() {
        return true;
    }

    /** Whether to draw GregTech's rotation marker in the center cell while sneaking. */
    default boolean rotatesWithWrench() {
        return false;
    }
}
