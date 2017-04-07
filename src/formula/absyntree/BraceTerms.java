package formula.absyntree;

import formula.parser.Visitor;

import java.util.ArrayList;

public class BraceTerms extends SetExp {
  public Terms ts;
  //z3
  public ArrayList<String> z3TermList = new ArrayList<String>();

  public BraceTerms(int p, Terms ts) {
    this.pos = p;
    this.ts = ts;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

}
