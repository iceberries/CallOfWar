package com.ice_berry.callofwar.banner;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 战旗注册器
 * 所有战旗类型通过静态字段在类加载时注册
 */
public class BannerRegistrar {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CallOfWar.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CallOfWar.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CallOfWar.MODID);

    // 静态初始化战旗 - 在类加载时立即注册
    public static final BannerType SPEED_BANNER = registerBanner(
        "speed_banner", "Speed Banner", 16.0, 40, false,
        builder -> builder.addEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 100, 1)
    );

    public static final BannerType STRENGTH_BANNER = registerBanner(
        "strength_banner", "Strength Banner", 12.0, 40, false,
        builder -> builder.addEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 100, 0)
    );

    public static final BannerType RESISTANCE_BANNER = registerBanner(
        "resistance_banner", "Resistance Banner", 10.0, 40, true,
        builder -> builder.addEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 100, 0)
    );

    /**
     * 注册战旗类型的辅助方法
     */
    private static BannerType registerBanner(String id, String name, double radius, int checkInterval, 
                                              boolean teamRestricted, java.util.function.Consumer<BannerTypeBuilder> configurer) {
        BannerTypeBuilder builder = BannerTypeBuilder.create(ResourceLocation.fromNamespaceAndPath(CallOfWar.MODID, id))
            .name(name)
            .radius(radius)
            .checkInterval(checkInterval)
            .teamRestricted(teamRestricted)
            .registers(BLOCKS, ITEMS, BLOCK_ENTITIES);
        
        configurer.accept(builder);
        
        return builder.buildAndRegister();
    }

    /**
     * 将注册器注册到事件总线
     */
    public static void registerToEventBus(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
