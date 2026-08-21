package dev.alexallocated.autosell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoSellSettingsTest {
	@Test
	void invalidValuesFallBackToSafeDefaults() {
		AutoSellSettings settings = AutoSellSettings.sanitized("  ", 0, -1, 0);

		assertEquals(AutoSellSettings.defaults(), settings);
	}

	@Test
	void validValuesArePreserved() {
		AutoSellSettings settings = AutoSellSettings.sanitized("Verkaufen", 80, 4, 12);

		assertEquals(new AutoSellSettings("Verkaufen", 80, 4, 12), settings);
	}
}
