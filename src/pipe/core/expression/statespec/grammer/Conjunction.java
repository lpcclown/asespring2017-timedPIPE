package pipe.core.expression.statespec.grammer;

import pipe.core.expression.statespec.EvaluationEngine;

/**
 *  expression AND expression
 *
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class Conjunction<T> extends BinaryExpression<T> {

  public Conjunction(final Expression<T> pLeftExpression, final Expression<T> pRightExpression) {
    super(pLeftExpression, pRightExpression);
  }

  @Override
  public boolean evaluate(final EvaluationEngine pEvaluationEngine, final T pDataModel) {
    return pEvaluationEngine.evaluate(this, pDataModel);
  }
}
