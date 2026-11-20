package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.gtnewhorizons.decorativehorizons.Mods;

/** Wrench detection, and the damage and sound of a wrench use. */
public final class Wrench {

    private Wrench() {}

    public static boolean isWrench(ItemStack stack) {
        if (stack == null) return false;
        if (DebugWrench.item != null && stack.getItem() == DebugWrench.item) return true;
        return Mods.GregTech.isModLoaded() && GregTechWrench.isWrench(stack);
    }

    public static void used(EntityPlayer player, World world, int x, int y, int z) {
        ItemStack stack = player.getHeldItem();
        if (Mods.GregTech.isModLoaded() && stack != null && GregTechWrench.isWrench(stack)) {
            GregTechWrench.use(player, world, x, y, z);
            return;
        }
        Block.SoundType sound = Block.soundTypeMetal;
        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, sound.func_150496_b(), 0.5F, 1.2F);
    }
}
