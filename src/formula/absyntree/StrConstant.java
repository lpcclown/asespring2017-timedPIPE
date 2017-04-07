package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Token;

public class StrConstant extends Constant<String> {

  public StrConstant(int p, String str) {
    super(p, str);
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
    DataType resultType = new DataType("stringTok", new String[]{BasicType.TYPES[BasicType.STRING]}, true, null);
    Token token = new Token(resultType);

    BasicType typeValue = new BasicType(BasicType.STRING, 0, this.value);
    if (token.add(new BasicType[]{typeValue})) {
      return token;
    }

    return null;
  }
}
