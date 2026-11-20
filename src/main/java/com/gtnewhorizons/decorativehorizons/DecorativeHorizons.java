package com.gtnewhorizons.decorativehorizons;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import com.gtnewhorizons.decorativehorizons.catwalks.Catwalks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = DecorativeHorizons.MODID,
    name = DecorativeHorizons.NAME,
    version = Tags.VERSION,
    dependencies = "required-after:gtnhlib@[0.12.8,);required-after:endlessids",
    acceptedMinecraftVersions = "[1.7.10]")
public class DecorativeHorizons {

    public static final String MODID = "decorativehorizons";
    public static final String NAME = "Decorative Horizons";

    @SidedProxy(
        clientSide = "com.gtnewhorizons.decorativehorizons.ClientProxy",
        serverSide = "com.gtnewhorizons.decorativehorizons.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {

        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(Catwalks.CATWALK);
        }
    };

    public static <T extends Block> T register(T block, String name, Class<? extends ItemBlock> item) {
        block.setCreativeTab(TAB);
        GameRegistry.registerBlock(block, item, name);
        return block;
    }

    /** {@code texture} is a path under {@code textures/items}. */
    public static <T extends Item> T register(T item, String name, String texture) {
        item.setUnlocalizedName(MODID + "." + name)
            .setTextureName(MODID + ":" + texture)
            .setCreativeTab(TAB);
        GameRegistry.registerItem(item, name);
        return item;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
