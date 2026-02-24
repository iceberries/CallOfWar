package com.ice_berry.callofwar.banner.forge;

import javax.annotation.Nullable;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 战旗工坊菜单
 * - 左侧槽：放置空白战旗/已铭刻战旗
 * - 中间预览区：实时渲染旗帜外观与当前属性
 * - 右侧材料槽：根据升级类型放置对应材料
 * - 底部输出：消耗材料后取出修改后的战旗
 */
public class BannerForgeMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int MATERIAL_SLOT_1 = 1;
    public static final int MATERIAL_SLOT_2 = 2;
    public static final int MATERIAL_SLOT_3 = 3;
    public static final int PATTERN_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    
    private final BannerForgeBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public BannerForgeMenu(int containerId, Inventory playerInventory, BannerForgeBlockEntity blockEntity) {
        super(BannerForgeRegistrar.MENU_TYPE.get(), containerId);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity.getBlockPos();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockPos);

        // 输入槽（左侧）
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), INPUT_SLOT, 26, 35));
        
        // 材料槽（右侧）
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), MATERIAL_SLOT_1, 134, 17));
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), MATERIAL_SLOT_2, 152, 35));
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), MATERIAL_SLOT_3, 134, 53));
        
        // 图案槽（中间下方）
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), PATTERN_SLOT, 80, 63));
        
        // 输出槽（底部）
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), OUTPUT_SLOT, 80, 107) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;  // 输出槽不能放入物品
            }
        });

        // 玩家背包
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 142 + row * 18));
            }
        }

        // 玩家快捷栏
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 200));
        }
    }

    /**
     * 客户端构造函数
     */
    public BannerForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data));
    }

    @Nullable
    private static BannerForgeBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof BannerForgeBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    public BannerForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BannerForgeRegistrar.BANNER_FORGE_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            if (slotIndex == OUTPUT_SLOT) {
                // 从输出槽 Shift 点击
                if (!this.moveItemStackTo(slotStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (slotIndex >= 6) {
                // 从玩家背包 Shift 点击到工坊槽
                // 先尝试输入槽，再尝试材料槽
                if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(slotStack, PATTERN_SLOT, PATTERN_SLOT + 1, false)) {
                        if (!this.moveItemStackTo(slotStack, MATERIAL_SLOT_1, MATERIAL_SLOT_3 + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                // 从工坊槽 Shift 点击到玩家背包
                if (!this.moveItemStackTo(slotStack, 6, 42, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            // 返回输入槽物品给玩家
            if (blockEntity != null) {
                for (int i = 0; i <= PATTERN_SLOT; i++) {
                    ItemStack stack = blockEntity.getItemHandler().extractItem(i, 64, false);
                    if (!stack.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(stack);
                    }
                }
            }
        });
    }
}
