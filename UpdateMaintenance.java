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

	
	
public class UpdateMaintenance {

	static String dataset;
		static HashMap<Integer, Double> edgeProbabilityMap;
	    private static DecimalFormat df = new DecimalFormat("0.00");
		static HashMap<Polynomial, Polynomial> dervToSymPolyMap = new HashMap();
		static HashMap<Polynomial, Double> probabilityMap = new HashMap();


	    public static void main(String[] args) throws IOException, ClassNotFoundException
	    {
	    	
	    	dataset = args[0];	
			String operation = args[1];
			String run = args[2];
			String selectionMode = "randomPoly";
	    	HashMap<String, Integer> querySizeMap = new HashMap();
		
		if (dataset.equals("gmark"))
	    	querySizeMap.putAll(initializeQuerySizeMap());

	    	File folder = new File("./meta/"+dataset+"/result/");	    	
	    	BufferedReader br;
	    	
	    	String poly = "";
	    	int qSize = 0;
	    	String qId = "";

		//PrintStream pw = System.out;

		//pw.printf("QDepth\tDerivationC\tEdgeC\tTouchedDerv\tUpdateTime\tRecompTime\tLineNo\n");

	    	for (File file: folder.listFiles()) {
		
	    		if(file.isDirectory())
	    			continue;
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
			} else if (dataset.equals("gmark"))
			    qSize = querySizeMap.get(file.getName());

			qId = file.getName().replace("\\.txt", "");
	    	

	    		br = new BufferedReader(new FileReader(file));
			
	    		File f = new File("./experiment/"+dataset+"/maintenance/"+operation+"/run"+run+"/"+file.getName());
			
	    		if (!f.exists())
	    			f.createNewFile();
	    		else 
	    			continue;

	    		FileWriter fw = new FileWriter("./experiment/"+dataset+"/maintenance/"+operation+"/run"+run+"/"+file.getName());
	    		PrintWriter pw = new PrintWriter(fw);

				pw.printf("QDepth\tDerivationC\tEdgeC\tTouchedDerv\tUpdateTime\tRecompTime\tLineNo\n");
			
	    		long count = 0;
	    		String c = "";
	    		
	    		while ((poly = br.readLine()) != null) {
	    		
	    			count++;
	    			c= "";
	   
	    			HashMap<Polynomial, Integer> polyMap = constructPolynomial(poly,qSize);
	    			Polynomial polynomial = polyMap.keySet().iterator().next();
	    			
	    			if (polynomial.size() <= 1) {
	    				continue; 	    		
	    			}

	    			int derivationCount = polynomial.size();
	    			int uniqEdgeCount = polyMap.values().iterator().next();    			
	    			

				String result = "";

	    		if ((polynomial.size() < 11) || (uniqEdgeCount < 9) || ((uniqEdgeCount - qSize) < 3)) {


	    			Integer selectedEdge = -1;
					selectedEdge = getEdge(polynomial, selectionMode,((long)count));
	    			
	    			if (operation.equals("insertion")) {
	    				result = insertEdge(polynomial, selectedEdge);
	
					} else if (operation.equals("deletion")) {
				    result = deleteDerivation(polynomial);
	
					} else if (operation.equals("update")) {
						result = updateEdgeProbability(polynomial, selectedEdge);

					}else {
						System.out.println("Invalid operation");
						System.exit(0);
					}
	    		
	    			pw.printf("%d\t%6d\t%10d\t%20s\t%5d\n", qSize, derivationCount, uniqEdgeCount, result,count );				
	    			pw.flush();

				} else {

					result = "0\t0\t0";
					pw.printf("%d\t%6d\t%10d\t%20s\t%5d\n", qSize, derivationCount, uniqEdgeCount, result,count );
					
	    			pw.flush();
				} 
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
				
				if (!p.containsKey(m)) {
					p.put(m, 1);
				} 
			}
			
			result.put(p, edgeSet.size());
			return result;
		}
	    
