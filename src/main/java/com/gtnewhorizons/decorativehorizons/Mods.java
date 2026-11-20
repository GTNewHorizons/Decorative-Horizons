package com.gtnewhorizons.decorativehorizons;

import java.util.Locale;

import com.gtnewhorizon.gtnhlib.util.data.IMod;

import cpw.mods.fml.common.Loader;

/** Same pattern as GT5-Unofficial's gregtech.api.enums.Mods. */
public enum Mods implements IMod {

    GregTech("gregtech");

    public final String id;
    public final String resourceDomain;
    private boolean checked, modLoaded;

    Mods(String id) {
        this.id = id;
        this.resourceDomain = id.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public final boolean isModLoaded() {
        if (!this.checked) {
            this.modLoaded = Loader.isModLoaded(id);
            this.checked = true;
        }
        return this.modLoaded;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getResourceLocation() {
        return resourceDomain;
    }
}
