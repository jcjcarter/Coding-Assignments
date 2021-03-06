package strategies;

import balls.Ball;

public class FastSlowStrategy implements IUpdateStrategy{
	
	/**
	 * Counts the number of frames that have occurred -
	 * used as a timer for the changing speed.
	 */
	private int frameCounter = 0;
	/**
	 * True while the speed is decreasing, 
	 * false when it is increasing instead.
	 */
	private boolean decrease = false;
	
	/**
	 * Updates the ball's state by changing its
	 * speed over a fixed interval.
	 * 
	 * @param context - The ball we use
	 */
	public void updateState(Ball context) {
		
		// Define direction of ball
		int xDir = 1;
		if (context.getXVel() < 0)
		{
			xDir = -1;
		}
		int yDir = 1;
		if (context.getYVel() < 0) {
			yDir = -1;
		}
		
		// Increment the ball's speed
		if(decrease) {
			context.setXVel(context.getXVel() - (1 * xDir));
			context.setYVel(context.getYVel() - (1 * yDir));
		}
		else {
			context.setXVel(context.getXVel() + (1 * xDir));
			context.setYVel(context.getYVel() + (1 * yDir));
		}
		
		// Increase the counter
		frameCounter += 1;
		if (frameCounter >= 40) {
			decrease = !decrease;
			frameCounter = 0;
		}
	}

}
