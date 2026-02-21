package com.ice_berry.callofwar.datagen;

import java.util.HashMap;
import java.util.Map;

import com.ice_berry.callofwar.CallOfWar;
import com.ice_berry.callofwar.banner.BannerType;
import com.ice_berry.callofwar.banner.BannerTypeRegistry;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BannerLanguageProvider extends LanguageProvider {

    private final String locale;
    private static final Map<String, String> EN_US = new HashMap<>();
    private static final Map<String, String> ZH_CN = new HashMap<>();

    static {
        // 英语翻译
        EN_US.put("itemGroup.callofwar", "Call of War");
        
        // 中文翻译
        ZH_CN.put("itemGroup.callofwar", "战争号召");
    }

    public BannerLanguageProvider(PackOutput output, String locale) {
        super(output, CallOfWar.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        Map<String, String> translations = "zh_cn".equals(locale) ? ZH_CN : EN_US;
        
        // 添加创意标签页
        add("itemGroup.callofwar", translations.getOrDefault("itemGroup.callofwar", "Call of War"));
        
        // 遍历所有注册的战旗类型
        for (BannerType bannerType : BannerTypeRegistry.getInstance().getAll()) {
            String name = bannerType.getId().getPath();
            String displayName = bannerType.getName();
            
            // 方块名称
            add("block." + CallOfWar.MODID + "." + name, displayName);
            
            // 物品名称（通常与方块名称相同）
            add("item." + CallOfWar.MODID + "." + name, displayName);
        }
    }
}
