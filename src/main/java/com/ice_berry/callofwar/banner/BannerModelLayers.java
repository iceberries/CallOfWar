package com.ice_berry.callofwar.banner;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.blockentity.BannerRenderer;

/**
 * 战旗模型层定义
 */
public class BannerModelLayers {

    public static final ModelLayerLocation BANNER = register("banner");

    private static ModelLayerLocation register(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CallOfWar.MODID, name), "main");
    }
}
