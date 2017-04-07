package pipe.core.expression.statespec;

import pipe.core.expression.statespec.grammer.*;
import pipe.dataLayer.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class TokenMatchingEvaluationEngine implements EvaluationEngine<DataLayer, EvaluateAble<DataLayer>> {
  private static final Token SPECIAL_TOKEN = new Token();
  private static final BasicType WILDCARD = new BasicType(BasicType.STRING, "*");

  private final DataLayer mDataModel;
  private Map<Object, Token> mResolvedTokensMap = new HashMap<>();

  public TokenMatchingEvaluationEngine(final DataLayer pDataLayer) {
    mDataModel = pDataLayer;
  }

  @Override
  public boolean evaluate(final Sentence<DataLayer> pExpression, final DataLayer pDataModel) {
    return pExpression.getExpression().evaluate(this, pDataModel);
  }

  @Override
  public boolean evaluate(final Compound<DataLayer> pExpression, final DataLayer pDataModel) {
    return pExpression.getExpression().evaluate(this, pDataModel);
  }

  @Override
  public boolean evaluate(final Conjunction<DataLayer> pExpression, final DataLayer pDataModel) {
    boolean leftEvaluationResult = pExpression.getLeftExpression().evaluate(this, pDataModel);
    boolean rightEvaluationResult = pExpression.getRightExpression().evaluate(this, pDataModel);

    return leftEvaluationResult && rightEvaluationResult;
  }

  @Override
  public boolean evaluate(final Disjunction<DataLayer> pExpression, final DataLayer pDataModel) {
    boolean leftEvaluationResult = pExpression.getLeftExpression().evaluate(this, pDataModel);
    boolean rightEvaluationResult = pExpression.getRightExpression().evaluate(this, pDataModel);

    return leftEvaluationResult || rightEvaluationResult;
  }

  @Override
  public boolean evaluate(final Inversion<DataLayer> pExpression, final DataLayer pDataModel) {
    boolean evaluationResult = pExpression.getExpression().evaluate(this, pDataModel);

    return !evaluationResult;
  }

  @Override
  public boolean evaluate(final StateExpression<DataLayer> pExpression, final DataLayer pDataModel) {
    Place place = mDataModel.getPlaceById(pExpression.getProperty());
    Token token = getResolvedToken(pExpression);

    boolean hasMatchingToken = false;
    if (token == SPECIAL_TOKEN) {
      hasMatchingToken = !place.getToken().listToken.isEmpty();
    }
    else {
      for (Token listedToken : place.getToken().listToken) {
        if (matchTokens(token, listedToken)) {
          hasMatchingToken = true;
          break;
        }
      }
    }

    return hasMatchingToken;
  }

  private boolean matchTokens(final Token pReference, final Token pToMatch) {
    if (pReference.Tlist.size() != pToMatch.Tlist.size()) {
      return false;
    }

    boolean matched = true;
    Iterator<BasicType> referenceIterator = pReference.Tlist.iterator();
    Iterator<BasicType> testingIterator = pToMatch.Tlist.iterator();
    while (referenceIterator.hasNext()) {
      BasicType reference = referenceIterator.next();
      BasicType toBeTested = testingIterator.next();
      if (reference != WILDCARD && !reference.equals(toBeTested)) {
        matched = false;
        break;
      }
    }

    return matched;
  }

  private Token getResolvedToken(final StateExpression<DataLayer> pExpression) {
    if (mResolvedTokensMap.containsKey(pExpression)) {
      return mResolvedTokensMap.get(pExpression);
    }

    String[] tokens = pExpression.getTokens();
    if (tokens == null || tokens.length == 0) {
      return null;
    }
    if (tokens.length == 1 && tokens[0].equals("*")) {
      return SPECIAL_TOKEN;
    }

    Place place = mDataModel.getPlaceById(pExpression.getProperty());
    DataType dataType = place.getDataType();
    if (tokens.length != dataType.getTypes().size()) {
      return null;
    }

    BasicType[] tokenElements = new BasicType[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].equals("*")) {
        tokenElements[i] = WILDCARD;
      }
      else {
        tokenElements[i] = new BasicType(dataType.getTypebyIndex(i), tokens[i]);
      }
    }

    Token token = new Token(dataType);
    token.add(tokenElements);
    mResolvedTokensMap.put(pExpression, token);

    return token;
  }


}
