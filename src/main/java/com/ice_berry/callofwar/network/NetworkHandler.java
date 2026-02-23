package com.ice_berry.callofwar.network;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.gui.BannerMenu;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络包处理器
 */
@EventBusSubscriber(modid = CallOfWar.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CallOfWar.MODID);

        // 注册战旗配置同步包（客户端 -> 服务端）
        registrar.playToServer(
            BannerConfigPayload.TYPE,
            BannerConfigPayload.STREAM_CODEC,
            BannerConfigPayload::handleServer
        );
        
        LOGGER.info("Network packets registered for {}", CallOfWar.MODID);
    }
}