	    private static String updateProbComputation(Polynomial polynomial, Polynomial newDerivation) {			

			if (polynomial == null)
				return "NA\tNA\tNA\tNA";

			double prob = 0;
			double termCount = 0;
			double coefficient = 0;
			
			Polynomial p = new Polynomial();

			Polynomial poly1 = newDerivation;
			long st = System.nanoTime();
			poly1 = p.sr_add(polynomial, newDerivation);
			long mt = System.nanoTime();

			
			
			for (Monomial mono: poly1.keySet()) {
				
				double  monoProb = 1;
				
				for (Integer edgeId: mono) {
					monoProb *=  0.5d;
				}
			
				coefficient = poly1.get(mono);				
				prob = prob + (monoProb * coefficient);
			
			}
			
			long et = System.nanoTime();
			
			termCount = poly1.size();
	
			return termCount+"\t"+Double.toString(prob)+"\t"+Long.toString(mt-st)+"\t"+Long.toString(et-mt);
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



		// New update functions following

		static Integer getEdge (Polynomial poly, String method, Long lineNo) {


			// Keep adding other edge selection methods with correct method code
			
			if (method.equals("random")) {
				return randomEdgeSelection(poly);
			
			} else if (method.equals("randomPoly")){
				return randomPolyEdgeSelection(poly, lineNo);
			} else {
				System.out.println("Incorrect selection method passed!!");
				return null;
			}

		}

		// random: Randomly select one of the edge invovled in derivation
		static Integer randomEdgeSelection (Polynomial poly) {

			HashSet<Integer> allEdgesInvovled = new HashSet();

			for (Monomial mono: poly.keySet()) {
				allEdgesInvovled.addAll(mono);
			}

			ArrayList<Integer> tmp = new ArrayList(allEdgesInvovled);
			
			Collections.shuffle(tmp);
			int totalMonoC = poly.size();
			Integer selectedEdge = -1;

			while (selectedEdge < 0) {
				int count = 0;
				
				for (Monomial m: poly.keySet()) {
					if (m.contains(tmp.get(0)))
						count++;
				}

				if (count != totalMonoC)
					selectedEdge = tmp.get(0);
				else
					tmp.remove(0);
			}
			
			//System.out.println("Edge selected is: "+selectedEdge);	
			return selectedEdge;

		}


		static Integer randomPolyEdgeSelection (Polynomial poly, Long lineNo) {

			HashSet<Integer> allEdgesInvovled = new HashSet();

			for (Monomial mono: poly.keySet()) {
				allEdgesInvovled.addAll(mono);
			}

			long numberOfEdges = allEdgesInvovled.size();
			ArrayList<Integer> tmp = new ArrayList(allEdgesInvovled);
			
			Collections.sort(tmp);
			boolean hasFound = false;
			long mod1, mod2, polyValue;
			int totalMonoC = poly.size();
			int selectedEdge = -1;

				mod1 = lineNo % numberOfEdges;
				polyValue = computeRandomPoly(mod1);
				mod2 = polyValue % numberOfEdges;
				selectedEdge = tmp.get(((int)mod2));

			while (!hasFound) {

				
				int count = 0;
				for (Monomial m: poly.keySet()) {
					if (m.contains(selectedEdge))
						count++;
				}

				if (count != totalMonoC) {
					hasFound = true;
				} else {
					mod2 = (mod2+1) % numberOfEdges;
					selectedEdge = tmp.get(((int)mod2)); 
					
				}
			}

			return selectedEdge;

		}

		static Long computeRandomPoly(long value) {


			//X^7+43x^6-101x^4+x^2+x+1009

			return ((long)Math.pow(value,7)) + 43 * ((long)Math.pow(value,6)) - 101 * ((long)Math.pow(value,4)) + ((long)Math.pow(value,2)) + value + 1009;
		}

		static String insertEdge (Polynomial derivationPoly, Integer edgeToInsert) {

			ArrayList<Monomial> toAddMonomials = new ArrayList();
			int removedMonomialCount = 0;
			Polynomial resultantPoly = new Polynomial();
			Polynomial completePoly = new Polynomial();
			completePoly.putAll(derivationPoly);
			
			/* Identify derivations which have this edgeToInsert; remove all such monomials (derivations) from the polynomial
			*	and add each monomial incrementally
			*/

			//Removing derivations which has edgeToSelect
			for (Monomial mono : derivationPoly.keySet()) {

				if (mono.contains(edgeToInsert)) {
					toAddMonomials.add(mono);
				}
			}

			for (Monomial m: toAddMonomials) {
				derivationPoly.remove(m);
			}

			removedMonomialCount = toAddMonomials.size();
			long totalTime = 0L, st = 0L, et = 0L, mt = 0L;
			resultantPoly.putAll(symbolicExpressionConstructor(derivationPoly)); //*TODO: add the symbolic polynomial here, i.e. convert the derivationPoly to symPoly first

			//Adding the removed derivations back incrementally
			st = System.nanoTime();

			for (Monomial toAddMono: toAddMonomials) {
				
				Polynomial polyToAdd = new Polynomial();
				polyToAdd.put(toAddMono,1);
				resultantPoly = resultantPoly.sr_add(resultantPoly,polyToAdd); //TODO: check this function
			}

			et = System.nanoTime();

			totalTime = (et-st);

			// Recompute Method: construct polynomial of all the derivations (old + new) and evaluate its value

			mt = System.nanoTime();
			Polynomial recomputeResultant = symbolicExpressionConstructor(completePoly);
			et = System.nanoTime();

			st = System.nanoTime();
			Double prob = evaluateSymbolicExpression(recomputeResultant);
			long tt = System.nanoTime() - st;

			return removedMonomialCount+"\t"+Long.toString(totalTime+tt)+"\t"+Long.toString((et-mt)+tt); //*TODO: discuss
		}


	   	static String deleteEdge (Polynomial derivationPoly, Integer edgeToDelete) { 

			int removedMonomialCount = 0;
			Polynomial polyAfterDeletion = new Polynomial();
			Polynomial symbolicPoly = new Polynomial();
			symbolicPoly.putAll(dervToSymPolyMap.get(derivationPoly)); //*TODO: construct the symboilcPoly

			/*
			* Identify monomials which contains the edgeToDelete and drop off those monomials from the polynomial (poly)
			*/

			//MaintenanceMethod: Removing monomials which has edgeToSelect

			HashSet<Monomial> tmp = new HashSet();
			tmp.addAll(symbolicPoly.keySet());

			if (symbolicPoly == null) {
				System.out.println("Symbolic expression is nul!!");
				System.exit(0);
			}
			long st = System.nanoTime();

			double oldProb = probabilityMap.get(symbolicPoly);
			Integer coeff = 0;
			
			
			for (Monomial mono : tmp) {

				if (mono.contains(edgeToDelete)) {
					
					coeff = symbolicPoly.get(mono);
					double monoProb = 1d;
					
					for (Integer e: mono) {
						monoProb = monoProb * 0.5;
					}
					
					oldProb = oldProb + (-1*monoProb*coeff);		
					symbolicPoly.remove(mono);

				}
			}


			//Reevaluate the updated polynomial to get updated probability (done within the previous for loop)

			long mt = System.nanoTime();

			//Recomputation Method: Constructing new polynomial after deleting the derivations
			for (Monomial mono : derivationPoly.keySet()) {

				if (!mono.contains(edgeToDelete)) {
					polyAfterDeletion.put(mono,derivationPoly.get(mono));
				} else {
					removedMonomialCount++;
				}
			}

			// *TODO: Reconstuct the symbolic expression and evaluate it 
			Polynomial resultingPoly = symbolicExpressionConstructor(polyAfterDeletion);
			evaluateSymbolicExpression(resultingPoly);
			
			long et = System.nanoTime();
			
			return removedMonomialCount+"\t"+Long.toString(mt-st)+"\t"+Long.toString(et-mt); // *TODO: discuss
		}

		static String deleteDerivation (Polynomial derivationPoly) { 

		    /*
		    
			int removedMonomialCount = 0;
			Polynomial polyAfterDeletion = new Polynomial();
			Polynomial symbolicPoly = new Polynomial();
			symbolicPoly.putAll(dervToSymPolyMap.get(derivationPoly)); //*TODO: construct the symboilcPoly
			
			/*
			* Identify monomials which contains the edgeToDelete and drop off those monomials from the polynomial (poly)
			*  


			//MaintenanceMethod: Removing monomials which has edgeToSelect

			HashSet<Monomial> tmp = new HashSet();
			tmp.addAll(symbolicPoly.keySet());

			if (symbolicPoly == null) {
				System.out.println("Symbolic expression is nul!!");
				System.exit(0);
			}
			long st = System.nanoTime();

			double oldProb = probabilityMap.get(symbolicPoly);
			Integer coeff = 0;
			
			
			for (Monomial mono : tmp) {

				if (mono.contains(edgeToDelete)) {
					
					coeff = symbolicPoly.get(mono);
					double monoProb = 1d;
					
					for (Integer e: mono) {
						monoProb = monoProb * 0.5;
					}
					
					oldProb = oldProb + (-1*monoProb*coeff);		
					symbolicPoly.remove(mono);

				}
			}


			//Reevaluate the updated polynomial to get updated probability (done within the previous for loop)
		//	evaluateSymbolicExpression(symbolicPoly);	//*TODO: find the functions

			long mt = System.nanoTime();

			//Recomputation Method: Constructing new polynomial after deleting the derivations
			for (Monomial mono : derivationPoly.keySet()) {

				if (!mono.contains(edgeToDelete)) {
					polyAfterDeletion.put(mono,derivationPoly.get(mono));
				} else {
					removedMonomialCount++;
				}
			}

			//sSystem.out.println(removedMonomialCount+"\t"+derivationPoly.size());
			// *TODO: Reconstuct the symbolic expression and evaluate it 
			Polynomial resultingPoly = symbolicExpressionConstructor(polyAfterDeletion);
			evaluateSymbolicExpression(resultingPoly);
			
			long et = System.nanoTime();
			
			return removedMonomialCount+"\t"+Long.toString(mt-st)+"\t"+Long.toString(et-mt); // *TODO: discuss
			*/
		    return "";
		}



	   static String updateEdgeProbability (Polynomial derivationPoly, Integer updatedEdge) {

			int removedMonomialCount = 0;
			Polynomial symbolicPoly =  dervToSymPolyMap.get(derivationPoly); //TODO: construct the symboilcPoly
			double oldProbability = probabilityMap.get(symbolicPoly); //TODO: evaluate the dervToSymPolyMap (maybe construct a map for this as well)

			/*
			* Identify monomials which contains the updatedEdge and reevaluate those monomials to compute the updated 
			* probability
			*/

			//MaintenanceMethod: Removing monomials which has edgeToSelect

			int min = 1, max = 49;
			Random random = new Random();
			 
			double difference = ((double)(random.nextInt(max - min) + min)/100); //TODO: intialize
			int sign = random.nextInt(1) ; // TODO: initialize
			
			if (sign == 0)
				sign = -1;
			
			long st = System.nanoTime();
			double updatedProbability = oldProbability;

			
			double newEdgeProbability = 0.5 + (sign*difference); // 
			difference = oldProbability - newEdgeProbability;

			for (Monomial mono : symbolicPoly.keySet()) {

				if (mono.contains(updatedEdge)) {

					double monoProb = 1d;

					for (Integer e: mono) {

						if (e.equals(updatedEdge))
							monoProb = monoProb * difference;
						else
							monoProb = monoProb * 0.5;
					}

					updatedProbability =  updatedProbability - monoProb   ; //TODO: correct the formula
				}
			}

			long mt = System.nanoTime();


			//Recomputation Method: Constructing new polynomial after deleting the derivations

			for (Monomial mono : symbolicPoly.keySet()) {
				double monoProb = 1L;

				for (Integer e: mono) {

					if (e.equals(updatedEdge))
						monoProb = monoProb * newEdgeProbability;
					else
						monoProb = monoProb * 0.5;
				}	
			}

			long et = System.nanoTime();
			
			return "NA\t"+Long.toString(mt-st)+"\t"+Long.toString(et-mt);
		}
	   

	   static void initializeMaps() throws IOException, ClassNotFoundException {

		File f = new File("./meta/"+dataset+"/derTosymMap.ser");
		File f1 = new File("./meta/"+dataset+"/symToProbMap.ser");
		
		FileInputStream fi1 = new FileInputStream(f);
		ObjectInputStream si1 = new ObjectInputStream(fi1);
			
		dervToSymPolyMap = ((HashMap<Polynomial,Polynomial>)si1.readObject());
		si1.close();
		fi1.close();

		FileInputStream fi2 = new FileInputStream(f1);
		ObjectInputStream si2 = new ObjectInputStream(fi2);
			
		probabilityMap = ((HashMap<Polynomial,Double>)si2.readObject());
		si2.close();
		fi2.close();


	   }	
}