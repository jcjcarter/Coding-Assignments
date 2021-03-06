package balls;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import strategies.IUpdateStrategy;

/**
 * The Ball class is a concrete class representing ball
 * that can be updated on demand and drawn using a graphics object.
 */
public class Ball implements Observer {

	/** 
	 * The color of the ball
	 */
	private Color color;
	/** 
	 * The x coordinate of the center of the ball
	 */
	private int xLoc;
	/** 
	 * The y coordinate of the center of the ball
	 */
	private int yLoc;
	/** 
	 * The x magnitude of the velocity of the ball
	 */
	private int xVel;
	/** 
	 * The y magnitude of the velocity of the ball
	 */
	private int yVel;
	/** 
	 * The radius of the ball 
	 * */
	private int radius;
	/** 
	 * The canvas on which the ball will be drawn 
	 * */
	private Component myCanvas;
	
	/**
	 * The flags showing if the ball is going to hit the wall. Init to false
	 */
	private boolean xHitwallFlag = false;
	private boolean yHitwallFlag = false;
	
	private IUpdateStrategy _strategy;
	
	/**
	 * General constructor for balls.
	 * 
	 * @param color		The color of the ball
	 * @param xLoc		The x coordinate of the center of the ball
	 * @param yLoc		The y coordinate of the center of the ball
	 * @param xVel		The magnitude of the x component of the velocity of the ball
	 * @param yVel		The magnitude of the y component of the velocity of the ball
	 * @param radius	The radius of the ball
	 * @param inCanvas	The canvas component that holds the ball
	 * @param strategy	The moving strategy for this ball.
	 */
	public Ball(Color color, int xLoc, int yLoc, int xVel, int yVel, int radius, Component inCanvas, IUpdateStrategy strategy) {
		this.color = color;
		this.xLoc = xLoc;
		this.yLoc = yLoc;
		this.xVel = xVel;
		this.yVel = yVel;
		this.radius = radius;
		this.myCanvas = inCanvas;
		this._strategy = strategy;
	}
	
	/**
	 * Gets the color of the ball.
	 * 
	 * @return color of the ball
	 */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Gets the x location of the center of the ball
	 * 
	 * @return x location of the center of the ball
	 */
	public int getXLoc() {
		return xLoc;
	}

	/**
	 * Gets the y location of the center of the ball
	 * 
	 * @return y location of the center of the ball
	 */
	public int getYLoc() {
		return yLoc;
	}
	/**
	 * Gets the magnitude of the x component of the velocity of the ball
	 * 
	 * @return magnitude of the x component of the velocity of the ball
	 */
	public int getXVel() {
		return xVel;
	}
	/**
	 * Gets the magnitude of the y component of the velocity of the ball
	 * 
	 * @return magnitude of the y component of the velocity of the ball
	 */
	public int getYVel() {
		return yVel;
	}
	/**
	 * Gets the radius of the ball
	 * 
	 * @return radius of the ball
	 */
	public int getRadius(){
		return radius;
	}
	
	/**
	 * Gets the canvas of the ball
	 *  
	 * @return canvas of the ball
	 */
	public Component getCanvas(){
		return this.myCanvas;
	}
	
	/**
	 * Sets the color of the ball
	 * 
	 * @param value - new color of the ball
	 */
	public void setColor(Color value) {
		this.color = value;
	}
	/**
	 * Sets the x location of the center of the ball
	 * 
	 * @param x - new x location of the center of the ball
	 */
	public void setXLoc(int x) {
		this.xLoc = x;
	}
	/**
	 * Sets the y location of the center of the ball
	 * 
	 * @param y - new y location of the center of the ball
	 */
	public void setYLoc(int y) {
		this.yLoc = y;
	}
	/**
	 * Sets the magnitude of the x component of the velocity of the ball
	 * 
	 * @param x - new magnitude of the x component of the velocity of the ball
	 */
	public void setXVel(int x) {
		this.xVel = x;
	}
	/**
	 * Sets the magnitude of the y component of the velocity of the ball
	 * 
	 * @param y - new magnitude of the y component of the velocity of the ball
	 */
	public void setYVel(int y) {
		this.yVel = y;
	}
	/**
	 * Sets the radius of the ball
	 * 
	 * @param r - new radius of the ball
	 */
	public void setRadius(int r) {
		this.radius = r;
	}
	
