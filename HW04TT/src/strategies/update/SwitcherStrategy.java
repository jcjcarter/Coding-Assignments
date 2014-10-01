package strategies.update;

import balls.Ball;

public class SwitcherStrategy implements IUpdateStrategy {

	private IUpdateStrategy _strategy;

	/**
	 * Constructor to init the strategy to StraightStrategy.
	 */
	public SwitcherStrategy() {
		_strategy = new StraightStrategy();
	}

	/**
	 * Update the state of the context ball with strategy
	 * 
	 * @param context - the ball we use
	 */
	public void updateState(Ball context) {
		_strategy.updateState(context);
	}

	/**
	 * Assign the strategy to be the SwitcherStrategy
	 * 
	 * @param newStrategy -- The strategy we want the switcher to have
	 */
	public void setStrategy(IUpdateStrategy newStrategy) {
		_strategy = newStrategy;
	}
}
