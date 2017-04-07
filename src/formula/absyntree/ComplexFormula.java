package formula.absyntree;

import formula.parser.Visitor;

public class ComplexFormula extends Formula {
  //	public int pos;
  public Quantifier q;
  public UserVariable uv;
  public Domain d;
  public Variable v;
  public Formula f;
  public int treeLevel;

  public ComplexFormula(int p, Quantifier q, UserVariable uv, Domain d, Variable v, Formula f) {
    this.pos = p;
    this.q = q;
    this.uv = uv;
    this.d = d;
    this.v = v;
    this.f = f;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
