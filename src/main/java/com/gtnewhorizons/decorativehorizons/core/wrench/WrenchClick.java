package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

/**
 * A wrench click on the {@code face} of a block. {@code target} is {@link WrenchSide#determineWrenchingSide} of the
 * click.
 */
@Desugar
public record WrenchClick(EntityPlayer player, ForgeDirection face, ForgeDirection target) {

    /** {@code side} and {@code hit} are the clicked side and hit vector that {@code onBlockActivated} receives. */
    public static WrenchClick of(EntityPlayer player, int side, Vec3 hit) {
        return new WrenchClick(
            player,
            ForgeDirection.getOrientation(side),
            WrenchSide.determineWrenchingSide(side, hit));
    }

    public boolean isSneaking() {
        return player.isSneaking();
    }
}
