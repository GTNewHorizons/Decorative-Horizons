package com.gtnewhorizons.decorativehorizons.core;

import java.lang.reflect.Type;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockPropertyTrait;

/**
 * A blockstate property computed from the world, such as from the neighbors. The getter runs on chunk render threads,
 * so the getter must only read the world.
 */
public final class WorldProperty implements BlockProperty<String> {

    public interface Value {

        String get(IBlockAccess world, int x, int y, int z);
    }

    public interface Setter {

        void set(World world, int x, int y, int z, String value);
    }

    private final String name;
    private final Value value;
    private final Setter setter;

    public WorldProperty(String name, Value value) {
        this(name, value, null);
    }

    public WorldProperty(String name, Value value, Setter setter) {
        this.name = name;
        this.value = value;
        this.setter = setter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return String.class;
    }

    @Override
    public boolean hasTrait(BlockPropertyTrait trait) {
        return trait == BlockPropertyTrait.SupportsWorld
            || (trait == BlockPropertyTrait.WorldMutable && setter != null);
    }

    @Override
    public String getValue(IBlockAccess world, int x, int y, int z) {
        return value.get(world, x, y, z);
    }

    @Override
    public void setValue(World world, int x, int y, int z, String value) {
        if (setter == null) throw new UnsupportedOperationException();
        setter.set(world, x, y, z, value);
    }

    @Override
    public String stringify(String value) {
        return value;
    }

    @Override
    public String parse(String text) {
        return text;
    }
}
