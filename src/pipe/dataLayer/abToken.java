package pipe.dataLayer;

import java.util.*;

public class abToken {
  private DataType tokenType;
  public Vector<Token> listToken;
  private boolean isDef;

  public abToken() {
    isDef = false;
    tokenType = null;
    listToken = new Vector();
  }

  public abToken(DataType input) {
    definetype(input);
    listToken = new Vector();
  }

  public void definetype(DataType input) {
    tokenType = input;
    isDef = true;
  }

  public boolean addToken(Token t) {
//		if(!isDef)
//			return false;
    listToken.add(t);
    System.out.println(listToken);
    Collections.sort((List)listToken);
    System.out.println(listToken);
    return true;
  }

  public boolean addTokens(final Vector<Token> pTokensList) {
    listToken.addAll(pTokensList);
    Collections.sort(listToken);
    return true;
  }

  public boolean deleteToken(Token t) {
    Iterator<Token> it = listToken.iterator();
    while (it.hasNext()) {
      if (it.next().equals(t)) {
        it.remove();
      }
    }

    return true;
  }

  /**
   * Has bug because when remove one object, the index of the vector is changed
   * @param index
   */
//	public void deleteTokenbyindex(int index)
//	{
//		if(!isDef || index < 0 || index >listToken.size())
//			return;
//		listToken.get(index).delete();
//		listToken.remove(index);
//		NumofToken --;
//	}

  /**
   * Has bug because when remove one object, the index of the vector is changed
   */
//	public void deleteTokens()
//	{
//		int size = listToken.size();
//		for(int i = 0; i < size; i++)
//		{
//			deleteTokenbyindex(i);
//		}
//	}
  public Token getTokenbyIndex(int index) {
    return listToken.get(index);
  }

  public int getTokenCount() {
    return listToken.size();
  }

  public DataType getDataType() {
    return tokenType;
  }

  public boolean getDef() {
    return isDef;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) return true;
    if (obj == null || !(obj instanceof abToken)) return false;

    abToken toTest = (abToken) obj;
    if (listToken.size() != toTest.listToken.size()) return false;
    for (Token token : listToken) {
      if (!toTest.listToken.contains(token)) {
        return false;
      }
    }

    return true;
  }

  public Vector<Token> getListToken() {
    return listToken;
  }
}
