package formula.parser;

import formula.absyntree.*;
import formula.function.FunctionInterpreter;
import pipe.dataLayer.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Simulation Interpreter
 *
 * @author su-home
 */
public class Interpreter implements Visitor {

    private static final BigDecimal MIN_NUMBER = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal MAX_NUMBER = BigDecimal.valueOf(Long.MAX_VALUE);

    ErrorMsg errorMsg;
    SymbolTable symTable;
    Transition iTransition;
    int mode = 0;//when mode is 0, means interpreter just check pre-condition(Check Enable)
    //when mode is 1, means interpreter is processing post-condition(Fire)
    public ArrayList<String> undefVars; //record all vars in formula that has not yet defined in arc var
    private HashSet<String> definedVars;
    private HashSet<String> mUserVariables = new HashSet<>();
	private FunctionInterpreter mFunctionHandler = new FunctionInterpreter();
    boolean debug = false;

    public Interpreter(ErrorMsg errorMsg, Transition transition, int mode) {
        this.errorMsg = errorMsg;
        iTransition = transition;
        this.symTable = iTransition.getTransSymbolTable();
        this.mode = mode;
        undefVars = new ArrayList<String>();
        definedVars = new HashSet<String>();
        searchDefinedVars();
    }

    /**
     * add defined vars to definedVars set for checking whether vars in formula are defined by arc vars
     */
    private void searchDefinedVars() {
        for (Arc a : this.iTransition.getArcList()) {
            String[] vars = a.getVars();
            for (int i = 0; i < vars.length; i++) {
                definedVars.add(vars[i]);
            }
        }
    }

    @Override
    public void visit(AndFormula elem) {
        if (debug) System.out.println("AndFormula");

        elem.f1.accept(this);
        boolean temp_f1 = false;
        if (elem.f1 instanceof AtFormula ||
            elem.f1 instanceof CpFormula ||
            elem.f1 instanceof CpxFormula) {
            temp_f1 = elem.f1.bool_val;
        } else {
            errorMsg.error(elem.pos, "AndFormula::LHS Formula type mismatch!");
        }

        elem.bool_val = false;
        if (temp_f1) {
            //short-circuit evaluation
            elem.f2.accept(this);
            boolean temp_f2 = false;
            if (elem.f2 instanceof AtFormula ||
                elem.f2 instanceof CpFormula ||
                elem.f2 instanceof CpxFormula) {
                temp_f2 = elem.f2.bool_val;
            } else {
                errorMsg.error(elem.pos, "AndFormula::RHS Formula type mismatch!");
            }
            //compare
            elem.bool_val = temp_f2;
        }
    }

    @Override
    public void visit(BraceTerm elem) {
        if (debug) System.out.println("BraceTerm");
        elem.t.accept(this);

        abToken resultToken = null;
        if (elem.t instanceof VariableTerm) {
            resultToken = toAbTokenFromVariableTerm((VariableTerm) elem.t);
        } else if (elem.t instanceof ConstantTerm)   //added by He - 8/5/15
        {
            ConstantTerm constantTerm = (ConstantTerm) elem.t;
            Token token = constantTerm.toToken();
            resultToken = new abToken(token.getTokentype());
            resultToken.addToken(token);
        }

        //assign the result token to absyntree
        elem.abTok = resultToken;
    }

    private abToken toAbTokenFromVariableTerm(final VariableTerm pTerm) {
        abToken resultToken = null;

        String key = pTerm.v.key;
        Object lookedupToken = symTable.lookup(key);
        if (pTerm.v instanceof IdVariable) {
            if (lookedupToken instanceof Token) {
                resultToken = new abToken(((Token) lookedupToken).getTokentype());
                resultToken.addToken((Token) lookedupToken);
            } else if (lookedupToken instanceof abToken) {
                resultToken = new abToken(((abToken) lookedupToken).getDataType());
                resultToken.addTokens(((abToken) lookedupToken).listToken);
            }
        } else if (pTerm.v instanceof IndexVariable && lookedupToken instanceof Token) {
            Token token = (Token) lookedupToken;
            int index = pTerm.index - 1;
            BasicType element = token.Tlist.elementAt(index);

            BasicType newValue = new BasicType(element.kind);
            newValue.setValue(element.getValue());

            String kind = BasicType.TYPES[element.kind];
            Token newToken = new Token(new DataType(kind + "Tok", new String[]{kind}, true, null));
            newToken.add(new BasicType[]{newValue});

            resultToken = new abToken(newToken.getTokentype());
            resultToken.addToken(newToken);
        }

        return resultToken;
    }

    @Override
    public void visit(BraceTerms elem) {
        if (debug) System.out.println("BraceTerms");
        elem.ts.accept(this);

        DataType resultType = elem.ts.Tok.getTokentype();
        abToken resultTok = new abToken(resultType);
        resultTok.addToken(elem.ts.Tok);
        elem.abTok = resultTok;
    }

    @Override
    public void visit(ComplexFormula elem) {
        if (debug) {
            System.out.println("ComplexFormula");
        }

        elem.q.accept(this);
        elem.uv.accept(this);
        elem.d.accept(this);
        elem.v.accept(this);


        //changed by He - 8/3/15
        if (elem.q instanceof ForAll) {
            elem.bool_val = determineValidityForQuantifiers(elem, true, false);
        } else if (elem.q instanceof Exists) {
            elem.bool_val = determineValidityForQuantifiers(elem, false, true);
        } else if (elem.q instanceof Nexists) {
            elem.bool_val = determineValidityForQuantifiers(elem, true, false);
        } else {
            errorMsg.error(elem.pos, "ComplexFormula::Quantifier type mismatch!");
        }
    }

