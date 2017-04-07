package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class SetOp extends Exp {
  public int type;
  public Term tleft, tright;
  public SetOp(int type, Term tleft, Term tright){
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
