package dev.alexallocated.autosell;

public final class AutoSellStateMachine {
	public enum Phase {
		DISABLED,
		MONITORING,
		WAITING_FOR_SCREEN,
		TRANSFERRING,
		CLOSE_DELAY
	}

	public enum Action {
		NONE,
		SEND_SELL_COMMAND,
		BEGIN_TRANSFER,
		MOVE_NEXT_SLOT,
		CLOSE_SCREEN,
		SCREEN_TIMEOUT,
		TRANSFER_INTERRUPTED
	}

	public record Input(
			boolean inWorld,
			boolean normalGameplay,
			boolean inventoryFull,
			boolean matchingSellScreen,
			boolean hasQueuedSlot,
			boolean transferSucceeded
	) {
	}

	private boolean enabled;
	private boolean armed;
	private Phase phase = Phase.DISABLED;
	private int ticksRemaining;

	public boolean toggle() {
		enabled = !enabled;
		armed = enabled;
		phase = enabled ? Phase.MONITORING : Phase.DISABLED;
		ticksRemaining = 0;
		return enabled;
	}

	public void reset() {
		enabled = false;
		armed = false;
		phase = Phase.DISABLED;
		ticksRemaining = 0;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Phase phase() {
		return phase;
	}

	public Action tick(Input input, AutoSellSettings settings) {
		if (!enabled) {
			return Action.NONE;
		}

		if (!input.inWorld()) {
			reset();
			return Action.NONE;
		}

		return switch (phase) {
			case DISABLED -> Action.NONE;
			case MONITORING -> monitor(input, settings);
			case WAITING_FOR_SCREEN -> waitForScreen(input);
			case TRANSFERRING -> transfer(input, settings);
			case CLOSE_DELAY -> waitToClose(input);
		};
	}

	private Action monitor(Input input, AutoSellSettings settings) {
		if (!input.inventoryFull()) {
			armed = true;
			return Action.NONE;
		}

		if (armed && input.normalGameplay()) {
			armed = false;
			phase = Phase.WAITING_FOR_SCREEN;
			ticksRemaining = settings.screenTimeoutTicks();
			return Action.SEND_SELL_COMMAND;
		}

		return Action.NONE;
	}

	private Action waitForScreen(Input input) {
		if (input.matchingSellScreen()) {
			phase = Phase.TRANSFERRING;
			ticksRemaining = 0;
			return Action.BEGIN_TRANSFER;
		}

		ticksRemaining--;
		if (ticksRemaining <= 0) {
			phase = Phase.MONITORING;
			return Action.SCREEN_TIMEOUT;
		}

		return Action.NONE;
	}

	private Action transfer(Input input, AutoSellSettings settings) {
		if (!input.matchingSellScreen()) {
			armed = input.transferSucceeded();
			phase = Phase.MONITORING;
			return Action.TRANSFER_INTERRUPTED;
		}

		if (ticksRemaining > 0) {
			ticksRemaining--;
			return Action.NONE;
		}

		if (input.hasQueuedSlot()) {
			ticksRemaining = settings.clickDelayTicks() - 1;
			return Action.MOVE_NEXT_SLOT;
		}

		phase = Phase.CLOSE_DELAY;
		ticksRemaining = settings.closeDelayTicks();
		return Action.NONE;
	}

	private Action waitToClose(Input input) {
		if (!input.matchingSellScreen()) {
			armed = input.transferSucceeded();
			phase = Phase.MONITORING;
			return Action.TRANSFER_INTERRUPTED;
		}

		ticksRemaining--;
		if (ticksRemaining <= 0) {
			armed = input.transferSucceeded();
			phase = Phase.MONITORING;
			return Action.CLOSE_SCREEN;
		}

		return Action.NONE;
	}
}
