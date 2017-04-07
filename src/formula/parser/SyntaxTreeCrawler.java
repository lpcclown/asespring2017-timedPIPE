package formula.parser;

import formula.absyntree.*;

import java.util.ArrayList;
import java.util.HashSet;

public class SyntaxTreeCrawler implements Visitor {
	
	//Transition iTransition;
	public ArrayList<String> mUndefinedVariables; //record all vars in formula that has not yet defined in arc var
	public String setdefVariable; //record new user defined variable in set definition
	public String quantifiedVariable; //record new user defined variable in set definition
	public boolean confirmed; //a new user defined variable is different from the quantified user variable
	private HashSet<String> mDefinedVariables = new HashSet<>();
	boolean debug = false;
	public SyntaxTreeCrawler(HashSet<String> pDefinedVariables){
		mUndefinedVariables = new ArrayList<String>();
		setdefVariable = "";
		quantifiedVariable ="";
		confirmed = true;
		mDefinedVariables.addAll(pDefinedVariables);
	}
	
	public void visit(AndFormula elem) {
		if(debug)System.out.println("AndFormula");
		
		elem.f1.accept(this);
		elem.f2.accept(this);
		

	}

	@Override
	public void visit(BraceTerm elem) {
		if(debug)System.out.println("BraceTerm");
		elem.t.accept(this);

	}

	@Override
	public void visit(BraceTerms elem) {
		if(debug)System.out.println("BraceTerms");
		elem.ts.accept(this);
		

	}

	@Override
	public void visit(ComplexFormula elem) {
		if(debug)System.out.println("ComplexFormula");
		elem.q.accept(this);
		elem.uv.accept(this);
		elem.d.accept(this);
		elem.v.accept(this);
	//	elem.f.accept(this);
	}

	@Override
	public void visit(Diff elem) {
		if(debug)System.out.println("Diff");
		elem.t1.accept(this);
		elem.t2.accept(this);


		}

	@Override
	public void visit(Div elem) {
		if(debug)System.out.println("Div");
		elem.t1.accept(this);
		elem.t2.accept(this);
		}

