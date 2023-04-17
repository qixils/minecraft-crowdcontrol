package dev.qixils.crowdcontrol.plugin.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import static dev.qixils.crowdcontrol.common.Plugin.DEFAULT_PASSWORD;
import static dev.qixils.crowdcontrol.common.Plugin.DEFAULT_PORT;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			FabricCrowdControlPlugin plugin = FabricCrowdControlPlugin.getInstance();
			plugin.loadConfig();
			ConfigBuilder builder = ConfigBuilder.create()
					// I wish I could hide the search bar
					.setParentScreen(parent)
					.setTitle(Text.translatable("config.crowdcontrol.title"))
					.setSavingRunnable(plugin::saveConfig);
			ConfigCategory category = builder.getOrCreateCategory(Text.translatable("config.crowdcontrol.category.general"));
			ConfigEntryBuilder entryBuilder = builder.entryBuilder();
			category.addEntry(entryBuilder.startStrField(Text.translatable("config.crowdcontrol.password.name"), plugin.getPasswordOrEmpty())
					.setDefaultValue(DEFAULT_PASSWORD)
					.setTooltip(Text.translatable("config.crowdcontrol.password.description"))
					.setSaveConsumer(plugin::setPassword)
					.build());
			category.addEntry(entryBuilder.startIntField(Text.translatable("config.crowdcontrol.port.name"), plugin.getPort())
					.setDefaultValue(DEFAULT_PORT)
					.setMin(0) // 0 is treated as a random port
					.setMax(65535)
					.setTooltip(Text.translatable("config.crowdcontrol.port.description"))
					.setSaveConsumer(plugin::setPort)
					.build());
			category.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.crowdcontrol.announce.name"), plugin.announceEffects())
					.setDefaultValue(true)
					.setTooltip(Text.translatable("config.crowdcontrol.announce.description"))
					.setSaveConsumer(plugin::setAnnounceEffects)
					.build());
			category.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.crowdcontrol.hide_names.name"), HideNames.class, plugin.getHideNames())
					.setDefaultValue(HideNames.NONE)
					.setTooltip(Text.translatable("config.crowdcontrol.hide_names.description"))
					.setSaveConsumer(plugin::setHideNames)
					.setEnumNameProvider(enumValue -> Text.translatable("config.crowdcontrol.hide_names.option." + ((HideNames) enumValue).getConfigCode()))
					.build());
			category.addEntry(entryBuilder.startTextDescription(Text.translatable("config.crowdcontrol.advanced_settings")).build());
			// TODO: add entity & item limits
			return builder.build();
		};
	}
}
