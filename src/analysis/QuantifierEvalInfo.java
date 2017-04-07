package analysis;

/**
 * Created by Maks on 5/23/2016.
 */
public class QuantifierEvalInfo {
  public enum QuantifierType {
    FORALL, EXISTS, NEXISTS
  }

  private String mFunctionName;
  private String mDeciderVariable;
  private String mLoopVariable;
  private String mSourceName;
  private QuantifierType mType;
  private String mCondition;

  public String getFunctionName() {
    return mFunctionName;
  }

  public String getDeciderVariable() {
    return mDeciderVariable;
  }

  public String getLoopVariableName() {
    return mLoopVariable;
  }

  public String getChannelName() {
    return mSourceName;
  }

  public QuantifierType getType() {
    return mType;
  }

  public String getCondition() {
    return mCondition;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private QuantifierEvalInfo mInfo = new QuantifierEvalInfo();

    public Builder setFunctionName(String pFunctionName) {
      mInfo.mFunctionName = pFunctionName;
      return this;
    }

    public Builder setDeciderVariable(String pDeciderVariable) {
      mInfo.mDeciderVariable = pDeciderVariable;
      return this;
    }

    public Builder setLoopVariableName(String pLoopVariable) {
      mInfo.mLoopVariable = pLoopVariable;
      return this;
    }

    public Builder setSourceName(String pSourceName) {
      mInfo.mSourceName = pSourceName;
      return this;
    }

    public Builder setType(QuantifierType mType) {
      mInfo.mType = mType;
      return this;
    }

    public Builder setCondition(String mCondition) {
      mInfo.mCondition = mCondition;
      return this;
    }

    public QuantifierEvalInfo build() {
      return mInfo;
    }
  }
}
