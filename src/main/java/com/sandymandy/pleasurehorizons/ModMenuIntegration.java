package com.sandymandy.pleasurehorizons;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

import net.neoforged.api.distmarker.OnlyIn;
import com.sandymandy.pleasurehorizons.config.ModConfig;

@OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
