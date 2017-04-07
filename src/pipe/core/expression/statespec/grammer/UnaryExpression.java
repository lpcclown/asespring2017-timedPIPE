package pipe.core.expression.statespec.grammer;

/**
 * Created by Maks on 2/13/2016.
 */
public abstract class UnaryExpression<T> implements Expression<T> {
  private Expression<T> mExpression;

  public UnaryExpression(final Expression pExpression) {
    mExpression = pExpression;
  }

  public Expression<T> getExpression() {
    return mExpression;
  }

  @Override
  public Expression getLeftExpression() {
    return mExpression;
  }

  @Override
  public final Expression getRightExpression() {
    throw new RuntimeException("This expression is unary.");
  }
}
