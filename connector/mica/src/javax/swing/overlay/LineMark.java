package javax.swing.overlay;

import java.awt.Color;
import java.awt.Graphics;

public class LineMark implements OverlayMarking {
	Color color;
	int x1, y1, x2, y2;

	public LineMark(Color c, int x1, int y1, int x2, int y2) {
		color = c;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public void paintMarking(Graphics g) {
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}
}
