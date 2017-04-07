package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class UnLogicOp extends LogicExp {
  public LogicExp l;
  public int type; // sym.NOT is accepted, and plus FOLTL op

  public UnLogicOp(LogicExp l, int type){
    this.l = l;
    this.type = type;
  }
  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
