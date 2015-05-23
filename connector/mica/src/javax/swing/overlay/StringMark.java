package javax.swing.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class StringMark implements OverlayMarking {
	Color color;
	Font font;
	String str;
	int x, y;
	public StringMark(Font f, Color c, String str, int x, int y) {
		font = f;
		color = c;
		this.str = str;
		this.x = x;
		this.y = y;
	}
	
	public void paintMarking(Graphics g) {
		g.setColor(color);
		if (font != null)
			g.setFont(font);
		g.drawString(str, x, y);
	}
}
