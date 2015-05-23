package javax.swing.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

public class PolygonMark implements OverlayMarking {
	boolean filled;
	Color color;
	Polygon p;
	int [] xPoints, yPoints;
	int numPoints;
	
	public PolygonMark(boolean fill, Color c, Polygon p) {
		filled = fill;
		color = c;
		this.p = p;
	}
	
	public PolygonMark(boolean fill, Color c, int [] xPoints, int [] yPoints, int numPoints) {
		filled = fill;
		color = c;
		this.xPoints = xPoints;
		this.yPoints = yPoints;
		this.numPoints = numPoints;
	}
	
	public void paintMarking(Graphics g) {
		g.setColor(color);
		if (p == null) {
			if (filled)
				g.fillPolygon(xPoints, yPoints, numPoints);
			else
				g.drawPolygon(xPoints, yPoints, numPoints);
		} else {
			if (filled)
				g.fillPolygon(p);
			else
				g.drawPolygon(p);
		}
	}
}
