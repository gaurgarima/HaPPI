
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

class Polynomial extends HashMap<Monomial,Integer>
{
    boolean satisfy(HashSet<Integer> world)
    {
	for (Monomial m : this.keySet())
	    if(world.containsAll(m)) return true;

	return false;
    }
    
	public  Monomial mul_mono(Monomial m1, Monomial m2) {
		Monomial mm = new Monomial();
		
		mm.addAll(m1);
		mm.addAll(m2);
		
		return mm;
	}

	public  Polynomial mul_poly(Polynomial p1, Polynomial p2) {
		
		Polynomial pm = new Polynomial();
		
		for(Entry<Monomial, Integer> m1: p1.entrySet()) {
			for (Entry<Monomial, Integer> m2: p2.entrySet())
			{
				Monomial mm = mul_mono(m1.getKey(), m2.getKey());
				Integer c = 0;
				
				if(pm.containsKey(mm)) {
					c = pm.get(mm);
				}
				pm.put(mm, c + m1.getValue()*m2.getValue());
				
				if(pm.get(mm) == 0) pm.remove(mm);
			}
		}
		
		return pm;
	}
	
	public Polynomial addsub_poly(HashMap<Monomial,Integer> p1, HashMap<Monomial,Integer> p2, Integer sub) {
		
		Polynomial pa = new Polynomial();
		
		Set<Monomial> ms =  new HashSet();
		
		ms.addAll(p1.keySet());
		ms.addAll(p2.keySet());
		
		for(Monomial m: ms) {
			Integer c1 = 0, c2 = 0;
			if(p1.containsKey(m)) {
				c1 = p1.get(m);
			}
			if(p2.containsKey(m)) {
				c2 = p2.get(m);
			}
			
			pa.put(m,c1+sub*c2);
			if(pa.get(m) == 0) pa.remove(m);
		}
		
		return pa;
	}
	

	public Polynomial modified_addsub_poly(HashMap<Monomial,Integer> p1, HashMap<Monomial,Integer> p2, Integer sub) {
		
		Polynomial pa = new Polynomial();
		
		Set<Monomial> ms =  new HashSet();
		Polynomial toAdd = new Polynomial();
		boolean isOrderReversed = false;

		if (p1.size() >= p2.size()) {
			toAdd.putAll(p2);
			ms.addAll(p2.keySet());
			pa.putAll(p1);
		} else {

			toAdd.putAll(p1);
			ms.addAll(p1.keySet());
			pa.putAll(p2);
			isOrderReversed = true;
		}

	//	ms.addAll(p1.keySet());
	//	ms.addAll(p2.keySet());
		
		for(Monomial m: ms) {
			Integer c1 = 0, c2 = 0;

			if(pa.containsKey(m)) {
				c1 = pa.get(m);
			} 
			c2 = toAdd.get(m);

			int coeffVal ;

			if (isOrderReversed)
				coeffVal = (c1 * sub) +c2;
			else
				coeffVal = c1 + (sub*c2);

			if (coeffVal == 0)
				pa.remove(m);
			else
				pa.put(m,coeffVal);

			/*pa.put(m,c1+sub*c2);
			if(pa.get(m) == 0) pa.remove(m);

			*/
		}
		
		return pa;
	}
	
	public Polynomial sr_add(Polynomial p1, Polynomial p2) {
		
		Polynomial pa = new Polynomial();

//System.out.println(p1.size()+"\t"+p2.size());
		
		pa = modified_addsub_poly(p1, p2, 1);
		pa = modified_addsub_poly(pa, mul_poly(p1, p2), -1);
		
		return pa;
	}

    
    
    
    
    
    
}