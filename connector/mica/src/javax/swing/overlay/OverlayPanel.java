package javax.swing.overlay;

import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * An extension of JPanel that allows simple shapes to be drawn over the panel's
 * components.
 * 
 * @author mmcgill
 */
public class OverlayPanel extends JPanel {
	Overlay overlay = null;

	public OverlayPanel() {
		super();
	}

	public OverlayPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public OverlayPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public OverlayPanel(LayoutManager layout) {
		super(layout);
	}

	public synchronized void paint(Graphics g) {
		super.paint(g);
		if (overlay != null)
			overlay.paintOverlay(g);
	}
	
	synchronized public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}
	
	synchronized public Overlay getOverlay() {
		return overlay;
	}
}