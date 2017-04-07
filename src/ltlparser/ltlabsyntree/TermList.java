package ltlparser.ltlabsyntree;

import java.util.Vector;

public class TermList{
  private Vector list;

  public TermList() {
    list = new Vector();
  }

  public void addElement(Term n) {
    list.addElement(n);
  }

  public Term elementAt(int i)  { 
    return (Term)list.elementAt(i); 
  }

  public int size() { 
    return list.size(); 
  }

}

