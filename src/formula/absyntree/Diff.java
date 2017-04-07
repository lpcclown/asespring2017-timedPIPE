package formula.absyntree;

import formula.parser.Visitor;

public class Diff extends SetExp {
  public Term t1, t2;

  //z3
  public boolean isPostCond = false;

  public Diff(int p, Term t1, Term t2) {
    this.pos = p;
    this.t1 = t1;
    this.t2 = t2;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
