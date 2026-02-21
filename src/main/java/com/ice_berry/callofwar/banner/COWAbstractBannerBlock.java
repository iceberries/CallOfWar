package com.ice_berry.callofwar.banner;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 战旗方块基类
 * 继承原版 AbstractBannerBlock，添加 tick 逻辑和行为回调
 */
public abstract class COWAbstractBannerBlock extends net.minecraft.world.level.block.AbstractBannerBlock {

    protected final BannerType bannerType;

    public COWAbstractBannerBlock(BannerType bannerType, DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
        this.bannerType = bannerType;
    }

    public BannerType getBannerType() {
        return bannerType;
    }

    @Override
    protected abstract MapCodec<? extends COWAbstractBannerBlock> codec();

    @Override
    @Nullable
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    /**
     * 添加 tick 逻辑支持（原版类没有）
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        BlockEntityType<?> expectedType = getBlockEntityType();
        if (blockEntityType == expectedType) {
            return (lvl, pos, st, blockEntity) -> {
                if (blockEntity instanceof BannerBlockEntity bannerEntity) {
                    bannerEntity.tick(lvl, pos, st);
                }
            };
        }
        return null;
    }

    protected abstract BlockEntityType<?> getBlockEntityType();

    /**
     * 行为回调 - 放置时触发（原版类没有）
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            bannerType.getBehavior().onPlaced(level, pos, state);
        }
    }

    /**
     * 行为回调 - 移除时触发（原版类没有）
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            bannerType.getBehavior().onRemoved(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
