package javax.swing.overlay;

import java.awt.Font;
import java.awt.Graphics;

public class FontMark implements OverlayMarking {
	Font font;
	public FontMark(Font f) {
		font = f;
	}
	public void paintMarking(Graphics g) {
		g.setFont(font);
	}
}
