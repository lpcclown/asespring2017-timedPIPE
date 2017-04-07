package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class RelOp extends Exp {
  public int type; // the type
  public Term tleft, tright;

  public RelOp(int type, Term tleft, Term tright){
    this.type = type;
    this.tleft = tleft;
    this.tright = tright;
  }
  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
