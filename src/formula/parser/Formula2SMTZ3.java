package formula.parser;

import formula.absyntree.*;
import hlpn2smt.HLPNModelToZ3Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.Arc;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;

public class Formula2SMTZ3 implements Visitor{
	ErrorMsg errorMsg;
	SymbolTable symTable;
	Transition iTransition;
	HashMap<String, Integer> placeNameIdMap;
	HashMap<String, Integer> placeNameSortMap;
	HashMap<String, String> userVarPlaceNameMap;
	HashMap<String, Integer> stringConstantMap;
	ArrayList<String> pre_conds;
//	ArrayList<String> post_conds;
	ArrayList<String> extra_vars;//include declarations of uservariables and temp variables
	int stateId;
	private static int tempId = 0;
	
	public Formula2SMTZ3(ErrorMsg _errorMsg, Transition transition, 
			int _stateId, HashMap<String, Integer> _placeNameIdMap,
			HashMap<String, Integer> _placeNameSortMap, HashMap<String, Integer> _stringConstantMap){
		this.errorMsg = _errorMsg;
		iTransition = transition;
		this.symTable = iTransition.getTransSymbolTable();
		placeNameIdMap = _placeNameIdMap;
		placeNameSortMap = _placeNameSortMap;
		userVarPlaceNameMap = new HashMap<String, String>();
		stringConstantMap = _stringConstantMap;
		pre_conds = new ArrayList<String>();
//		post_conds = new ArrayList<String>();
		extra_vars = new ArrayList<String>();
		stateId = _stateId;
	}
	
	public ArrayList<String> z3GetPreConds(){
		return pre_conds;
	}
	
//	public ArrayList<String> z3GetPostConds(){
//		return post_conds;
//	}	
	
	public ArrayList<String> z3GetExtraVars(){
		return extra_vars;
	}
//	public String z3GetPlace(int state, String placeName ){
//		return "mk_unary_app(ctx, proj_decls["+this.placeNameIdMap.get(placeName)+"], S"+state+")";
//	}
	
//	public String z3GetPlaceField(int state, String placeName, int field){
//		String getPlace = "mk_unary_app(ctx, proj_decls["+this.placeNameIdMap.get(placeName)+"], S"+state+")";
//		String getField = "mk_unary_app(ctx, "+placeName+"_proj_decls["+(field-1)+"], "+getPlace+")";
//		
//		return getField;
//	}
	
	/**
	 * get arcvar in formula
	 * @param arcVar
	 * @param placeName
	 * @return
	 */
	public String getZ3ArcIdVar(String arcVar, String placeName){
		return "S"+stateId+"_"+iTransition.getName()+"_"+placeName+"_"+arcVar;
	}
	
	/**
	 * 
	 * @param arcVar
	 * @param placeName
	 * @param index
	 * @return
	 */
	public String getZ3ArcIndexVar(String arcVar, String placeName, int index){
		return "mk_unary_app(ctx, DT"+placeNameSortMap.get(placeName)+"_proj_decls["+(index-1)+"], "+getZ3ArcIdVar(arcVar, placeName)+")";
	}
	
	public void visit(AndFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);
		String left = "";
		String right = "";

		if(elem.f1 instanceof AtFormula){
			left = ((AtFormula)(elem.f1)).z3str;
		}else if(elem.f1 instanceof CpFormula){
			left = ((CpFormula)(elem.f1)).z3str;
		}else if(elem.f1 instanceof CpxFormula){
			left = ((CpxFormula)(elem.f1)).z3str;
		}else errorMsg.error(elem.pos, "AndFormula::LHS Formula type mismatch!");
		
		if(elem.f2 instanceof AtFormula){
			right = ((AtFormula)(elem.f2)).z3str;
		}else if(elem.f2 instanceof CpFormula){
			right = ((CpFormula)(elem.f2)).z3str;
		}else if(elem.f2 instanceof CpxFormula){
			right = ((CpxFormula)(elem.f2)).z3str;
		}else errorMsg.error(elem.pos, "AndFormula::RHS Formula type mismatch!");		
		
