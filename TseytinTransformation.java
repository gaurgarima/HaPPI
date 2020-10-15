
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set.*;
import java.util.ArrayList;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.FileNotFoundException;


public class TseytinTransformation {

	static HashMap<HashSet<String>, Integer> intermediateClausesMap = new HashMap();
	static ArrayList<String> clausesList = new ArrayList();
	static Integer nodeIndex = 0;
	static HashMap<String, Integer> edgeIdMap = new HashMap();
	static Integer resultNode = 0;
	static	HashMap<Integer, Double> treeNodeToProbMap;
	static	HashMap<Integer, Double> leaveNodeToProbMap;
	static String queryId = "";
	static String dataset = "";
	static String lineToStore = new String();
	static String tool = new String();
	static Long totalTime = 0L;
	static String run;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	
		tool = args[0];
		dataset = args[1];
		run = args[2];

		File folder = new File("./polyfiles/"+dataset+"/");
    	      	
	    for (File file: folder.listFiles()) {
	
    		if(file.isDirectory())
     			continue;
	    	
    		queryId = file.getName().replace(".txt", "");
    		evaluate(queryId);
    		System.out.println(queryId);
       	}

	}

	static void evaluate(String qId) throws IOException, InterruptedException {


		BufferedReader br = new BufferedReader(new FileReader(new File("./polyfiles/"+dataset+"/"+qId+".txt")));
		String line = "";
		String poly = "";
		FileWriter fw = new FileWriter("./executionTime/"+tool+"/"+dataset+"/complete_run"+run+"/"+qId+".txt"); //todo: correct filename
		PrintWriter pw = new PrintWriter(fw);
		int count = 0;
		HashMap<HashMap<HashSet<String>,Integer>,Integer> temp;
		HashMap<HashSet<String>,Integer> polynomial;

		int uniqueEdgeCount = 0; 

	//	bw.write("Q_id\tDerivationC\tEdgeC\tModelC\tParseTime\tTotalTime\tTerminalTime\tNodes\tEdges\n");
		
		if (tool.equals("d4"))
			pw.write("Q_id\tDerivationC\tEdgeC\tModelC\tParseTime\tTotalTime\tNodes\tEdges\tConversionTime\tTerminalTime\tComputationTime\tProbability\tTotalTime\tLineNo\n");
		else
			pw.write("Q_id\tDerivationC\tEdgeC\tPreTime\tCompileTime\tPostTime\tNodes\tEdges\tConversionTime\tTerminalTime\tComputeTime\tProbability\tTotalTime\tLineNo\n");
		
		//	//For c2d	bw.write("Q_id\tDerivationC\tEdgeC\tPreTime\tCompileTime\tPostTime\tNodes\tEdges\tConversionTime\tTerminalTime\tProbability\tLineNo\n");


		while ((line = br.readLine()) != null) {
			totalTime = 0L;

			lineToStore = "";
			count += 1;
			poly = line;
			String  monomials[] = poly.split("\\+"); 

			temp = new HashMap();
			temp.putAll(constructPolynomial(poly));

			polynomial = new HashMap();
			polynomial.putAll(temp.keySet().iterator().next());

			if (polynomial.size()==1)
				continue;
		/*	else if(polynomial.size() >25) {
				pw.print("out-of-bound\t"+count+"\n");
				pw.flush();
				continue;
			}	
		*/
			uniqueEdgeCount = temp.values().iterator().next();

			evaluateProbability(convertToCNF(polynomial));

			pw.printf("%s\t%d\t%d\t%s\t%d\t%3d\n", qId, polynomial.size() ,uniqueEdgeCount, lineToStore,totalTime,count);
			pw.flush();

		}

		pw.close();
		br.close();
	}



	static HashSet<Integer> convertToCNF(HashMap<HashSet<String>,Integer> poly) throws IOException, FileNotFoundException, InterruptedException {

		
		
		 intermediateClausesMap = new HashMap();
		 clausesList = new ArrayList();
		 nodeIndex = 0;
		 edgeIdMap = new HashMap();
		
		//String monomials[] = poly.split("\\+");
		ArrayList<Integer> monomialRootIndexList = new ArrayList();
		treeNodeToProbMap = new HashMap();
		leaveNodeToProbMap = new HashMap();
		ArrayList<ArrayList<Integer>> monoConstructList = new ArrayList();
		nodeIndex = 0;
		resultNode = 0;
		
		for (HashSet<String> mono: poly.keySet()) {

			ArrayList<Integer> construct = new ArrayList();

			for (String edgeId: mono) {

				if (edgeId.length() > 0) {
					
					if (edgeIdMap.containsKey(edgeId)) {
						//intermediateClausesMap.put(temp, edgeIdMap.get(edgeId));
					} else {
						edgeIdMap.put(edgeId,++nodeIndex);
						//intermediateClausesMap.put(temp, edgeIdMap.get(edgeId));
					}
					leaveNodeToProbMap.put(edgeIdMap.get(edgeId), 0.5);
					construct.add(edgeIdMap.get(edgeId));
				}
			} // one monomial handled
		
			monoConstructList.add(construct);

		//	monomialRootIndexList.add(TseytinTansformation(construct,"AND").toString());
		} // polynomial handled

		long cnfSt = System.nanoTime(); // Check

		for (ArrayList<Integer> construct: monoConstructList) {
			monomialRootIndexList.add(CorrectTseytinTansformation(construct,"AND"));
		}

		resultNode = CorrectTseytinTansformation(monomialRootIndexList,"OR");
		
		long cnfEt = System.nanoTime();
		

/*		System.out.println("Root indexes: "+monomialRootIndexList.toString());

		for (String id: edgeIdMap.keySet())
			System.out.println(id+"\t"+edgeIdMap.get(id));


		for (String clause: clausesList)
			System.out.println(clause);


	for (HashSet<String> set: intermediateClausesMap.keySet())
			System.out.println(set.toString()+"\t"+intermediateClausesMap.get(set));


for (int id: treeNodeToProbMap.keySet())
			System.out.println(id+"\t"+treeNodeToProbMap.get(id));
*/
		File f  = new File("./test.cnf");
		f.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.write("p cnf "+resultNode+" "+(clausesList.size()+1)+"\n");

		for (String clause: clausesList)
			bw.write(clause+"\n");

		bw.write(resultNode+" 0");
		bw.close();

	/*	File f2  = new File("./test_nnf.txt");

		if(!f2.exists())
			f2.createNewFile();
	*/	//f2.close();

		//Consturuct the dnf file

			String fileName = "test.cnf";
			ProcessBuilder pb = null;


			if (tool.equals("d4"))
				 pb = new ProcessBuilder("./d4", fileName, "-out=test.nnf"); 
			else
				pb = new ProcessBuilder("./c2d_linux", "-in", fileName);

			
			pb.directory(new File("/data/home/garima/Library/c2d/"));

			long st = System.nanoTime();
			Process proc= pb.start();
			long et = System.nanoTime();

			 BufferedReader stdInput = new BufferedReader(new 
					     InputStreamReader(proc.getInputStream()));
			
			String s = null;
			String line = "";

			 if (tool.equals("d4")) {
				
					// For d4 output

		 			String modelC="";
					String edgeC="";
					String nodeC="";
					String parseT="";
					String finalT="";
					

					while ((s = stdInput.readLine()) != null) {

						if (s.startsWith("s ")) {
							modelC = s.split(" ")[1];
						}

						if (s.startsWith("c Parse time: ")) {
							
							parseT = s.split("Parse time: ")[1];
						}				

						if (s.startsWith("c Final time: ")) {
							
							finalT = s.split("Final time: ")[1];
						}				

						if (s.startsWith("c Number of nodes: ")) {
							
							nodeC = s.split("Number of nodes: ")[1];
						}				

						if (s.startsWith("c Number of edges: ")) {
							
							edgeC = s.split("Number of edges: ")[1];
						}				
					}
				 
				 
			// modelC, parseT, finalT, (et - st), nodeC, edgeC);
				
				lineToStore = lineToStore.concat(modelC+"\t"+parseT+"\t"+finalT+"\t"+nodeC+"\t"+edgeC+"\t"+Long.toString(cnfEt-cnfSt)+"\t"+Long.toString(et-st)+"\t");


			 } else {
				 // For c2d output
					while ((s = stdInput.readLine()) != null) {
						
						if(s.startsWith("Compile Time")) {
							
						   String timeComp[] = s.split("s / ");
						
						    line  = line.concat(timeComp[1].split(": ")[1].replace("s","")+"\t"+timeComp[0].split(": ")[1].replace("s","")+"\t"+timeComp[2].split(": ")[1].replace("s",""));
					//	    System.out.println("CT: "+timeComp[0].split(": ")[1]+"\tPreT: "+timeComp[1].split(": ")[1]+"\tPostT: "+timeComp[2].split(": ")[1].replace("s",""));

						} else if (s.startsWith("Saving ")) {
							String sComp[] = s.split(" ");												
							line = line.concat("\t"+sComp[1]+"\t"+sComp[4]);
						}
					}
					
					//PreTime\tCompileTime\tPostTime\tNodes\tEdges
					lineToStore = lineToStore.concat(line+"\t"+Long.toString(cnfEt-cnfSt)+"\t"+Long.toString(et-st)+"\t");
			 }

			 totalTime = (cnfEt - cnfSt) + (et-st);

		return new HashSet(edgeIdMap.values());
		/*double prob = evaluateProbability(new HashSet(edgeIdMap.values()));

		System.out.println("\n Probability is: "+prob);
*/

	}




