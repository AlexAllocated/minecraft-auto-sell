package dev.alexallocated.autosell;

public record AutoSellSettings(
		String windowTitle,
		int screenTimeoutTicks,
		int clickDelayTicks,
		int closeDelayTicks
) {
	public static final String DEFAULT_WINDOW_TITLE = "Items verkaufen";
	public static final int DEFAULT_SCREEN_TIMEOUT_TICKS = 100;
	public static final int DEFAULT_CLICK_DELAY_TICKS = 3;
	public static final int DEFAULT_CLOSE_DELAY_TICKS = 10;

	public static AutoSellSettings defaults() {
		return new AutoSellSettings(
				DEFAULT_WINDOW_TITLE,
				DEFAULT_SCREEN_TIMEOUT_TICKS,
				DEFAULT_CLICK_DELAY_TICKS,
				DEFAULT_CLOSE_DELAY_TICKS
		);
	}

	public static AutoSellSettings sanitized(
			String windowTitle,
			int screenTimeoutTicks,
			int clickDelayTicks,
			int closeDelayTicks
	) {
		return new AutoSellSettings(
				windowTitle == null || windowTitle.isBlank() ? DEFAULT_WINDOW_TITLE : windowTitle,
				screenTimeoutTicks > 0 ? screenTimeoutTicks : DEFAULT_SCREEN_TIMEOUT_TICKS,
				clickDelayTicks > 0 ? clickDelayTicks : DEFAULT_CLICK_DELAY_TICKS,
				closeDelayTicks > 0 ? closeDelayTicks : DEFAULT_CLOSE_DELAY_TICKS
		);
	}
}
