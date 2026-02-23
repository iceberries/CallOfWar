package com.ice_berry.callofwar.banner;

import javax.annotation.Nullable;

import com.ice_berry.callofwar.banner.gui.BannerMenuProvider;
import com.ice_berry.callofwar.banner.gui.BannerMenu;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
     * 记录放置者（用于团队检查）
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BannerBlockEntity bannerEntity) {
                bannerEntity.setPlacer(placer);
            }
        }
    }

    /**
     * Shift + 右键打开配置 GUI（仅限放置者）
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, 
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BannerBlockEntity bannerEntity) {
                // 只有放置者本人才能打开GUI
                if (!bannerEntity.isPlacer(player)) {
                    return InteractionResult.FAIL;
                }
                // 打开配置 GUI，同步数据到客户端
                player.openMenu(new BannerMenuProvider(bannerEntity), buf -> {
                    BannerMenu.writeData(buf, bannerEntity);
                });
                return InteractionResult.SUCCESS;
            }
        }
        
        // 非 shift 或客户端，使用默认行为
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

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
