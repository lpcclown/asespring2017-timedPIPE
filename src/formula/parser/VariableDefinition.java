package formula.parser;

import analysis.PromelaUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Maks on 5/30/2016.
 */
public class VariableDefinition {
  public enum Type {
    GLOBAL, LOCAL, SET_ELEMENT
  }

  private final String mVariableName;
  private List<String> mInputPlaceNames = new ArrayList<>(2);
  private List<String> mOutputPlaceNames = new ArrayList<>(1);
  private boolean mIsSetVariable;
  private String mDeclaration;
  private String mReceiveSequence;
  private String mEvalSequence;
  private Type mType;

  public VariableDefinition(final String pVariableName) {
    mVariableName = pVariableName;
  }

  public void setIsSetVariable(final boolean pIsSetVariable) {
    mIsSetVariable = pIsSetVariable;
  }

  public void addInputPlaceName(final String pPlaceName) {
    mInputPlaceNames.add(pPlaceName);
  }

  public void addInputPlaceNames(final Collection<String> pPlaceNames) {
    mInputPlaceNames.addAll(pPlaceNames);
  }

  public void addOutputPlaceName(final String pPlaceName) {
    mOutputPlaceNames.add(pPlaceName);
  }

  public void addOutputPlaceNames(final Collection<String> pPlaceNames) {
    mOutputPlaceNames.addAll(pPlaceNames);
  }

  public void setDeclaration(final String pDeclaration) {
    mDeclaration = pDeclaration;
  }

  public void setReceiveSequence(final String pReceiveSequence) {
    mReceiveSequence = pReceiveSequence;
  }

  public void setEvalSequence(final String pEvalSequence) {
    mEvalSequence = pEvalSequence;
  }

  public String getVariableName() {
    return mVariableName;
  }

  public List<String> getInputPlaceNames() {
    return Collections.unmodifiableList(mInputPlaceNames);
  }

  public List<String> getOutputPlaceNames() {
    return Collections.unmodifiableList(mOutputPlaceNames);
  }

  public boolean isSetVariable() {
    return mIsSetVariable;
  }

  public boolean isOutputVariable() {
    return mOutputPlaceNames.size() > 0;
  }

  public boolean isInputVariable() {
    return mInputPlaceNames.size() > 0;
  }

  public String getDeclaration() {
    return mDeclaration;
  }

  public String getReceiveSequence() {
    return mReceiveSequence;
  }

  public String getEvalSequence() {
    if (StringUtils.isBlank(mEvalSequence)) {
      mEvalSequence = PromelaUtil.receiveSequenceToEvalSequence(mReceiveSequence);
    }
    return mEvalSequence;
  }

  public Type getType() {
    return mType;
  }

  public void setType(final Type pType) {
    mType = pType;
  }
}
