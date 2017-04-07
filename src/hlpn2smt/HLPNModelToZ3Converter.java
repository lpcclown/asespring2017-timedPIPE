package hlpn2smt;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import formula.parser.ErrorMsg;
import formula.parser.Formula2SMTZ3;
import formula.parser.Parse;
import formula.absyntree.Sentence;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.Transition;

public class HLPNModelToZ3Converter {

	DataLayer model;
	static int depth;
	ArrayList<Property> properties;
	HashMap<String, Integer> placeNameIdMap;
	String nextline = "\n";
	Place[] places;
	Transition[] transitions;
	
	//places are sharing common data types(data type to multiple places), Integer indicates the i-th datatype sort
	ArrayList<Vector<String>> placeTypes;
	HashMap<String, Integer> placeNameSortMap;
	HashMap<String, Integer> stringConstantMap;
	public static int stringConstCounter = 0;
	PropertyBuilder pb;
	
	public HLPNModelToZ3Converter(DataLayer _model, int _depth, ArrayList<Property> _prop){
		this.model = _model;
		HLPNModelToZ3Converter.depth = _depth;
		this.properties = _prop;
		placeNameIdMap = new HashMap<String, Integer>();
		placeNameSortMap = new HashMap<String,Integer>();
		placeTypes = new ArrayList<Vector<String>>();
		stringConstantMap = new HashMap<String,Integer>();
		
		places = model.getPlaces();
		transitions = model.getTransitions();
		iniPlaceNameSortMap();
		
		//property building initiation
		pb = new PropertyBuilder(model, properties, placeNameIdMap, placeNameSortMap, stringConstantMap);
		
		//convert and save
		completeChecking();
	}
	/**
	 * if base step checking is SAT true, return trace;
	 * if induction step checking is UNSAT return TRUE; property hold;
	 * 		else k++; checking new k base step...
	 */
	
	public void completeChecking() {
		baseStepChecking();
		refineOutputs("z3output.txt");
	}
	
	public void baseStepChecking() {
		saveToFile(convert(depth));		
	}
	
	public String convert(int depth){
		StringBuilder z3str = new StringBuilder();
		String functionHead = "void "+ model.pnmlName + "Checker(Z3_context ctx) {\n";
		z3str.append(functionHead);
		System.out.print("");
		z3str.append(declaration());
		z3str.append(buildStates());			
		z3str.append(iniStates());
		z3str.append(buildTransitions());
		z3str.append(pb.buildProperties());
		z3str.append("\n}\n");
		return z3str.toString();
	}
	
//	public String inductionConvert() {
//		StringBuilder z3str = new StringBuilder();
//		String functionHead = "void "+ model.pnmlName + "InductionChecker(Z3_context ctx) {\n";
//		z3str.append(functionHead);
//		System.out.print("");
//		z3str.append(declaration());
//		z3str.append(buildStates());
//		//initial state constraint removed in induction step
////		z3str.append(iniTokens());
//		z3str.append(buildTransitions());
//		z3str.append(buildLoopFreePath());
////		pb.set_new_bakery_protocal_mutex();
//		z3str.append(pb.buildInductionProperties());
//		z3str.append("\n}\n");
//		return z3str.toString();
//	}
	
	//when there is replicated place types, we don't create new type but leverage the types defined before
	public void iniPlaceNameSortMap(){
		
		for(int i=0;i<places.length;i++)
		{
			DataType dt = places[i].getDataType();
			boolean isContained = false;
			int index = -1;
			for(int j=0;j<placeTypes.size();j++)
			{
				if(dt.getTypes().equals(placeTypes.get(j)))
				{
					isContained = true;
					index = j;
					break;
				}
			}
			if(isContained)
			{
				placeNameSortMap.put(places[i].getName(), index);
			}else
			{
				placeTypes.add(dt.getTypes());
				index = placeTypes.size()-1;
				placeNameSortMap.put(places[i].getName(), index);
			}
		}
	}

