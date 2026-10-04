package com.skyrimcraft;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyrimCraft implements ModInitializer {
    public static final String MOD_ID = "skyrimcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SkyrimMap MAP;

    @Override
    public void onInitialize() {
        try {
            MAP = new SkyrimMap(
                "/heightmap_test.png",
                "/biomes_test.png");
            LOGGER.info("SkyrimCraft loaded - Fus Ro Dah! center Y={} biome={}",
                MAP.surfaceY(0, 0), MAP.biomeAt(0, 0));
        } catch (Exception e) {
            LOGGER.error("SkyrimCraft failed to load maps", e);
        }
    }
}