    private boolean determineValidityForQuantifiers(final ComplexFormula pElem, final boolean pDefaultValue, final boolean pShouldKeepAlive) {
        boolean value = pDefaultValue;
        List<Token> tokensList = ((abToken) symTable.lookup(pElem.v.key)).listToken;
        for (Token t : tokensList) {
//			if (pShouldUpdate && symTable.exist(pElem.uv.s)) {
//				symTable.update(pElem.uv.s, t, 0);
//			}
//			else {
//				symTable.insert(pElem.uv.s, t, 0);
//			}
            symTable.insert(pElem.uv.s, t, Symbol.TYPE.SINGLE);
            pElem.f.accept(this);
            if (pElem.f instanceof AtFormula ||
                    pElem.f instanceof CpFormula ||
                    pElem.f instanceof CpxFormula) {
                value = pElem.f.bool_val;
            }

            if (!pShouldKeepAlive) {
                symTable.delete(pElem.uv.s);
            }

            if (value == !pDefaultValue) {
                break;
            }
        }

        return value;
    }

    @Override
    public void visit(Diff elem) {
        if (debug) System.out.println("Diff");
        elem.t1.accept(this);
        elem.t2.accept(this);

        abToken token1 = null;
        if (elem.t1 instanceof VariableTerm) {
            token1 = toAbTokenFromVariableTerm((VariableTerm) elem.t1);
        } else if (elem.t1 instanceof ExpTerm) {
            abToken termToken = ((SExp) ((ExpTerm) (elem.t1)).e).abTok;
            token1 = new abToken(termToken.getDataType());
            token1.addTokens(termToken.listToken);
        }

        if (token1 != null) {
            if (elem.t2 instanceof VariableTerm) {
                abToken token2 = toAbTokenFromVariableTerm((VariableTerm) elem.t2);
                if (token2 != null) {
                    for (Token token : token2.listToken) {
                        token1.deleteToken(token);
                    }
                }
            } else if (elem.t2 instanceof ExpTerm) {
                if (((ExpTerm) (elem.t2)).e instanceof SExp) {
                    for (Token t : ((SExp) ((ExpTerm) (elem.t2)).e).abTok.listToken) {
                        token1.deleteToken(t);
                    }
                }
            }
        }

        //assign the result token to absyntree
        elem.abTok = token1;
    }

    @Override
    public void visit(Div elem) {
        if (debug) System.out.println("Div");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = BigDecimal.ZERO;
        BigDecimal rightValue = BigDecimal.ZERO;
        try {
            leftValue = numberValueOfTerm(elem.t1);
            rightValue = numberValueOfTerm(elem.t2);
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Div:: Invalid value - " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "Div::Tree type mismatch! " + e.getMessage());
        }

        //Execute
        if (rightValue.doubleValue() == 0) {
            errorMsg.error(elem.pos, "Div::Divided by 0!");
        } else {
            elem.value = leftValue.divide(rightValue);
        }

    }

