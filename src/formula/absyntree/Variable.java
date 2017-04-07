package formula.absyntree;

import formula.parser.Visitor;

public abstract class Variable {
  public String key;
  public int pos;

  private String identity = "";

  public abstract void accept(Visitor v);

  public String identityToString() {
    if (identity.length() == 0) {
      identity = String.format("%s@%d", getClass().getName(), System.identityHashCode(this));
    }
    return identity;
  }
}
