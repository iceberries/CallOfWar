package com.ice_berry.callofwar.banner;

import com.ice_berry.callofwar.CallOfWar;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端事件处理器
 * 注册战旗渲染器和模型
 */
@EventBusSubscriber(modid = CallOfWar.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BannerClientEvents {

    /**
     * 注册模型层定义
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BannerModelLayers.BANNER, BannerBlockEntityRenderer::createBodyLayer);
    }

    /**
     * 注册方块实体渲染器
     */
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 为所有注册的战旗类型注册渲染器
        for (var holder : BannerRegistrar.BLOCK_ENTITIES.getEntries()) {
            event.registerBlockEntityRenderer(
                (net.minecraft.world.level.block.entity.BlockEntityType<BannerBlockEntity>) holder.get(),
                BannerBlockEntityRenderer::new
            );
        }
    }
}
