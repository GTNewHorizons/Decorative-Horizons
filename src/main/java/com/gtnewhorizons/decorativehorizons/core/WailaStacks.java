package com.gtnewhorizons.decorativehorizons.core;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.gtnewhorizons.decorativehorizons.Mods;

import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Waila shows a block as an item whose damage is the block's metadata. An {@link IVariantBlock} stores state in the
 * metadata too, so this provider returns the pick-block item instead.
 */
public final class WailaStacks implements IWailaDataProvider {

    private WailaStacks() {}

    public static void init() {
        FMLInterModComms.sendMessage(Mods.Waila.id, "register", WailaStacks.class.getName() + ".register");
    }

    public static void register(IWailaRegistrar registrar) {
        registrar.registerStackProvider(new WailaStacks(), IVariantBlock.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        MovingObjectPosition hit = accessor.getPosition();
        return accessor.getBlock()
            .getPickBlock(hit, accessor.getWorld(), hit.blockX, hit.blockY, hit.blockZ);
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        return tag;
    }
}
