	import java.io.BufferedReader;
	import java.io.PrintWriter;
	import java.io.PrintStream;
	import java.io.FileWriter;
	import java.io.BufferedWriter;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.FileReader;
	import java.io.FileWriter;
	import java.io.IOException;
	import java.io.ObjectInputStream;
	import java.io.ObjectOutputStream;
	import java.text.DecimalFormat;
	import java.util.*;
	import java.util.Map.Entry;

	
	
public class HappiQueryExecutor {
	static String dataset;
		static HashMap<Integer, Double> edgeProbabilityMap;
	    private static DecimalFormat df = new DecimalFormat("0.00");
		private static  Long globalTotalTime = 0L;

	    public static void main(String[] args) throws IOException, ClassNotFoundException
	    {
	    	dataset = args[0];	
	    	edgeProbabilityMap = new HashMap();
			String run = args[1];
	    
	    	HashMap<String, Integer> querySizeMap = new HashMap();
		if (dataset.equals("gmark"))	    	
			querySizeMap.putAll(initializeQuerySizeMap());

	    	File folder = new File("./meta/"+dataset+"/result/");
	    	
	    	BufferedReader br;
	    	String poly = "";
	    	int qSize = 0;
	    	String qId = "";

	//	PrintStream pw = System.out; //new PrintWriter(fw);
	//	pw.printf("Q_id\tQDepth\tDerivationC\tEdgeC\tMonomialC\tOurCompileT\tOurComputeT\tTotalTime\tPWTime\tLineNum\n");
					
	    	for (File file: folder.listFiles()) {
		
	    		if(file.isDirectory())
	    			continue;
				
	    //		For yago : 
if (dataset.equals("yago")) {
			
	    		if (file.getName().equals("Q_1.txt"))
	    			qSize = 6;
	    		else if (file.getName().equals("Q_3.txt"))
	    			qSize = 4;
	    		else if (file.getName().equals("Q_4.txt"))
	    			qSize = 6;
	    		else if (file.getName().equals("Q_5.txt"))
	    			qSize = 3;
	    		else if (file.getName().equals("Q_6.txt"))
	    			qSize = 6;
	    	} else {
	    		qSize = querySizeMap.get(file.getName());	    		
	    		}
qId = file.getName().replace(".txt", "");
	    		
	    		br = new BufferedReader(new FileReader(file));
	    		
	    		File f = new File("./experiment/"+dataset+"/pos_world/run"+run+"/stats_"+file.getName());
	    		
	    		if (!f.exists())
	    			f.createNewFile();
	    		else 
	    			continue;
	    		
	    		FileWriter fw = new FileWriter("./experiment/"+dataset+"/pos_world/run"+run+"/stats_"+file.getName());
	    		PrintWriter pw = new PrintWriter(fw);
				pw.printf("Q_id\tQDepth\tDerivationC\tEdgeC\tMonomialC\tProbability\tOurCompileT\tOurComputeT\tTotalTime\tPWTime\tLineNum\n");
	        
	    		long count = 0;
	    		String c = "";
	    		
	    		while ((poly = br.readLine()) != null) {
	    			globalTotalTime = 0L;

	    			count++;
	    			c= "";
	   
	    			
	    			HashMap<Polynomial, Integer> polyMap = constructPolynomial(poly,qSize);
	    			Polynomial polynomial = polyMap.keySet().iterator().next();
	    			
	    			if (polynomial.size() <= 1) {
	    				continue; 	    		
	    			}

	    			int derivationCount = polynomial.size();
	    			int uniqEdgeCount = polyMap.values().iterator().next();    			
	    			

				double prob = -1.0d;
				long st = 0L, mt = 0L;

				if (uniqEdgeCount < 14) {
		    			 st = System.nanoTime();
		    			prob = PossWorld.chanceNew(polynomial, edgeProbabilityMap);
		    			 mt = System.nanoTime();

	    			} else {
					prob = 0;
				}
	
	    			if (prob==0)
	    				c = "0";
	    			else
	    				c = Long.toString(mt-st);
	    			

	    			if ((polynomial.size() < 11) || (uniqEdgeCount < 9) || ((uniqEdgeCount - qSize) < 3)) {

	    				String termCount = semiringProbComputation(polynomial);				
	    				pw.printf("%s\t%d\t%6d\t%10d\t%20s\t%10s\t%5d\n", qId, qSize, derivationCount, uniqEdgeCount, termCount, c, count );

	    			} else {
						String str = "0\t0\t0\t0\t0";
	    				pw.printf("%s\t%d\t%6d\t%10d\t%20s\t%10s\t%5d\n", qId, qSize, derivationCount, uniqEdgeCount, str, c, count );

	    			}

	    			pw.flush();
	    		}
	    	
	    		pw.close();
	    		br.close();
	    		System.gc();
	    	}
	    	
	    	
	    }
	    
	    
		private static Map<? extends String, ? extends Integer> initializeQuerySizeMap() throws NumberFormatException, IOException {

			HashMap<String, Integer> map = new HashMap();
			
			BufferedReader br = new BufferedReader(new FileReader(new File("./meta/"+dataset+"/cypher_query.txt")));
			String line = "";
			
			while ((line = br.readLine()) != null) {
				String lineComp[] = line.split("\t");
				map.put("Q_"+lineComp[0]+".txt", Integer.parseInt(lineComp[1]));
			}
			br.close();
			
			return map;
		}


