package com.ice_berry.callofwar.datagen;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.BannerType;
import com.ice_berry.callofwar.banner.BannerTypeRegistry;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BannerBlockStateProvider extends BlockStateProvider {

    public BannerBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CallOfWar.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // 遍历所有注册的战旗类型
        for (BannerType bannerType : BannerTypeRegistry.getInstance().getAll()) {
            generateBannerBlockState(bannerType);
        }
    }

    private void generateBannerBlockState(BannerType bannerType) {
        var block = bannerType.getBlockSupplier().get();
        String name = bannerType.getId().getPath();
        
        // 创建方块模型（继承原版旗帜模板）
        ModelFile bannerModel = models().withExistingParent(name, "minecraft:block/template_banner")
            .texture("particle", "minecraft:block/white_wool");
        
        // 生成 16 个旋转状态的 variants
        getVariantBuilder(block).forAllStates(state -> {
            int rotation = state.getValue(BlockStateProperties.ROTATION_16);
            // ROTATION_16 的值是 0-15，对应 22.5 度的增量
            int yRot = rotation * 22;
            
            return ConfiguredModel.builder()
                .modelFile(bannerModel)
                .rotationY(yRot)
                .build();
        });
    }
}
