package pipe.client.api.model;

/**
 * Created by dalam004 on 2/16/2016.
 */
public enum AnimationType {

  SingleRandomFiring("RF", "Random Fire"),
  FixedStepAnimation("FSA", "Fixed steps animation"),
  StateSearchingAnimation("SSA", "State searching animation");

  private final String mDisplayString;
  private final String mType;

  AnimationType(final String pType, final String pDisplayString) {
    mType = pType;
    mDisplayString = pDisplayString;
  }

  public String getDisplayString() {
    return mDisplayString;
  }
}
