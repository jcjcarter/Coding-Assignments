package strategies.paint;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import balls.Ball;

public class AnimatePaintStrategy extends APaintStrategy {

	/**
	 * Constructor for Animate strategy that calls a super
	 * on an AffineTransform object
	 * @param a
	 */
	public AnimatePaintStrategy(AffineTransform a) {
		super(a);
	}

	/* (non-Javadoc)
	 * @see strategies.paint.APaintStrategy#paintXfrm(java.awt.Graphics, balls.Ball, java.awt.geom.AffineTransform)
	 */
	@Override
	public void paintXfrm(Graphics g, Ball host, AffineTransform af) {

	}

}
