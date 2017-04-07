package pipe.core.expression.statespec.grammer;

import formula.absyntree.Exp;

/**
 * Created by Maks on 2/13/2016.
 */
public abstract class BinaryExpression<T> implements Expression<T> {
  Expression<T> mLeftExpression;
  Expression<T> mRightExpression;

  public BinaryExpression(Expression<T> pLeftExpression, final Expression<T> pRightExpression) {
    mLeftExpression = pLeftExpression;
    mRightExpression = pRightExpression;
  }

  @Override
  public Expression<T> getLeftExpression() {
    return mLeftExpression;
  }

  @Override
  public Expression<T> getRightExpression() {
    return mRightExpression;
  }

  @Override
  public final boolean isUnaryExpression() {
    return false;
  }
}
