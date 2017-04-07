package pipe.gui;

import formula.parser.ErrorMsg;
import pipe.client.api.model.AnimationType;
import pipe.core.expression.statespec.TokenMatchingEvaluationEngine;
import pipe.core.expression.statespec.grammer.Sentence;
import pipe.core.expression.statespec.parser.ExpressionParser;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class ReachabilityTestingAnimationStrategy implements AnimationStrategy {

  private final String mPropertySpecificationText;
  private TokenMatchingEvaluationEngine mEvaluationEngine;
  private Sentence mPropertySpecification;
  private boolean mIsRunning;

  public ReachabilityTestingAnimationStrategy(final String pPropertySpecificationText, final DataLayer pDataModel) {
    ErrorMsg errorMsg = new ErrorMsg("");
    ExpressionParser parser = new ExpressionParser();
    mPropertySpecificationText = pPropertySpecificationText;
    try {
      mPropertySpecification = parser.parseExpression(pPropertySpecificationText, errorMsg);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    mEvaluationEngine = new TokenMatchingEvaluationEngine(pDataModel);
  }

  @Override
  public AnimationType getAnimationType() {
    return AnimationType.StateSearchingAnimation;
  }

  @Override
  public String terminationCriteria() {
    return mPropertySpecificationText;
  }

  @Override
  public void startAnimation() {
    mIsRunning = true;
  }

  @Override
  public void stopAnimation() {
    mIsRunning = false;
  }

  @Override
  public boolean shouldContinueNextStep() {
    boolean isTerminationConditionMatched = mEvaluationEngine.evaluate(mPropertySpecification, CreateGui.getModel());
    return mIsRunning && !isTerminationConditionMatched;
  }

  @Override
  public void setFiredTransition(final Transition pTransition) {

  }
}
