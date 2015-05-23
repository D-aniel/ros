package javax.swing.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Vector;

import javax.swing.JComponent;

/**
 * Draws a set of shapes onto a Graphics object.
 * 
 * @author mmcgill
 */
public class Overlay {
	JComponent component;
	Vector markings = new Vector();
	Font font;
	Color color = Color.BLACK;
	float strokeWidth = 2;
	
	public Overlay(JComponent component) {
		this.component = component;
	}
	
	synchronized public void cleanOverlay() {
		markings.removeAllElements();
		if (font != null)
			markings.addElement(new FontMark(font));
		component.repaint();
	}
	
	synchronized public void addMarking(OverlayMarking mark) {
		markings.add(mark);
		component.repaint();
	}
	
	synchronized public void paintOverlay(Graphics g) {
		((Graphics2D)g).setStroke(new BasicStroke(strokeWidth));
		for (int i = 0; i < markings.size(); i++) {
			((OverlayMarking)markings.get(i)).paintMarking(g);
		}
	}
	
	public void setColor(Color c) {
		color = c;
	}
	
	public void drawLine(int x1, int y1, int x2, int y2) {
		addMarking(new LineMark(color, x1, y1, x2, y2));
	}
	
	public void drawOval(int x, int y, int width, int height) {
		addMarking(new OvalMark(false, color, x, y, width, height));
	}
	
	public void drawPolygon(int [] xPoints, int [] yPoints, int numPoints) {
		addMarking(new PolygonMark(false, color, xPoints, yPoints, numPoints));
	}
	
	public void drawPolygon(Polygon p) {
		addMarking(new PolygonMark(false, color, p));
	}
	
	public void drawRect(int x, int y, int width, int height) {
		addMarking(new RectangleMark(false, color, x, y, width, height));
	}
	
	public void drawString(String str, int x, int y) {
		addMarking(new StringMark(font, color, str, x, y));
	}
	
	public void fillOval(int x, int y, int width, int height) {
		addMarking(new OvalMark(true, color, x, y, width, height));
	}
	
	public void fillPolygon(int [] xPoints, int [] yPoints, int numPoints) {
		addMarking(new PolygonMark(true, color, xPoints, yPoints, numPoints));
	}
	
	public void fillPolygon(Polygon p) {
		addMarking(new PolygonMark(true, color, p));
	}
	
	public void fillRect(int x, int y, int width, int height) {
		addMarking(new RectangleMark(true, color, x, y, width, height));
	}
	
	public void setFont(Font f) {
		font = f;
		addMarking(new FontMark(f));
	}
	
	public Font getFont() {
		if (font == null)
			return component.getFont();
		return font;
	}
}
