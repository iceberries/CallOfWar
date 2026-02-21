package com.ice_berry.callofwar.banner;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 站立式战旗方块
 * 继承 COWAbstractBannerBlock，使用原版旗帜模型，支持16个方向旋转
 */
public class BannerBlock extends COWAbstractBannerBlock {

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public BannerBlock(BannerType bannerType, DyeColor color, BlockBehaviour.Properties properties) {
        super(bannerType, color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
        BY_COLOR.put(color, this);
    }

    @Override
    protected MapCodec<? extends BannerBlock> codec() {
        return MapCodec.unit(this);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }
    
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, 
                                     LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.canSurvive(level, currentPos)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(getBlockEntityType(), pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return bannerType.getBlockEntitySupplier().get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 与原版一致：加 180 度旋转
        return this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }
    
    /**
     * 根据颜色获取战旗方块
     */
    public static Block byColor(DyeColor color) {
        return BY_COLOR.getOrDefault(color, Blocks.WHITE_BANNER);
    }
}