	public String buildNewCheckerFromTemplate(String templateName, String z3str, String checkerName){
		String template = "";
		try {
			InputStream ins = new BufferedInputStream(getClass().getResourceAsStream(templateName));
			template = IOUtils.toString(ins);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		String newChecker = template.replaceAll(Pattern.quote("/**insert checker function **/"), z3str);
		newChecker = newChecker.replaceAll(Pattern.quote("/**checker function name**/"), checkerName);
		return newChecker;
	}
	
	public void saveToFile(String z3str){
		String newchecker = buildNewCheckerFromTemplate("/checkertemplate.tmpl", z3str, model.pnmlName+"Checker");
		//replace template with new checker function
		
		String outFileName = String.format("%sChecker.c", model.pnmlName);
		try{
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFileName));
			IOUtils.write(newchecker, fos);
			fos.close();
			System.out.println(model.pnmlName + "C Checker saved to: " + new File(outFileName).getAbsolutePath());
		}catch (IOException e) {
            throw new RuntimeException(e);
        }
		try {
			long startTime = System.currentTimeMillis();
			System.out.println("Start Checking...");
//			String command = "/WinRunZ3.bat";   //Modified on 11/7/15
			String command = "LinuxRunZ3.sh";
			if(System.getProperty("os.name").startsWith("Mac OS X")){
				command = "MacRunZ3.sh";
		 	}
			else if(System.getProperty("os.name").startsWith("Windows")) {
				command = "commands/WinRunZ3.bat";
			}

			File commandFile = new File(getClass().getResource("/"+command).toURI());
			File commandSpace = commandFile.getParentFile();
			Process process = Runtime.getRuntime().exec(command+" > out.log 2>&err.log", new String[]{outFileName}, commandSpace);
			System.out.println("CheckING...");
			int result = process.waitFor();
			InputStream errorStream = process.getErrorStream();
			System.out.println("Result:: "+IOUtils.toString(errorStream));
//			p = Runtime.getRuntime().exec("./MacRunZ3.sh "+model.pnmlName+"Checker.c");
//			p = Runtime.getRuntime().exec("./LinuxRunZ3.sh "+model.pnmlName+"Checker.c");
//			p = Runtime.getRuntime().exec(".\\WinRunZ3.bat "+model.pnmlName+"Checker.c");
			
			System.out.println("Z3Checking Finish!!!");
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("time elapsed for checking is: "+totalTime/1000.0+" seconds.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
	
	public void refineOutputs(String outputFileName){
		StringBuilder newOutput = new StringBuilder();
		try {
			InputStream ins = new BufferedInputStream(new FileInputStream(outputFileName));
			String temp = IOUtils.toString(ins);
//			Pattern state = Pattern.compile("S\\d+.*\\(State [^.$^)]*\\)\\)");
			Pattern state = Pattern.compile("S\\d+\\s->\\s\\(.*?\\)\\)", Pattern.DOTALL);
			Matcher s = state.matcher(temp);
			while(s.find()){
				newOutput.append(nextline+s.group(0));
			}
			
			Pattern tokens = Pattern.compile("k!.*\\{[^.$]*\\}");
			Matcher t = tokens.matcher(temp);
			while(t.find()){
				newOutput.append(nextline+t.group(0));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//append string to integer map
		newOutput.append(nextline+this.stringConstantMap.toString());
		
		//save to new output
		try{
			OutputStream fos = new BufferedOutputStream(new FileOutputStream("newOutput.txt"));
			IOUtils.write(newOutput.toString(), fos);
			fos.close();
		}catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	
	private String declaration(){
		StringBuilder z3decl = new StringBuilder();
		String logstr = "LOG_MSG(\"Test a high level Petri net, unrolled transitions\");\n" +
		"printf(\"Test a high level Petri net, unrolled transitions\");"+nextline;
		z3decl.append(logstr);
		//declare functions
		z3decl.append("//function declarations"+nextline);
		int psize = places.length;
		int sort_size = this.placeTypes.size();
		String declfuncstr = "Z3_func_decl mk_tuple_decl, proj_decls["+psize+"];\n";
		String declpnames = "Z3_symbol names["+psize+"];\n";
		String declpsorts = "Z3_sort sorts["+psize+"];\n";
		z3decl.append(declfuncstr+declpnames+declpsorts);
		
		//initialize place names
		for(int i=0;i<psize;i++){
			String name = "names["+i+"] = Z3_mk_string_symbol(ctx, \""+places[i].getName()+"\");"+nextline;
			placeNameIdMap.put(places[i].getName(), i);
			z3decl.append(name);
		}
		//initialize place sorts
		for(int j=0;j<sort_size;j++){
			String buildsort = mkPlaceSort(placeTypes.get(j), j);
//			String assignsort = "sorts["+j+"] = Z3_mk_set_sort(ctx, "+ places[j].getName() +"SORT);"+nextline;
			z3decl.append(buildsort);
		}
		//assign sort to each places
		for(int k=0;k<psize;k++){
			z3decl.append("sorts["+k+"] = Z3_mk_set_sort(ctx, DT"+ this.placeNameSortMap.get(places[k].getName()) +"SORT);"+nextline);
		}
		
		//initialize state tuple sort
		z3decl.append("Z3_sort STATE_TUPLE = Z3_mk_tuple_sort(ctx, Z3_mk_string_symbol(ctx, \"State\"), "+
				psize+", names, sorts, &mk_tuple_decl, proj_decls);"+nextline);
		
		return z3decl.toString();
	}
	
	private String buildStates(){
		StringBuilder states = new StringBuilder();
		states.append("//build all depth number of states"+nextline);
		for(int i=0;i<=depth;i++){//build depth+1 states
			String declstate = "Z3_ast S"+i+" = Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+i+"\"), STATE_TUPLE);\n";
			states.append(declstate);
		}
		
		return states.toString();
	}
	
	/**
	 * for induction checking, where initial states are not specified with value but should limit token numbers in the ini state
	 * @return
	 */
	private String iniTokens() {
		StringBuilder ini = new StringBuilder();
		int psize = places.length;
		int t = 0;
		ini.append("Z3_ast S0_and["+psize+"];"+nextline);
		for(int i=0;i<psize;i++){
			int tokcount = places[i].getToken().getTokenCount();
			ini.append("Z3_ast ini_set"+t+" = Z3_mk_empty_set(ctx, "+ getSortByPlaceName(places[i].getName())+");"+nextline);
			t++;
			if(tokcount > 0){
				
				for(int j=0;j<tokcount;j++){
					String newtok = "Z3_ast "+places[i].getName()+"_tok"+j+" = " +
							"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \""+places[i].getName()+"_tok"+j+"\"), "+getSortByPlaceName(places[i].getName())+");"
							+nextline;
					ini.append(newtok);
//					DataType dt = places[i].getDataType();
//					int dtsize = dt.getNumofElement();
//					Token tok = places[i].getToken().getTokenbyIndex(j);
//					
//					ini.append("Z3_ast "+places[i].getName()+"_tok"+j+"_and["+dtsize+"];"+nextline);
//					for(int k=0;k<dtsize;k++){
//						int val;
//						if(tok.Tlist.get(k).kind == 0){
//							val = tok.Tlist.get(k).Tint;
//						}else{
//							String s = tok.Tlist.get(k).Tstring;
//							if(stringConstantMap.containsKey(s)){
//								val = stringConstantMap.get(s);
//							}else{
//								val = HLPNModelToZ3Converter.stringConstCounter;
//								this.stringConstantMap.put(s, HLPNModelToZ3Converter.stringConstCounter++);
//							}
//						}
//						
//						String f = places[i].getName()+"_tok"+j+"_and["+k+"] = " +
//								"Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+this.placeNameSortMap.get(places[i].getName())+"_proj_decls["+k+"], "
//								+places[i].getName()+"_tok"+j+"), mk_int(ctx, "+val+"));"+
//								nextline;
//						ini.append(f);
//					}
//					ini.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+dtsize+", "+places[i].getName()+"_tok"+j+"_and));"+nextline);
					ini.append("Z3_ast ini_set"+t+" = Z3_mk_set_add(ctx, ini_set"+(t-1)+", "+places[i].getName()+"_tok"+j+");"+nextline);
					t++;
				}
			}
			ini.append("S0_and["+i+"] = Z3_mk_eq(ctx, "+getPlaceSet(0, places[i].getName())+", ini_set"+(t-1)+");"+nextline);
		}
		ini.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+psize+", S0_and));"+nextline);
		return ini.toString();
	}
	
	private String iniStates(){
		StringBuilder ini = new StringBuilder();
		//for each place, if it has a token, build an empty set, add all tokens to the set, then assign to state 0;
		int psize = places.length;
		int t = 0;
		ini.append("Z3_ast S0_and["+psize+"];"+nextline);
		for(int i=0;i<psize;i++){
			int tokcount = places[i].getToken().getTokenCount();
			ini.append("Z3_ast ini_set"+t+" = Z3_mk_empty_set(ctx, "+ getSortByPlaceName(places[i].getName())+");"+nextline);
			t++;
			if(tokcount > 0){
				
				for(int j=0;j<tokcount;j++){
					String newtok = "Z3_ast "+places[i].getName()+"_tok"+j+" = " +
							"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \""+places[i].getName()+"_tok"+j+"\"), "+getSortByPlaceName(places[i].getName())+");"
							+nextline;
					ini.append(newtok);
					DataType dt = places[i].getDataType();
					int dtsize = dt.getNumofElement();
					Token tok = places[i].getToken().getTokenbyIndex(j);
					
					ini.append("Z3_ast "+places[i].getName()+"_tok"+j+"_and["+dtsize+"];"+nextline);
					for(int k=0;k<dtsize;k++){
						int val;
						if(tok.Tlist.get(k).kind == 0){
							val = tok.Tlist.get(k).getValueAsInt();
						}else{
							String s = tok.Tlist.get(k).getValueAsString();
							if(stringConstantMap.containsKey(s)){
								val = stringConstantMap.get(s);
							}else{
								val = HLPNModelToZ3Converter.stringConstCounter;
								this.stringConstantMap.put(s, HLPNModelToZ3Converter.stringConstCounter++);
							}
						}
						
						String f = places[i].getName()+"_tok"+j+"_and["+k+"] = " +
								"Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+this.placeNameSortMap.get(places[i].getName())+"_proj_decls["+k+"], "
								+places[i].getName()+"_tok"+j+"), mk_int(ctx, "+val+"));"+
								nextline;
						ini.append(f);
					}
					ini.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+dtsize+", "+places[i].getName()+"_tok"+j+"_and));"+nextline);
					ini.append("Z3_ast ini_set"+t+" = Z3_mk_set_add(ctx, ini_set"+(t-1)+", "+places[i].getName()+"_tok"+j+");"+nextline);
					t++;
				}
			}
			ini.append("S0_and["+i+"] = Z3_mk_eq(ctx, "+getPlaceSet(0, places[i].getName())+", ini_set"+(t-1)+");"+nextline);
		}
		ini.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+psize+", S0_and));"+nextline);
		return ini.toString();
	}
	
	private String buildTransitions(){
		StringBuilder z3transitions = new StringBuilder();
		z3transitions.append("//transitions"+nextline);
		//build depth number of transitions
		z3transitions.append("Z3_ast transitions_and["+depth+"];"+nextline);
		for(int i=0;i<depth;i++){
			z3transitions.append(nextline);
			z3transitions.append(oneBigTrans(i));
			String t = "transitions_and["+i+"] = BigTrans_S"+i+";"+nextline;
			z3transitions.append(t);
		}
		z3transitions.append(nextline);
		z3transitions.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+depth+", transitions_and));"+nextline);
		
		return z3transitions.toString();
	}
	
	private String oneBigTrans(int currentStateID){
		StringBuilder trans = new StringBuilder();
		Transition[] transitions = model.getTransitions();
		int tsize = transitions.length+1;//the extra 1 is used for adding a dump transition
		trans.append("Z3_ast S"+currentStateID+"_trans_or["+tsize+"];"+nextline);
		for(int i=0;i<tsize-1;i++){
			trans.append(oneTrans(currentStateID, i));
			String t = "S"+currentStateID+"_trans_or["+i+"] = t"+i+"S"+currentStateID+";"+nextline;
			trans.append(t);
		}
		//add dump transition
		String tid = "tDumpS"+currentStateID;
		trans.append("Z3_ast "+tid+"_and["+places.length+"];"+nextline);
		for(int i=0;i<places.length;i++){
			String pname = places[i].getName();
			trans.append(tid+"_and["+i+"] = "+
					mk_eq(getPlaceSet(currentStateID+1, pname), getPlaceSet(currentStateID, pname))+
					";"+nextline);
		}
		trans.append("Z3_ast "+tid+" = " +
				mk_and(places.length, tid+"_and")+";"+nextline);
		trans.append("S"+currentStateID+"_trans_or["+(tsize-1)+"] = Z3_mk_implies(ctx, Z3_mk_true(ctx), "+tid+");"+nextline);
		
		//disjunct small transitions to a big one
		trans.append("Z3_ast BigTrans_S"+currentStateID+" = Z3_mk_or(ctx, "+tsize+", S"+currentStateID+"_trans_or);"+nextline);
		return trans.toString();
	}	
	
	private String oneTrans(int currentStateID, int currTransID){
		StringBuilder oneTrans = new StringBuilder();
		Transition t = model.getTransition(currTransID);
		String transitionName = t.getName();
		String formula = t.getFormula();
		ArrayList<String> arcVarList = new ArrayList<String>();
		ArrayList<String> transPreConds = new ArrayList<String>();
		
		//build token const for every arc variables
		//add token_arcvar const belongs to place set
		Iterator<Arc> itr_in = t.getArcInList().iterator();
		while(itr_in.hasNext()){
			Arc thisArc = itr_in.next();
			String arcVar = thisArc.getVar();
			String inPlaceName = thisArc.getSource().getName();
			arcVarList.add(arcVar);
			String z3var = "S"+currentStateID+"_"+transitionName+"_"+inPlaceName+"_"+arcVar;
			if(!(thisArc.getSource() instanceof Place)){
				continue;
			}
			
			//if it is powerset, new a const set and mk_eq to place set;
			//if it is a regular place, new a const token, and mk_set_member to place set;
			String setArcVarToPlaceClause = "";
			if(((Place)(thisArc.getSource())).getDataType().getPow()){
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						"Z3_mk_set_sort(ctx, "+getSortByPlaceName(inPlaceName)+"));"
						+nextline);	
				setArcVarToPlaceClause =
					"Z3_mk_eq(ctx, "+z3var+", "+getPlaceSet(currentStateID, inPlaceName)+")";
			}else{
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						getSortByPlaceName(inPlaceName)+");"
						+nextline);
				setArcVarToPlaceClause = 
					"Z3_mk_set_member(ctx, "+z3var+", "+getPlaceSet(currentStateID, inPlaceName)+")";
			}
			if(!setArcVarToPlaceClause.equals(""))
				transPreConds.add(setArcVarToPlaceClause);
		}
		Iterator<Arc> itr_out = t.getArcOutList().iterator();
		while(itr_out.hasNext()){
			Arc thisArc = itr_out.next();
			String arcVar = thisArc.getVar();
			String outPlaceName = thisArc.getTarget().getName();
//			TODO: solve the arcVar ' pie problem
//			if(arcVar.charAt(arcVar.length()-1) == 47){
//				arcVar = arcVar.substring(0, arcVar.length()-1) + "_pie";
//			}
			arcVarList.add(arcVar);
			String z3var = "S"+currentStateID+"_"+transitionName+"_"+outPlaceName+"_"+arcVar;
			if(!(thisArc.getTarget() instanceof Place)){
				continue;
			}
			//if it is powerset, new a const set and mk_eq to place set;
			//if it is a regular place, new a const token, and mk_set_member to place set;
			if(((Place)(thisArc.getTarget())).getDataType().getPow()){
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						"Z3_mk_set_sort(ctx, "+getSortByPlaceName(outPlaceName)+"));"
						+nextline);	
			}else{
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+
						getSortByPlaceName(outPlaceName)+");"
						+nextline);
			}
			