		private static void storeMap() throws IOException, ClassNotFoundException {
			
			File file = new File("./meta/"+dataset+"/edgeProbIndex"); //TODO: check file name
			
				
				FileOutputStream fi = new FileOutputStream(file);
				ObjectOutputStream si = new ObjectOutputStream(fi);
				
				si.writeObject(edgeProbabilityMap);

				si.close();
			    fi.close();

			
		}

		private static Polynomial symbolicExpressionConstructor(Polynomial polynomial) {
			
			double prob = 0;
			
			ArrayList<Monomial> monos = new ArrayList();
			monos.addAll(polynomial.keySet());
			
			Polynomial poly1 = new Polynomial();
			poly1.put(monos.get(0), polynomial.get(monos.get(0)));
			Polynomial poly2;
			
			long st = System.nanoTime();
			
			for (int i = 1; i < monos.size(); i++) {
								
				Polynomial p = new Polynomial();
				
				poly2 = new Polynomial();
				poly2.put(monos.get(i), polynomial.get(monos.get(i)));
				poly1 = p.sr_add(poly1, poly2);
			}
			
			return poly1;
		}

		  private static Double evaluateSymbolicExpression(Polynomial poly) {			

			double prob = 0;
			double coefficient = 0;
			
			
			for (Monomial mono: poly.keySet()) {
				
				double  monoProb = 1;
				
				for (Integer edgeId: mono) {
					monoProb *=  0.5d;
				}
			
				coefficient = poly.get(mono);				
				prob = prob + (monoProb * coefficient);
			}
			
		
			return prob;
		}


		private static String semiringProbComputation(Polynomial polynomial) {

			long st, et, mt;

			st = System.nanoTime();
			Polynomial recomputeResultant = symbolicExpressionConstructor(polynomial);
			mt = System.nanoTime();

			long symConstructionTime = mt-st;
	
			int termCount =  recomputeResultant.size();
			et = System.nanoTime();
			Double prob = evaluateSymbolicExpression(recomputeResultant);
			long evalTime = System.nanoTime() - et;


			return termCount+"\t"+prob+"\t"+Long.toString(symConstructionTime)+"\t"+Long.toString(evalTime)+"\t"+Long.toString(symConstructionTime+evalTime);
		}

		private static HashMap<Polynomial, Integer> constructPolynomial(String poly, int size) {
			
			Polynomial p = new Polynomial();
			String monomials[] = poly.split("\\+");
			HashMap<Polynomial,Integer> result =new HashMap();
			
			HashSet<Integer> edgeSet = new HashSet();
			
			for( String mono : monomials) {
				Monomial m = new Monomial();
				
				String eComp[] = mono.split("e");
				
				for (String edge: eComp) {
					
					if (edge.length() > 0) {
						m.add(Integer.parseInt(edge));
						edgeSet.add(Integer.parseInt(edge));
					}	
				}
				
				if (m.size() > size)
					System.out.println("Error:"+m.size());
				if (p.containsKey(m)) {
					//int c = p.get(m);
					//c++;
					//p.put(m, c);
				} else {
					p.put(m, 1);
				}
			}
			
			result.put(p, edgeSet.size());
			//System.out.println("Number of terms: "+edgeSet.size());
			//return p;
			
		/*	for (Integer edgeId: edgeSet) {
				
				if (!edgeProbabilityMap.containsKey(edgeId)) {
					double probVal =  ((double)(Math.random()*100d)/100d); 

					Double eProb = Double.parseDouble(df.format(probVal));
					
					edgeProbabilityMap.put(edgeId, eProb);
				}
				
			}
			*/
			
			return result;
		}
	    
	    
	    
	   
	    
	    
	
}