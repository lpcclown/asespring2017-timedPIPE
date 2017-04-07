package formula.absyntree;

import formula.parser.Visitor;

public abstract class Formula {
  public int pos;
  public String strPre = "";
  public String strPost = "";
  public String z3str;
  public boolean bool_val;
  private String identity = "";

  public abstract void accept(Visitor v);

  public String identityToString() {
    if (identity.length() == 0) {
      identity = String.format("%s@%d", getClass().getName(), System.identityHashCode(this));
    }
    return identity;
  }
}