		elem.z3str = "mk_and(ctx, "+left+", "+right+")";
	}

	@Override
	public void visit(BraceTerm elem) {
		elem.t.accept(this);
		if(elem.t instanceof VariableTerm){
			elem.z3str = ((VariableTerm)(elem.t)).z3str;
			elem.placeName = ((VariableTerm)(elem.t)).placeName;
		}else if(elem.t instanceof ExpTerm){
			elem.z3str = ((ExpTerm)(elem.t)).z3str;
			elem.placeName = ((ExpTerm)(elem.t)).placeName;
		}else if(elem.t instanceof ConstantTerm){
			elem.z3str = ((ConstantTerm)(elem.t)).z3str;
			elem.placeName = ((ConstantTerm)(elem.t)).placeName;
		}else{
			System.out.println("Error: BraceTerm type mismatch!");
		}
		
	}

	@Override
	public void visit(BraceTerms elem) {
		//maintian a list of term
		//the upper level union or diff will make a constant of its left placename type that mk_eq to these terms in the list
		elem.ts.accept(this);
		elem.z3TermList = elem.ts.z3TermList;
	}

	@Override
	public void visit(ComplexFormula elem) {
		elem.q.accept(this);
		elem.uv.accept(this);
		elem.d.accept(this);
		elem.v.accept(this);
		
		
		String placeName = "";
		String arcVar = "";
		String userVariable = ((UserVariable)(elem.uv)).s;
		String powerSetVariable = ((IdVariable)(elem.v)).key;
		
		
		//find connected input place name of powersetvariable
		Iterator<Arc> itr_in = iTransition.getArcInList().iterator();
		while(itr_in.hasNext()){
			Arc thisArc = itr_in.next();
			String var = thisArc.getVar();
			if(var.equals(powerSetVariable)){
				arcVar = var;
				placeName = thisArc.getSource().getName();	
			}
		}
		
		this.userVarPlaceNameMap.put(userVariable, placeName);
//		String z3var = "S"+stateId+"_"+placeName+"_"+userVariable;
		String z3var = "S"+stateId+"_"+iTransition.getName()+"_"+placeName+"_"+userVariable;
		String z3PowVar = "S"+stateId+"_"+iTransition.getName()+"_"+placeName+"_"+powerSetVariable;
		String extravar = "Z3_ast "+z3var+" = " +
			"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+stateId+placeName+userVariable+"\"), "+ 
			"DT"+this.placeNameSortMap.get(placeName)+"SORT"+");";
		extra_vars.add(extravar);
		String pcond = "Z3_mk_set_member(ctx, "+z3var+", "+z3PowVar+")";
		pre_conds.add(pcond);
		
		elem.f.accept(this);
		
		if(elem.f instanceof AtFormula){
			elem.z3str = ((AtFormula)(elem.f)).z3str;
		}else if(elem.f instanceof CpFormula){
			elem.z3str = ((CpFormula)(elem.f)).z3str;
		}else if(elem.f instanceof CpxFormula){
			elem.z3str = ((CpxFormula)(elem.f)).z3str;
		}
	}

	@Override
	public void visit(ConstantTerm elem) {
		//we consider every constant to be integer, so here string is translated into integer
		elem.c.accept(this);
		if(elem.c instanceof NumConstant){
//			elem.int_val = ((NumConstant)(elem.c)).int_val;
			elem.z3str = "mk_int(ctx, "+elem.c.value+")";
		}else if (elem.c instanceof StrConstant){
//			elem.str_val = ((StrConstant)elem.c).str;
//			elem.int_val = stringConstantToInteger(elem.str_val);
			elem.z3str = "mk_int(ctx, "+stringConstantToInteger(elem.c.value.toString())+")";
		}
		
	}
	
	public int stringConstantToInteger(String s){
		
		if(stringConstantMap.containsKey(s)){
			return stringConstantMap.get(s);
		}else{
			this.stringConstantMap.put(s, HLPNModelToZ3Converter.stringConstCounter++);
			return HLPNModelToZ3Converter.stringConstCounter-1;
		}
		
	}

	/**
	 * here we only handle the case: 
	 * left:SET, right: elem or exp(result in an elem)
	 */
	public void visit(Diff elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
			elem.placeName = ((VariableTerm)(elem.t1)).placeName;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
			elem.placeName = ((ExpTerm)(elem.t1)).placeName;
		}

		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
			
			if(elem.placeName.equals("")){
				System.out.println("ERROR:placeName is null");
			}
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerms){
					ArrayList<String> termlist = ((ExpTerm)(elem.t2)).z3TermList;
					String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
					this.extra_vars.add(mk_temp_elem);
					
					for(int i=0;i<termlist.size();i++){
						String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls["+i+"], S"+stateId+"temp_"+(tempId)+"), " +
								termlist.get(i)+")";
							this.pre_conds.add(mk_eq);
					}
					
					right = "S"+stateId+"temp_"+tempId;
					tempId++;
				}else if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerm){
					right = ((ExpTerm)(elem.t2)).z3str;
				}
			}else{ 
				String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
				"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
				this.extra_vars.add(mk_temp_elem);
				String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls[0], S"+stateId+"temp_"+(tempId)+"), " +
					((ExpTerm)(elem.t2)).z3str+")";
				this.pre_conds.add(mk_eq);
				right = "S"+stateId+"temp_"+tempId;
				tempId++;
			}
		}
		elem.z3str = "Z3_mk_set_del(ctx, "+left+", "+right+")";
		elem.isPostCond = false; //set operation limit to be on the right side
	}

	@Override
	public void visit(Div elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			//since the place variable is placesort, not intsort, we need to get out the proj[0] int sort
			if(((VariableTerm)(elem.t1)).v instanceof IdVariable){
				left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			if(((VariableTerm)(elem.t2)).v instanceof IdVariable){
				right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}		
		
		
		elem.z3str = "mk_div(ctx, "+left+", "+right+")";		
	}

	@Override
	public void visit(EqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		boolean useVarName = false;//check if two sides are both IdVar, if yes, just use var name, otherwise, use varname[0]
		
		if(elem.t1 instanceof VariableTerm && elem.t2 instanceof VariableTerm){
			if((((VariableTerm)(elem.t1)).v instanceof IdVariable) && (((VariableTerm)(elem.t2)).v instanceof IdVariable)){
				useVarName = true;
			}
		}
		
		//if left or right hand side of eq is set expression, use var name instead of varName[0];
		if(elem.t1 instanceof ExpTerm){
			if(((ExpTerm)(elem.t1)).e instanceof SExp){
				useVarName = true;
			}
		}
		
		if(elem.t2 instanceof ExpTerm){
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				useVarName = true;
			}
		}
		
		if(elem.t1 instanceof VariableTerm){
			// not necessary to diff idvar and indexvar, since both of them use get place
			elem.isPostCond = ((VariableTerm)(elem.t1)).isPostCond;
			elem.placeName = ((VariableTerm)(elem.t1)).placeName;
			if(!useVarName){
				if(((VariableTerm)(elem.t1)).v instanceof IdVariable)
					left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
				else if(((VariableTerm)(elem.t1)).v instanceof IndexVariable){
					left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, ((VariableTerm)(elem.t1)).index);
				}
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
			elem.placeName = ((ExpTerm)(elem.t1)).placeName;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			// not necessary to diff idvar and indexvar, since both of them use get place
			if(!useVarName){
				if(((VariableTerm)(elem.t2)).v instanceof IdVariable)
					right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
				else if(((VariableTerm)(elem.t2)).v instanceof IndexVariable){
					right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, ((VariableTerm)(elem.t2)).index);
				}
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			if(elem.placeName.equals("")){
				System.out.println("ERROR:placeName is null");
			}
			//if t2 is braceterms
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerms){
					ArrayList<String> termlist = ((ExpTerm)(elem.t2)).z3TermList;
					String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
					this.extra_vars.add(mk_temp_elem);
					
					for(int i=0;i<termlist.size();i++){
						String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls["+i+"], S"+stateId+"temp_"+(tempId)+"), " +
								termlist.get(i)+")";
							this.pre_conds.add(mk_eq);
					}
					
					right = "S"+stateId+"temp_"+tempId;
					tempId++;
				}else{
					right = ((ExpTerm)(elem.t2)).z3str;
				}
			}else if(((ExpTerm)(elem.t2)).e instanceof AExp) {
				right = ((ExpTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ConstantTerm){
				right = ((ConstantTerm)(elem.t2)).z3str;
		}
		elem.z3str = "Z3_mk_eq(ctx, "+left+", "+right+")";
		System.out.println("Eq Relation"+ elem.z3str);   //debug by He 11/15
	}

	@Override
	public void visit(EquivFormula elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Exists elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExpTerm elem) {
		elem.e.accept(this);
		if(elem.e instanceof AExp){
			elem.z3str = ((AExp)(elem.e)).z3str;
			elem.placeName = ((AExp)(elem.e)).placeName;
		}else if(elem.e instanceof RExp){
			elem.z3str = ((RExp)(elem.e)).z3str;
			elem.isPostCond = ((RExp)(elem.e)).isPostCond;
			elem.isValidClause = true;
			elem.placeName = ((RExp)(elem.e)).placeName;
		}else if(elem.e instanceof SExp){
			elem.z3str = ((SExp)(elem.e)).z3str;
			elem.placeName = ((SExp)(elem.e)).placeName;
			elem.z3TermList = ((SExp)(elem.e)).z3TermList;
//			elem.isPostCond = ((RExp)(elem.e)).isPostCond;
		}		
	}

	@Override
	public void visit(False elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ForAll elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}
		
		elem.z3str = "Z3_mk_ge(ctx, "+left+", "+right+")";			
	}

	@Override
	public void visit(GtRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}
		
		elem.z3str = "Z3_mk_gt(ctx, "+left+", "+right+")";		
	}

	@Override
	public void visit(Identifier elem) {
				
	}

	@Override
	public void visit(IdVariable elem) {
		
	}

	@Override
	public void visit(ImpFormula elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(In elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Index elem) {
		elem.n.accept(this);
		elem.int_val = Integer.parseInt(elem.n.n);
	}

	@Override
	public void visit(IndexVariable elem) {
		elem.i.accept(this);
		elem.idx.accept(this);
		
		elem.key = elem.i.key;
		elem.index = elem.idx.int_val;
	}

	@Override
	public void visit(InRel elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}
		
		elem.z3str = "Z3_mk_le(ctx, "+left+", "+right+")";			
	}

	@Override
	public void visit(LtRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}
		
		elem.z3str = "Z3_mk_lt(ctx, "+left+", "+right+")";	
		
	}

	@Override
	public void visit(Minus elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			//since the place variable is placesort, not intsort, we need to get out the proj[0] int sort
			if(((VariableTerm)(elem.t1)).v instanceof IdVariable){
				left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			if(((VariableTerm)(elem.t2)).v instanceof IdVariable){
				right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}		
		
		
		elem.z3str = "mk_sub(ctx, "+left+", "+right+")";
		
	}

	@Override
	public void visit(Mod elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			//since the place variable is placesort, not intsort, we need to get out the proj[0] int sort
			if(((VariableTerm)(elem.t1)).v instanceof IdVariable){
				left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			if(((VariableTerm)(elem.t2)).v instanceof IdVariable){
				right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}		
		
		elem.z3str = "mk_mod(ctx, "+left+", "+right+")";
		
	}

	@Override
	public void visit(Mul elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			//since the place variable is placesort, not intsort, we need to get out the proj[0] int sort
			if(((VariableTerm)(elem.t1)).v instanceof IdVariable){
				left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			if(((VariableTerm)(elem.t2)).v instanceof IdVariable){
				right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}		
		
		
		elem.z3str = "mk_mul(ctx, "+left+", "+right+")";
		
	}

	@Override
	public void visit(NegExp elem) {
		elem.t.accept(this);
		
		String v = "";
		
		if(elem.t instanceof VariableTerm){
			v = ((VariableTerm)(elem.t)).z3str;
		}else if(elem.t instanceof ExpTerm){
			v = ((ExpTerm)(elem.t)).z3str;
		}else if(elem.t instanceof ConstantTerm){
			v = ((ConstantTerm)(elem.t)).z3str;
		}
	
		elem.z3str = "Z3_mk_unary_minus(ctx, "+v+")";		
	}

	@Override
	public void visit(NeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		boolean useVarName = false;//check if two sides are both IdVar, if yes, just use var name, otherwise, use varname[0]
		
		if(elem.t1 instanceof VariableTerm && elem.t2 instanceof VariableTerm){
			if((((VariableTerm)(elem.t1)).v instanceof IdVariable) && (((VariableTerm)(elem.t2)).v instanceof IdVariable)){
				useVarName = true;
			}
		}
		
		//if left or right hand side of eq is set expression, use var name instead of varName[0];
		if(elem.t1 instanceof ExpTerm){
			if(((ExpTerm)(elem.t1)).e instanceof SExp){
				useVarName = true;
			}
		}
		
		if(elem.t2 instanceof ExpTerm){
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				useVarName = true;
			}
		}
		
		if(elem.t1 instanceof VariableTerm){
			// not necessary to diff idvar and indexvar, since both of them use get place
//			elem.isPostCond = ((VariableTerm)(elem.t1)).isPostCond;
			elem.placeName = ((VariableTerm)(elem.t1)).placeName;
			if(!useVarName){
				if(((VariableTerm)(elem.t1)).v instanceof IdVariable)
					left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
				else if(((VariableTerm)(elem.t1)).v instanceof IndexVariable){
					left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, ((VariableTerm)(elem.t1)).index);
				}
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
			elem.placeName = ((ExpTerm)(elem.t1)).placeName;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			// not necessary to diff idvar and indexvar, since both of them use get place
			if(!useVarName){
				if(((VariableTerm)(elem.t2)).v instanceof IdVariable)
					right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
				else if(((VariableTerm)(elem.t2)).v instanceof IndexVariable){
					right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, ((VariableTerm)(elem.t2)).index);
				}
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			if(elem.placeName.equals("")){
				System.out.println("ERROR:placeName is null");
			}
			//if t2 is braceterms
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerms){
					ArrayList<String> termlist = ((ExpTerm)(elem.t2)).z3TermList;
					String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
					this.extra_vars.add(mk_temp_elem);
					
					for(int i=0;i<termlist.size();i++){
						String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls["+i+"], S"+stateId+"temp_"+(tempId)+"), " +
								termlist.get(i)+")";
							this.pre_conds.add(mk_eq);
					}
					
					right = "S"+stateId+"temp_"+tempId;
					tempId++;
				}else{
					right = ((ExpTerm)(elem.t2)).z3str;
				}
			}else if(((ExpTerm)(elem.t2)).e instanceof AExp) {
				right = ((ExpTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ConstantTerm){
				right = ((ConstantTerm)(elem.t2)).z3str;
		}
		
		elem.z3str = "Z3_mk_not(ctx, Z3_mk_eq(ctx, "+left+", "+right+"))";
		
	}

	@Override
	public void visit(Nexists elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Nin elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NinRel elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotFormula elem) {
		elem.f.accept(this);
		
		if(elem.f instanceof AtFormula){
			elem.z3str = "Z3_mk_not(ctx, "+((AtFormula)(elem.f)).z3str+")";
		}else if(elem.f instanceof CpFormula){
			elem.z3str = "Z3_mk_not(ctx, "+((CpFormula)(elem.f)).z3str+")";
		}else if(elem.f instanceof CpxFormula){
			elem.z3str = "Z3_mk_not(ctx, "+((CpxFormula)(elem.f)).z3str+")";
		}
	}

	@Override
	public void visit(NumConstant elem) {
		elem.num.accept(this);
		elem.value = Integer.parseInt(elem.num.n);
	}

	@Override
	public void visit(StrConstant elem) {
				
	}

	@Override
	public void visit(Num elem) {
		
		
	}

	@Override
	public void visit(OrFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);
		String left = "";
		String right = "";
		
		if(elem.f1 instanceof AtFormula){
			left = ((AtFormula)(elem.f1)).z3str;
		}else if(elem.f1 instanceof CpFormula){
			left = ((CpFormula)(elem.f1)).z3str;
		}else if(elem.f1 instanceof CpxFormula){
			left = ((CpxFormula)(elem.f1)).z3str;
		}else errorMsg.error(elem.pos, "AndFormula::LHS Formula type mismatch!");
		
		if(elem.f2 instanceof AtFormula){
			right = ((AtFormula)(elem.f2)).z3str;
		}else if(elem.f2 instanceof CpFormula){
			right = ((CpFormula)(elem.f2)).z3str;
		}else if(elem.f2 instanceof CpxFormula){
			right = ((CpxFormula)(elem.f2)).z3str;
		}else errorMsg.error(elem.pos, "AndFormula::RHS Formula type mismatch!");		
		
		elem.z3str = "mk_or(ctx, "+left+", "+right+")";		
	}

	@Override
	public void visit(Plus elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		
		if(elem.t1 instanceof VariableTerm){
			
			//since the place variable is placesort, not intsort, we need to get out the proj[0] int sort
			if(((VariableTerm)(elem.t1)).v instanceof IdVariable){
				left = this.getZ3ArcIndexVar(((VariableTerm)(elem.t1)).var_key, ((VariableTerm)(elem.t1)).placeName, 1);
			}else{
				left = ((VariableTerm)(elem.t1)).z3str;
			}
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
		}else if(elem.t1 instanceof ConstantTerm){
			left = ((ConstantTerm)(elem.t1)).z3str;
		}
		
		if(elem.t2 instanceof VariableTerm){
			
			if(((VariableTerm)(elem.t2)).v instanceof IdVariable){
				right = this.getZ3ArcIndexVar(((VariableTerm)(elem.t2)).var_key, ((VariableTerm)(elem.t2)).placeName, 1);
			}else{
				right = ((VariableTerm)(elem.t2)).z3str;
			}
		}else if(elem.t2 instanceof ExpTerm){
			right = ((ExpTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ConstantTerm){
			right = ((ConstantTerm)(elem.t2)).z3str;
		}		
		
		
		elem.z3str = "mk_add(ctx, "+left+", "+right+")";
	}

	@Override
	public void visit(TermRest elem) {
		elem.t.accept(this);
		if(elem.t instanceof VariableTerm){
			elem.z3str = ((VariableTerm)(elem.t)).z3str;
			elem.placeName = ((VariableTerm)(elem.t)).placeName;
		}else if(elem.t instanceof ExpTerm){
			elem.z3str = ((ExpTerm)(elem.t)).z3str;
			elem.placeName = ((ExpTerm)(elem.t)).placeName;
		}else if(elem.t instanceof ConstantTerm){
			elem.z3str = ((ConstantTerm)(elem.t)).z3str;
			elem.placeName = ((ConstantTerm)(elem.t)).placeName;
		}else{
			System.out.println("Error: TermRest type mismatch!");
		}
	}

	@Override
	public void visit(Terms elem) {
		elem.t.accept(this);
		
		String z3str = "";
		if(elem.t instanceof VariableTerm){
			z3str = ((VariableTerm)(elem.t)).z3str;
		}else if(elem.t instanceof ConstantTerm){
			z3str = ((ConstantTerm)(elem.t)).z3str;
		}else if(elem.t instanceof ExpTerm){
			z3str = ((ExpTerm)(elem.t)).z3str;
		}else{
			System.out.println("Error: Terms term type not match!");
		}
		elem.z3TermList.add(z3str);
		
		for(int i=0; i<elem.tr.size(); i++){
			elem.tr.elementAt(i).accept(this);
			if(elem.tr.elementAt(i).t instanceof VariableTerm){
				z3str = ((VariableTerm)(elem.tr.elementAt(i).t)).z3str;
			}else if(elem.tr.elementAt(i).t instanceof ConstantTerm){
				z3str = ((ConstantTerm)(elem.tr.elementAt(i).t)).z3str;
			}else if(elem.tr.elementAt(i).t instanceof ExpTerm){
				z3str = ((ExpTerm)(elem.tr.elementAt(i).t)).z3str;
			}else{
				System.out.println("Error: Terms term type not match!");
			}
			elem.z3TermList.add(z3str);
		}
		
	}

	@Override
	public void visit(True elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Union elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = "";
		String right = "";
		//here we only handle the case: 
		// left:SET, right: elem or exp(result in an elem)
		
		if(elem.t1 instanceof VariableTerm){
			left = ((VariableTerm)(elem.t1)).z3str;
			elem.placeName = ((VariableTerm)(elem.t1)).placeName;
		}else if(elem.t1 instanceof ExpTerm){
			left = ((ExpTerm)(elem.t1)).z3str;
			elem.placeName = ((ExpTerm)(elem.t1)).placeName;
		}
		
		if(elem.t2 instanceof VariableTerm){
			right = ((VariableTerm)(elem.t2)).z3str;
		}else if(elem.t2 instanceof ExpTerm){
//			right = ((ExpTerm)(elem.t2)).z3str;
			if(elem.placeName.equals("")){
				System.out.println("ERROR:placeName is null");
			}
			//if t2 is braceterms
			if(((ExpTerm)(elem.t2)).e instanceof SExp){
				if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerms){
					ArrayList<String> termlist = ((ExpTerm)(elem.t2)).z3TermList;
					String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
					this.extra_vars.add(mk_temp_elem);
					
					for(int i=0;i<termlist.size();i++){
						String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls["+i+"], S"+stateId+"temp_"+(tempId)+"), " +
								termlist.get(i)+")";
							this.pre_conds.add(mk_eq);
					}
					
					right = "S"+stateId+"temp_"+tempId;
					tempId++;
				}else if(((SExp)(((ExpTerm)(elem.t2)).e)).se instanceof BraceTerm){
					right = ((ExpTerm)(elem.t2)).z3str;
				}
			}else{ //TODO:here we need to know if other types Aexp and Rexp are the same??
				String mk_temp_elem = "Z3_ast S"+stateId+"temp_"+tempId+" = " +
				"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"temp_"+tempId+"\"), DT"+this.placeNameSortMap.get(elem.placeName)+"SORT);";
				this.extra_vars.add(mk_temp_elem);
				String mk_eq = "Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+placeNameSortMap.get(elem.placeName)+"_proj_decls[0], S"+stateId+"temp_"+(tempId)+"), " +
					((ExpTerm)(elem.t2)).z3str+")";
				this.pre_conds.add(mk_eq);
				right = "S"+stateId+"temp_"+tempId;
				tempId++;
			}
		}
		elem.z3str = "Z3_mk_set_add(ctx, "+left+", "+right+")";
		elem.isPostCond = false; //set operation limit to be on the right side
		
	}

	@Override
	public void visit(Setdef elem) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(FunctionExp pTerm) {
		throw new RuntimeException("Not Impletemented yet");
	}

	@Override
	public void visit(UserVariable elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VariableTerm elem) {
		elem.v.accept(this);

		elem.var_key = elem.v.key;
		System.out.println("Key"+ elem.var_key);   //debug by He 11/15
		String placeName = findNextPlaceName(elem);
		if (StringUtils.isBlank(placeName)) {
			//it may be a uservariable
			placeName = this.userVarPlaceNameMap.get(elem.var_key);
		}

		if(StringUtils.isBlank(placeName)) {
			throw new IllegalStateException(String.format("Could not find related place name for the variable  key %s of term ", elem.var_key, elem));
		}

		elem.placeName = placeName;
		if(elem.v instanceof IdVariable){
			elem.z3str = this.getZ3ArcIdVar(elem.var_key, elem.placeName);
		}
		else if(elem.v instanceof IndexVariable){
			elem.index = ((IndexVariable) (elem.v)).index;
			if(elem.index<1){
				System.out.println("Error: variable index cannot less than 1");
			}
			elem.z3str = this.getZ3ArcIndexVar(elem.var_key, elem.placeName, elem.index);
		}
	}

	private String findNextPlaceName(final VariableTerm pElem) {
		String placeName = "";
		for (Arc thisArc : iTransition.getArcOutList()) {
			Place po = (Place)(thisArc.getTarget());
			String[] vars = new String[] { thisArc.getVar()};
			if(po.getToken().getDataType().getPow())
			{
				vars = thisArc.getVars();
			}

			for(int i=0;i<vars.length;i++)
			{
				if(vars[i].equals(pElem.var_key)) {
					pElem.isPostCond = true;
					placeName = po.getName();
					break;
				}
			}
		}

		for (Arc thisArc : iTransition.getArcInList()) {
			Place pi = (Place)(thisArc.getSource());
			String[] vars = new String[]{thisArc.getVar()};
			if(pi.getToken().getDataType().getPow())
			{
				vars = thisArc.getVars();
			}

				for(int i=0;i<vars.length;i++)
				{
					if(vars[i].equals(pElem.var_key)) {
						pElem.isPostCond = false;
						placeName = pi.getName();
						break;
					}
				}
		}

		return placeName;
	}

	@Override
	public void visit(AExp elem) {
		elem.ae.accept(this);
		
		if(elem.ae instanceof Minus){
			elem.z3str = ((Minus)(elem.ae)).z3str;
		}else if(elem.ae instanceof Plus){
			elem.z3str = ((Plus)(elem.ae)).z3str;
		}else if(elem.ae instanceof Mul){
			elem.z3str = ((Mul)(elem.ae)).z3str;
		}else if(elem.ae instanceof Mod){
			elem.z3str = ((Mod)(elem.ae)).z3str;
		}else if(elem.ae instanceof NegExp){
			elem.z3str = ((NegExp)(elem.ae)).z3str;
		}else errorMsg.error(elem.pos, "AExp::tree type mismatch!");		
	}

	@Override
	public void visit(RExp elem) {
		elem.re.accept(this);
		
		if(elem.re instanceof EqRel){
			elem.isPostCond = ((EqRel)(elem.re)).isPostCond;
			elem.z3str = ((EqRel)(elem.re)).z3str;
		}else if(elem.re instanceof NeqRel){
			elem.z3str = ((NeqRel)(elem.re)).z3str;
		}else if(elem.re instanceof GtRel){
			elem.z3str = ((GtRel)(elem.re)).z3str;
		}else if(elem.re instanceof LtRel){
			elem.z3str = ((LtRel)(elem.re)).z3str;
		}else if(elem.re instanceof GeqRel){
			elem.z3str = ((GeqRel)(elem.re)).z3str;
		}else if(elem.re instanceof LeqRel){
			elem.z3str = ((LeqRel)(elem.re)).z3str;
		}else if(elem.re instanceof InRel){
			elem.z3str = ((InRel)(elem.re)).z3str;
		}else if(elem.re instanceof NinRel){
			elem.z3str = ((NinRel)(elem.re)).z3str;
		}else errorMsg.error(elem.pos, "RExp::tree type mismatch!");
	}

	@Override
	public void visit(SExp elem) {
		elem.se.accept(this);
		
		if(elem.se instanceof Union){
			elem.z3str = ((Union)(elem.se)).z3str;
			elem.placeName = ((Union)(elem.se)).placeName;
		}else if(elem.se instanceof Diff){
			elem.z3str = ((Diff)(elem.se)).z3str;
			elem.placeName = ((Diff)(elem.se)).placeName;
		}else if(elem.se instanceof BraceTerm){
			elem.z3str = ((BraceTerm)(elem.se)).z3str;
			elem.placeName = ((BraceTerm)(elem.se)).placeName;
		}else if(elem.se instanceof BraceTerms){
			elem.z3TermList = ((BraceTerms)(elem.se)).z3TermList;
		}
		
	}

	@Override
	public void visit(AtomicTerm elem) {
		elem.t.accept(this);
		
		if(elem.t instanceof ConstantTerm){
			elem.z3str = ((ConstantTerm)(elem.t)).z3str;
			elem.isPostCond = false;
		}else if(elem.t instanceof VariableTerm){
			elem.z3str = ((VariableTerm)(elem.t)).z3str;
			elem.isPostCond = ((VariableTerm)(elem.t)).isPostCond;
		}else if(elem.t instanceof ExpTerm){
			elem.z3str = ((ExpTerm)(elem.t)).z3str;
			elem.isPostCond = ((ExpTerm)(elem.t)).isPostCond;
			elem.isValidClause = ((ExpTerm)(elem.t)).isValidClause;
		}else errorMsg.error(elem.pos, "AtomicTerm::Cannot be VariableTerm or tree type mismatch!");
				
	}


	public void visit(AtFormula elem) {
		elem.af.accept(this);
		
		if(elem.af instanceof NotFormula){
//			elem.z3str = "Z3_mk_not(ctx, "+((NotFormula)(elem.af)).z3str+")";
			elem.z3str = ((NotFormula)(elem.af)).z3str;
		}else if(elem.af instanceof AtomicTerm){
			elem.isPostCond = ((AtomicTerm)(elem.af)).isPostCond;
			elem.z3str = ((AtomicTerm)(elem.af)).z3str;
//			elem.isValidClause = ((AtomicTerm)(elem.af)).isValidClause;
//			if(!elem.isPostCond){
//				if(elem.isValidClause)
//					this.pre_conds.add(elem.z3str);
//			}else{
//				if(elem.isValidClause)
//					this.post_conds.add(elem.z3str);
//			}
		}else errorMsg.error(elem.pos, "AtFormula::tree type mismatch!");
	}

	
	public void visit(CpFormula elem) {
		elem.cf.accept(this);
	
		if(elem.cf instanceof AndFormula){
			elem.z3str = ((AndFormula)(elem.cf)).z3str;
		}else if(elem.cf instanceof OrFormula){
			elem.z3str = ((OrFormula)(elem.cf)).z3str;
		}else if(elem.cf instanceof ImpFormula){
			elem.z3str = ((ImpFormula)(elem.cf)).z3str;
		}else if(elem.cf instanceof EquivFormula){
			elem.z3str = ((EquivFormula)(elem.cf)).z3str;
		}else errorMsg.error(elem.pos, "CpFormula::tree type mismatch!");		
	}

	@Override
	public void visit(CpxFormula elem) {
		elem.cpf.accept(this);
		elem.z3str = elem.cpf.z3str;
	}

	public void visit(Sentence elem) {
		elem.f.accept(this);
		
		if(elem.f instanceof AtFormula){
			elem.z3str = ((AtFormula)(elem.f)).z3str;
		}else if(elem.f instanceof CpFormula){
			elem.z3str = ((CpFormula)(elem.f)).z3str;
		}else if(elem.f instanceof CpxFormula){
			elem.z3str = ((CpxFormula)(elem.f)).z3str;
		}		
	}

	@Override
	public void visit(Empty elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EmptyTerm elem) {
		// TODO Auto-generated method stub
		
	}

}
