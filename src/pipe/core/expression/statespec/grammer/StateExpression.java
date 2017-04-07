package pipe.core.expression.statespec.grammer;

import org.apache.commons.lang.Validate;
import pipe.core.expression.statespec.EvaluationEngine;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class StateExpression<T> implements Expression<T> {

  private String mProperty;
  private String[] mTokens;

  public StateExpression(final String pProperty, final String pState) {
    Validate.notNull(pProperty, "Property name must not be empty");
    Validate.notNull(pState, "State information must not be empty. Either a '*' or a comma separated list of values required");

    mProperty = pProperty;
    mTokens = pState.split(",");
  }

  public String getProperty() {
    return mProperty;
  }

  public String[] getTokens() {
    return mTokens;
  }

  @Override
  public Expression getLeftExpression() {
    return null;
  }

  @Override
  public Expression getRightExpression() {
    return null;
  }

  @Override
  public boolean evaluate(final EvaluationEngine pEvaluationEngine, final T pDataModel) {
    return pEvaluationEngine.evaluate(this, pDataModel);
  }
}
