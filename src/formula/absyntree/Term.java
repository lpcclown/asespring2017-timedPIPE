package formula.absyntree;

import formula.parser.Visitor;

public abstract class Term {
  public int pos;
  public String str = "";
  public boolean firstField = false;
  public String placeName; //in case firstField is true

  private String identity = "";
  public abstract void accept(Visitor v);

  public String identityToString() {
    if (identity.length() == 0) {
      identity = String.format("%s@%d", getClass().getName(), System.identityHashCode(this));
    }
    return identity;
  }
}
