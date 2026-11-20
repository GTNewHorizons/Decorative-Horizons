package com.gtnewhorizons.decorativehorizons.catwalks;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.api.IBlockWithOutlineShape;
import com.gtnewhorizon.gtnhlib.api.OutlineShape;
import com.gtnewhorizons.decorativehorizons.DecorativeHorizons;
import com.gtnewhorizons.decorativehorizons.core.Boxes;
import com.gtnewhorizons.decorativehorizons.core.IVariantBlock;
import com.gtnewhorizons.decorativehorizons.core.wrench.IWrenchable;
import com.gtnewhorizons.decorativehorizons.core.wrench.WrenchClick;

/**
 * Base class of the catwalk blocks.
 * <p>
 * The metadata bits below {@code variantShift} are the block's state. The bits from {@code variantShift} up are the
 * variant, and the variant is the item damage.
 * <p>
 * Metadata changes go through {@code World.setBlock}. Forge's placement snapshot records changes made with
 * {@code setBlock} and not changes made with {@code setBlockMetadataWithNotify}. When a handler cancels the place
 * event, Forge restores the snapshot, neighboring blocks included.
 */
public abstract class BlockFrame extends Block implements IBlockWithOutlineShape, IVariantBlock, IWrenchable {

    protected static final OutlineShape FULL = OutlineShape
        .of(Collections.singletonList(Boxes.px(0, 0, 0, 16, 16, 16)));

    private final int variantShift;

    protected BlockFrame(String name, String particleTexture, int variantShift) {
        super(Material.iron);
        this.variantShift = variantShift;
        setBlockName(DecorativeHorizons.MODID + "." + name);
        setBlockTextureName(DecorativeHorizons.MODID + ":catwalks/" + particleTexture);
        setHardness(1.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public final int variantMeta(int damage) {
        return damage << variantShift;
    }

    protected final int variantBits(int meta) {
        return meta & -(1 << variantShift);
    }

    @Override
    public int damageDropped(int meta) {
        return meta >> variantShift;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int damage : variants()) list.add(new ItemStack(item, 1, damage));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        return activateWrench(world, x, y, z, WrenchClick.of(player, side, Vec3.createVectorHelper(hitX, hitY, hitZ)));
    }

    /** Minecraft calls {@code breakBlock} on a metadata change too. Call this before unlinking the neighbors. */
    protected boolean isRemoved(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) != this;
    }

    @Override
    public OutlineShape getOutlineShape(World world, int x, int y, int z) {
        if (world.isRemote && DecorativeHorizons.proxy.wantsFullBlockOutline()) return FULL;
        return getPartsShape(world, x, y, z);
    }

    protected OutlineShape getPartsShape(World world, int x, int y, int z) {
        return FULL;
    }

    @Override
    public String getHarvestTool(int meta) {
        return "wrench";
    }

    @Override
    public int getHarvestLevel(int meta) {
        return 0;
    }

    @Override
    public int getDamageValue(World world, int x, int y, int z) {
        return damageDropped(world.getBlockMetadata(x, y, z));
    }

    // For proper item handling
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x + 1 / 16.0, y, z + 1 / 16.0, x + 15 / 16.0, y + 1, z + 15 / 16.0);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
}
