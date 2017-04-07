package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class TupleSel extends Exp {
  public Term tleft, tright;
  public TupleSel(Term tleft, Term tright){
    this.tleft = tleft;
    this.tright = tright;
  }

  public void accept(Visitor v){
    v.visit(this);
  }
/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