//			String setArcVarToPlaceClause = 
//				"Z3_mk_set_member(ctx, "+z3var+", "+getPlaceSet(currentStateID, outPlaceName)+")";
//			transPreConds.add(setArcVarToPlaceClause);			
		}
		
		ErrorMsg errorMsg = new ErrorMsg(formula);
		Parse p = new Parse(formula, errorMsg);
		Sentence s = p.absyn;
		Formula2SMTZ3 f2z = new Formula2SMTZ3(errorMsg, t, currentStateID, 
				this.placeNameIdMap, this.placeNameSortMap, this.stringConstantMap);
		s.accept(f2z);
		transPreConds.add(s.z3str);
		
		//extra variables are generated from uservariables when quantifier presents.
		for(String extraVars:f2z.z3GetExtraVars()){
			oneTrans.append(extraVars+nextline);
		}
		transPreConds.addAll(f2z.z3GetPreConds());
//		transPreConds.addAll(f2z.z3GetPostConds());//all the clauses are considered as pre-conditions, trans_true only deal with places add or del tokens
		
		//prepare info for transitions
		ArrayList<Place> placeInList = t.getPlaceInList();
		ArrayList<Place> placeOutList = t.getPlaceOutList();
		ArrayList<Place> restPlacesList = new ArrayList<Place>(Arrays.asList(places)); 
		restPlacesList.removeAll(placeInList);
		restPlacesList.removeAll(placeOutList);
		
		String tsid = "t"+currTransID+"S"+currentStateID;
		//t0S0_cond
		oneTrans.append("Z3_ast "+tsid+"_cond_and["+transPreConds.size()+"];"+nextline);//t0S0_cond_and[size]
		for(int i=0;i<transPreConds.size();i++){
			oneTrans.append(tsid+"_cond_and["+i+"] = "
					+transPreConds.get(i)+";"+nextline);
		}		
		oneTrans.append("Z3_ast "+tsid+"_cond " +
				"= Z3_mk_and(ctx, "+transPreConds.size()+", t"+currTransID+"S"+currentStateID+"_cond_and);"+nextline);//t0S0_cond
		
		//t0S0_true
		String true_and = tsid+"_true_and";
		String conditions = "";
		
		int and_ID = 0;
		Iterator<Arc> itr_in_true = t.getArcInList().iterator();
		while(itr_in_true.hasNext()){
			Arc thisArc = itr_in_true.next();
			String arcVar = thisArc.getVar();
			//power set place does not automatically delete token when fire
			if (((Place)(thisArc.getSource())).getDataType().getPow())
				continue;
			String inPlaceName = thisArc.getSource().getName();
			String z3Var = "S"+currentStateID+"_"+transitionName+"_"+inPlaceName+"_"+arcVar;
			conditions += true_and+"["+and_ID+"] = "
					+mk_eq(getPlaceSet(currentStateID+1, inPlaceName), set_del(getPlaceSet(currentStateID, inPlaceName), z3Var))
					+";"+nextline;
//			oneTrans.append(true_and+"["+and_ID+"] = "
//					+mk_eq(getPlaceSet(currentStateID+1, inPlaceName), set_del(getPlaceSet(currentStateID, inPlaceName), z3Var))
//					+";"+nextline);
			and_ID++;
		}
		Iterator<Arc> itr_out_true = t.getArcOutList().iterator();
		while(itr_out_true.hasNext()){
			Arc thisArc = itr_out_true.next();
			String arcVar = thisArc.getVar();
			String outPlaceName = thisArc.getTarget().getName();
			String z3Var = "S"+currentStateID+"_"+transitionName+"_"+outPlaceName+"_"+arcVar;
			if (((Place)(thisArc.getTarget())).getDataType().getPow()){
				conditions += true_and+"["+and_ID+"] = "
						+mk_eq(getPlaceSet(currentStateID+1, outPlaceName), z3Var)
						+";"+nextline;
//				oneTrans.append(true_and+"["+and_ID+"] = "
//						+mk_eq(getPlaceSet(currentStateID+1, outPlaceName), z3Var)
//						+";"+nextline);
			}else{
				conditions += true_and+"["+and_ID+"] = "
						+mk_eq(getPlaceSet(currentStateID+1, outPlaceName), set_add(getPlaceSet(currentStateID, outPlaceName), z3Var))
						+";"+nextline;
//				oneTrans.append(true_and+"["+and_ID+"] = "
//						+mk_eq(getPlaceSet(currentStateID+1, outPlaceName), set_add(getPlaceSet(currentStateID, outPlaceName), z3Var))
//						+";"+nextline);
			}
			and_ID++;
		}
		Iterator<Place> itr_rest = restPlacesList.iterator();
		while(itr_rest.hasNext()){
			Place thisPlace = itr_rest.next();
			String restPlaceName = thisPlace.getName();
			conditions += true_and+"["+and_ID+"] = "+
					mk_eq(getPlaceSet(currentStateID+1, restPlaceName), getPlaceSet(currentStateID, restPlaceName))+
					";"+nextline;
//			oneTrans.append(true_and+"["+and_ID+"] = "+
//					mk_eq(getPlaceSet(currentStateID+1, restPlaceName), getPlaceSet(currentStateID, restPlaceName))+
//					";"+nextline);
			and_ID++;
		}
		
		oneTrans.append("Z3_ast "+true_and+"["+and_ID+"];"+nextline);//t0S0_true_and[]
		oneTrans.append(conditions);//append conditions
		
		//To this transition, when arc in place and arc out place is the same place, the check is meaningless and wrong.