/*

	static HashSet<Integer> convertToCNF(String poly) throws IOException, FileNotFoundException, InterruptedException {

		String monomials[] = poly.split("\\+");
		ArrayList<String> monomialRootIndexList = new ArrayList();
		treeNodeToProbMap = new HashMap();
		leaveNodeToProbMap = new HashMap();
		ArrayList<ArrayList<String>> monoConstructList = new ArrayList();

		for (String mono: monomials) {

			String monoComp[] = mono.split("e");
			ArrayList<String> construct = new ArrayList();

			System.out.println(mono);

			for (String edgeId: monoComp) {

				if (edgeId.length() > 0) {

					HashSet<String> temp = new HashSet();
					temp.add("L"+edgeId.toString());
					
					if (edgeIdMap.containsKey(edgeId)) {
						intermediateClausesMap.put(temp, edgeIdMap.get(edgeId));
					} else {
						edgeIdMap.put(edgeId,++nodeIndex);
						intermediateClausesMap.put(temp, edgeIdMap.get(edgeId));
					}
					leaveNodeToProbMap.put(edgeIdMap.get(edgeId), 0.5);
					construct.add(edgeId.toString());
				}
			} // one monomial handled
		
			monoConstructList.add(construct);

		//	monomialRootIndexList.add(TseytinTansformation(construct,"AND").toString());
		} // polynomial handled

		for (ArrayList<String> construct: monoConstructList) {
			monomialRootIndexList.add(TseytinTansformation(construct,"AND").toString());
		}

		resultNode = TseytinTansformation(monomialRootIndexList,"OR");




		File f  = new File("./test.cnf");

		//if(!f.exists())
			f.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.write("p cnf "+resultNode+" "+(clausesList.size()+1)+"\n");
//bw.write("p cnf "+resultNode+" "+clausesList.size()+"\n");

			for (String clause: clausesList)
			bw.write(clause+"\n");

		bw.write(resultNode+" 0");
		bw.close();

		File f2  = new File("./test_nnf.txt");

		if(!f2.exists())
			f2.createNewFile();
		//f2.close();

		//Consturuct the dnf file
			String fileName = "test.cnf";
			ProcessBuilder pb = new ProcessBuilder("./d4", fileName, "-out=test_nnf.txt"); 	
			pb.directory(new File("/data/home/garima/Library/c2d/"));

			Process proc= pb.start();
		
			 proc.waitFor();
			//proc.destroy();

		return new HashSet(edgeIdMap.values());


	}
*/

	static Double evaluateProbability(HashSet<Integer> leaveNodes) throws IOException, FileNotFoundException {

		
			BufferedReader br = null;
			
			if (tool.equals("d4"))
				br = new BufferedReader(new FileReader(new File("./test.nnf")));
			else
				br = new BufferedReader(new FileReader(new File("./test.cnf.nnf")));
			
			String line = "";
			String lineComp[] = null;
			
		//	System.out.println(leaveNodes.toString());

			line = br.readLine();
			int index = 0;
		//	System.out.println("Ineval function "+line);

			String op = "";
			String c = "";
			double prob = 0;
		
			int count = 0;
			long st = System.nanoTime();
			
			while ((line = br.readLine()) != null) {
				lineComp = line.split(" ");

				op = lineComp[0];
				double val = 0;
				int opCount = 0;
				int opId = 0;

				if (op.equals("L"))
					opId = 0;
				else if(op.equals("A"))
					opId = 1;
				else if (op.equals("O"))
					opId = 2;		

				switch(opId) {

					case 0: c = lineComp[1];
							
							  String id = c.replace("-","");
							   val = 0;
							  boolean isLeave = false;
				//	System.out.println(count+"Handling leves!"+id);

							  if (leaveNodes.contains(Integer.parseInt(id))) {
							 	val = leaveNodeToProbMap.get(Integer.parseInt(id));
								isLeave = true;
					//			System.out.println("It ia leave!!\t"+val);
							  }
							  else
								val = 1;

							  if (c.startsWith("-") && isLeave)
								val = 1-val;

							  treeNodeToProbMap.put(index,val);
							  break;

					case 1: c = lineComp[1];
						//	System.out.println(count +"And node");
								 opCount = Integer.parseInt(c);
								 val = 1;
								if (opCount==0)
									val = 1;
								else
								for (int i = 1; i<= opCount; i++) {
									val = val * treeNodeToProbMap.get(Integer.parseInt(lineComp[1+i]));
								}
							  treeNodeToProbMap.put(index,val);
							  break;

					case 2: c = lineComp[2];
						//	System.out.println(count+"Or node");
								 opCount = Integer.parseInt(c);
								 val = 0;
								for (int i = 1; i<= opCount; i++) {
									val = val + treeNodeToProbMap.get(Integer.parseInt(lineComp[2+i]));
								}
							  treeNodeToProbMap.put(index,val);
							  break;

				}

					count++;
				index++;
				prob = val;
			//	System.out.println(line+"\nP: "+prob);
			}

			long et = System.nanoTime();
			
			if (tool.equals("d4"))
				lineToStore = lineToStore.concat(Long.toString(et-st)+"\t"+Double.toString(prob));
			else
				lineToStore = lineToStore.concat(Long.toString(et-st)+"\t"+Double.toString(prob));

			totalTime = totalTime + (et-st);
			return prob;

	}


