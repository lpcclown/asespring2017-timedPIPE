package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class Variable extends Term {
  public Identifier id;
  public Variable(Identifier id){
    this.id = id;
  }
  public void accept(Visitor v){
    v.visit(this);
  }

/*public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
