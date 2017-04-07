package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class SetMembOp extends Exp {
  public Variable v;
  public Variable vset;

  public SetMembOp(Variable v, Variable vset/*Exp exp*/){
    this.v = v;
    //this.exp = exp;
	this.vset = vset;
  }

  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
