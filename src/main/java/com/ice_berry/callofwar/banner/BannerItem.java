package com.ice_berry.callofwar.banner;

import java.util.function.Consumer;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * 战旗物品
 * 支持自定义渲染器以匹配方块渲染
 */
public class BannerItem extends BlockItem {

    private final BannerType bannerType;
    private final net.minecraft.world.item.DyeColor color;

    public BannerItem(Block block, BannerType bannerType, net.minecraft.world.item.DyeColor color, Properties properties) {
        super(block, properties);
        this.bannerType = bannerType;
        this.color = color;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new BannerItemRenderer(bannerType, color));
    }
    
    public BannerType getBannerType() {
        return bannerType;
    }
    
    public net.minecraft.world.item.DyeColor getColor() {
        return color;
    }
}