	@Override
	public void visit(EqRel elem) {
		if(debug)System.out.println("EqRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
		}

	@Override
	public void visit(EquivFormula elem) {
		if(debug)System.out.println("EquivFormula");
		elem.f1.accept(this);
		elem.f2.accept(this);
		
	
	}

	@Override
	public void visit(Exists elem) {
		if(debug)System.out.println("Exists");
	}

	@Override
	public void visit(False elem) {
		if(debug)System.out.println("False");
		elem.value = false;
	}

	@Override
	public void visit(ForAll elem) {
		if(debug)System.out.println("ForAll");
//		elem.quant_type = 0;
	}

	@Override
	public void visit(GeqRel elem) {
		if(debug)System.out.println("GeqRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
	}

	@Override
	public void visit(GtRel elem) {
		if(debug)System.out.println("GtRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
	}

	@Override
	public void visit(Identifier elem) {
		if(debug)System.out.println("Identifier");

	}

	@Override
	public void visit(IdVariable elem) {
		if(debug)System.out.println("IdVariable");
	}

	@Override
	public void visit(ImpFormula elem) {
		if(debug)System.out.println("ImpFormula");
		elem.f1.accept(this);
		elem.f2.accept(this);
	}

	@Override
	public void visit(In elem) {
		if(debug)System.out.println("In");
		elem.domain_type = 0;
	}

	@Override
	public void visit(Index elem) {
		if(debug)System.out.println("Index");
		elem.n.accept(this);
		elem.int_val = Integer.parseInt(elem.n.n);
	}

	@Override
	public void visit(IndexVariable elem) {
		if(debug)System.out.println("IndexVariable");
		elem.i.accept(this);
		elem.idx.accept(this);
		
		elem.key = elem.i.key;
		elem.index = elem.idx.int_val;
	}

	@Override
	public void visit(InRel elem) {
		if(debug)System.out.println("InRel");

		elem.t1.accept(this);
		elem.t2.accept(this);
		

	}

	@Override
	public void visit(LeqRel elem) {
		if(debug)System.out.println("LeqRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		
	}

	@Override
	public void visit(LtRel elem) {
		if(debug)System.out.println("LtRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
	
	}

	@Override
	public void visit(Minus elem) {
		if(debug)System.out.println("Minus");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
	}

	@Override
	public void visit(Mod elem) {
		if(debug)System.out.println("Mod");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		
	}

	@Override
	public void visit(Mul elem) {
		if(debug)System.out.println("Mul");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
	}


	public void visit(NegExp elem) {
		if(debug)System.out.println("NegExp");
		elem.t.accept(this);
		
	}

	@Override
	public void visit(NeqRel elem) {
		if(debug)System.out.println("NeqRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		
	}

	@Override
	public void visit(Nexists elem) {
//		elem.quant_type = 2;
		if(debug)System.out.println("Nexists");
	}

	@Override
	public void visit(Nin elem) {
		elem.domain_type = 1;
		if(debug)System.out.println("Nin elem");
	}

	@Override
	public void visit(NinRel elem) {
		if(debug)System.out.println("NinRel");
		elem.t1.accept(this);
		elem.t2.accept(this);
		
		
	}

	@Override
	public void visit(NotFormula elem) {
		if(debug)System.out.println("NotFormula");
		elem.f.accept(this);
		
	/*	if(elem.f instanceof AtFormula){
			if(((AtFormula)(elem.f)).bool_val == true){
				elem.bool_val = false;
			}else elem.bool_val = true;
		}else if(elem.f instanceof CpFormula){
			if(((CpFormula)(elem.f)).bool_val == true){
				elem.bool_val = false;
			}else elem.bool_val = true;
		}else if(elem.f instanceof CpxFormula){
			if(((CpxFormula)(elem.f)).bool_val == true){
				elem.bool_val = false;
			}else elem.bool_val = true;
		}
	*/	
	}

	@Override
	public void visit(NumConstant elem) {
		if(debug)System.out.println("NumConstant");
//		elem.num.accept(this);
//		elem.value = Integer.parseInt(elem.num.n);
	}

	@Override
	public void visit(Num elem) {
		if(debug)System.out.println("Num");
		//elem.d = Double.parseDouble(elem.n);
	}

	@Override
	public void visit(OrFormula elem) {
		if(debug)System.out.println("OrFormula");
		elem.f1.accept(this);
		elem.f2.accept(this);
		
	}

	@Override
	public void visit(Plus elem) {
		if(debug)System.out.println("Plus");
		elem.t1.accept(this);
		elem.t2.accept(this);

		
	}

    @Override
	
	public void visit(Setdef elem) {
		if(debug)System.out.println("Setdef");
		if ((Term) elem.u instanceof VariableTerm){
			((VariableTerm) elem.u).isUserVariable=true;
			setdefVariable = ((IdVariable)((VariableTerm) elem.u).v).key;
		}
		elem.u.accept(this);
		elem.v.accept(this);
		elem.sf.accept(this);
		if (!confirmed) {
			((VariableTerm) elem.u).isUserVariable=false;
			confirmed = true;
		}
		setdefVariable = "";
		quantifiedVariable = "";
	}

	@Override
	public void visit(FunctionExp pTerm) {

	}

	@Override
	public void visit(TermRest elem) {
		// TODO Auto-generated method stub
		if(debug)System.out.println("TermRest");
		elem.t.accept(this);
	}

	@Override
	public void visit(Terms elem) {
		// TODO Auto-generated method stub
		if(debug)System.out.println("Terms");
		elem.t.accept(this);
		
		
	}

	@Override
	public void visit(True elem) {
		if(debug)System.out.println("True");
		elem.value = true;
	}

	@Override
	
	public void visit(Union elem) {
		if(debug)System.out.println("Union");
		elem.t1.accept(this);
		elem.t2.accept(this);

		
	}

	@Override
	public void visit(UserVariable elem) {
		if(debug)System.out.println("UserVariable");
		quantifiedVariable = elem.s;
		if (elem.s.equals(setdefVariable)) 
			setdefVariable="";
			confirmed = false;
	}

	@Override
	public void visit(ConstantTerm elem) {
		if(debug)System.out.println("ConstantTerm");
		elem.c.accept(this);
		
		
	}

	@Override
	public void visit(ExpTerm elem) {
		if(debug)System.out.println("ExpTerm");
		elem.e.accept(this);
		
//		if(elem.e instanceof AExp){
//			elem.int_val = 10;
//		}else if(elem.e instanceof RExp){
//			elem.bool_val = true;
//		}else if(elem.e instanceof SExp){
//			elem.abTok = ((SExp)(elem.e)).abTok;
//		}
	}

	@Override
	public void visit(VariableTerm elem) {
		if(debug)System.out.println("VariableTerm");
		elem.v.accept(this);
		if(elem.v instanceof IdVariable){
			elem.var_key = ((IdVariable)elem.v).key;
		}else if(elem.v instanceof IndexVariable){
			elem.var_key = ((IndexVariable)elem.v).key;
			elem.index = ((IndexVariable)elem.v).index;
		};
		if (setdefVariable.equals(elem.var_key)) {
			elem.isUserVariable=true;
		};
			
		if(!mDefinedVariables.contains(elem.var_key)&&(!elem.isUserVariable)&&!(quantifiedVariable.equals(elem.var_key)))
		{
			this.mUndefinedVariables.add(elem.var_key);
		};
	}
	
	@Override
	public void visit(StrConstant elem) {
		if(debug)System.out.println("StrConstant");
	}
	
	public void visit(AExp elem){
		if(debug)System.out.println("AExp");
		elem.ae.accept(this);
		

	}
	
	public void visit(RExp elem){
		if(debug)System.out.println("RExp");
		elem.re.accept(this);


	}
	
	public void visit(SExp elem){
		if(debug)System.out.println("SExp");
		elem.se.accept(this);


	}

	@Override
	public void visit(AtomicTerm elem) {
		if(debug)System.out.println("AtomicTerm");
		elem.t.accept(this);
		
	}

	@Override
	public void visit(AtFormula elem) {
		if(debug)System.out.println("AtFormula");
		elem.af.accept(this);
		
	}

	@Override
	public void visit(CpFormula elem) {
		if(debug)System.out.println("CpFormula");
		elem.cf.accept(this);
			
	}

	@Override
	public void visit(CpxFormula elem) {
		if(debug)System.out.println("CpxFormula");
		elem.cpf.accept(this);
		
	}

	@Override
	public void visit(Sentence elem) {
		if(debug)System.out.println("Sentence");
		elem.f.accept(this);
		
	}
	@Override
	public void visit(Empty elem) {
		if(debug)System.out.println("Empty");
	}
	@Override
	public void visit(EmptyTerm elem) {
		if(debug)System.out.println("EmptyTerm");
		elem.e.accept(this);
		
	}
}
