package com.ice_berry.callofwar.datagen;

import com.ice_berry.callofwar.CallOfWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CallOfWar.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Datagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        // Block states and models
        generator.addProvider(event.includeClient(), 
            new BannerBlockStateProvider(packOutput, existingFileHelper));
        
        // Item models
        generator.addProvider(event.includeClient(), 
            new BannerItemModelProvider(packOutput, existingFileHelper));
        
        // Language files
        generator.addProvider(event.includeClient(), 
            new BannerLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), 
            new BannerLanguageProvider(packOutput, "zh_cn"));
    }
}
