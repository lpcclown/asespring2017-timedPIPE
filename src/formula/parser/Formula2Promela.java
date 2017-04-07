package formula.parser;

import analysis.FunctionGenerator;
import analysis.PromelaUtil;
import analysis.QuantifierEvalInfo;
import analysis.SetDefinitionInfo;
import formula.absyntree.*;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.Arc;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static analysis.QuantifierEvalInfo.QuantifierType;

public class Formula2Promela implements Visitor{

  private static final AtomicInteger sQuantifierCounter = new AtomicInteger(1);
  private static final String FIELD_ASSIGNMENT_TEMPLATE = "  %s.field%d = %s.field%d;%n";

  private static final int COMPLEX_CONTEXT_QUANTIFIER = 32;
  private static final int COMPLEX_CONTEXT_SETDEF = 64;

  ErrorMsg errorMsg;
	SymbolTable symTable;
	Transition iTransition;
	int mode = 0;
	
	//For ComplexFormula
	String cpx_v1, cpx_v2;
	String cpx_PlaceName1, cpx_PlaceName2;
	int cpx_index = 0; //v1 and v2 are both vacant, 1 means v1 occupied
	                   //2 means v1 and v2 both occupied
//	public ArrayList<UserVarType> arrUserVar;
  private List<QuantifierEvalInfo> quantifiersEvalInfo = new ArrayList<>(3);
	private String preCondition;
	private String postCondition;
	private String tempPlaceName = "";

  private HashMap<String, VariableDefinition> mVariableDefinitions = new HashMap<>();
  private HashMap<String, UserVarType> mUserVariables = new HashMap<>(3);
  Map<String, String> mSetAssignments = new HashMap();
  List<SetDefinitionInfo> setDefinitionInfo = new ArrayList<>(3);
	Set<String> invokedFunction = new HashSet<>();
	Map<String, FunctionGenerator> functionGeneratorFactory;

  private int complexFormulaContext;

  /**
   * List of place names those involves in "All Tokens" transfer.
   */
  private Set<String> includedPlaces = new HashSet<>();

	String pname_union = ""; // in case "union", set place name for "!"
	boolean pname_union_status = false;  //once true, indicating output "!" for union
	
	public Formula2Promela(ErrorMsg errorMsg, Transition transition, int mode){
		this.errorMsg = errorMsg;
		iTransition = transition;
		this.symTable = iTransition.getTransSymbolTable();
		this.mode = mode;
//		arrUserVar = new ArrayList<>();
	}
	
	public String termTostring(Term t){
		String st = "";
		if(t instanceof VariableTerm){
			st = ((VariableTerm)t).pVarName;
		}else if(t instanceof ConstantTerm){
			st = PromelaUtil.stringToMType(((ConstantTerm)t).var_key);
		}else if(t instanceof ExpTerm){
			st = ((ExpTerm)t).str;
		}
    ObjectUtils.identityToString(st);
		return st; 
	}
	
	public String getPrestr(Formula t){
		String st = "";
		if (t instanceof AtFormula || t instanceof CpFormula || t instanceof CpxFormula) {
			st = t.strPre;
		}
		return st; 
	}
	
	public String getPoststr(Formula t){
		String st = "";
		if (t instanceof AtFormula || t instanceof CpFormula || t instanceof CpxFormula) {
			st = t.strPost;
		}
		return st; 
	}
	
	@Override
	public void visit(AndFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);

