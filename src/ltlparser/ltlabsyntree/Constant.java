package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class Constant extends Term {
  public Object obj; // the reference to the "constant" object
  public int type; // the only admissible types are sym.BOOL, sym.NUM, sym.STR and sym.EMPTY

  public Constant(Object obj, int type){
    this.obj = obj;
    this.type = type;
  }
  public void accept(Visitor v){
    v.visit(this);
  }

/*public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
