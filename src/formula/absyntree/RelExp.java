package formula.absyntree;

import formula.parser.Visitor;

public abstract class RelExp extends SingleValuedExp<Boolean> {
  public String strPre = "";
  public String strPost = "";

  public final Term t1;
  public final Term t2;

  protected RelExp(final int pPos, final Term pT1, final Term pT2) {
    pos = pPos;
    t1 = pT1;
    t2 = pT2;
  }

}
