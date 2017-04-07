package formula.absyntree;

import formula.parser.Visitor;

public abstract class CompoundFormula extends Formula {
  public int treeLevel;

  public abstract void accept(Visitor v);
}
