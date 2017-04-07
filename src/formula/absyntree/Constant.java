package formula.absyntree;

import formula.parser.Visitor;
import org.apache.commons.lang.ObjectUtils;
import pipe.dataLayer.Token;

public abstract class Constant<T> {
  public int pos;
  public T value;
  private String mIdentity = "";

  public Constant(final int pPos, final T pValue)
  {
    value = pValue;
    pos = pPos;
  }

  public abstract void accept(Visitor v);

  public abstract boolean isValidAsToken();

  public abstract Token toToken();

  public String identityToString() {
    if (mIdentity.length() == 0) {
      mIdentity = ObjectUtils.identityToString(this);
    }

    return mIdentity;
  }
}
