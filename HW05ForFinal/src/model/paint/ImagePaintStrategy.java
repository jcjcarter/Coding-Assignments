package model.paint;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import model.ball.Ball;

/**
 * Strategy for painting an image from a file.
 * 
 * @author lss4, yh20, jfe2
 *
 */
public class ImagePaintStrategy extends APaintStrategy {

	/**
	 * The image to be painted.
	 */
	Image image;
	/**
	 * The bounds for the image to remain inside of
	 */
	Component imageObs;
	/**
	 * Scaling for the image to display properly
	 */
	Double scaleFactor;
	/**
	 * Filling factor for the image fill.
	 */
	Double fillFactor = .5;
	/**
	 * AffineTransform to properly scale and draw the image.
	 */
	public static AffineTransform localAT = new AffineTransform();

	/**
	 * Constructs an ImagePainStrategy using the input image file.
	 * 
	 * @param filename
	 *            path to the image to use
	 */
	public ImagePaintStrategy(String filename) {
		super(new AffineTransform());
		try {
			image = Toolkit.getDefaultToolkit().getImage(
					this.getClass().getResource(filename));
		} catch (Exception e) {
			System.err.println("ImagePaintStrategy: Error reading file: "
					+ filename + "\n" + e);
		}
	}

	/**
	 * Load the image as part of the intialization of the ball.
	 */
	@Override
	public void init(Ball host) {
		imageObs = host.getCanvas();
		MediaTracker mt = new MediaTracker(host.getCanvas());
		mt.addImage(image, 1);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			System.out
					.println("ImagePaintStrategy.init(): Error waiting for image.  Exception = "
							+ e);
		}

		scaleFactor = 2.0 / (fillFactor
				* (image.getWidth(imageObs) + image.getHeight(imageObs)) / 2.0);
	}

	/**
	 * Scale the image and translate appropriately before applying true
	 * transform.
	 */
	@Override
	public void paintXfrm(Graphics g, Ball host, AffineTransform at) {
		localAT.setToScale(scaleFactor, scaleFactor);
		localAT.translate(-image.getWidth(imageObs) / 2.0,
				-image.getHeight(imageObs) / 2.0);
		localAT.preConcatenate(at);
		((Graphics2D) g).drawImage(image, localAT, imageObs);
	}

	/**
	 * Change the fillFactor for the ImagePaintStrategy
	 * 
	 * @param newFillFactor
	 *            new fill ratio
	 */
	public void setFillFactor(double newFillFactor) {
		fillFactor = newFillFactor;
	}

	@Override
	public void paint(Graphics g, Ball host) {
		// TODO Auto-generated method stub
		
	}

}
