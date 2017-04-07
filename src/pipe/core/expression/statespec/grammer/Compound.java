package pipe.core.expression.statespec.grammer;

import pipe.core.expression.statespec.EvaluationEngine;

/**
 * Expression => ( Expression )
 *
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class Compound<T> extends UnaryExpression<T> {

  public Compound(final Expression pExpression) {
    super(pExpression);
  }

  @Override
  public boolean evaluate(final EvaluationEngine pEvaluationEngine, final T pDataModel) {
    return pEvaluationEngine.evaluate(this, pDataModel);
  }
}
