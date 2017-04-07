package formula.parser;

import formula.absyntree.*;
import org.apache.commons.lang.ObjectUtils;
import pipe.dataLayer.Transition;

import java.util.HashMap;
import java.util.Objects;

public class Formula2Maude implements Visitor{

  private ErrorMsg errorMsg;
  private SymbolTable symTable;
  private Transition iTransition;
  private int mode = 0;

  private HashMap<String, String> mElementsToMaude = new HashMap<>();

  public Formula2Maude(ErrorMsg errorMsg, Transition transition, int mode){
    this.errorMsg = errorMsg;
    iTransition = transition;
    this.symTable = iTransition.getTransSymbolTable();
    this.mode = mode;
  }

  @Override
  public void visit(AndFormula elem) {

    elem.f1.accept(this);
    elem.f2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.f1.identityToString(), elem.f2.identityToString(), "/\\"));
  }

  @Override
  public void visit(BraceTerm elem) {
    elem.t.accept(this);
  }

  @Override
  public void visit(BraceTerms elem) {
    elem.ts.accept(this);
  }

  @Override
  public void visit(ComplexFormula elem) {
    elem.q.accept(this);
    elem.uv.accept(this);
    elem.d.accept(this);
    elem.v.accept(this);
  }

  @Override
  public void visit(Diff elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);
  }

  @Override
  public void visit(Div elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);
  }

  @Override
  public void visit(EqRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    String conjunction = "=";
    if (elem.t1 instanceof VariableTerm && ((VariableTerm) elem.t1).postcond) {
      conjunction = ":=";
    }
    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), conjunction));
  }

  @Override
  public void visit(EquivFormula elem) {
    elem.f1.accept(this);
    elem.f2.accept(this);
  }

  @Override
  public void visit(Exists elem) {
    //Empty by design
  }

  @Override
  public void visit(False elem) {
    //Empty by design
  }

  @Override
  public void visit(ForAll elem) {
    //Empty by design
  }

  @Override
  public void visit(GeqRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), ">="));
  }

  @Override
  public void visit(GtRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), ">"));
  }

  @Override
  public void visit(Identifier elem) {

  }

  @Override
  public void visit(IdVariable elem) {
    mElementsToMaude.put(elem.identityToString(), String.format("%s_f1", elem.key));
  }

  @Override
  public void visit(ImpFormula elem) {
    elem.f1.accept(this);
    elem.f2.accept(this);
  }

  @Override
  public void visit(In elem) {
    //Empty by design
  }

  @Override
  public void visit(Index elem) {
    elem.n.accept(this);
  }

  @Override
  public void visit(IndexVariable elem) {
    elem.i.accept(this);
    elem.idx.accept(this);

    mElementsToMaude.put(elem.identityToString(), String.format("%s_f%d", elem.i.key, elem.idx.int_val));
  }

  @Override
  public void visit(InRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);
  }

  @Override
  public void visit(LeqRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "<="));
  }

  @Override
  public void visit(LtRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "<"));
  }

  @Override
  public void visit(Minus elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "-"));
  }

  @Override
  public void visit(Mod elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "rem"));
  }

  @Override
  public void visit(Mul elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

  }

  public void visit(NegExp elem) {
    elem.t.accept(this);
  }

  @Override
  public void visit(NeqRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "=/="));
  }

  private String getBinaryExpression(final String pLeftId, final String pRightId, final String pConjunction) {
    String left = mElementsToMaude.get(pLeftId);
    String right = mElementsToMaude.get(pRightId);

    Objects.requireNonNull(left, "LHS operand is not realized for " + pLeftId);
    Objects.requireNonNull(right, "RHS operand is not realized for " + pRightId);

    return String.format("%s %s %s", left, pConjunction, right);
  }

  @Override
  public void visit(Nexists elem) {

  }

  @Override
  public void visit(Nin elem) {

  }

  @Override
  public void visit(NinRel elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);
  }



  @Override
  public void visit(NumConstant elem) {
//    elem.num.accept(this);
    mElementsToMaude.put(elem.identityToString(), String.format("%d", elem.value.intValue()));
  }

  @Override
  public void visit(Num elem) {

  }

  @Override
  public void visit(OrFormula elem) {
    elem.f1.accept(this);
    elem.f2.accept(this);

  }

  @Override
  public void visit(Plus elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

    mElementsToMaude.put(elem.identityToString(), getBinaryExpression(elem.t1.identityToString(), elem.t2.identityToString(), "+"));
  }

  @Override
  public void visit(TermRest elem) {
    elem.t.accept(this);

    String term = mElementsToMaude.get(elem.t.identityToString());

    Objects.requireNonNull(term, "Element could not be translated. " + elem.t);

    mElementsToMaude.put(ObjectUtils.identityToString(elem), term);
  }

  @Override
  public void visit(Terms elem) {
    elem.t.accept(this);

    String term = mElementsToMaude.get(elem.t.identityToString());

    Objects.requireNonNull(term, "Element could not be translated. " + elem.t);

    mElementsToMaude.put(ObjectUtils.identityToString(elem), term);
  }

  @Override
  public void visit(True elem) {

  }

  @Override
  public void visit(Union elem) {
    elem.t1.accept(this);
    elem.t2.accept(this);

  }

  @Override
  public void visit(UserVariable elem) {

  }

  @Override
  public void visit(ConstantTerm elem) {
    elem.c.accept(this);

    String maude = mElementsToMaude.get(elem.c.identityToString());
    Objects.requireNonNull(maude, "Translation failed: " + elem.c);
    mElementsToMaude.put(elem.identityToString(), maude);
    elem.var_key = elem.c.value.toString();
  }

  @Override
  public void visit(ExpTerm elem) {
    elem.e.accept(this);

    mElementsToMaude.put(elem.identityToString(), mElementsToMaude.get(elem.e.identityToString()));
  }

  @Override
  public void visit(VariableTerm elem) {
    elem.v.accept(this);

    mElementsToMaude.put(elem.identityToString(), mElementsToMaude.get(elem.v.identityToString()));

    boolean isInArcOutVarList = false;
    String var_key = "";

    if (elem.v instanceof IdVariable) {
      var_key = ((IdVariable) elem.v).key;

      for (String s : iTransition.getArcOutVarList()) {
        if (s.equals(var_key) || (s.replace('{', ' ').replace('}', ' ').trim()+",").contains(var_key+","))
          isInArcOutVarList = true;
      }
    }

    if (elem.v instanceof IndexVariable) {
      var_key = ((IndexVariable) elem.v).key;
      for (String s : iTransition.getArcOutVarList()) {
        if ((s.replace('{', ' ').replace('}', ' ').trim()+",").contains(var_key+","))
          isInArcOutVarList = true;
      }
    }

    elem.postcond = isInArcOutVarList;
  }

  @Override
  public void visit(StrConstant elem) {
    mElementsToMaude.put(elem.identityToString(), String.format("\"%s\"", elem.value));
  }

  public void visit(AExp elem){
    elem.ae.accept(this);

    String maude = mElementsToMaude.get(elem.ae.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.ae);
    mElementsToMaude.put(elem.identityToString(), String.format("(%s)", maude));
  }

  public void visit(RExp elem){
    elem.re.accept(this);

    String maude = mElementsToMaude.get(elem.re.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.re);
    mElementsToMaude.put(elem.identityToString(), maude);
  }

  public void visit(SExp elem){
    elem.se.accept(this);

    String maude = mElementsToMaude.get(elem.se.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.se);
    mElementsToMaude.put(elem.identityToString(), maude);
  }

  @Override
  public void visit(AtomicTerm elem) {
    elem.t.accept(this);

    String maude = mElementsToMaude.get(elem.t.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.t);
    mElementsToMaude.put(elem.identityToString(), maude);
  }

  @Override
  public void visit(NotFormula elem) {
    elem.f.accept(this);
    System.out.println("ERROR: Formula2Promela: NotFormula not implemented.");

  }

  @Override
  public void visit(AtFormula elem) {
    elem.af.treeLevel = elem.treeLevel;
    elem.af.accept(this);

    String maude = mElementsToMaude.get(elem.af.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.af);
    mElementsToMaude.put(elem.identityToString(), maude);
  }



  @Override
  public void visit(CpFormula elem) {
    elem.cf.treeLevel = elem.treeLevel;
    elem.cf.accept(this);

    String maude = mElementsToMaude.get(elem.cf.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.cf);
    mElementsToMaude.put(elem.identityToString(), maude);
  }

  @Override
  public void visit(CpxFormula elem) {
    elem.cpf.treeLevel = elem.treeLevel;
    elem.cpf.accept(this);

  }

  @Override
  public void visit(Sentence elem) {
    elem.f.accept(this);

    String maude = mElementsToMaude.get(elem.f.identityToString());
    Objects.requireNonNull(maude, "Failed to translate " + elem.f);
    mElementsToMaude.put(ObjectUtils.identityToString(elem), maude);
  }

  @Override
  public void visit(Empty elem) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(EmptyTerm elem) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Setdef elem) {
    //TODO: to be implemented
  }

  @Override
  public void visit(FunctionExp pTerm) {
    throw new RuntimeException("Not Implemented yet");
  }


  public String getTranslation(final String pElementId) {
    return mElementsToMaude.get(pElementId);
  }
}
