package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.Token;

public class True extends Constant<Boolean> {

  public True(int p) {
    super(p, true);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public boolean isValidAsToken() {
    return true;
  }

  @Override
  public Token toToken() {
    return null;
  }
}
