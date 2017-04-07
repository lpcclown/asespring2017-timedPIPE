package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;


public class BinLogicOp extends LogicExp {
  public LogicExp l1,l2;
  public int type;
  /*
  acceptable types sym.OR, sym.AND, sym.IMP, sym.EQUIV
  */
  public BinLogicOp(LogicExp l1, LogicExp l2, int type){
    this.l1 = l1;
    this.l2 = l2;
    this.type = type;
  }

  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
