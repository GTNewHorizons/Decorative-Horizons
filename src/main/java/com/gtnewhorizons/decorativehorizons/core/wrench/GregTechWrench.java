package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTUtility;

/**
 * GregTech's wrenches. This class loads GregTech classes, so check {@code Mods.GregTech.isModLoaded()} before any call.
 * {@link #use} does what {@code BaseMetaTileEntity.onRightclick} does after a wrench action.
 */
public final class GregTechWrench {

    private GregTechWrench() {}

    public static boolean isWrench(ItemStack stack) {
        return GTUtility.isStackInList(stack, GregTechAPI.sWrenchList);
    }

    public static void use(EntityPlayer player, World world, int x, int y, int z) {
        ItemStack stack = player.getHeldItem();
        GTModHandler.damageOrDechargeItem(stack, 1, 1000, player);
        GTUtility.sendSoundToPlayers(world, SoundResource.GTCEU_OP_WRENCH, 1.0F, 1, x + 0.5, y + 0.5, z + 0.5);
        if (stack.stackSize == 0) ForgeEventFactory.onPlayerDestroyItem(player, stack);
    }
}