    elem.strPre = constructionBinaryConditions(getPrestr(elem.f1), getPrestr(elem.f2), " && ", false, "true");
    elem.strPost = constructionBinaryConditions(getPoststr(elem.f1), getPoststr(elem.f2), "", false, "");
	}

  private String constructionBinaryConditions(final String pLeft, final String pRight, final String pSeparator, final boolean pStrict, String pFallback) {
    StringBuilder sb = new StringBuilder();
    if (pStrict && (StringUtils.isBlank(pLeft) || StringUtils.isBlank(pRight))) {
      throw new IllegalStateException("One or both of the expressions are empty in strict mode");
    }

    if (StringUtils.isNotBlank(pLeft)) {
      sb.append(pLeft).append(pSeparator);
    }

    if(StringUtils.isNotBlank(pRight)) {
      sb.append(pRight);
    }
    else {
      if(sb.length() == 0) {
//        both the parts are empty. return the fallback
        return pFallback;
      }

//      only second part is empty. remove the separator
      sb.setLength(sb.length() - pSeparator.length());
    }

    return sb.toString();
  }

  @Override
	public void visit(BraceTerm elem) {
		elem.t.accept(this);
		
		if(elem.t instanceof VariableTerm){
			elem.varName = ((VariableTerm)(elem.t)).pVarName;
			elem.isUserVariable = ((VariableTerm)(elem.t)).isUserVariable;
			elem.placeName = ((VariableTerm)(elem.t)).placeName;
		}

	}

	@Override
	public void visit(BraceTerms elem) {
		elem.ts.accept(this);
		elem.str = elem.ts.str;
		elem.placeName = elem.ts.placeName;
	}

	@Override
	public void visit(ComplexFormula elem) {
		
		elem.q.accept(this);
		elem.uv.accept(this);
		elem.d.accept(this);
		elem.v.accept(this);
	

		String userVariable = elem.uv.s;
		String powerSetVariable = elem.v.key;
		String vPlaceName = "";
		//find user variable matched place
		for (Arc arc : iTransition.getArcInList()) {
			//The powerset must be in input places
			if (arc.getVar().equals(powerSetVariable)) {
				vPlaceName = arc.getSource().getName();
//				this.arrUserVar.add(new UserVarType(userVariable, powerSetVariable, vPlaceName));
        VariableDefinition vd = getVariableDefinition(userVariable);
        vd.setType(VariableDefinition.Type.LOCAL);
        vd.addInputPlaceName(vPlaceName);
				mUserVariables.put(userVariable, (new UserVarType(userVariable, powerSetVariable, vPlaceName)));
				break;
			}
		}
//		if (cpx_index == 0) {
//			cpx_PlaceName1 = vPlaceName;
//			cpx_v1 = ((UserVariable)(elem.uv)).s;
//			cpx_index++;
//		}
//		else if (cpx_index == 1) {
//			cpx_PlaceName2 = vPlaceName;
//			cpx_v2 = ((UserVariable)(elem.uv)).s;
//			cpx_index++;
//		}
		 
		elem.f.accept(this);

    if (complexFormulaContext == COMPLEX_CONTEXT_SETDEF) {
      elem.strPre = elem.f.strPre;
      elem.strPost = elem.f.strPost;
    }
    else {
      int quantifierNumber = sQuantifierCounter.getAndIncrement();
      String functionName = String.format("evaluate_quantifier_%d", quantifierNumber);
      String returnVariable = String.format("q_%d_accepted", quantifierNumber);
      quantifiersEvalInfo.add(QuantifierEvalInfo.builder()
          .setFunctionName(functionName)
          .setLoopVariableName(elem.uv.s)
          .setDeciderVariable(returnVariable)
          .setSourceName(String.format("%s", vPlaceName))
          .setCondition(elem.f.strPre)
          .setType(elem.q instanceof ForAll ? QuantifierType.FORALL : QuantifierType.EXISTS)
          .build());

      elem.strPre = String.format("%s == true", returnVariable);
      elem.strPost = elem.f.strPost;
    }

//    System.out.println(String.format("Quantifier precondition: %s", formulaPre));
//    System.out.println(String.format("Quantifier postcondition: %s", formulaPost));

//    if(elem.f instanceof CpxFormula){
//			formulaPre = ((CpxFormula)(elem.f)).strPre;
//			formulaPost = ((CpxFormula)(elem.f)).strPost;
//		}
//		else if (elem.f instanceof CpFormula){
//			formulaPre = ((CpFormula)(elem.f)).strPre;
//			formulaPost = ((CpFormula)(elem.f)).strPost;
//		}

		//write promela for complex formula
//		elem.strPre = "atomic{\n";
//		elem.strPre += "	pick(var_"+vPlaceName+", place_"+vPlaceName
//			+", "+vPlaceName+");\n";
//		elem.strPre += "	place_"+vPlaceName+"?<"+vPlaceName+">;\n";
//		elem.strPre += "	"+formulaPre+"\n";
//		elem.strPre += "	}";
	}

	@Override
	public void visit(Diff elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		elem.str = "";
		String t1PlaceName = "";
		String t2PlaceName = "";
//		assert pname_union_status;
		
		if(elem.t1 instanceof VariableTerm){
			t1PlaceName = ((VariableTerm)(elem.t1)).placeName;
			if(elem.t2 instanceof ExpTerm){
				t2PlaceName = ((ExpTerm)(elem.t2)).placeName;
				if(t1PlaceName.equalsIgnoreCase(t2PlaceName) && !("").equals(t1PlaceName)){
					elem.str += "	place_"+t1PlaceName+"?"+t1PlaceName+";\n";
				}
				
			}
		}
		
		if(!("").equals(t1PlaceName)){
			this.tempPlaceName = t1PlaceName;
			elem.placeName = t1PlaceName;
		}
	}

	@Override
	public void visit(Div elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
		elem.str = String.format("%s / %s", left, right);
		
	}

	@Override
	public void visit(EqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		//judge eq is post condition or a eq relation, 
		//determined only by t1
		boolean postcond = false;

		if (elem.t1 instanceof VariableTerm)
		{
      VariableDefinition vd = getVariableDefinition(((VariableTerm) elem.t1).var_key);
			postcond = !vd.isInputVariable() && vd.isOutputVariable();
		}
		else if (elem.t1 instanceof ExpTerm)
		{
			postcond = ((ExpTerm)(elem.t1)).postcond;
		}
		
		/**
		 * written by Su Liu
		 * Changed by X. He 12/18/15
		 */
		if (postcond == true) {
			//when eq means post assignment
			elem.strPost = "";
			Place thisPlace = null;
			for (Arc ao : iTransition.getArcOutList()) {
				if (ao.getTarget().getName().equals(elem.t1.placeName)) {
					thisPlace = (Place) ao.getTarget();
					break;
				}
			}

      Objects.requireNonNull(thisPlace, String.format("%s does not correspond to any valid Place", elem.t1.placeName));

      String leftPlaceNameToInclude = elem.t1.placeName;
      String rightPlaceNameToInclude = elem.t2.placeName;
			if(elem.t1 instanceof VariableTerm) {
        VariableTerm leftTerm = (VariableTerm) elem.t1;
        if (elem.t2 instanceof VariableTerm) {
          VariableTerm rightTerm = (VariableTerm) elem.t2;

          VariableDefinition left = getVariableDefinition(leftTerm.v.key);
          VariableDefinition right = getVariableDefinition(rightTerm.v.key);

          if (leftTerm.kind == 0 && rightTerm.kind == 0) {
//            both are IdVariable. The assignment should be field by field assignment
            Vector<String> types = thisPlace.getDataType().getTypes();
            StringBuilder sb = new StringBuilder();
            if (left.isSetVariable() && right.isSetVariable()) {
//              String template = "    "+FIELD_ASSIGNMENT_TEMPLATE;
//              sb.append("  do\n  ::");
//              sb.append(String.format("  place_%s?%s;%n", rightTerm.placeName, rightTerm.placeName));
//              for (int j = 1; j <= types.size(); j++){
//                sb.append(String.format(template, leftTerm.placeName, j, rightTerm.placeName, j));
//              }
//              sb.append(String.format("      place_%s!%s;%n", leftTerm.placeName, leftTerm.placeName));
//              sb.append("  od\n");
//
//              leftPlaceNameToInclude = null;
//              rightPlaceNameToInclude = null;
              mSetAssignments.put(left.getVariableName(), right.getVariableName());
            }
            else {
              for (int j = 1; j <= types.size(); j++){
                sb.append(String.format("  %s = %s", PromelaUtil.getFieldName(leftTerm.var_key, j), PromelaUtil.getFieldName(rightTerm.var_key, j)));
              }
            }
            elem.strPost = sb.toString();
          }
          else {
//          if either of the two is IndexVariable, the other must be either an IndexVariable or has a single field data type
            elem.strPost = String.format("  %s = %s;%n", leftTerm.pVarName, rightTerm.pVarName);
          }
        }
        else if (elem.t2 instanceof ExpTerm) {
          Exp exp = ((ExpTerm) elem.t2).e;
          //TODO: revisit this for non-single-valued Exps like SExp
          if (exp instanceof SExp) {
            elem.strPost = String.format("%s(place_%s);%n", elem.t2.str, leftTerm.placeName);
            mVariableDefinitions.remove(leftTerm.var_key);
          }
					else if (exp instanceof FunctionExp) {
						elem.strPost = String.format("  %s;  %s = %s;%n", elem.t2.str, leftTerm.pVarName, ((FunctionExp)exp).outputVariable );
					}
          else {
            elem.strPost = String.format("  %s = %s;%n", leftTerm.pVarName, elem.t2.str);
          }
        }
        else if (elem.t2 instanceof ConstantTerm) {
          elem.strPost = String.format("  %s = %s;%n", leftTerm.pVarName, ((ConstantTerm) elem.t2).var_key);
        }
			}

      if (leftPlaceNameToInclude != null) {
        includedPlaces.add(leftPlaceNameToInclude);
      }

      if (rightPlaceNameToInclude != null) {
        includedPlaces.add(rightPlaceNameToInclude);
      }
		}
    else {
      //when eq means relation
      String left = termTostring(elem.t1);
      String right = termTostring(elem.t2);
      elem.strPre = String.format("%s == %s", left, right);
    }
	}

	@Override
	public void visit(EquivFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);

		String leftPre = getPrestr(elem.f1);
		String rightPre = getPrestr(elem.f2);
		String leftPost = getPoststr(elem.f1);
		String rightPost= getPoststr(elem.f2);

    if (StringUtils.isNotBlank(leftPre) && StringUtils.isNotBlank(rightPre)) {
      elem.strPre = String.format("(%s && %s || !(%s || %s))", leftPre, rightPre, leftPre, rightPre);
		}
    else if(StringUtils.isBlank(leftPre)){
			elem.strPre = rightPre;
		}
    else if(StringUtils.isBlank(rightPre)){
			elem.strPre = leftPre;
		}
    else {
      elem.strPre = "true";
    }

    elem.strPost = constructionBinaryConditions(leftPost, rightPost, "", false, "");
	}

	@Override
	public void visit(Exists elem) {

	}

	@Override
	public void visit(False elem) {

	}

	@Override
	public void visit(ForAll elem) {

	}

	@Override
	public void visit(GeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
		String right = termTostring(elem.t2);
		elem.strPre = String.format("%s >= %s", left, right);
	}

	@Override
	public void visit(GtRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

    String left = termTostring(elem.t1);
		String right = termTostring(elem.t2);
		elem.strPre = String.format("%s > %s", left, right);
	}

	@Override
	public void visit(Identifier elem) {

	}

	@Override  //rewritten by He 12/17/15
	public void visit(IdVariable elem) {
//    addVariableDefinitions(elem.key);
	}

  @Override
	public void visit(ImpFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);

		String leftPre = getPrestr(elem.f1);
		String rightPre = getPrestr(elem.f2);
		String leftPost = getPoststr(elem.f1);
		String rightPost= getPoststr(elem.f2);
	
    if (StringUtils.isNotBlank(leftPre) && StringUtils.isNotBlank(rightPre)) {
      elem.strPre = String.format("(!(%s) || %s)", leftPre, rightPre);
    }
    else if(StringUtils.isBlank(leftPre)){
      elem.strPre = "true";
    }
    else {
      elem.strPre = leftPre;
    }

    elem.strPost = constructionBinaryConditions(leftPost, rightPost, "", false, "");
	}

	@Override
	public void visit(In elem) {

	}

	@Override
	public void visit(Index elem) {
		elem.n.accept(this);
	}

	@Override  //Rewritten by He 12/17/15
	public void visit(IndexVariable elem) {
		elem.i.accept(this);
		elem.idx.accept(this);

//    addVariableDefinitions(elem.i.key);
	}

	@Override
	public void visit(InRel elem) {

		elem.t1.accept(this);
		elem.t2.accept(this);
		 
	}

	@Override
	public void visit(LeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
    elem.strPre = String.format("%s <= %s", left, right);
	}

	@Override
	public void visit(LtRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
    elem.strPre = String.format("%s < %s", left, right);
	}

	@Override
	public void visit(Minus elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
		elem.str = String.format("%s - %s", left, right);

	}

	@Override
	public void visit(Mod elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
		elem.str = String.format("%s %% %s", left, right);
	}

	@Override
	public void visit(Mul elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
		elem.str = String.format("%s * %s", left, right);
	}


	public void visit(NegExp elem) {
		elem.t.accept(this);
	}

	@Override
	public void visit(NeqRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);

		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);
    elem.strPre = String.format("%s != %s", left, right);
	}

	@Override
	public void visit(Nexists elem) {

	}

	@Override
	public void visit(Nin elem) {

	}

	@Override
	public void visit(NinRel elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
	}



	@Override
	public void visit(NumConstant elem) {
//		elem.num.accept(this);
	}

	@Override
	public void visit(Num elem) {
		
	}

	@Override
	public void visit(OrFormula elem) {
		elem.f1.accept(this);
		elem.f2.accept(this);

		String leftPre = getPrestr(elem.f1);
		String rightPre = getPrestr(elem.f2);
		String leftPost = getPoststr(elem.f1);
		String rightPost= getPoststr(elem.f2);

    elem.strPre = String.format("%s", constructionBinaryConditions(leftPre, rightPre, " || ", false, "true"));
    elem.strPost = constructionBinaryConditions(leftPost, rightPost, "", false, "");
	}

	@Override
	public void visit(Plus elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		String left = termTostring(elem.t1);
    String right = termTostring(elem.t2);

		elem.str = String.format("%s + %s", left, right);
	}

	@Override
	public void visit(TermRest elem) {
		elem.t.accept(this);
		elem.str = elem.t.str;
	}

	@Override
	public void visit(Terms elem) {
		elem.t.accept(this);
		elem.str = "";
		String tsPlaceName = this.tempPlaceName; //get the related Place type of this braced terms
		elem.placeName = tsPlaceName;
		String tPlaceVarName;
		String tConstantKey;
		String fieldCount = "1";
//		elem.str = elem.t.str;
		if(elem.t instanceof VariableTerm){
			tPlaceVarName = ((VariableTerm)(elem.t)).pVarName;
			if(!("").equals(tsPlaceName)){
				if(((VariableTerm)(elem.t)).isUserVariable){
					elem.str += "  "+tsPlaceName+".field1 = "
									+tPlaceVarName+";\n";
				}
			}
		}else if(elem.t instanceof ConstantTerm){
			tConstantKey = ((ConstantTerm)(elem.t)).var_key;
			if(!("").equals(tsPlaceName)){
				elem.str += "  "+tsPlaceName+".field1 = "
						+tConstantKey+";\n";
			}
		}
		
		int i=0;
		for (i=0; i<elem.tr.size(); i++)
		{
			((TermRest)(elem.tr.list.get(i))).accept(this);
//			elem.str += "," + ((TermRest)(elem.tr.list.get(i))).str;
			Term t = elem.tr.list.get(i).t;
			if(t instanceof VariableTerm){
				tPlaceVarName = ((VariableTerm)t).pVarName;
				if(!("").equals(tsPlaceName)){
					if(((VariableTerm)t).isUserVariable){
						 fieldCount = Integer.toString(i+2);
						elem.str += "  "+tsPlaceName+".field"+fieldCount+" = "
										+tPlaceVarName+";\n";
					}
				}
			}else if(t instanceof ConstantTerm){
				tConstantKey = ((ConstantTerm)t).var_key;
				if(!("").equals(tsPlaceName)){
					 fieldCount = Integer.toString(i+2);
					elem.str += "  "+tsPlaceName+".field"+fieldCount+" = "
							+tConstantKey+";\n";
				}
			}
		}
	}

	@Override
	public void visit(True elem) {

	}

	@Override
	public void visit(Union elem) {
		elem.t1.accept(this);
		elem.t2.accept(this);
		elem.str = "";
		String t1PlaceName = "";
		String t2PlaceName = "";
		
		if(elem.t1 instanceof ExpTerm){
			t1PlaceName = ((ExpTerm)(elem.t1)).placeName;
			if(elem.t2 instanceof ExpTerm){
				t2PlaceName = ((ExpTerm)(elem.t2)).placeName;
				
				elem.str = ((ExpTerm)(elem.t1)).str;
				elem.str += ((ExpTerm)(elem.t2)).str;
				elem.str += "	place_"+t1PlaceName+"!"+t2PlaceName+";\n";
			}
		}
		
		if(!("").equals(t1PlaceName))
		elem.placeName = t1PlaceName;
		
//		assert pname_union_status;
//		
//		elem.str = elem.t1.str + pname_union + "!" + elem.t2.str + ";\n";
//		pname_union_status = false; //as we are done with t2

	}

	@Override
	public void visit(Setdef elem) {
    String setVariable = ((VariableTerm)elem.u).v.key;
    String userVariable = ((CpxFormula)elem.sf).cpf.uv.s;
    if (!setVariable.equals(userVariable)) {
      if (mUserVariables.containsKey(setVariable)) {
        String msg = String.format("Warning:: duplicated definition of set variable %s. Result may be undefined.", setVariable);
        System.out.println(msg);
        errorMsg.error(elem.pos, msg);
      }
      else {
        Place place = null;
        for (Arc arc : iTransition.getArcOutList()) {
          if (arc.hasVariable(elem.v.key)) {
            place = (Place) arc.getTarget();
            break; //we only need to know the datatype associated to the set variable.
          }
        }
        if (place == null) {
          throw new IllegalArgumentException(String.format("The set variable definition for %s is incomplete. The variable %s is not associated to a place.", setVariable, elem.v.key));
        }

        VariableDefinition vd = getVariableDefinition(setVariable);
        vd.setType(VariableDefinition.Type.SET_ELEMENT);
        vd.addOutputPlaceName(place.getName());
        vd.setDeclaration(PromelaUtil.dataTypeToVariableDeclaration(place.getDataType(), setVariable));
        vd.setReceiveSequence(PromelaUtil.dataTypeToReceiveSequence(place.getDataType(), setVariable));
        vd.setEvalSequence(PromelaUtil.dataTypeToEvalSequence(place.getDataType(), setVariable));
        mUserVariables.put(setVariable, new UserVarType(setVariable, elem.v.key, place.getName()));
      }
    }

    if (elem.sf instanceof CpxFormula) {
      complexFormulaContext = COMPLEX_CONTEXT_SETDEF;
      ComplexFormula formula = ((CpxFormula) elem.sf).cpf;
      formula.accept(this);

      int count = sQuantifierCounter.getAndIncrement();
      SetDefinitionInfo sdInfo = new SetDefinitionInfo();
      sdInfo.setFunctionName(String.format("make_set_%d", count));
      sdInfo.setLoopVariable(mUserVariables.get(userVariable).getUserVariable());
      sdInfo.setPreCondition(formula.strPre);
      sdInfo.setPostCondition(formula.strPost);
      sdInfo.setSource(mUserVariables.get(userVariable).getPlaceName());
      sdInfo.setTarget(mUserVariables.get(setVariable).getPlaceName());
      sdInfo.setDeclaration(setVariable);
//      sdInfo.setDeclaration(mVariableDefinitions.get(setVariable).getDeclaration());
      sdInfo.setReceiveSequence(mVariableDefinitions.get(setVariable).getReceiveSequence());

      setDefinitionInfo.add(sdInfo);
      elem.str = sdInfo.getFunctionName();

      System.out.println(String.format("PRE CONDITION: %s", formula.strPre));
      System.out.println(String.format("POST CONDITION: %s", formula.strPost));
    }

    complexFormulaContext = -1;
    mUserVariables.remove(setVariable);
    mVariableDefinitions.remove(setVariable);
	}

	@Override
	public void visit(FunctionExp pTerm) {
		String functionName = pTerm.getName();
		if (functionName.equals("random")){
			List<String> translatedArgs = new ArrayList<>(pTerm.getArgumentTerms().size());
			for (Term term : pTerm.getArgumentTerms()) {
				term.accept(this);
				translatedArgs.add(termTostring(term));
			}
			invokedFunction.add(pTerm.getName());
			pTerm.outputVariable = "r";
			pTerm.str = functionGeneratorFactory.get(functionName).getCallableStatement(functionName, translatedArgs);
		}
	}

	@Override
	public void visit(UserVariable elem) {

	}

	@Override
	public void visit(ConstantTerm elem) {
		elem.c.accept(this);

		elem.str = elem.c.value.toString();
		elem.var_key = elem.str;
	}

	@Override
	public void visit(ExpTerm elem) {
		elem.e.accept(this);
		if (elem.e instanceof RExp) {
			elem.strPre = ((RExp)(elem.e)).strPre;
			elem.strPost = ((RExp)(elem.e)).strPost;
		}
		else if (elem.e instanceof SExp) {
			elem.varName = ((SExp)(elem.e)).varName;
			elem.isUserVariable = ((SExp)(elem.e)).isUserVariable;
//			if(((SExp)(elem.e)).isUserVariable){
//				elem.placeName  = ((SExp)(elem.e)).placeName;
//			}
			elem.placeName = ((SExp)(elem.e)).placeName;
			elem.str = ((SExp)(elem.e)).str;
		}
		else if (elem.e instanceof SingleValuedExp) {
			elem.str = ((SingleValuedExp) elem.e).str;
		}
	}

	@Override  //Rewritten by He 12/17/15
	public void visit(VariableTerm elem) {
		elem.v.accept(this);

    if (elem.v instanceof IndexVariable) {
      elem.index = ((IndexVariable) elem.v).index;
    }
    elem.str = elem.v.key;
    elem.var_key = elem.v.key;
    elem.pVarName = PromelaUtil.getFieldName(elem.v.key, elem.index);

    VariableDefinition variableDefinition = getVariableDefinition(elem.v.key);
    if (variableDefinition.getType() == VariableDefinition.Type.LOCAL) {
      elem.isUserVariable = true;
      elem.placeName = variableDefinition.getInputPlaceNames().get(0);//getPlaceNameByUserVariable(elem.v.key);
      elem.pVarName = PromelaUtil.getFieldName(elem.var_key, elem.index);
      elem.postcond = false;
    }
    else if (variableDefinition.getType() == VariableDefinition.Type.SET_ELEMENT) {
      elem.isUserVariable = true;
      elem.placeName = variableDefinition.getOutputPlaceNames().get(0);
      elem.pVarName = PromelaUtil.getFieldName(elem.var_key, elem.index);
      elem.postcond = true;
    }
    else {
      elem.placeName = variableDefinition.isInputVariable()?
          variableDefinition.getInputPlaceNames().get(0) : variableDefinition.getOutputPlaceNames().get(0);
      elem.postcond = variableDefinition.isOutputVariable() && !variableDefinition.isInputVariable();
    }
	}

  public VariableDefinition getVariableDefinition(final String pVariable) {
    VariableDefinition variableDefinition = mVariableDefinitions.get(pVariable);
    if (variableDefinition != null) {
      return variableDefinition;
    }

    variableDefinition = createVariableDefinition(pVariable);
    mVariableDefinitions.put(pVariable, variableDefinition);

    return variableDefinition;
  }

  private VariableDefinition createVariableDefinition(final String pVariable) {
    final VariableDefinition variableDefinition = new VariableDefinition(pVariable);

    List<Arc> matchingArcs = new ArrayList<>();
    iTransition.getArcInList().stream().filter(arc -> arc.hasVariable(pVariable)).forEach(arc -> {
      variableDefinition.addInputPlaceName(arc.getSource().getName());
      matchingArcs.add(arc);
    });

    iTransition.getArcOutList().stream().filter(arc -> arc.hasVariable(pVariable)).forEach(arc -> {
      variableDefinition.addOutputPlaceName(arc.getTarget().getName());
      matchingArcs.add(arc);
    });

    if (matchingArcs.size() > 0) {
      boolean isSetVariable = matchingArcs.get(0).isSetVar();
      boolean isAllMatch = matchingArcs.stream().allMatch(arc -> arc.isSetVar()==isSetVariable);
      if (!isAllMatch) {
        throw new IllegalStateException("All the arcs with same variable should be identical");
      }
      variableDefinition.setIsSetVariable(isSetVariable);

      DataType dataType = matchingArcs.get(0).getDataType();
      variableDefinition.setDeclaration(PromelaUtil.dataTypeToVariableDeclaration(dataType, pVariable));
      variableDefinition.setReceiveSequence(PromelaUtil.dataTypeToReceiveSequence(dataType, pVariable));
      variableDefinition.setType(VariableDefinition.Type.GLOBAL);
    }

    return variableDefinition;
  }

  private List<Place> findPlaceForVariable(String pVariable, LinkedList<Arc> pArcsList) {
    List<Place> places = new ArrayList<>();
    for(Arc arc : pArcsList){
      Place place = arc.getSource() instanceof Place ? ((Place) arc.getSource()) : ((Place) arc.getTarget());
      if(place.getToken().getDataType().getPow())
      {
        for (String var : arc.getVars()) {
          if (var.equals(pVariable)) {
            places.add(place);
          }
        }
      }
      else {
        if (arc.getVar().equals(pVariable)) {
          places.add(place);
        }
      }
    }

    return places;
  }

  @Override
	public void visit(StrConstant elem) {

	}
	
	public void visit(AExp elem){
		elem.ae.accept(this);

		String format = elem.isParenthesized()? "(%s)" : "%s";
		elem.str = String.format(format, elem.ae.str);
	}
	
	public void visit(RExp elem){
		elem.re.accept(this);

		String format = elem.isParenthesized()? "(%s)" : "%s";
		elem.strPre = String.format(format, elem.re.strPre);
		elem.strPost = elem.re.strPost;
	}
	
	public void visit(SExp elem){
		elem.se.accept(this);
//		elem.str = elem.se.str;

		elem.placeName = elem.se.placeName;
		elem.str = elem.se.str;
		if(elem.se instanceof BraceTerm){
			elem.varName = ((BraceTerm)(elem.se)).varName;
		}
	}

	@Override
	public void visit(AtomicTerm elem) {
		elem.t.accept(this);
		
		if(elem.t instanceof ConstantTerm){
			elem.strPre = elem.t.str;
		}else if(elem.t instanceof ExpTerm){
			elem.strPre = ((ExpTerm)(elem.t)).strPre;
			elem.strPost = ((ExpTerm)(elem.t)).strPost;
		}else errorMsg.error(elem.pos, "AtomicTerm::Cannot be VariableTerm or tree type mismatch!");
		
	}
	
	@Override
	public void visit(NotFormula elem) {
		elem.f.accept(this);

		String precondition = getPrestr(elem.f);
		
		if(StringUtils.isNotBlank(precondition)){
			elem.strPre = "!" + precondition;
		}
    else if (precondition.equals("true")){
			elem.strPre = "false";
		}
    else if (precondition.equals("false")){
			elem.strPre = "true";	
		}
		
	}

	@Override
	public void visit(AtFormula elem) {
		elem.af.treeLevel = elem.treeLevel;
		elem.af.accept(this);
		
//		elem.strPre = elem.af.strPre;
//		elem.strPost = elem.af.strPost;
		
		if(elem.af instanceof NotFormula){
			elem.str = ((NotFormula)(elem.af)).str;
			elem.strPre = ((NotFormula)(elem.af)).strPre;
		}else if(elem.af instanceof AtomicTerm){
			elem.strPre = ((AtomicTerm)(elem.af)).strPre;
			elem.strPost = ((AtomicTerm)(elem.af)).strPost;
		}else errorMsg.error(elem.pos, "AtFormula::tree type mismatch!");
	}



	@Override
	public void visit(CpFormula elem) {
		
		elem.cf.treeLevel = elem.treeLevel;
		elem.cf.accept(this);

		elem.strPre = elem.cf.strPre;
		elem.strPost = elem.cf.strPost;
	}

	@Override
	public void visit(CpxFormula elem) {
		elem.cpf.treeLevel = elem.treeLevel;
		elem.cpf.accept(this);
		
		elem.strPre = elem.cpf.strPre;
		elem.strPost = elem.cpf.strPost;
//		if(elem.cpf instanceof AtFormula){
//			
//		}
	}

	@Override
	public void visit(Sentence elem) {

		elem.f.accept(this);

		elem.strPre = elem.f.strPre;
		elem.strPost = elem.f.strPost;

		preCondition = StringUtils.isBlank(elem.strPre) ? "true" : elem.strPre;
		postCondition = elem.strPost;

    addUnusedVariablesToDefinitions();
    Iterator<VariableDefinition> iterator = mVariableDefinitions.values().iterator();
    while (iterator.hasNext()) {
      VariableDefinition vd = iterator.next();
      if (vd.getType() != VariableDefinition.Type.GLOBAL) {
        iterator.remove();
      }
    }
	}

  private void addUnusedVariablesToDefinitions() {
    //TODO:: re-think the plausibility of this operation when some input variable is used to test pre-condition but unused in post condition.
    HashSet<String> definedVariables = new HashSet<>();
    iTransition.getArcInList().forEach(arc -> definedVariables.addAll(Arrays.asList(arc.getVars())));
    iTransition.getArcOutList().forEach(arc -> definedVariables.addAll(Arrays.asList(arc.getVars())));
    definedVariables.removeAll(mVariableDefinitions.keySet());
    definedVariables.forEach(s -> {
      VariableDefinition vd = getVariableDefinition(s);
      if (!vd.isInputVariable() || !vd.isOutputVariable()) {
        mVariableDefinitions.remove(vd.getVariableName());
      }
    });
  }

  @Override
	public void visit(Empty elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EmptyTerm elem) {
		// TODO Auto-generated method stub
		
	}
	
	private String getPlaceNameByUserVariable(String userVar){
		String placeName = "";
		
//		Iterator<UserVarType> itr_uv = this.arrUserVar.iterator();
//		while(itr_uv.hasNext()){
//			UserVarType uv = itr_uv.next();
//			if(uv.getUserVariable().equals(userVar)){
//				placeName = uv.getPlaceName();
//			}
//		}
    if (mUserVariables.containsKey(userVar)) {
      placeName = mUserVariables.get(userVar).getPlaceName();
    }
		
		return placeName;
	}

	public List<QuantifierEvalInfo> getQuantifiersEvalInfo() {
		return quantifiersEvalInfo;
	}

	public String getPreCondition() {
		return preCondition;
	}

	public String getPostCondition() {
		return postCondition;
	}

  public Collection<String> getIncludedPlaces() {
    return includedPlaces;
  }

  public Collection<VariableDefinition> getVariableDefinitions() {
    return mVariableDefinitions.values();
  }

  public Map<String, String> getSetAssignments() {
    return mSetAssignments;
  }

  public List<SetDefinitionInfo> getSetDefinitionInfo() {
    return setDefinitionInfo;
  }

	public Set<String> getInvokedFunction() {
		return invokedFunction;
	}

	public void setFunctionGeneratorFactory(final Map<String, FunctionGenerator> functionGeneratorFactory) {
		this.functionGeneratorFactory = functionGeneratorFactory;
	}
}
