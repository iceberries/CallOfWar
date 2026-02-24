package com.ice_berry.callofwar.banner.forge;

import javax.annotation.Nullable;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * 战旗工坊方块实体
 * 管理物品槽位和战旗铭刻/强化逻辑
 */
public class BannerForgeBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;        // 输入槽：空白战旗/已铭刻战旗
    public static final int MATERIAL_SLOT_1 = 1;   // 材料槽1
    public static final int MATERIAL_SLOT_2 = 2;   // 材料槽2
    public static final int MATERIAL_SLOT_3 = 3;   // 材料槽3
    public static final int PATTERN_SLOT = 4;      // 图案槽：旗帜图案
    public static final int OUTPUT_SLOT = 5;       // 输出槽
    
    private static final int SLOT_COUNT = 6;
    
    // 物品存储
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };
    
    // 当前铭刻效果缓存
    @Nullable
    private Holder<MobEffect> currentEtchingEffect;
    private int etchingAmplifier = 0;
    
    // GUI 标题
    private static final Component TITLE = Component.translatable("gui.callofwar.banner_forge");

    public BannerForgeBlockEntity(BlockPos pos, BlockState state) {
        super(BannerForgeRegistrar.BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    /**
     * 获取物品处理器
     */
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * 获取当前铭刻效果
     */
    @Nullable
    public Holder<MobEffect> getCurrentEtchingEffect() {
        return currentEtchingEffect;
    }

    /**
     * 获取铭刻等级
     */
    public int getEtchingAmplifier() {
        return etchingAmplifier;
    }

    /**
     * 设置铭刻效果
     */
    public void setEtchingEffect(Holder<MobEffect> effect, int amplifier) {
        this.currentEtchingEffect = effect;
        this.etchingAmplifier = amplifier;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BannerForgeMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", itemHandler.serializeNBT(registries));
        
        if (currentEtchingEffect != null) {
            tag.putString("EtchingEffect", currentEtchingEffect.unwrapKey()
                .map(key -> key.location().toString())
                .orElse(""));
            tag.putInt("EtchingAmplifier", etchingAmplifier);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Items"));
        }
        
        if (tag.contains("EtchingEffect")) {
            String effectId = tag.getString("EtchingEffect");
            // 效果加载需要在服务端世界可用时进行延迟处理
            // 这里仅保存ID，实际解析在 tick 中完成
        }
        etchingAmplifier = tag.getInt("EtchingAmplifier");
    }

    /**
     * 服务端 tick 逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BannerForgeBlockEntity blockEntity) {
        // 检查是否有输入物品
        if (blockEntity.itemHandler.getStackInSlot(INPUT_SLOT).isEmpty()) {
            return;
        }
        
        // TODO: 实现铭刻和强化逻辑
    }

    /**
     * 尝试进行铭刻
     */
    public boolean tryEtch() {
        // TODO: 实现铭刻逻辑
        return false;
    }

    /**
     * 尝试进行强化
     */
    public boolean tryReinforce() {
        // TODO: 实现强化逻辑
        return false;
    }

    /**
     * 尝试应用图案
     */
    public boolean tryApplyPattern() {
        // TODO: 实现图案应用逻辑
        return false;
    }

    /**
     * 检查输出槽是否可以接收物品
     */
    public boolean canOutput() {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty();
    }

    /**
     * 取出输出物品
     */
    public void extractOutput(Player player) {
        if (!itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            ItemStack output = itemHandler.extractItem(OUTPUT_SLOT, 64, false);
            if (!output.isEmpty()) {
                player.getInventory().placeItemBackInInventory(output);
            }
        }
    }
}
