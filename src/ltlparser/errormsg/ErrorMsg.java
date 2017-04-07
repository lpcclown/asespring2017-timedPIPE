package ltlparser.errormsg;

import java.util.Vector;

public class ErrorMsg {
  public class Msg{
    public int pos;
    public String msg;
  };
  public boolean anyErrors;
  private String str;
  private Vector msgs;

  public ErrorMsg(String str) {
    this.str = str;
    msgs = new Vector(5);
  }

  public void error(int pos, String msg) {
    anyErrors=true;
    Msg m = new Msg();
    m.pos = pos;
    m.msg = msg;
    msgs.add(m);
  }
  public Vector getMsgs(){
    return msgs;
  }
}

