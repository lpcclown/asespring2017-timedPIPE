package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class QuantLogicOp extends LogicExp {
  public int type; // quantifier type
  public QuantList qlist;
  public LogicExp exp;
  public QuantLogicOp(int type, QuantList qlist, LogicExp exp ){
    this.type = type;
    this.qlist = qlist;
    this.exp = exp;
  }

  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
