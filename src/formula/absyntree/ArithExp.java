package formula.absyntree;

import java.math.BigDecimal;

public abstract class ArithExp extends SingleValuedExp<BigDecimal> {
  public final Term t1;
  public final Term t2;

  protected ArithExp(final int pPos, final Term pT1, final Term pT2) {
    pos = pPos;
    t1 = pT1;
    t2 = pT2;
  }

  protected ArithExp(final int pPos, final Term pT1) {
    pos = pPos;
    t1 = pT1;
    t2 = null;
  }
}
