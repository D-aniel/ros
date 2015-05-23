package unsw.cse.framescript;

public class PilotFrameBridge {
	public static Domain getDomain(String name) {
		Atom a = Atom.intern(name);
		if (a.value instanceof Domain)
			return (Domain)a.value;
		return new Domain(a);
	}
	
	public static Generic getGeneric(String name) {
		Atom a = Atom.intern(name);
		if (a.value instanceof Generic)
			return (Generic)a.value;
		Generic rval = new Generic(a);
		if (a.value == null)
			a.value = rval;
		try {
			rval.initFrame();
		} catch (FSException e) {
			e.printStackTrace();
		}
		return rval;
	}
	
	public static Instance getInstance(Generic parent) throws FSException {
		Instance i = new Instance();
		i.addParent(parent);
		i.initFrame();
		return i;
	}
	
	public static void addSlot(Instance i, String slot, Term value) throws FSException {
		i.putSlot(Atom.intern(slot), value);
	}
	
	public static void testDomain(Domain d){
		System.err.println("domain: " + d);
		System.err.println("t: " + d.topics.topic); 
	}
}
