package com.ice_berry.callofwar.datagen;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.BannerType;
import com.ice_berry.callofwar.banner.BannerTypeRegistry;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BannerItemModelProvider extends ItemModelProvider {

    public BannerItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CallOfWar.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 遍历所有注册的战旗类型
        for (BannerType bannerType : BannerTypeRegistry.getInstance().getAll()) {
            String name = bannerType.getId().getPath();
            
            // 物品模型继承对应的方块模型
            withExistingParent(name, modLoc("block/" + name));
        }
    }
}
