package com.ice_berry.callofwar.banner.forge;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 战旗工坊注册器
 */
public class BannerForgeRegistrar {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CallOfWar.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CallOfWar.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CallOfWar.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = 
        DeferredRegister.create(Registries.MENU, CallOfWar.MODID);

    // 注册方块
    public static final DeferredHolder<Block, BannerForgeBlock> BANNER_FORGE_BLOCK = 
        BLOCKS.register("banner_forge", () -> new BannerForgeBlock(
            Block.Properties.of()
                .strength(3.5f, 5.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()  // 非正常方块模型，允许透明渲染
        ));

    // 注册物品
    public static final DeferredHolder<Item, BlockItem> BANNER_FORGE_ITEM = 
        ITEMS.register("banner_forge", () -> new BlockItem(
            BANNER_FORGE_BLOCK.get(), 
            new Item.Properties()
        ));

    // 注册方块实体类型
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BannerForgeBlockEntity>> BLOCK_ENTITY_TYPE = 
        BLOCK_ENTITIES.register("banner_forge", () -> BlockEntityType.Builder.of(
            BannerForgeBlockEntity::new, 
            BANNER_FORGE_BLOCK.get()
        ).build(null));

    // 注册菜单类型
    public static final DeferredHolder<MenuType<?>, MenuType<BannerForgeMenu>> MENU_TYPE = 
        MENUS.register("banner_forge", () -> IMenuTypeExtension.create(BannerForgeMenu::new));

    /**
     * 注册到事件总线
     */
    public static void registerToEventBus(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        
        CallOfWar.LOGGER.info("BannerForge registered successfully");
    }
}
