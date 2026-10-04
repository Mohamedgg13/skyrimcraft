package com.skyrimcraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

/** يقرأ الـ heightmap وخريطة البيومات من داخل المود ويحوّلها لحجم العالم (5000x5000 بلوك). */
public final class SkyrimMap {
    public static final int WORLD_SIZE = 5000;   // بلوك
    public static final int SEA_LEVEL = 63;
    public static final int MIN_Y = 64 - 1;      // أقل ارتفاع للأرض
    public static final int MAX_HEIGHT = 200;    // أعلى قمة جبل

    public enum Biome { OCEAN, BEACH, PLAINS, FOREST, TUNDRA, MOUNTAIN, SNOW }

    private final Raster height;
    private final BufferedImage biomes;
    private final int hw, hh, bw, bh;

    public SkyrimMap(String heightPath, String biomePath) throws IOException {
        BufferedImage hImg = read(heightPath);
        this.height = hImg.getRaster();
        this.hw = hImg.getWidth();
        this.hh = hImg.getHeight();
        this.biomes = read(biomePath);
        this.bw = biomes.getWidth();
        this.bh = biomes.getHeight();
    }

    private static BufferedImage read(String path) throws IOException {
        try (InputStream in = SkyrimMap.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing resource: " + path);
            return ImageIO.read(in);
        }
    }

    /** x و z إحداثيات العالم، (0,0) هو منتصف الخريطة. */
    public boolean inside(int x, int z) {
        int half = WORLD_SIZE / 2;
        return x >= -half && x < half && z >= -half && z < half;
    }

    private double u(int worldCoord) { return (worldCoord + WORLD_SIZE / 2.0) / WORLD_SIZE; }

    /** ارتفاع سطح الأرض بالبلوكات (Y) عند هذه النقطة، مع interpolation ناعم. */
    public int surfaceY(int x, int z) {
        if (!inside(x, z)) return SEA_LEVEL - 20;
        double fx = Math.min(Math.max(u(x) * (hw - 1), 0), hw - 1);
        double fz = Math.min(Math.max(u(z) * (hh - 1), 0), hh - 1);
        int x0 = (int) fx, z0 = (int) fz;
        int x1 = Math.min(x0 + 1, hw - 1), z1 = Math.min(z0 + 1, hh - 1);
        double tx = fx - x0, tz = fz - z0;
        double a = sample(x0, z0) * (1 - tx) + sample(x1, z0) * tx;
        double b = sample(x0, z1) * (1 - tx) + sample(x1, z1) * tx;
        double v = a * (1 - tz) + b * tz; // 0..1
        double sea = 0.20;
        if (v < sea) { // تحت الماء: قاع المحيط
            return (int) Math.round(SEA_LEVEL - 25 + (v / sea) * 20);
        }
        double land = (v - sea) / (1 - sea);
        return (int) Math.round(SEA_LEVEL + 1 + land * (MAX_HEIGHT - SEA_LEVEL - 1));
    }

    private double sample(int px, int pz) {
        return height.getSample(px, pz, 0) / 65535.0;
    }

    public Biome biomeAt(int x, int z) {
        if (!inside(x, z)) return Biome.OCEAN;
        int px = Math.min((int) (u(x) * bw), bw - 1);
        int pz = Math.min((int) (u(z) * bh), bh - 1);
        int rgb = biomes.getRGB(px, pz) & 0xFFFFFF;
        return switch (rgb) {
            case 0x1E50A0 -> Biome.OCEAN;
            case 0xE6D796 -> Biome.BEACH;
            case 0x6EAA50 -> Biome.PLAINS;
            case 0x286E3C -> Biome.FOREST;
            case 0x96A096 -> Biome.TUNDRA;
            case 0x828282 -> Biome.MOUNTAIN;
            case 0xFAFAFA -> Biome.SNOW;
            default -> Biome.PLAINS;
        };
    }
}
