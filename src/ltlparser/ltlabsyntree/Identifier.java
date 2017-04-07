package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class Identifier {
  public String s;

  public Identifier(String as) { 
    s=as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public String toString(){
    return s;
  }

/*  public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
