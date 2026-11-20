package com.gtnewhorizons.decorativehorizons.rails;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.decorativehorizons.core.Footsteps;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/** The powered rail, or the activator rail when {@code activator} is set. */
public class BlockSturdyPoweredRail extends BlockRailPowered implements ISturdyRail {

    private final boolean activator;

    public BlockSturdyPoweredRail(boolean activator) {
        this.activator = activator;
        Rails.setup(this, activator ? "sturdy_activator_rail" : "sturdy_powered_rail");
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return Rails.canPlaceAt(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        int meta = world.getBlockMetadata(x, y, z);
        func_150048_a(world, x, y, z, meta, meta & 7, neighbor);
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
    public ForgeDirection[] getValidRotations(World world, int x, int y, int z) {
        return new ForgeDirection[0];
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        return false;
    }

    /**
     * Boosts, brakes or activates the cart. {@code EntityMinecart} does this only on {@code Blocks.golden_rail} and
     * {@code Blocks.activator_rail}.
     */
    @Override
    public void onMinecartPass(World world, EntityMinecart cart, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = (meta & 8) != 0;
        if (activator) {
            cart.onActivatorRailPass(x, y, z, powered);
            return;
        }
        double speed = Math.sqrt(cart.motionX * cart.motionX + cart.motionZ * cart.motionZ);
        if (!powered) {
            // a rider pushing a cart slower than 0.1 adds 0.1 to the speed, and vanilla then skips the brake
            if (cart.riddenByEntity instanceof EntityLivingBase rider && rider.moveForward > 0 && speed < 0.2) return;
            double keep = speed < 0.03 ? 0 : 0.5;
            cart.motionX *= keep;
            cart.motionY = 0;
            cart.motionZ *= keep;
        } else if (speed > 0.01) {
            cart.motionX += cart.motionX / speed * 0.06;
            cart.motionZ += cart.motionZ / speed * 0.06;
        } else if ((meta & 7) == 1) {
            if (world.getBlock(x - 1, y, z)
                .isNormalCube()) cart.motionX = 0.02;
            else if (world.getBlock(x + 1, y, z)
                .isNormalCube()) cart.motionX = -0.02;
        } else if ((meta & 7) == 0) {
            if (world.getBlock(x, y, z - 1)
                .isNormalCube()) cart.motionZ = 0.02;
            else if (world.getBlock(x, y, z + 1)
                .isNormalCube()) cart.motionZ = -0.02;
        }
    }
}
