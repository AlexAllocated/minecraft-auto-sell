package dev.alexallocated.autosell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class AutoSellConfigStore {
	private static final Logger LOGGER = LoggerFactory.getLogger("autosell");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autosell.json");

	private AutoSellConfigStore() {
	}

	static AutoSellSettings load() {
		AutoSellSettings settings = AutoSellSettings.defaults();

		if (Files.exists(CONFIG_PATH)) {
			try {
				ConfigData data = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigData.class);
				if (data != null) {
					settings = AutoSellSettings.sanitized(
							data.windowTitle,
							data.screenTimeoutTicks,
							data.clickDelayTicks,
							data.closeDelayTicks
					);
				}
			} catch (IOException | JsonParseException exception) {
				LOGGER.warn("Could not read {}; using safe defaults", CONFIG_PATH, exception);
			}
		}

		save(settings);
		return settings;
	}

	private static void save(AutoSellSettings settings) {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(ConfigData.from(settings)));
		} catch (IOException exception) {
			LOGGER.warn("Could not write {}", CONFIG_PATH, exception);
		}
	}

	private static final class ConfigData {
		private String windowTitle = AutoSellSettings.DEFAULT_WINDOW_TITLE;
		private int screenTimeoutTicks = AutoSellSettings.DEFAULT_SCREEN_TIMEOUT_TICKS;
		private int clickDelayTicks = AutoSellSettings.DEFAULT_CLICK_DELAY_TICKS;
		private int closeDelayTicks = AutoSellSettings.DEFAULT_CLOSE_DELAY_TICKS;

		private static ConfigData from(AutoSellSettings settings) {
			ConfigData data = new ConfigData();
			data.windowTitle = settings.windowTitle();
			data.screenTimeoutTicks = settings.screenTimeoutTicks();
			data.clickDelayTicks = settings.clickDelayTicks();
			data.closeDelayTicks = settings.closeDelayTicks();
			return data;
		}
	}
}
