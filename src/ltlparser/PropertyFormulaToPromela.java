package ltlparser;

import ltlparser.ltlabsyntree.BinArithOp;
import ltlparser.ltlabsyntree.BinLogicOp;
import ltlparser.ltlabsyntree.Constant;
import ltlparser.ltlabsyntree.Function;
import ltlparser.ltlabsyntree.Identifier;
import ltlparser.ltlabsyntree.LogicSentence;
import ltlparser.ltlabsyntree.Predicate;
import ltlparser.ltlabsyntree.QuantLogicOp;
import ltlparser.ltlabsyntree.RelOp;
import ltlparser.ltlabsyntree.SentenceWPar;
import ltlparser.ltlabsyntree.SetDef;
import ltlparser.ltlabsyntree.SetMembOp;
import ltlparser.ltlabsyntree.SetOp;
import ltlparser.ltlabsyntree.TupleSel;
import ltlparser.ltlabsyntree.UnArithOp;
import ltlparser.ltlabsyntree.UnLogicOp;
import ltlparser.ltlabsyntree.Variable;
import ltlparser.visitor.Visitor;
import ltlparser.errormsg.ErrorMsg;

public class PropertyFormulaToPromela implements Visitor{

	ErrorMsg errorMsg;
	
	public PropertyFormulaToPromela(ErrorMsg errorMsg){
		this.errorMsg = errorMsg;
	}
	@Override
	public void visit(LogicSentence elem) {
		elem.exp.accept(this);
		
		elem.formula += "ltl f{ ";
		if(elem.exp instanceof BinLogicOp){
			elem.formula += ((BinLogicOp)(elem.exp)).formula;
		}else if(elem.exp instanceof QuantLogicOp){
			elem.formula += ((QuantLogicOp)(elem.exp)).formula;
		}else if(elem.exp instanceof Predicate){
			elem.formula += ((Predicate)(elem.exp)).formula;
		}else if(elem.exp instanceof UnLogicOp){
			elem.formula += ((UnLogicOp)(elem.exp)).formula;
		}else if(elem.exp instanceof ltlparser.ltlabsyntree.Term){
			elem.formula += ((ltlparser.ltlabsyntree.Term)(elem.exp)).formula;
		}
		
		elem.formula += "}\n";
	}

	@Override
	public void visit(Variable elem) {
		elem.id.accept(this);
		
		elem.formula = elem.id.s;
		
	}

	@Override
	public void visit(UnLogicOp elem) {
		elem.l.accept(this);
		
		//TODO: next, until and wkuntil not finished yet
		switch(elem.type){
			case sym.NOT:  
				elem.formula = "( !("+elem.l.formula+") )";
				break;
			case sym.FLTL_ALWAYS:  
				elem.formula = "( []("+elem.l.formula+") )";
				break;
			case sym.FLTL_NEXT: 
				break;
			case sym.FLTL_SOMETIMES:  //eventually
				elem.formula = "( <>("+elem.l.formula+") )";
				break;
			case sym.FLTL_UNTIL:  
				break;
			case sym.FLTL_WKUNTIL:  
				break;
		}
		
	}

	@Override
	public void visit(UnArithOp elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TupleSel elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SetOp elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SetMembOp elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RelOp elem) {
		elem.tleft.accept(this);
		elem.tright.accept(this);
		
		elem.formula = "( ";
		
		switch(elem.type){
		case sym.EQ:
			elem.formula += elem.tleft.formula+" = "+elem.tright.formula;
			break;
		case sym.NEQ:
			elem.formula += elem.tleft.formula+" != "+elem.tright.formula;
			break;
		case sym.GT:
			elem.formula += elem.tleft.formula+" > "+elem.tright.formula;
			break;
		case sym.LT:
			elem.formula += elem.tleft.formula+" < "+elem.tright.formula;
			break;
		case sym.GEQ:
			elem.formula += elem.tleft.formula+" >= "+elem.tright.formula;
			break;
		case sym.LEQ:
			elem.formula += elem.tleft.formula+" <= "+elem.tright.formula;
			break;
		}
		
	}

	@Override
	public void visit(QuantLogicOp elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Identifier elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Constant elem) {
		switch(elem.type){
		case sym.BOOL:
			elem.formula = (Boolean) (elem.obj) ? "true" : "false";
			break;
		case sym.NUM:
			elem.formula = (String)(elem.obj);
			break;
		case sym.STR:
			elem.formula = (String)(elem.obj);
			break;
		case sym.EMPTY:
			elem.formula = "null";
			break;
		}
		
	}

	@Override
	public void visit(BinLogicOp elem) {
		elem.l1.accept(this);
		elem.l2.accept(this);
		
		elem.formula = "( ";
		
		switch(elem.type){
			case sym.AND:  
				elem.formula += elem.l1.formula+" /\\ "+elem.l2.formula;
				break;
			case sym.OR:
				elem.formula += elem.l1.formula+" \\/ "+elem.l2.formula;
				break;
			case sym.IMP:
				elem.formula += elem.l1.formula+" -> "+elem.l2.formula;
				break;
			case sym.EQUIV:
				elem.formula += elem.l1.formula+" <-> "+elem.l2.formula;
				break;
		}
		
		elem.formula += " )";
		
	}

	@Override
	public void visit(BinArithOp elem) {
		elem.tleft.accept(this);
		elem.tright.accept(this);
		
		switch(elem.type){
			case sym.MINUS:  
				elem.formula = elem.tleft.formula+" - "+elem.tright.formula;
				break;
			case sym.PLUS:
				elem.formula = elem.tleft.formula+" + "+elem.tright.formula;
				break;
			case sym.MULT:
				elem.formula = elem.tleft.formula+" * "+elem.tright.formula;
				break;
			case sym.DIV:
				elem.formula = elem.tleft.formula+" / "+elem.tright.formula;
				break;
		}
		
	}

	@Override
	public void visit(Predicate elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SetDef elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SentenceWPar elem) {
		elem.exp.accept(this);
		
		elem.formula = "( "+elem.exp.formula+" )";
	}

}
