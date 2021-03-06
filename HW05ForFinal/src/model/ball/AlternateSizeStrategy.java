package model.ball;

import util.Dispatcher;

/**
 * Strategy that causes the ball to change its alternate its size whenever it enters a collision.
 * 
 * @author jonathanerickson
 *
 */
public class AlternateSizeStrategy implements IUpdateStrategy {

	private int big = 0;

	@Override
	/**
	 * Initializes the context ball by setting its interact strategy.
	 * @param context
	 * 		The ball which will have the ColorCollideStrategy
	 */
	public void init(Ball context) {
		context.setInteractStrategy(new MultiInteractStrategy(context.getInteractStrategy(),
				new IInteractStrategy() {				
					@Override
					public void interact(Ball ball, Ball target, Dispatcher disp) {
						
						if (big == 0) {
							ball.setRadius(ball.getRadius() ^ 4);
							big = 1;
						} else {
							ball.setRadius(ball.getRadius() ^ (1 / 4));
							big = 0;
						}
					}

				}));

	}


	@Override
	/**
	 * Doesn't do anything special for updating the state.
	 */
	public void updateState(Ball ball, Dispatcher dispatcher) {
		// no-op

	}

}
