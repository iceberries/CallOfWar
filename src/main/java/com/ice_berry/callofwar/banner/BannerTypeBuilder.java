package com.ice_berry.callofwar.banner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ice_berry.callofwar.CallOfWar;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 战旗类型构建器
 * 使用延迟注入避免循环依赖
 */
public class BannerTypeBuilder {

    private final ResourceLocation id;
    private String name = "";
    private double radius = 10.0;
    private int checkInterval = 20;
    private boolean teamRestricted = false;
    private DyeColor color = DyeColor.WHITE;  // 默认白色
    private final List<MobEffectInstance> effects = new ArrayList<>();

    private DeferredRegister.Blocks blocksRegister;
    private DeferredRegister.Items itemsRegister;
    private DeferredRegister<BlockEntityType<?>> blockEntitiesRegister;

    private BannerTypeBuilder(ResourceLocation id) {
        this.id = id;
    }

    public static BannerTypeBuilder create(ResourceLocation id) {
        return new BannerTypeBuilder(id);
    }

    public BannerTypeBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BannerTypeBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    public BannerTypeBuilder checkInterval(int interval) {
        this.checkInterval = interval;
        return this;
    }

    public BannerTypeBuilder teamRestricted(boolean restricted) {
        this.teamRestricted = restricted;
        return this;
    }
    
    /**
     * 设置旗帜颜色
     */
    public BannerTypeBuilder color(DyeColor color) {
        this.color = color;
        return this;
    }

    public BannerTypeBuilder addEffect(net.minecraft.core.Holder<MobEffect> effect, int duration, int amplifier) {
        this.effects.add(new MobEffectInstance(effect, duration, amplifier));
        return this;
    }

    public BannerTypeBuilder addEffect(MobEffectInstance effectInstance) {
        this.effects.add(effectInstance);
        return this;
    }

    public BannerTypeBuilder effects(List<MobEffectInstance> effects) {
        this.effects.clear();
        this.effects.addAll(effects);
        return this;
    }

    public BannerTypeBuilder registers(DeferredRegister.Blocks blocks, 
                                        DeferredRegister.Items items,
                                        DeferredRegister<BlockEntityType<?>> blockEntities) {
        this.blocksRegister = blocks;
        this.itemsRegister = items;
        this.blockEntitiesRegister = blockEntities;
        return this;
    }

    /**
     * 构建并注册战旗类型
     */
    public BannerType buildAndRegister() {
        if (effects.isEmpty()) {
            throw new IllegalStateException("BannerType must have at least one effect: " + id);
        }
        if (blocksRegister == null || itemsRegister == null || blockEntitiesRegister == null) {
            throw new IllegalStateException("Registers must be set before building: " + id);
        }

        String path = id.getPath();
        final List<MobEffectInstance> effectCopy = List.copyOf(effects);
        final double finalRadius = radius;
        final int finalCheckInterval = checkInterval;
        final boolean finalTeamRestricted = teamRestricted;
        final String finalName = name;
        final DyeColor finalColor = color;

        // 创建行为
        IBannerBehavior behavior = createBehavior(effectCopy, finalRadius, finalCheckInterval, finalTeamRestricted);

        // 创建 BannerType
        final ValueRef<BannerType> bannerTypeRef = new ValueRef<>();
        bannerTypeRef.value = BannerType.builder(id)
            .name(finalName)
            .behavior(behavior)
            .teamRestricted(finalTeamRestricted)
            .checkInterval(finalCheckInterval)
            .build();

        // 使用 ValueRef 包装器来延迟获取引用
        final ValueRef<DeferredHolder<BlockEntityType<?>, BlockEntityType<BannerBlockEntity>>> beHolderRef = new ValueRef<>();
        final ValueRef<BannerBlock> blockRef = new ValueRef<>();

        // 1. 注册方块 - 使用 BannerBlock（站立式旗帜）
        DeferredHolder<net.minecraft.world.level.block.Block, BannerBlock> blockHolder = 
            blocksRegister.register(path, () -> {
                BannerBlock block = new BannerBlock(bannerTypeRef.value, finalColor,
                    BlockBehaviour.Properties.of()
                        .strength(1.0f, 3.0f));
                blockRef.value = block;
                return block;
            });

        // 2. 注册 BlockEntity
        DeferredHolder<BlockEntityType<?>, BlockEntityType<BannerBlockEntity>> beHolder = 
            blockEntitiesRegister.register(path, () -> {
                BannerBlock block = blockRef.value;
                return BlockEntityType.Builder.of(
                    (pos, state) -> new BannerBlockEntity(beHolderRef.value != null ? beHolderRef.value.get() : null, pos, state),
                    block != null ? block : blockHolder.get()
                ).build(null);
            });
        beHolderRef.value = beHolder;

        // 3. 注册物品 - 使用自定义 BannerItem 支持渲染器
        itemsRegister.register(path, () -> new BannerItem(
            blockHolder.get(), 
            bannerTypeRef.value, 
            finalColor,
            new net.minecraft.world.item.Item.Properties()
        ));

        // 更新 BannerType 引用
        bannerTypeRef.value = BannerType.builder(id)
            .name(finalName)
            .behavior(behavior)
            .block(() -> blockHolder.get())
            .blockEntity(() -> beHolder.get())
            .teamRestricted(finalTeamRestricted)
            .checkInterval(finalCheckInterval)
            .build();

        BannerTypeRegistry.getInstance().register(bannerTypeRef.value);

        CallOfWar.LOGGER.info("Built and registered BannerType: {} with {} effects", id, effects.size());

        return bannerTypeRef.value;
    }

    private IBannerBehavior createBehavior(List<MobEffectInstance> effectCopy, 
                                           double radius, int checkInterval, boolean teamRestricted) {
        return new IBannerBehavior() {
            @Override
            public double getRadius() {
                return radius;
            }

            @Override
            public List<MobEffectInstance> getEffects() {
                return effectCopy;
            }

            @Override
            public boolean shouldAffectEntity(net.minecraft.world.level.Level level, 
                                               net.minecraft.core.BlockPos pos, 
                                               net.minecraft.world.entity.LivingEntity entity, 
                                               BannerType bannerType) {
                if (bannerType.isTeamRestricted()) {
                    return TeamHelper.isSameTeam(level, entity, null);
                }
                return true;
            }

            @Override
            public int getCheckInterval() {
                return checkInterval;
            }

            @Override
            public boolean isTeamRestricted() {
                return teamRestricted;
            }
        };
    }

    public String getName() {
        return name;
    }

    public double getRadius() {
        return radius;
    }

    public List<MobEffectInstance> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    /**
     * 简单的值引用包装器
     */
    private static class ValueRef<T> {
        T value;
    }
}
