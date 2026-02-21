package com.ice_berry.callofwar.banner;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * 战旗类型数据对象
 * 聚合行为、视觉、属性配置
 */
public class BannerType {

    private final ResourceLocation id;
    private final String name;
    private final IBannerBehavior behavior;
    private final Supplier<? extends net.minecraft.world.level.block.AbstractBannerBlock> blockSupplier;
    private final Supplier<? extends BlockEntityType<?>> blockEntitySupplier;
    private final boolean teamRestricted;
    private final int checkInterval;

    private BannerType(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.behavior = builder.behavior;
        this.blockSupplier = builder.blockSupplier;
        this.blockEntitySupplier = builder.blockEntitySupplier;
        this.teamRestricted = builder.teamRestricted;
        this.checkInterval = builder.checkInterval;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IBannerBehavior getBehavior() {
        return behavior;
    }

    @SuppressWarnings("unchecked")
    public <T extends net.minecraft.world.level.block.AbstractBannerBlock> Supplier<T> getBlockSupplier() {
        return (Supplier<T>) blockSupplier;
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntityType<?>> Supplier<T> getBlockEntitySupplier() {
        return (Supplier<T>) blockEntitySupplier;
    }

    public boolean isTeamRestricted() {
        return teamRestricted;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {
        private final ResourceLocation id;
        private String name = "";
        private IBannerBehavior behavior;
        private Supplier<? extends net.minecraft.world.level.block.AbstractBannerBlock> blockSupplier;
        private Supplier<? extends BlockEntityType<?>> blockEntitySupplier;
        private boolean teamRestricted = false;
        private int checkInterval = 20;

        public Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder behavior(IBannerBehavior behavior) {
            this.behavior = behavior;
            return this;
        }

        public Builder block(Supplier<? extends net.minecraft.world.level.block.AbstractBannerBlock> blockSupplier) {
            this.blockSupplier = blockSupplier;
            return this;
        }

        public Builder blockEntity(Supplier<? extends BlockEntityType<?>> blockEntitySupplier) {
            this.blockEntitySupplier = blockEntitySupplier;
            return this;
        }

        public Builder teamRestricted(boolean teamRestricted) {
            this.teamRestricted = teamRestricted;
            return this;
        }

        public Builder checkInterval(int checkInterval) {
            this.checkInterval = checkInterval;
            return this;
        }

        public BannerType build() {
            if (behavior == null) {
                throw new IllegalStateException("Behavior must be set for BannerType");
            }
            return new BannerType(this);
        }
    }
}
