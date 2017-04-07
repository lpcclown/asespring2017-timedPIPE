package analysis;

/**
 * Created by Maks on 6/8/2016.
 */
public class SetDefinitionInfo {
  private String mFunctionName;
  private String mLoopVariable;
  private String mPreCondition;
  private String mPostCondition;
  private String mTarget;
  private String mReceiveSequence;
  private String mDeclaration;
  private String mSource;

  public String getFunctionName() {
    return mFunctionName;
  }

  public String getLoopVariable() {
    return mLoopVariable;
  }

  public String getPreCondition() {
    return mPreCondition;
  }

  public String getPostCondition() {
    return mPostCondition;
  }

  public String getTarget() {
    return mTarget;
  }
  public String getSource() {
    return mSource;
  }

  public String getReceiveSequence() {
    return mReceiveSequence;
  }

  public String getDeclaration() {
    return mDeclaration;
  }

  public void setFunctionName(final String pFunctionName) {
    mFunctionName = pFunctionName;
  }

  public void setLoopVariable(final String pLoopVariable) {
    mLoopVariable = pLoopVariable;
  }

  public void setPreCondition(final String pPreCondition) {
    mPreCondition = pPreCondition;
  }

  public void setPostCondition(final String pPostCondition) {
    mPostCondition = pPostCondition;
  }

  public void setTarget(final String pTarget) {
    mTarget = pTarget;
  }

  public void setReceiveSequence(final String pReceiveSequence) {
    mReceiveSequence = pReceiveSequence;
  }

  public void setDeclaration(final String pDeclaration) {
    mDeclaration = pDeclaration;
  }

  public void setSource(final String pSource) {
    mSource = pSource;
  }
}
