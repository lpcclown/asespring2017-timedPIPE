package ltlparser.ltlabsyntree;

import java.util.Vector;

// We only allow SetMemOp and Variable to be part of this!!!
public class QuantList{
  private Vector list;

  public QuantList() {
    list = new Vector();
  }

  /*public void addElement(Exp n) {
    list.addElement(n);
  }*/
  public void addElement(Variable n) {
    list.addElement(n);
  }
  public void addElement(SetMembOp n) {
    list.addElement(n);
  }

  public Term elementAt(int i)  { 
    return (Term)list.elementAt(i); 
  }

  public int size() { 
    return list.size(); 
  }

}