    @Override
    public void visit(EqRel elem) {
        if (debug) {
            System.out.println("EqRel");
        }
        elem.t1.accept(this);
        elem.t2.accept(this);

        String lhsVariableName = "";
        //whether the LHS variable belongs to arcOutVarList
        boolean isInArcOutVarList = false;
        //added by He 7/30/15
        boolean isInArcInVarList = false;
        if (elem.t1 instanceof VariableTerm) {
            lhsVariableName = ((VariableTerm) elem.t1).v.key;
            for (Arc arc : iTransition.getArcOutList()) {
                if (arc.hasVariable(lhsVariableName, true)) {
                    isInArcOutVarList = true;
                    break;
                }
            }

            for (Arc arc : iTransition.getArcInList()) {
                if (arc.hasVariable(lhsVariableName, false)) {
                    isInArcInVarList = true;
                    break;
                }
            }
        }

        boolean isAssignable = isInArcOutVarList || mUserVariables.contains(lhsVariableName);
        //newly modified to deal with the condition when isInArcOutVarList is true and mode is 1;
        if (isAssignable && !isInArcInVarList && mode == 1)  //modified by He 11/5/15
        {
            //Do the assignment
            performAssignment(elem);
            elem.value = true;
        } else if (!isAssignable || isInArcInVarList) {
            //Do the comparison
            try {
                Object valueOfTerm1 = valueOfTerm(elem.t1);
                Object valueOfTerm2 = valueOfTerm(elem.t2);

                if (elem.t2 instanceof EmptyTerm) {
                    elem.value = isEmptyValue(valueOfTerm1);
                }
                elem.value = Objects.equals(valueOfTerm1, valueOfTerm2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            elem.value = true;
        }
    }

    private void performAssignment(final EqRel pElem) {
        if (!(pElem.t1 instanceof VariableTerm)) {
            return;
        }

        Variable variable1 = ((VariableTerm)pElem.t1).v;
        Symbol term1Symbol = symTable.lookUpForSymbol(variable1.key);
        if (term1Symbol == null) {
            term1Symbol = iTransition.getSymbolForOutputVariable(variable1.key);
            symTable.table.put(term1Symbol.key, term1Symbol);
        }

        Object term2Object = valueOfRHS(pElem.t2);
        if (term2Object == null) {
            System.out.println("Warning:: could not determine the right hand side of the assignment");
            return;
        }

        if (term2Object instanceof abToken) {
            //Case: Z = X\Y or Z=XUY, etc. Here all of X, Y and Z should be set variables
            if (symTable.lookup(variable1.key) instanceof abToken) {
                symTable.update(variable1.key, term2Object, Symbol.TYPE.MULTI);
            }
            else {
                throw new IllegalStateException("Type mismatch. Both LHS and RHS variables should be set variable");
            }
        }
        else {
            if (variable1 instanceof IdVariable && term2Object instanceof Token) {
                //Case:: z=y
                symTable.update(variable1.key, term2Object, Symbol.TYPE.SINGLE);
            }
            else {
                //Case: z=y[1]
                int index = 0;
                if (variable1 instanceof IndexVariable) {
                    //Case: y[1]=z or z[1]=y[1]
                    index = ((IndexVariable)variable1).index-1;
                    if (term2Object instanceof Token) {
                        term2Object = ((Token)term2Object).Tlist.firstElement().getValue();
                    }
                }

                Token term1Token = (Token) symTable.lookUpForSymbol(variable1.key).binder;
                term1Token.Tlist.get(index).setValue(term2Object);
                symTable.update(variable1.key, term1Token, Symbol.TYPE.SINGLE);
            }

        }
    }

    private Object valueOfRHS(final Term pTerm) {
        Object term2Object = null;
        if (pTerm instanceof VariableTerm) {
            Variable variable2 = ((VariableTerm)pTerm).v;
            Symbol term2Symbol = symTable.lookUpForSymbol(variable2.key);
            if (variable2 instanceof IdVariable) {
                term2Object = term2Symbol.binder;
            }
            else {
                term2Object = ((Token)term2Symbol.binder).Tlist.elementAt(((IndexVariable)variable2).index-1).getValue();
            }
        }
        else if (pTerm instanceof ExpTerm) {
            Exp exp2 = ((ExpTerm)pTerm).e;
            if (exp2 instanceof SingleValuedExp) {
                term2Object = ((SingleValuedExp)exp2).value;
            }
            else if (exp2 instanceof SExp) {
                term2Object = ((SExp)exp2).abTok;
            }
        }
        else if (pTerm instanceof ConstantTerm) {
            Constant constant = ((ConstantTerm) pTerm).c;
            if (constant instanceof True || constant instanceof False) {
                System.out.println("Boolean valued token is not supported, token is default to 0"); //added by He - 7/25/2015
            }
            else {
                term2Object = constant.value;
            }
        }

        return term2Object;
    }

    private Boolean isEmptyValue(final Object pValue) {
        return (pValue instanceof Integer && ((Integer) pValue).intValue() == 0) ||
                (pValue instanceof String && pValue.toString().equals(""));
    }

    @Override
    public void visit(EquivFormula elem) {
        if (debug) System.out.println("EquivFormula");
        elem.f1.accept(this);
        elem.f2.accept(this);

        if (checkFormulaTypeEquivalence(elem.f1, elem.f2)) {
            elem.bool_val = (elem.f1.bool_val && elem.f2.bool_val) || (!elem.f1.bool_val && !elem.f2.bool_val);
        } else {
            errorMsg.error(elem.pos, "EquivFormula::LHS and RHS Formula type mismatch!");
            elem.bool_val = false;
        }
    }

    /**
     * This method checks if both the formula are of same type.
     *
     * @param pLHSFormula Left hand side formula0
     * @param pRHSFormula Right hand side formula
     * @return true if both the formula is compatible
     */
    private boolean checkFormulaTypeEquivalence(final Formula pLHSFormula, final Formula pRHSFormula) {
        return (pLHSFormula instanceof AtFormula || pLHSFormula instanceof CpxFormula || pLHSFormula instanceof CpFormula) &&
                (pRHSFormula instanceof AtFormula || pRHSFormula instanceof CpxFormula || pRHSFormula instanceof CpFormula);
    }

    @Override
    public void visit(Exists elem) {
        if (debug) System.out.println("Exists");
//		elem.quant_type = 1;
    }

    @Override
    public void visit(False elem) {
        if (debug) System.out.println("False");
        elem.value = false;
    }

    @Override
    public void visit(ForAll elem) {
        if (debug) System.out.println("ForAll");
//		elem.quant_type = 0;
    }

    @Override
    public void visit(GeqRel elem) {
        if (debug) System.out.println("GeqRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = MIN_NUMBER;
        BigDecimal rightValue = MAX_NUMBER;

        try {
            leftValue = numberValueOfTerm(elem.t1);  //deal with LHS term
            rightValue = numberValueOfTerm(elem.t2); //deal with RHS term
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "GeqRel::Invalid value: " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "GeqRel::Tree type mismatch! " + e.getMessage());
        }

        //Compare LHS and RHS
        int comparison = leftValue.compareTo(rightValue);
        elem.value = (comparison == 1 || comparison == 0);
    }

    @Override
    public void visit(GtRel elem) {
        if (debug) System.out.println("GtRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = MIN_NUMBER;
        BigDecimal rightValue = MAX_NUMBER;

        try {
            leftValue = numberValueOfTerm(elem.t1);  //deal with LHS term
            rightValue = numberValueOfTerm(elem.t2); //deal with RHS term
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "GtRel::Invalid value: " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "GtRel::Tree type mismatch! " + e.getMessage());
        }

        //Compare LHS and RHS
        int comparison = leftValue.compareTo(rightValue);
        elem.value = (comparison == 1);
    }

    @Override
    public void visit(Identifier elem) {
        if (debug) System.out.println("Identifier");

    }

    @Override
    public void visit(IdVariable elem) {
        if (debug) System.out.println("IdVariable");
    }

    @Override
    public void visit(ImpFormula elem) {
        if (debug) System.out.println("ImpFormula");

        if (checkFormulaTypeEquivalence(elem.f1, elem.f2)) {
            elem.bool_val = true;
            elem.f1.accept(this);
            if (elem.f1.bool_val) {
                elem.f2.accept(this);
                elem.bool_val = elem.f2.bool_val;
            }
        } else {
            errorMsg.error(elem.pos, "ImpFormula::LHS and RHS Formula type mismatch!");
            elem.bool_val = false;
        }
    }

    @Override
    public void visit(In elem) {
        if (debug) System.out.println("In");
        elem.domain_type = 0;
    }

    @Override
    public void visit(Index elem) {
        if (debug) System.out.println("Index");
        elem.n.accept(this);
        elem.int_val = Integer.parseInt(elem.n.n);
    }

    @Override
    public void visit(IndexVariable elem) {
        if (debug) System.out.println("IndexVariable");
        elem.i.accept(this);
        elem.idx.accept(this);

        elem.key = elem.i.key;
        elem.index = elem.idx.int_val;
    }

    @Override
    public void visit(InRel elem) {
        if (debug) System.out.println("InRel");

        elem.t1.accept(this);
        elem.t2.accept(this);

        Object leftValue = null;

        //deal with LHS term
        if (elem.t1 instanceof ConstantTerm) {
            leftValue = ((ConstantTerm) elem.t1).c.value;
        } else if (elem.t1 instanceof VariableTerm) {
            Variable variable = ((VariableTerm) elem.t1).v;
            Token token = (Token) symTable.lookup(variable.key);

            if (((VariableTerm) (elem.t1)).v instanceof IdVariable) {
                leftValue = token;
            } else if (variable instanceof IndexVariable) {
                leftValue = token.Tlist.get(((IndexVariable) variable).index);
            }
        } else errorMsg.error(elem.pos, "InRel::Tree type mismatch!");

        //RHS tackled inside compare part

        //Compare LHS and RHS
        elem.value = false;
        if (elem.t2 instanceof VariableTerm) {
            Variable variable = ((VariableTerm) elem.t2).v;
            int index = 0;
            if (variable instanceof IndexVariable) {
                index = ((IndexVariable) variable).index;
            }

            abToken token = (abToken) symTable.lookup(variable.key);
            for (Token tok : token.listToken) {
                if (tok.equals(leftValue) || tok.Tlist.get(index).equals(leftValue)) {
                    elem.value = true;
                }
            }
        }
    }

    @Override
    public void visit(LeqRel elem) {
        if (debug) System.out.println("LeqRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = MIN_NUMBER;
        BigDecimal rightValue = MAX_NUMBER;

        try {
            leftValue = numberValueOfTerm(elem.t1);  //deal with LHS term
            rightValue = numberValueOfTerm(elem.t2); //deal with RHS term
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Invalid value: " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "LeqRel::Tree type mismatch! " + e.getMessage());
        }

        //Compare LHS and RHS
        int comparison = leftValue.compareTo(rightValue);
        elem.value = (comparison == -1 || comparison == 0);
    }

//	private int intValueOfTerm(final Term pTerm) throws Exception {
//		Object value = valueOfTerm(pTerm);
//		if (value != null) {
//			try {
//				return value instanceof Number ? ((Number)value).intValue() : Integer.parseInt(value.toString());
//			}
//			catch (Exception ex){
//				//Empty by design
//			}
//		}
//
//		throw new IllegalArgumentException("The value of the term "+pTerm+"is not a valid integer value ");
//	}

    private BigDecimal numberValueOfTerm(final Term pTerm) throws Exception {
        Object value = valueOfTerm(pTerm);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        throw new IllegalArgumentException("The value of the term " + pTerm + "is not a valid integer value ");
    }

    @Override
    public void visit(LtRel elem) {
        if (debug) System.out.println("LtRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        Number leftValue = MIN_NUMBER;
        Number rightValue = MAX_NUMBER;

        try {
            leftValue = numberValueOfTerm(elem.t1);  //deal with LHS term
            rightValue = numberValueOfTerm(elem.t2); //deal with RHS term
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Invalid value: " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "LtRel::Tree type mismatch! " + e.getMessage());
        }

        //Compare LHS and RHS
        elem.value = leftValue.doubleValue() < rightValue.doubleValue();
    }

    @Override
    public void visit(Minus elem) {
        if (debug) System.out.println("Minus");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = MIN_NUMBER;
        BigDecimal rightValue = MAX_NUMBER;

        try {
            leftValue = numberValueOfTerm(elem.t1);  //deal with LHS term
            rightValue = numberValueOfTerm(elem.t2); //deal with RHS term
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Minus:: Invalid value - " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "Minus::Tree type mismatch! " + e.getMessage());
        }

        //Execute
        elem.value = leftValue.subtract(rightValue);
    }

    @Override
    public void visit(Mod elem) {
        if (debug) System.out.println("Mod");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = BigDecimal.ZERO;
        BigDecimal rightValue = BigDecimal.ONE;
        try {
            leftValue = numberValueOfTerm(elem.t1);
            rightValue = numberValueOfTerm(elem.t2);
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Mod:: Invalid value - " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "Mod::Tree type mismatch! " + e.getMessage());
        }


        //Execute
        if (rightValue.equals(BigDecimal.ZERO)) {
            errorMsg.error(elem.pos, "Mod:: Mod by 0!!");
        } else {
            elem.value = leftValue.remainder(rightValue);
        }
    }

    @Override
    public void visit(Mul elem) {
        if (debug) System.out.println("Mul");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = BigDecimal.ZERO;
        BigDecimal rightValue = BigDecimal.ZERO;
        try {
            leftValue = numberValueOfTerm(elem.t1);
            rightValue = numberValueOfTerm(elem.t2);
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Mul:: Invalid value - " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "Mul::Tree type mismatch! " + e.getMessage());
        }

        //Execute
        elem.value = leftValue.multiply(rightValue);
    }


    public void visit(NegExp elem) {
        if (debug) System.out.println("NegExp");
        elem.t.accept(this);

        BigDecimal val = BigDecimal.ZERO;
        try {
            val = numberValueOfTerm(elem.t);
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Invalid value:: " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "NegExp::Tree type mismatch! " + e.getMessage());
        }

        //Execute
        elem.value = val.negate();
    }

    @Override
    public void visit(NeqRel elem) {
        if (debug) System.out.println("NeqRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        Object leftValue = null;
        Object rightValue = null;

        try {
            leftValue = valueOfTerm(elem.t1);  //deal with LHS term
            rightValue = valueOfTerm(elem.t2); //deal with RHS term
        } catch (Exception e) {
            errorMsg.error(elem.pos, "NeqRel::Tree type mismatch! " + e.getMessage());
        }

        //Compare LHS and RHS
        elem.value = !Objects.equals(leftValue, rightValue);
    }

    private Object valueOfTerm(final Term pTerm) throws Exception {
        Object value = null;
        if (pTerm instanceof ConstantTerm) {
            value = ((ConstantTerm) pTerm).c.value;
        } else if (pTerm instanceof VariableTerm) {
            Variable variable = ((VariableTerm) pTerm).v;
            Token token = (Token) symTable.lookup(variable.key);

            //Fix-Me:: proper inheritence to improve the following statement
            int index = variable instanceof IndexVariable ? ((IndexVariable) variable).index - 1 : 0;
            value = token.Tlist.get(index).getValue();
        } else if (pTerm instanceof ExpTerm) {
            Exp exp = ((ExpTerm) pTerm).e;
            if (exp instanceof SingleValuedExp) {
                value = ((SingleValuedExp) exp).value;
            } else if (exp instanceof SExp) {
                value = ((SExp) exp).abTok;
            }
        } else {
            throw new Exception("Unsupported term " + pTerm);
        }

        return value;
    }

    @Override
    public void visit(Nexists elem) {
//		elem.quant_type = 2;
        if (debug) System.out.println("Nexists");
    }

    @Override
    public void visit(Nin elem) {
        elem.domain_type = 1;
        if (debug) System.out.println("Nin elem");
    }

    @Override
    public void visit(NinRel elem) {
        if (debug) System.out.println("NinRel");
        elem.t1.accept(this);
        elem.t2.accept(this);

        int Ltype = 0;//1 is int; 2 is str;  3 is tok

        String Lstr_val = "";
        double Lint_val = 0;
        Token Ltok = new Token();

        //deal with LHS term
        if (elem.t1 instanceof ConstantTerm) {
            if (((ConstantTerm) (elem.t1)).c instanceof NumConstant) {
                Lint_val = ((NumConstant) ((ConstantTerm) (elem.t1)).c).value.doubleValue();
                Ltype = 1;
            } else if (((ConstantTerm) (elem.t1)).c instanceof StrConstant) {
                Lstr_val = ((StrConstant) ((ConstantTerm) (elem.t1)).c).value;
                Ltype = 2;
            }
        } else if (elem.t1 instanceof VariableTerm) {
            if (((VariableTerm) (elem.t1)).v instanceof IdVariable) {
                Ltok = (Token) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t1)).v).key));
                Ltype = 3;
            } else if (((VariableTerm) (elem.t1)).v instanceof IndexVariable) {
                if (((Token) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t1)).v).key))).Tlist.elementAt(((VariableTerm) elem.t1).index - 1).kind == 0) {
                    Lint_val = ((Token) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t1)).v).key))).Tlist.elementAt(((VariableTerm) elem.t1).index - 1).getValueAsInt();
                    Ltype = 1;
                } else if (((Token) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t1)).v).key))).Tlist.elementAt(((VariableTerm) elem.t1).index - 1).kind == 1) {
                    Lstr_val = ((Token) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t1)).v).key))).Tlist.elementAt(((VariableTerm) elem.t1).index - 1).getValueAsString();
                    Ltype = 2;
                }
            }
        } else errorMsg.error(elem.pos, "InRel::Tree type mismatch!");

        //RHS tackled inside compare part

        //Compare LHS and RHS
        elem.value = true;
        if (Ltype == 1) {

            if (((VariableTerm) (elem.t2)).v instanceof IdVariable) {
                int size = ((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.size();
                for (int i = 0; i < size; i++) {
                    if (Lint_val == ((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.elementAt(i).Tlist.firstElement().getValueAsInt()) {
                        elem.value = false;
                    }
                }
            } else if (((VariableTerm) (elem.t2)).v instanceof IndexVariable) {
                int size = ((abToken) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.size();
                for (int i = 0; i < size; i++) {
                    if (Lint_val == ((abToken) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.elementAt(i).Tlist.elementAt(((VariableTerm) elem.t2).index - 1).getValueAsInt()) {
                        elem.value = false;
                    }
                }
            }

        } else if (Ltype == 2) {

            if (((VariableTerm) (elem.t2)).v instanceof IdVariable) {
                int size = ((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.size();
                for (int i = 0; i < size; i++) {
                    if (Lstr_val == ((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.elementAt(i).Tlist.firstElement().getValueAsString()) {
                        elem.value = true;
                    }
                }
            } else if (((VariableTerm) (elem.t2)).v instanceof IndexVariable) {
                int size = ((abToken) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.size();
                for (int i = 0; i < size; i++) {
                    if (Lstr_val == ((abToken) (symTable.lookup(((IndexVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.elementAt(i).Tlist.elementAt(((VariableTerm) elem.t2).index - 1).getValueAsString()) {
                        elem.value = true;
                    }
                }
            }
        } else if (Ltype == 3) {

            if (((VariableTerm) (elem.t2)).v instanceof IdVariable) {
                int size = ((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.size();
                for (int i = 0; i < size; i++) {
                    if (((abToken) (symTable.lookup(((IdVariable) ((VariableTerm) (elem.t2)).v).key))).listToken.elementAt(i).Tlist.equals(Ltok)) {
                        elem.value = true;
                    }
                }
            }
        } else errorMsg.error(elem.pos, "InRel::LHS type cannot compare with RHS!");

    }

    @Override
    public void visit(NotFormula elem) {
        if (debug) System.out.println("NotFormula");
        elem.f.accept(this);

        if (elem.f instanceof AtFormula ||
                elem.f instanceof CpFormula ||
                elem.f instanceof CpxFormula) {
            //Fix-me: why is it really needed to check this instance
            elem.bool_val = !elem.f.bool_val;
        } else {
            //Fix-me: why this default assignment
            elem.bool_val = true;
        }
    }

    @Override
    public void visit(NumConstant elem) {
        if (debug) System.out.println("NumConstant");
//		elem.num.accept(this);
//		elem.value = Integer.parseInt(elem.num.n);
    }

    @Override
    public void visit(Num elem) {
        if (debug) System.out.println("Num");
        //elem.d = Double.parseDouble(elem.n);
    }

    @Override
    public void visit(OrFormula elem) {
        if (debug) System.out.println("OrFormula");
        elem.f1.accept(this);
        boolean temp_f1 = false;
        if (elem.f1 instanceof AtFormula ||
            elem.f1 instanceof CpFormula ||
            elem.f1 instanceof CpxFormula) {
            temp_f1 = elem.f1.bool_val;
        } else {
            errorMsg.error(elem.pos, "OrFormula::LHS Formula type mismatch!");
        }

        elem.bool_val = true;
        if (!temp_f1) {
            //Short-circuit evaluation
            elem.f2.accept(this);
            boolean temp_f2 = false;
            if (elem.f2 instanceof AtFormula ||
                elem.f2 instanceof CpFormula ||
                elem.f2 instanceof CpxFormula) {
                temp_f2 = elem.f2.bool_val;
            } else {
                errorMsg.error(elem.pos, "OrFormula::RHS Formula type mismatch!");
            }

            elem.bool_val = temp_f2;
        }
    }

    @Override
    public void visit(Plus elem) {
        if (debug) System.out.println("Plus");
        elem.t1.accept(this);
        elem.t2.accept(this);

        BigDecimal leftValue = BigDecimal.ZERO;
        BigDecimal rightValue = BigDecimal.ZERO;
        try {
            leftValue = numberValueOfTerm(elem.t1);
            rightValue = numberValueOfTerm(elem.t2);
        } catch (IllegalArgumentException iae) {
            errorMsg.error(elem.pos, "Plus:: Invalid value - " + iae.getMessage());
        } catch (Exception e) {
            errorMsg.error(elem.pos, "Minus::Tree type mismatch! " + e.getMessage());
        }

        //Execute
        elem.value = leftValue.add(rightValue);
    }

    @Override
    public void visit(TermRest elem) {
        // TODO Auto-generated method stub
        if (debug) System.out.println("TermRest");
        elem.t.accept(this);
    }

    @Override
    public void visit(Terms elem) {
        if (debug) System.out.println("Terms");
        elem.t.accept(this);

        BasicType value1 = null;
        BasicType[] valuesArray = new BasicType[elem.tr.size() + 1];

        if (elem.t instanceof VariableTerm) {
            if (((VariableTerm) (elem.t)).v instanceof IndexVariable) {
                IndexVariable indexVariable = (IndexVariable) ((VariableTerm) (elem.t)).v;
                Object lookedupObject = symTable.lookup(indexVariable.key);
                if (lookedupObject instanceof Token) {
                    value1 = ((Token) lookedupObject).Tlist.elementAt(indexVariable.index - 1);
                }
            }
        } else if (elem.t instanceof ConstantTerm) {
            value1 = ((ConstantTerm) elem.t).toToken().Tlist.get(0);
        } else if (elem.t instanceof ExpTerm) {
            //TODO: to be finished
        }

        //add term to the first basic type of the token;
        valuesArray[0] = value1;

        for (int i = 0; i < elem.tr.size(); i++) {
            elem.tr.elementAt(i).accept(this);
            BasicType btRest = null;
            if (elem.tr.elementAt(i).t instanceof VariableTerm) {
                VariableTerm variableTerm = (VariableTerm) elem.tr.elementAt(i).t;
                if (variableTerm.v instanceof IndexVariable) {
                    IndexVariable indexVariable = (IndexVariable) variableTerm.v;
                    if (symTable.lookup(indexVariable.key) instanceof Token) {
                        btRest = ((Token) symTable.lookup(indexVariable.key)).Tlist.elementAt(indexVariable.index - 1);
                    }
                }
            } else if (elem.tr.elementAt(i).t instanceof ConstantTerm) {
                btRest = ((ConstantTerm) elem.tr.elementAt(i).t).toToken().Tlist.get(0);
            } else if (elem.tr.elementAt(i).t instanceof ExpTerm) {
                btRest = new BasicType(0, ((ExpTerm) (elem.tr.elementAt(i).t)).int_val + "");
            }

            valuesArray[i + 1] = btRest;
        }

        Token tok = new Token();
        tok.add(valuesArray);
        tok.UpdateDataTypeByTlist();
        elem.Tok = tok;
    }

    @Override
    public void visit(True elem) {
        if (debug) System.out.println("True");
        elem.value = true;
    }

    @Override

    public void visit(Union elem) {
        if (debug) System.out.println("Union");
        elem.t1.accept(this);
        elem.t2.accept(this);

        //allocate space to a temp abToken to store result
        abToken resultTok = null;

        if (elem.t1 instanceof VariableTerm) {
            resultTok = toAbTokenFromVariableTerm((VariableTerm) elem.t1);
        } else if (elem.t1 instanceof ExpTerm) {
            abToken termToken = ((SExp) ((ExpTerm) (elem.t1)).e).abTok;
            resultTok = new abToken(termToken.getDataType());
            resultTok.addTokens(termToken.listToken);
        } else {
            errorMsg.error(elem.pos, "Union::Tree type mismatch!");
        }

        //union right term
        if (resultTok != null) {
            if (elem.t2 instanceof VariableTerm) {
                abToken token2 = toAbTokenFromVariableTerm((VariableTerm) elem.t2);
                resultTok.addTokens(token2.listToken);
            } else if (elem.t2 instanceof ExpTerm) {
                if (((ExpTerm) (elem.t2)).e instanceof SExp) {
                    for (Token t : ((SExp) ((ExpTerm) (elem.t2)).e).abTok.listToken) {
                        resultTok.addToken(t);
                    }
                }
            } else if (((ExpTerm) (elem.t2)).e instanceof AExp) {
                //when meet AExp at right, the result of AExp is int and left elem is an abtoken (a set), so the int from AExp is added to abToken, the type has to be [int]
                Token token = new Token(resultTok.getDataType());
                BasicType tempBt = new BasicType(0);
                tempBt.setValue(((AExp) ((ExpTerm) (elem.t2)).e).value);
                token.Tlist.add(tempBt);
                resultTok.addToken(token);
            }
        }

        //assign the result token to absyntree
        elem.abTok = resultTok;
    }

    //added He 8/17/15
    public void visit(Setdef elem) {
        if (debug) {
            System.out.println("Set Definition");
        }

        String setVariable = ((VariableTerm) elem.u).v.key;

        boolean sameuservar = false;
        if (setVariable.equals(((CpxFormula) elem.sf).cpf.uv.s)) {
            sameuservar = true;
        } else if (elem.u instanceof VariableTerm) {
            System.out.println("New set def variable");
            if (mUserVariables.contains(setVariable)) {
                System.out.println(String.format("Warning:: duplicated definition of set variable %s. Result may be undefined.", setVariable));
            } else {
                mUserVariables.add(setVariable);
            }
        } else {
            errorMsg.error(elem.pos, "User variable in set definition is incorrect!");
        }

        DataType resultType = null;
        //allocate space to a temp abToken to store result
        abToken resultTok = new abToken();

        //Need to find the result type of v from output place when a new user variable is used in set definition
        if (!sameuservar) {
            boolean found = false;
            if (elem.v instanceof IdVariable) {
                String var_key = ((IdVariable) (elem.v)).key;
                for (Arc ao : iTransition.getArcOutList()) {
                    Place po = (Place) (ao.getTarget());
                    String svar = ao.getVar();
                    if (svar.equals(var_key)) {
                        resultType = po.getDataType();
                        found = true;
                    }
                }
            } else {
                errorMsg.error(elem.pos, "Set definition data type is incorrect!");
            }
            if (!found) {
                errorMsg.error(elem.pos, "Set definition data type is incorrect!");
            }
        }

        //find and add tokens
        if (elem.sf instanceof CpxFormula) {
            ComplexFormula complexFormula = ((CpxFormula) elem.sf).cpf;
            if (complexFormula.v instanceof IdVariable) {
                Object lookedupObject = symTable.lookup(((IdVariable) complexFormula.v).key);
                if (lookedupObject instanceof abToken) {
                    for (Token t : ((abToken) lookedupObject).listToken) {

                        //create a temporary token and insert user defined set variable into symbol table
                        if (!sameuservar) {
                            Token tok = new Token();
                            tok.defineTlist(resultType);
                            symTable.update(setVariable, tok, Symbol.TYPE.SINGLE);
                        }
                        symTable.update(complexFormula.uv.s, t, Symbol.TYPE.SINGLE);

                        complexFormula.f.accept(this);
                        if (complexFormula.f instanceof AtFormula ||
                                complexFormula.f instanceof CpFormula ||
                                complexFormula.f instanceof CpxFormula) {
                            if (complexFormula.f.bool_val == true) {
                                if (!sameuservar) {
                                    resultTok.addToken((Token) symTable.lookup(setVariable));
                                } else {
                                    resultTok.addToken((Token) symTable.lookup(complexFormula.uv.s));
                                }
                            }
                        }
                        symTable.delete(complexFormula.uv.s);
                    }
                }
            }
        } else {
            errorMsg.error(elem.pos, "SetDef::Incorrect Set Definition!");
        }

        symTable.delete((((IdVariable) ((VariableTerm) elem.u).v)).key);
        //assign the result token to absyntree

//			newsetvar ="";
        //System.out.println("result token"+resultTok.getTokenbyIndex(1).displayToken());
        mUserVariables.remove(setVariable);
        elem.abTok = resultTok;
    }

    @Override
    public void visit(FunctionExp pTerm) {
			String functionName = pTerm.getName();
			Object[] arguments = new Object[pTerm.getArgumentTerms().size()];
			int index = 0;
			for (Term term : pTerm.getArgumentTerms()) {
				term.accept(this);
				try {
					arguments[index++] = valueOfTerm(term);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pTerm.value = mFunctionHandler.invoke(functionName, arguments);
    }


    @Override
    public void visit(UserVariable elem) {
        if (debug) System.out.println("UserVariable");
    }

    @Override
    public void visit(ConstantTerm elem) {
        if (debug) System.out.println("ConstantTerm");
        elem.c.accept(this);
    }

    @Override
    public void visit(ExpTerm elem) {
        if (debug) System.out.println("ExpTerm");
        elem.e.accept(this);

        try {
            if (elem.e instanceof AExp) {
                elem.int_val = ((AExp) (elem.e)).value.doubleValue();
            } else if (elem.e instanceof RExp) {
                elem.bool_val = ((RExp) (elem.e)).value;
            } else if (elem.e instanceof SExp) {
                elem.abTok = ((SExp) (elem.e)).abTok;
            }
        } catch (RuntimeException ex) {
            System.out.println(String.format("Exception to process formula: %s. Element %s could not be evaluated properly", iTransition.getFormula(), elem.e));
            throw ex;
        }

    }

    @Override
    public void visit(VariableTerm elem) {
        if (debug) System.out.println("VariableTerm");
        elem.v.accept(this);

        if (elem.v instanceof IdVariable) {
            elem.var_key = ((IdVariable) elem.v).key;
        } else if (elem.v instanceof IndexVariable) {
            elem.var_key = ((IndexVariable) elem.v).key;
            elem.index = ((IndexVariable) elem.v).index;
        } else errorMsg.error(elem.pos, "Variable can only be instance of IdVariable or IndexVariable");

        if (!definedVars.contains(elem.var_key)) {
            this.undefVars.add(elem.var_key);
        }
    }

    @Override
    public void visit(StrConstant elem) {
        if (debug) System.out.println("StrConstant");
    }

    public void visit(AExp elem) {
        if (debug) System.out.println("AExp");
        elem.ae.accept(this);

        elem.value = elem.ae.value;
    }

    public void visit(RExp elem) {
        if (debug) System.out.println("RExp");
        elem.re.accept(this);

        elem.value = elem.re.value;
    }

    public void visit(SExp elem) {
        if (debug) System.out.println("SExp");
        elem.se.accept(this);

        if (elem.se instanceof Union ||
                elem.se instanceof Diff ||
                elem.se instanceof BraceTerm ||
                elem.se instanceof Setdef)  //modified by He 8/19/2015
        {
            elem.abTok = elem.se.abTok;
        }
    }

    @Override
    public void visit(AtomicTerm elem) {
        if (debug) System.out.println("AtomicTerm");
        elem.t.accept(this);

        if (elem.t instanceof ConstantTerm) {
            Constant constant = ((ConstantTerm) elem.t).c;
            if (constant instanceof True || constant instanceof False) {
                elem.bool_val = (boolean) constant.value;
            }
        } else if (elem.t instanceof ExpTerm) {
            elem.bool_val = ((ExpTerm) (elem.t)).bool_val;
        } else {
            errorMsg.error(elem.pos, "AtomicTerm::Cannot be VariableTerm or tree type mismatch!");
        }
    }

    @Override
    public void visit(AtFormula elem) {
        if (debug) System.out.println("AtFormula");

        elem.af.accept(this);
        elem.bool_val = elem.af.bool_val;

        //Fix-me:: checking of illegal instance of AtFormula.af should be verified by the testing of the parser and irrelevant here.
    }

    @Override
    public void visit(CpFormula elem) {
        if (debug) System.out.println("CpFormula");

        elem.cf.accept(this);
        elem.bool_val = elem.cf.bool_val;
    }

    @Override
    public void visit(CpxFormula elem) {
        if (debug) System.out.println("CpxFormula");
        elem.cpf.accept(this);

        elem.bool_val = elem.cpf.bool_val;
    }

    @Override
    public void visit(Sentence elem) {
        if (debug) System.out.println("Sentence");

        elem.f.accept(this);
        elem.bool_val = elem.f.bool_val;
    }

    @Override
    public void visit(Empty elem) {
        if (debug) System.out.println("Empty");
    }

    @Override
    public void visit(EmptyTerm elem) {
        if (debug) System.out.println("EmptyTerm");
        elem.e.accept(this);

    }
}
