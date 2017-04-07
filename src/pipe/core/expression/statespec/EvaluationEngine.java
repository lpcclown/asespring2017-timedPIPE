package pipe.core.expression.statespec;

import pipe.core.expression.statespec.grammer.*;

/**
 *
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public interface EvaluationEngine<T, E extends EvaluateAble<T>> {
  //Fix-Me: check whether the generic types are really needed. Also whether the passing of the data model. Eventually this will be
  // utilized by an concrete implementation that can have its own data.

  boolean evaluate(final Sentence<T> pExpression, final T pDataModel);

  boolean evaluate(final Compound<T> pExpression, final T pDataModel);

  boolean evaluate(final Conjunction<T> pExpression, final T pDataModel);

  boolean evaluate(final Disjunction<T> pExpression, final T pDataModel);

  boolean evaluate(final Inversion<T> pExpression, final T pDataModel);

  boolean evaluate(final StateExpression<T> pExpression, final T pDataModel);
}
