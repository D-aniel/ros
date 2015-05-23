package unsw.cse.mica.demo;


import java.awt.Polygon;

import unsw.cse.framescript.*;

/**
 * A FrameScript module that provides access to basic Java functions to FrameScript.
 *  
 * @author mmcgill
 */
public class MapModule {
	public static void init() throws FSException {
		new Subr("insidePolygon", 4) {
			public Term apply(Instance currentObject, Term[] args, StackFrame frame)
						throws FSException {
				FSInteger x = check_integer(currentObject, args, 1, frame);
				FSInteger y = check_integer(currentObject, args, 2, frame);
				FSList xPoints = check_list(currentObject, args, 3, frame);
				FSList yPoints = check_list(currentObject, args, 4, frame);
				int numPoints = xPoints.size()<yPoints.size()?xPoints.size():yPoints.size();
				int [] xint = new int [numPoints];
				int [] yint = new int [numPoints];
				for (int i = 0; i < numPoints; i++) {
					Term t = xPoints.get(i);
					if (t instanceof FSInteger)
						xint[i] = (int)((FSInteger)t).getInteger();
					else
						xint[i] = 0;
					t = yPoints.get(i);
					if (t instanceof FSInteger)
						yint[i] = (int)((FSInteger)t).getInteger();
					else
						yint[i] = 0;
				}
				Polygon p = new Polygon(xint, yint, numPoints);
				return p.contains(x.getValue(), y.getValue())?Atom._true:Atom._false;
			}
		};
		
		new Subr("as_integer", 1) {
			public Term apply(Instance currentObject, Term[] args, StackFrame frame)
					throws FSException {
				Term t = args[1].eval(currentObject, frame);
				if (t instanceof FSDouble)
					return FSNumber.getNumber(((long)((FSDouble)t).getValue()));
				return t;
			}
		};
	}
}
