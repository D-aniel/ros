package javax.swing.overlay;

import java.awt.Graphics;

/**
 * All shapes that can be drawn using an Overlay need to inherit this interface.
 * 
 * @author mmcgill
 */
public interface OverlayMarking {
	public void paintMarking(Graphics g);
}