//		if(and_ID != places.length){
//			System.out.println("State: "+currentStateID+" Transition: "+currTransID+" arraylist size mismatch!");
//		}
		
		oneTrans.append("Z3_ast "+tsid+"_true = "+
				mk_and(and_ID, true_and)+";"+nextline);
		
		///t0S0_false
		oneTrans.append("Z3_ast "+tsid+"_false_and["+places.length+"];"+nextline);
		for(int i=0;i<places.length;i++){
			String pname = places[i].getName();
			oneTrans.append(tsid+"_false_and["+i+"] = "+
					mk_eq(getPlaceSet(currentStateID+1, pname), getPlaceSet(currentStateID, pname))+
					";"+nextline);
		}
		oneTrans.append("Z3_ast "+tsid+"_false = " +
				mk_and(places.length, tsid+"_false_and")+";"+nextline);
		
		//if-then-else
		oneTrans.append("Z3_ast "+tsid+" = Z3_mk_ite(ctx, "+
				tsid+"_cond, "+tsid+"_true, "+tsid+"_false);"+
				nextline);
		return oneTrans.toString();
	}
	
	/**
	 * buildLoopFreePath is by adding constraints that non of the state is equal
	 */
	private String buildLoopFreePath() {
		StringBuilder loopFree = new StringBuilder();
		loopFree.append(nextline+"//loopFree path construction"+nextline);
		loopFree.append("Z3_ast loopFree_and["+(depth+1)+"];"+nextline);
		for(int i =0;i<=depth;i++) {
			String andStr = "loopFree_and["+i+"] = S"+i+";"+nextline;
			loopFree.append(andStr);
		}
		loopFree.append("Z3_assert_cnstr(ctx, Z3_mk_distinct(ctx, "+(depth+1)+", loopFree_and));"+nextline);
		return loopFree.toString();
	}
	

	
	/**
	 * return a place in the model of sort set
	 * @param state
	 * @param placeName
	 * @return
	 */
	public String getPlaceSet(int state, String placeName ){
		return "mk_unary_app(ctx, proj_decls["+this.placeNameIdMap.get(placeName)+"], S"+state+")";
	}
	
	
	/**
	 * return a field in a place set
	 * @param state
	 * @param placeName
	 * @param field
	 * @return
	 */
	public String getPlaceField(int state, String placeName, int field){
		String getPlace = "mk_unary_app(ctx, proj_decls["+this.placeNameIdMap.get(placeName)+"], S"+state+")";
		String getField = "mk_unary_app(ctx, "+placeName+"_proj_decls["+field+"], "+getPlace+")";
		return getField;
	}
	
	/**
	 * build a place type (tuple) sort
	 * @param p
	 * @return
	 */
	private String mkPlaceSort(Vector<String> types, int index){
		int dtSize = types.size();
		StringBuilder z3sort = new StringBuilder();
		z3sort.append("Z3_func_decl "+"DT"+index+"_mk_tuple_decl;"+nextline);
		z3sort.append("Z3_func_decl "+"DT"+index+"_proj_decls["+dtSize+"];"+nextline);
		z3sort.append("Z3_symbol "+"DT"+index+"_names["+dtSize+"];"+nextline);
		z3sort.append("Z3_sort "+"DT"+index+"_sorts["+dtSize+"];"+nextline);
		for(int i=0;i<dtSize;i++){
			String namestr = "DT"+index+"f"+i;
			String name = "DT"+index+"_names["+i+"] = Z3_mk_string_symbol(ctx, \""+namestr+"\");"+nextline;
			z3sort.append(name);
			String sort = "DT"+index+"_sorts["+i+"] = Z3_mk_int_sort(ctx);"+nextline;
			z3sort.append(sort);
		}
		z3sort.append("Z3_sort "+"DT"+index+"SORT = Z3_mk_tuple_sort(ctx, " +
				"Z3_mk_string_symbol(ctx, \""+"DT"+index+"SORT\"), "+dtSize+", "+
				"DT"+index+"_names, "+"DT"+index+"_sorts, &"+"DT"+index+"_mk_tuple_decl, "+
				"DT"+index+"_proj_decls);"+nextline);
		return z3sort.toString();
	}
	
	/**
	 * z3_mk_eq(left,right)
	 * Z3_mk_eq (__in Z3_context c, __in Z3_ast l, __in Z3_ast r)
	 * @param left
	 * @param right
	 * @return
	 */
	private String mk_eq(String left, String right){
		return "Z3_mk_eq(ctx, "+left+", "+right+")";
	}
	
	/**
	 * Z3_mk_and (__in Z3_context c, __in unsigned num_args, __in_ecount(num_args) Z3_ast const args[])
	 * @param andSize
	 * @param andList
	 * @return
	 */
	private String mk_and(int andSize, String andList){
		return "Z3_mk_and(ctx, "+andSize+", "+andList+")";
	}
	/**
	 * 
	 * @param set
	 * @param elem
	 * @return
	 */
	private String set_add(String set, String elem){
		return "Z3_mk_set_add(ctx, "+set+", "+elem+")";
	}
	/**
	 * 
	 * @param set
	 * @param elem
	 * @return
	 */
	private String set_del(String set, String elem){
		return "Z3_mk_set_del(ctx, "+set+", "+elem+")";
	}
	/**
	 * 
	 * @param set
	 * @param elem
	 * @return
	 */
	private String set_member(String set, String elem){
		return "Z3_mk_set_member(ctx, "+elem+", "+set+")";
	}
	
	private String getSortByPlaceName(String pname){
		return "DT"+this.placeNameSortMap.get(pname)+"SORT";
	}
}





