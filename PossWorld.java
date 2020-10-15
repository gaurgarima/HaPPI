
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

class PossWorld {

    static double chance(Polynomial poly, HashMap<Integer, Double> eProb)
    {
	HashSet<Integer> vars = new HashSet<Integer>();

	double prob = 0;
	
	for(Monomial m : poly.keySet()) vars.addAll(m);

	int wsize = vars.size();

	long wnum = 1 << wsize;
	System.out.println(wnum);

	for(int i = 0; i < wnum; ++i)
	    {
		HashSet<Integer> world = new HashSet<Integer>();
		double p = 1;

		int j = 0;

		for(Integer v : vars)
		    {
			if((i & (1 << j)) != 0) {
			    world.add(v);
				p *= (double)(Math.random()*100d)/100d;

			    //p *= eProb.get(v);
			}
			else {
				
				p *= 1- (double)(Math.random()*100d)/100d;

			    //p *= 1 - eProb.get(v);
			}
			
			++j;
		    }
		if(poly.satisfy(world))
		    prob += p;
	    }
	return prob;
    }
    
    static double chanceNew(Polynomial poly, HashMap<Integer, Double> eProb)
    {
	HashSet<Integer> vars = new HashSet<Integer>();

	double prob = 0;
	
	for(Monomial m : poly.keySet()) vars.addAll(m);

	int wsize = vars.size();

	long wnum = 1L << wsize;
	//System.out.println(wnum);
	long st = System.nanoTime();
	
	for(int i = 0; i < wnum; ++i)
	    {
		
	//	if (i%10 == 0) {
			if ((System.nanoTime()-st)/1000000000 > 1)
				return 0;
		//}
		HashSet<Integer> world = new HashSet<Integer>();
		double p = 1;

		int j = 0;

		for(Integer v : vars)
		    {
			if((i & (1 << j)) != 0) {
			    world.add(v);
				p *= (double)(Math.random()*100d)/100d;

			    //p *= eProb.get(v);
			}
			else {
				
				p *= 1- (double)(Math.random()*100d)/100d;

			    //p *= 1 - eProb.get(v);
			}
			
			++j;
		    }
		if(poly.satisfy(world))
		    prob += p;
		
		
			
		
	    }
	return prob;
    }
}