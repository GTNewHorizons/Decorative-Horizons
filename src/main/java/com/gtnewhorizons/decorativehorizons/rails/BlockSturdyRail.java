package com.gtnewhorizons.decorativehorizons.rails;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.decorativehorizons.core.Footsteps;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** A rail that stays in place with no block underneath. */
public class BlockSturdyRail extends BlockRail implements ISturdyRail {

    public BlockSturdyRail() {
        Rails.setup(this, "sturdy_rail");
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return Rails.canPlaceAt(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        int meta = world.getBlockMetadata(x, y, z);
        func_150048_a(world, x, y, z, meta, meta, neighbor);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> list,
        Entity entity) {
        if (entity instanceof EntityMinecart) return;
        Rails.addWalkway(list, mask, x, y, z);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        super.onEntityCollidedWithBlock(world, x, y, z, entity);
        Footsteps.onEntityInside(y, entity, this);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        return activateWrench(world, x, y, z, WrenchClick.of(player, side, Vec3.createVectorHelper(hitX, hitY, hitZ)));
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return "pickaxe".equals(type) || super.isToolEffective(type, metadata);
    }

    @Override
    public ForgeDirection[] getValidRotations(World world, int x, int y, int z) {
        return new ForgeDirection[0];
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        return false;
    }
}
