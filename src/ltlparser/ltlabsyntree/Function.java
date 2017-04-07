package ltlparser.ltlabsyntree;

import ltlparser.visitor.Visitor;
/*import visitor.TypeVisitor;
import visitor.ExpVisitor;*/

public class Function extends Term {
  public Identifier name;
  public TermList params;
  public Function(Identifier name, TermList params){
    this.name = name;
    this.params = params;
  }
  public void accept(Visitor v){
    v.visit(this);
  }

/*public abstract Type accept(TypeVisitor v);
  public abstract semant.Exp accept(ExpVisitor v);*/

}