	/**
	 * Bounces the ball off of the bounds of the canvas.
	 */
	public void bouncing() {
		if(xLoc+xVel-radius < 0 || xLoc+xVel+radius > myCanvas.getWidth()){
			reverseX();
			xHitwallFlag = true;
		} 
		if (yLoc+yVel-radius < 0 || yLoc+yVel+radius > myCanvas.getHeight()){
			reverseY();
		    yHitwallFlag = true;
		}
	}
	
	/**
	 * Corrects the ball's location if it is out of bounds.
	 */
	public void correctLoc(){
		if(xLoc-radius < 0){
			xLoc = radius;
		} else if ( xLoc+radius > myCanvas.getWidth()){
			xLoc = myCanvas.getWidth()-radius;
		}
		if (yLoc-radius < 0 ){
			yLoc = radius;
		} else if (yLoc+ radius > myCanvas.getHeight()){
			yLoc = myCanvas.getHeight()-radius;
		}
	}
	
	/**
	 *  Move the ball. This method is mainly dealing with balls have their next frame off bound.
	 *  If so, correct their movement by sticking them on to the impact point on the wall for 1 frame.
	 */
	public void move(){
		
		if(xHitwallFlag && !yHitwallFlag){
			if(xLoc+(xVel*-1)-radius < 0){
				// Let it hit the wall
				this.xLoc = radius;
				this.yLoc += this.yVel * ((xLoc - radius - 0)/(xVel * -1));
			} else if (xLoc+(xVel*-1)+radius >myCanvas.getWidth()){
				// Let it hit the wall
				this.xLoc = myCanvas.getWidth()-radius;
				this.yLoc += this.yVel * ((myCanvas.getWidth() - (xLoc+radius))/(xVel * -1));
			}
			xHitwallFlag = false;
		} else if(yHitwallFlag && !xHitwallFlag){
			if(yLoc+(yVel*-1)-radius < 0){
				// Let it hit the wall
				this.yLoc = radius;
				this.xLoc += this.xVel * ((yLoc - radius - 0)/(yVel * -1));
			} else if (yLoc+(yVel*-1)+radius >myCanvas.getHeight()){
				// Let it hit the wall
				this.yLoc = myCanvas.getHeight()-radius;
				this.xLoc += this.xVel * ((myCanvas.getHeight() - (yLoc+radius))/(yVel * -1));
			}
			yHitwallFlag = false;
		} else if(xHitwallFlag && yHitwallFlag){
			// Must be at one of the four corners
			if(xVel >= 0 && yVel >= 0){
				this.xLoc = radius;
				this.yLoc = radius;
			} else if (xVel >= 0 && yVel < 0){
				this.xLoc = radius;
				this.yLoc = myCanvas.getHeight()-radius;
			} else if (xVel < 0 && yVel >= 0){
				this.xLoc = myCanvas.getWidth()-radius;
				this.yLoc = radius;
			} else if (xVel < 0 && yVel < 0){
				this.xLoc = myCanvas.getWidth()-radius;
				this.yLoc = myCanvas.getHeight()-radius;
			}
			xHitwallFlag = false;
			yHitwallFlag = false;
		} else {
			// Not hitting any walls
			this.xLoc += this.xVel;
			this.yLoc += this.yVel;	
		}
	}
	
	@Override
	/**
	 * Updates the position of the ball and
	 * draws it using the given graphics object.
	 * 
	 * @param o - the Observable holding the ABall
	 * @param arg - a Graphics object
	 */
	public void update(Observable o, Object arg) {
		
		// Update the state
		this._strategy.updateState(this);
		this.bouncing();
		
		this.move();
		
		// Correct the location and paint the ball
		this.correctLoc();
		this.paint((Graphics)arg);
	}
	
	/**
	 * Reverses the x velocity of the ball.
	 */
	public void reverseX() {
		this.xVel *= -1;
	}
	
	/**
	 * Reverses the y velocity of the ball.
	 */
	public void reverseY() {
		this.yVel *= -1;
	}
	
	/**
	 * Paints the ball using the given graphics object.
	 * 
	 * @param g  Graphics object
	 */
	public void paint(Graphics g) {
		g.setColor(color);
		g.fillOval(xLoc - radius, yLoc - radius, radius*2, radius*2);
	}
	
	/**
	 * Get the strategy of this ball.
	 * 
	 * @return Strategy of the ball
	 */
	public IUpdateStrategy getStrategy(){
		return _strategy;
	};
	
	/**
	 * Set the strategy of this ball
	 * 
	 * @param strategy  Strategy to set to this ball
	 */
	public void setStrategy(IUpdateStrategy strategy){
		this._strategy = strategy;
	}
	
}


