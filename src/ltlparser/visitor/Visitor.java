package ltlparser.visitor;

import ltlparser.ltlabsyntree.*;

public interface Visitor {
  public void visit(LogicSentence elem);
  public void visit(Variable elem);
  public void visit(UnLogicOp elem);
  public void visit(UnArithOp elem);
  public void visit(TupleSel elem);
  public void visit(SetOp elem);
  public void visit(SetMembOp elem);
  public void visit(RelOp elem);
  public void visit(QuantLogicOp elem);
  public void visit(Identifier elem);
  public void visit(Function elem);
  public void visit(Constant elem);
  public void visit(BinLogicOp elem);
  public void visit(BinArithOp elem);
  public void visit(Predicate elem);
  public void visit(SetDef elem);
  public void visit(SentenceWPar elem); 
}
