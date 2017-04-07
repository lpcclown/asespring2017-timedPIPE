package hlpn2smt;

import java.util.ArrayList;
import java.util.HashMap;

import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.Transition;

public class PropertyBuilder {
	DataLayer model;
	ArrayList<Property> properties;
	HashMap<String, Integer> placeNameIdMap;
	String nextline = "\n";
	Place[] places;
	Transition[] transitions;
	
	HashMap<String, Integer> placeNameSortMap;
	HashMap<String, Integer> stringConstantMap;
	
	public PropertyBuilder(DataLayer _model, ArrayList<Property> _properties, HashMap<String, Integer> _placeNameIdMap,  
			HashMap<String, Integer> _placeNameSortMap, HashMap<String, Integer> _stringConstantMap) {
		this.model = _model;
		this.properties = _properties;
		this.placeNameIdMap = _placeNameIdMap;
		this.placeNameSortMap = _placeNameSortMap;
		this.stringConstantMap = _stringConstantMap;
		places = model.getPlaces();
		transitions = model.getTransitions();
		
	}
	/** property for refined method on share memory model
	 *  test more than 5 processors with same property
	 */
	public void set_SharedMemory() {
		String placeName0 = "Ext_Mem_Acc";
		Place p0  = model.getPlaceByName(placeName0);
		DataType dt0 = p0.getDataType();
		Token tok0 = new Token(dt0);
		tok0.Tlist.add(new BasicType(0, 3,""));
		tok0.Tlist.add(new BasicType(0, 0,""));
		this.properties.add(new Property(placeName0, tok0,  Property.RelationType.CONJUNCTION, Property.Operator.EQ));

		String placeName1 = "Ext_Mem_Acc";
		Place p1  = model.getPlaceByName(placeName1);
		DataType dt1 = p1.getDataType();
		Token tok1 = new Token(dt1);
		tok1.Tlist.add(new BasicType(0, 2,""));
		tok1.Tlist.add(new BasicType(0, 4,""));
		this.properties.add(new Property(placeName1, tok1,  Property.RelationType.CONJUNCTION, Property.Operator.NEQ));
		}
	
	/**
	 * set property for seabed rig workflow
	 */
	public void seabedRigProp() {
		String placeName0 = "RG_CenterDown";
		Place p0  = model.getPlaceByName(placeName0);
		DataType dt0 = p0.getDataType();
		Token tok0 = new Token(dt0);
		tok0.Tlist.add(new BasicType(0, 1,""));
//		tok0.Tlist.add(new BasicType(0, 0,""));
		this.properties.add(new Property(placeName0, tok0,  Property.RelationType.CONJUNCTION, Property.Operator.EQ));
		
	}
	
	public void seabedRigProp2() {
		String placeName0 = "RO_hasGripper";
		Place p0  = model.getPlaceByName(placeName0);
		DataType dt0 = p0.getDataType();
		Token tok0 = new Token(dt0);
		tok0.Tlist.add(new BasicType(0, 1,""));
		tok0.Tlist.add(new BasicType(0, 1,""));
		this.properties.add(new Property(placeName0, tok0,  Property.RelationType.CONJUNCTION, Property.Operator.EQ));
	}

	/**
	 * set new property is for testing two directional algorithm
	 */
	public void set_newprop() {
		String placeName0 = "P5";
		Place p0  = model.getPlaceByName(placeName0);
		DataType dt0 = p0.getDataType();
		Token tok0 = new Token(dt0);
		tok0.Tlist.add(new BasicType(0, 5,""));
//		tok0.Tlist.add(new BasicType(0, 0,""));
		this.properties.add(new Property(placeName0, tok0,  Property.RelationType.CONJUNCTION, Property.Operator.EQ));
		
	}
	

