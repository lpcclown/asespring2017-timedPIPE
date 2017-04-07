package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.Token;

public class False extends Constant<Boolean> {

  public False(int p) {
    super(p, false);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public boolean isValidAsToken() {
    return false;
  }

  @Override
  public Token toToken() {
    return null;
  }
}
