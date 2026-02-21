package com.ice_berry.callofwar.banner;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * 墙面战旗方块
 * 继承 COWAbstractBannerBlock，可放置在墙上
 */
public class WallBannerBlock extends COWAbstractBannerBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public WallBannerBlock(BannerType bannerType, DyeColor color, BlockBehaviour.Properties properties) {
        super(bannerType, color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        Direction direction = context.getClickedFace();
        if (direction.getAxis() == Direction.Axis.Y) {
            direction = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends WallBannerBlock> codec() {
        return MapCodec.unit(this);
    }
}
