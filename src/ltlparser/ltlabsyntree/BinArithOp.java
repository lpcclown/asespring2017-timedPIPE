package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;


public class BinArithOp extends Exp {
  public int type; // the type of the arithmetic operation
  public Term tleft;
  public Term tright;
  public BinArithOp(int type, Term tleft, Term tright){
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
