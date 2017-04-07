package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Token;

import java.math.BigDecimal;

public class NumConstant extends Constant<Number> {
  public Num num;

  public NumConstant(int p, Num num) {
    super(p, new BigDecimal(num.n));
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
    DataType resultType = new DataType("numberTok", new String[]{BasicType.TYPES[BasicType.NUMBER]}, true, null);
    Token token = new Token(resultType);

    BasicType typeValue = new BasicType(BasicType.NUMBER);
    typeValue.setValue(this.value);
    if (token.add(new BasicType[]{typeValue})) {
      return token;
    }

    return null;
  }
}
