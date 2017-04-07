package pipe.core.expression.statespec.grammer;

import formula.absyntree.Exp;
import pipe.core.expression.statespec.EvaluationEngine;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public interface Expression<T> extends EvaluateAble<T> {

  default boolean isUnaryExpression() {
    return true;
  }

  default boolean isTerminalExpression() {
    return false;
  }

  Expression getLeftExpression();

  Expression getRightExpression();

}
