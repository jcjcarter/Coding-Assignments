package strategies.paint;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import balls.Ball;

public class ADecoratorPaintStrategy extends APaintStrategy {

	public ADecoratorPaintStrategy(AffineTransform a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	/*
	 * public ADecoratorPaintStrategy( APaintStrategy decoree) {
	 * 
	 * }
	 */

	public void init(Ball host) {}

	public void paint(Graphics g, Ball host) {}

	@Override
	public void paintXfrm(Graphics g, Ball host, AffineTransform af) {

	}

}