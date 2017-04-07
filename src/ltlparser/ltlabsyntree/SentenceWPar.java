package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;



public class SentenceWPar extends Exp{ // Should inherit from LogicExp
  public LogicExp exp;
  public SentenceWPar(LogicExp e){
    exp = e;
  }
  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/
}
