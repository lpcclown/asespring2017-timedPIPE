package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class Predicate extends LogicExp {
  public Identifier id;
  public TermList termLst;
  public Predicate(Identifier id, TermList lst){
    this.id = id;
    this.termLst = lst;
  }
  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
