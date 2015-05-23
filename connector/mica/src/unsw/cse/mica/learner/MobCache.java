/*
 * Created on 7/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unsw.cse.mica.learner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unsw.cse.mica.data.Mob;

/**
 * @author waleed
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MobCache {

	Map cache = new HashMap(); 
	Set hotTypes = new HashSet(); 

	public void cacheMob(Mob m){
		if(hotTypes.contains(m.getType())){
			cache.put(m.getType(), m);
		} 
	}
	
	public Mob getMob(String mobType){
		return (Mob) cache.get(mobType); 
	}
	
	public void addHotType(String mobType){
		hotTypes.add(mobType); 
	}
	public void removeHotType(String mobType){
		hotTypes.remove(mobType); 
		cache.remove(mobType); 
	}

	public Set getHotTypes(){
		return hotTypes;
	}

	public static void main(String[] args) {
	}
}
