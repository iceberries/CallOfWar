package com.ice_berry.callofwar.banner;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 简单战旗方块实现
 * 继承 COWAbstractBannerBlock，使用延迟获取BlockEntityType避免循环依赖
 */
public class SimpleBannerBlock extends COWAbstractBannerBlock {

    private Supplier<BlockEntityType<BannerBlockEntity>> blockEntityTypeSupplier;

    public SimpleBannerBlock(BannerType bannerType, DyeColor color, BlockBehaviour.Properties properties) {
        super(bannerType, color, properties);
    }

    /**
     * 设置BlockEntityType供应商（延迟注入）
     */
    public void setBlockEntityTypeSupplier(Supplier<BlockEntityType<BannerBlockEntity>> supplier) {
        this.blockEntityTypeSupplier = supplier;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (blockEntityTypeSupplier == null) {
            return new BannerBlockEntity(null, pos, state);
        }
        return new BannerBlockEntity(blockEntityTypeSupplier.get(), pos, state);
    }

    @Override
    @Nullable
    protected BlockEntityType<?> getBlockEntityType() {
        if (blockEntityTypeSupplier == null) {
            return null;
        }
        return blockEntityTypeSupplier.get();
    }

    @Override
    protected MapCodec<? extends SimpleBannerBlock> codec() {
        return MapCodec.unit(this);
    }
}
