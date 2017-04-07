package pipe.gui;

import pipe.client.api.model.AnimationType;
import pipe.dataLayer.Transition;

/**
 * Created by Maks on 2/8/2016.
 */
public class FixedStepsAnimationStrategy implements AnimationStrategy {
  private boolean mIsStarted;
  private int mCurrentStep = -1;
  private int mNumberOfStepsToFollow = 1;


  public FixedStepsAnimationStrategy(final int pNumberOfStepsToFollow) {
    mNumberOfStepsToFollow = pNumberOfStepsToFollow;
  }

  public void setNumberOfStepsToFollow(final int pNumberOfStepsToFollow) {
    mNumberOfStepsToFollow = pNumberOfStepsToFollow;
  }

  public int getCurrentStep() {
    return mCurrentStep;
  }

  public int getNumberOfStepsToFollow() {
    return mNumberOfStepsToFollow;
  }

  @Override
  public AnimationType getAnimationType() {
    return mNumberOfStepsToFollow == 1 ? AnimationType.SingleRandomFiring : AnimationType.FixedStepAnimation;
  }

  @Override
  public String terminationCriteria() {
    return String.format("Number of steps %d", mNumberOfStepsToFollow);
  }

  @Override
  public void startAnimation() {
    mIsStarted = true;
  }

  @Override
  public void stopAnimation() {
    mIsStarted = false;
  }

  @Override
  public boolean shouldContinueNextStep() {
    mCurrentStep++;
    boolean shouldContinue = mIsStarted && mCurrentStep < mNumberOfStepsToFollow;
    if (!shouldContinue) {
      mIsStarted = false;
      mCurrentStep = -1;
      mNumberOfStepsToFollow = 1;
    }
    return shouldContinue;
  }

  @Override
  public void setFiredTransition(final Transition pTransition) {
    // doNothing by design
  }
}
