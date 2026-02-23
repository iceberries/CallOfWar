package com.ice_berry.callofwar.network;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.BannerBlockEntity;
import com.ice_berry.callofwar.banner.team.TargetFilterMode;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 战旗配置同步网络包
 * 客户端 -> 服务端：同步筛选模式配置
 */
public record BannerConfigPayload(BlockPos pos, String filterModeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BannerConfigPayload> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CallOfWar.MODID, "banner_config"));

    public static final StreamCodec<FriendlyByteBuf, BannerConfigPayload> STREAM_CODEC = 
        StreamCodec.of(
            BannerConfigPayload::encode,
            BannerConfigPayload::decode
        );

    private static void encode(FriendlyByteBuf buf, BannerConfigPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeUtf(payload.filterModeId);
    }

    private static BannerConfigPayload decode(FriendlyByteBuf buf) {
        return new BannerConfigPayload(buf.readBlockPos(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理
     */
    public static void handleServer(BannerConfigPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        
        context.enqueueWork(() -> {
            // 从服务端世界获取 BlockEntity（不信任客户端数据）
            if (player.level() instanceof ServerLevel serverLevel) {
                BlockEntity be = serverLevel.getBlockEntity(payload.pos());
                if (be instanceof BannerBlockEntity blockEntity) {
                    // 验证是否是放置者
                    if (blockEntity.isPlacer(player)) {
                        TargetFilterMode mode = TargetFilterMode.fromId(payload.filterModeId());
                        blockEntity.setFilterMode(mode);
                        CallOfWar.LOGGER.info("Banner at {} filter mode changed to {} by {}", 
                            payload.pos(), mode, player.getName().getString());
                    } else {
                        CallOfWar.LOGGER.warn("Player {} tried to modify banner at {} but is not the placer", 
                            player.getName().getString(), payload.pos());
                    }
                }
            }
        });
    }
}