static Integer CorrectTseytinTansformation(ArrayList<Integer> construct, String form) {

			int rootNodeIndex = 0;

			if (construct.size() == 1)
				return construct.get(0);

			++nodeIndex;
			String clause = "";

			int localRoot = nodeIndex;
			String longClause = "";

			if (form.equals("AND")) {

				for(int nodeId : construct) {
					clause = nodeId+" -"+localRoot+" 0";
					clausesList.add(clause);
					longClause = longClause.concat("-"+nodeId+" ");

				}
				longClause = longClause.concat(localRoot+" 0");
				clausesList.add(longClause);
 
			} else {

				for(int nodeId : construct) {
					clause = "-"+nodeId+" "+localRoot+" 0";
					clausesList.add(clause);
					longClause = longClause.concat(nodeId+" ");

				}
				longClause = longClause.concat("-"+localRoot+" 0");
				clausesList.add(longClause);

			}

			return localRoot;





			}


	static Integer TseytinTansformation(ArrayList<String> construct, String form) {

			int rootNodeIndex = 0;

			//GLobal datastructure
			
			String clause = "";
			int arg1 = 0;
			int arg2 = 0;
			int arg3 = 0;
			HashSet<String> set1 = new HashSet();
			HashSet<String> set2 = new HashSet();
			HashSet<String> clauseSet = new HashSet();


				if (form.equals("AND")) 
					set1.add("L"+construct.get(0));
				else
					set1.add(construct.get(0));



				if (intermediateClausesMap.containsKey(set1))
					arg1 = intermediateClausesMap.get(set1);
				else {
					++nodeIndex;
					arg1 = nodeIndex;
					intermediateClausesMap.put(set1,nodeIndex);
				}


				for (int i = 1; i < construct.size(); i++) {

					set2.clear();



					if (form.equals("AND"))
						set2.add("L"+construct.get(i));
					else
						set2.add(construct.get(i));

					if (intermediateClausesMap.containsKey(set2)) {
						arg2 = intermediateClausesMap.get(set2);
					} else {

						arg2 = ++nodeIndex;
						intermediateClausesMap.put(set2,arg2);
					}

					clauseSet.clear();
					clauseSet.add(new Integer(arg1).toString());
					clauseSet.add(new Integer(arg2).toString());
					clauseSet.add(form);

				//	System.out.println(clauseSet.toString());

					if (intermediateClausesMap.containsKey(clauseSet))
						arg3 = intermediateClausesMap.get(clauseSet);
					else {
						arg3 = ++nodeIndex;
						intermediateClausesMap.put(clauseSet,arg3);
					}

					if (form.equals("AND")) {
						clause = "-"+arg1+" -"+arg2+" "+arg3+" 0";
						clausesList.add(clause);

						clause = arg1+" -"+arg3+" 0";
						clausesList.add(clause);


						clause = arg2+" -"+arg3+" 0";
						clausesList.add(clause);

					} else if (form.equals("OR")) {

						clause = arg1+" "+arg2+" -"+arg3+" 0";
						clausesList.add(clause);

						clause = "-"+arg1+" "+arg3+" 0";
						clausesList.add(clause);


						clause = "-"+arg2+" "+arg3+" 0";
						clausesList.add(clause);

					}

					HashSet<String> clauseSet1 = new HashSet();
					clauseSet1.add(new Integer(arg3).toString());
					intermediateClausesMap.put(clauseSet1, arg3);
				
					arg1 = arg3;
					rootNodeIndex = arg3;


				
				}

			/*	System.out.println("C: "+construct.toString());

					for (HashSet<String> set: intermediateClausesMap.keySet())
			System.out.println(set.toString()+"\t"+intermediateClausesMap.get(set));
*/

			return rootNodeIndex;	
	}


	private static HashMap<HashMap<HashSet<String>, Integer>, Integer> constructPolynomial(String poly) {
			
			HashMap<HashSet<String>, Integer> p = new HashMap<HashSet<String>, Integer>();
			String monomials[] = poly.split("\\+");
			HashMap<HashMap<HashSet<String>, Integer>, Integer> result =new HashMap();
			
			HashSet<String> edgeSet = new HashSet();
			
			for( String mono : monomials) {
				HashSet<String> m = new HashSet<String>();
				
				String eComp[] = mono.split("e");
				
				for (String edge: eComp) {
					
					if (edge.length() > 0) {
					//	m.add(Integer.parseInt(edge));
						m.add(edge);
					//	edgeSet.add(Integer.parseInt(edge));
						edgeSet.add(edge);

					}	
				}
				
				if (p.containsKey(m)) {
					int c = p.get(m);
				//	c++;
				//	p.put(m, c);
				} else {
					p.put(m, 1);
				}
			}
			
			result.put(p, edgeSet.size());

			return result;
		}
}