	/**
	 * this build properties function is for reachability problem whether a  specified token will appear in a place or not at any state.
	 * 11/20/2013
	 * adding considered conjunction/disjunction property;
	 * adding eq/neq operators to property;
	 * @return
	 */
	public String buildProperties() {
		StringBuilder property = new StringBuilder();
		property.append(nextline+"//properties"+nextline);
		property.append("Z3_ast property_or["+(HLPNModelToZ3Converter.depth+1)+"];"+nextline);
		
		for(int i=0;i<=HLPNModelToZ3Converter.depth;i++) {
			String propertyFormula = "Z3_mk_true(ctx)"; //for the use of building all properties from inside which is true;
			for(int p=0;p<properties.size();p++) {
				//collect info
				Property curProp = properties.get(p);
				String placeName = curProp.placeName;
				Token t = curProp.tok;
				//build a token in z3
					//mk token const
				String z3Tok = "Z3_ast PS"+i+"Tok"+p+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"PS"+i+"_"+placeName+"_tok"+p+"\"), "+
						getSortByPlaceName(placeName)+");"+ nextline;
				property.append(z3Tok);
				//set datas inside token
				property.append("Z3_ast S"+i+placeName+"_tok_"+p+"_and["+t.Tlist.size()+"];"+nextline);
				for(int k=0;k<t.Tlist.size();k++){
					int intval;
					if(t.Tlist.get(k).kind==0){
						intval = t.Tlist.get(k).getValueAsInt();
					}else{
						String s = t.Tlist.get(k).getValueAsString();
						if(stringConstantMap.containsKey(s)){
							intval = stringConstantMap.get(s);
						}else{
							intval = HLPNModelToZ3Converter.stringConstCounter;
							this.stringConstantMap.put(s, HLPNModelToZ3Converter.stringConstCounter++);
						}
					}
					String val = "mk_int(ctx, "+intval+")";
					property.append("S"+i+placeName+"_tok_"+p+"_and["+k+"] = " +
							"Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+this.placeNameSortMap.get(placeName)+"_proj_decls["+k+"], PS"+i+"Tok"+p+"), "+val+");"+nextline); 
				}
				property.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+t.Tlist.size()+", S"+i+placeName+"_tok_"+p+"_and));"+nextline);
				//at depth i, place has this token
				String newTokenProp = "";
				if(curProp.op.equals(Property.Operator.EQ)) {
					newTokenProp = "Z3_mk_set_member(ctx, PS"+i+"Tok"+p+", "+getPlaceSet(i, placeName)+")";
				}else if(curProp.op.equals(Property.Operator.NEQ)) {
					newTokenProp = "Z3_mk_not(ctx, Z3_mk_set_member(ctx, PS"+i+"Tok"+p+", "+getPlaceSet(i, placeName)+"))";
				}else {
					System.out.println("Property operator not included!");
				}
				
				if(curProp.type.equals(Property.RelationType.CONJUNCTION)) {
					propertyFormula = "mk_and(ctx, "+propertyFormula+", "+newTokenProp+")";
				}else if(curProp.type.equals(Property.RelationType.DISJUNCTION)) {
					propertyFormula = "mk_or(ctx, "+propertyFormula+", "+newTokenProp+")";
				}else {
					System.out.println("Property RelationType not included!");
				}
			}
			property.append("property_or["+i+"] = "+propertyFormula+";"+nextline);
		}
		
		property.append("Z3_assert_cnstr(ctx, Z3_mk_or(ctx, "+(HLPNModelToZ3Converter.depth+1)+", property_or));"+nextline);
		return property.toString();
	}
	
	/**
	 * this build properties function is for reachability problem whether a  specified token will appear in a place or not at any state.
	 * 
	 * @return
	 */
	public String buildProperties1(){
		StringBuilder property = new StringBuilder();
		property.append(nextline+"//properties"+nextline);
		property.append("Z3_ast property_or["+(HLPNModelToZ3Converter.depth+1)+"];"+nextline);
		
		for(int i=0;i<=HLPNModelToZ3Converter.depth;i++){
			property.append("Z3_ast PS"+i+"_and["+properties.size()+"];"+nextline);
			for(int p=0;p<properties.size();p++){
				//collect info
				Property curProp = properties.get(p);
				String placeName = curProp.placeName;
				Token t = curProp.tok;
				//build a token in z3
					//mk token const
				String z3Tok = "Z3_ast PS"+i+"Tok"+p+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"PS"+i+"_"+placeName+"_tok"+p+"\"), "+
						getSortByPlaceName(placeName)+");"+ nextline;
				property.append(z3Tok);
					//set datas inside token
				property.append("Z3_ast S"+i+placeName+"_tok_"+p+"_and["+t.Tlist.size()+"];"+nextline);
				for(int k=0;k<t.Tlist.size();k++){
					int intval;
					if(t.Tlist.get(k).kind==0){
						intval = t.Tlist.get(k).getValueAsInt();
					}else{
						String s = t.Tlist.get(k).getValueAsString();
						if(stringConstantMap.containsKey(s)){
							intval = stringConstantMap.get(s);
						}else{
							intval = HLPNModelToZ3Converter.stringConstCounter;
							this.stringConstantMap.put(s, HLPNModelToZ3Converter.stringConstCounter++);
						}
					}
					String val = "mk_int(ctx, "+intval+")";
					property.append("S"+i+placeName+"_tok_"+p+"_and["+k+"] = " +
							"Z3_mk_eq(ctx, mk_unary_app(ctx, DT"+this.placeNameSortMap.get(placeName)+"_proj_decls["+k+"], PS"+i+"Tok"+p+"), "+val+");"+nextline); 
				}
				property.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+t.Tlist.size()+", S"+i+placeName+"_tok_"+p+"_and));"+nextline);
				//at depth i, place has this token
				//TODO:add place does not have this token when non-equal property
				property.append("PS"+i+"_and["+p+"] = "+
						"Z3_mk_set_member(ctx, PS"+i+"Tok"+p+", "+getPlaceSet(i, placeName)+");"+
						nextline);
			}
			//TODO:add mk_or for disjunctive properties
			property.append("Z3_ast PS"+i+" = Z3_mk_and(ctx, "+properties.size()+", PS"+i+"_and);"+nextline);
			property.append("property_or["+i+"] = PS"+i+";"+nextline);
		}
		
		property.append("Z3_assert_cnstr(ctx, Z3_mk_or(ctx, "+(HLPNModelToZ3Converter.depth+1)+", property_or));"+nextline);
		
		return property.toString();
	}
	
	/**
	 * buildMondexProperties is for building the mondex sum of all purse balance is specified. 
	 * there is no c api in z3 to get the n-th element from a set
	 */
	public String buildMondexProperties() {
		String balanceSum = "mk_int(ctx, 300)";
		
		StringBuilder property = new StringBuilder();
		property.append(nextline+"//properties"+nextline);
		property.append("Z3_ast property_or["+(HLPNModelToZ3Converter.depth+1)+"];"+nextline);
		
		for(int i=0;i<=HLPNModelToZ3Converter.depth;i++){
			//purse 1 in place AND purse2 in place AND purse1!=purse2 AND purse1.balance + purse2.balance != balanceSum
			//so it has 4 AND clauses
			property.append("Z3_ast PS"+i+"_and[4];"+nextline); 
			//get place name "ConPurse"
			String placeName = "ConPurse";
			//build a token for purse 0
			String z3TokPurse0 = "Z3_ast PS"+i+"Purse0 = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"PS"+i+"_Purse0\"), "+
					getSortByPlaceName(placeName)+");"+ nextline;
			property.append(z3TokPurse0);
			//build a token for purse 1
			String z3TokPurse1 = "Z3_ast PS"+i+"Purse1 = " +
					"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"PS"+i+"_Purse1\"), "+
					getSortByPlaceName(placeName)+");"+ nextline;
			property.append(z3TokPurse1);
			
			//at depth i, ConPurse has purse 0 and purse 1, setMember
			property.append("PS"+i+"_and[0] = "+
					"Z3_mk_set_member(ctx, PS"+i+"Purse0, "+getPlaceSet(i, placeName)+");"+
					nextline);
			property.append("PS"+i+"_and[1] = "+
					"Z3_mk_set_member(ctx, PS"+i+"Purse1, "+getPlaceSet(i, placeName)+");"+
					nextline);
			//purse0 != purse1
			property.append("PS"+i+"_and[2] = "+
					"Z3_mk_not(ctx, Z3_mk_eq(ctx, PS"+i+"Purse0, PS"+i+"Purse1));"+
					nextline
			);
			//purse0 balance + purse1 balance > balanceSum
			String purse1bal = "mk_unary_app(ctx, DT"+this.placeNameSortMap.get(placeName)+"_proj_decls[1], PS"+i+"Purse0)";
			String purse2bal = "mk_unary_app(ctx, DT"+this.placeNameSortMap.get(placeName)+"_proj_decls[1], PS"+i+"Purse1)";
			property.append("PS"+i+"_and[3] = "+
					"Z3_mk_not(ctx, Z3_mk_le(ctx, mk_add(ctx, "+purse1bal+", "+purse2bal+"), "+balanceSum+"));"+
					nextline
			);
			property.append("Z3_ast PS"+i+" = Z3_mk_and(ctx, 4, PS"+i+"_and);"+nextline);
			property.append("property_or["+i+"] = PS"+i+";"+nextline);
		}
		property.append("Z3_assert_cnstr(ctx, Z3_mk_or(ctx, "+(HLPNModelToZ3Converter.depth+1)+", property_or));"+nextline);		
		return property.toString();
	}
	
	/**
	 * build counter properties
	 * make an empty set and check whether the counter place equal to the empty set or not for each state
	 */
	public String buildCounterProperties() {
		StringBuilder property = new StringBuilder();
		property.append(nextline+"//counter properties"+nextline);
		property.append("Z3_ast property_or["+(HLPNModelToZ3Converter.depth+1)+"];"+nextline);
		//make an empty set called "EMPTYSET"
		String placeName = "counter";
		String placeSort = "DT"+this.placeNameSortMap.get(placeName)+"SORT";
		property.append("Z3_ast EMPTYSET = Z3_mk_empty_set(ctx, "+placeSort+");"+nextline);
		
		for(int i=0;i<=HLPNModelToZ3Converter.depth;i++){
			//EMPTYSET = counter place set
			String counterPlace = getPlaceSet(i, placeName);
			property.append("Z3_ast PS"+i+" = Z3_mk_eq(ctx, EMPTYSET, "+counterPlace+");"+nextline);
			property.append("property_or["+i+"] = PS"+i+";"+nextline);
		}
		property.append("Z3_assert_cnstr(ctx, Z3_mk_or(ctx, "+(HLPNModelToZ3Converter.depth+1)+", property_or));"+nextline);		
		return property.toString();
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
