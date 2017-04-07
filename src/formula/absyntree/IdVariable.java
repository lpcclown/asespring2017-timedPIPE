package formula.absyntree;

import formula.parser.Visitor;

public class IdVariable extends Variable {

  public IdVariable(int p, String as) {
    pos = p;
    key = as;
  }


  public void accept(Visitor v) {
    v.visit(this);
  }
}
