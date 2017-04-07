package formula.absyntree;

import formula.parser.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dalam004 on 8/16/2016.
 */
public class FunctionExp extends SingleValuedExp {
  final private String mName;
  final private List<Term> mArgumentTerms;
  public String outputVariable;

  public FunctionExp(final String pName) {
    mName = pName;
    mArgumentTerms = Collections.emptyList();
  }

  public FunctionExp(final String pName, final Terms pArgumentTerms) {
    mName = pName;
    mArgumentTerms = new ArrayList<>(pArgumentTerms.tr.size() + 1);
    mArgumentTerms.add(pArgumentTerms.t);
    mArgumentTerms.addAll(pArgumentTerms.tr.list.stream().map(tr -> tr.t).collect(Collectors.toList()));
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public String getName() {
    return mName;
  }

  public List<Term> getArgumentTerms() {
    return mArgumentTerms;
  }

  public void addArgumentTerms(final List<Term> pArgumentTerms) {
    this.mArgumentTerms.addAll(pArgumentTerms);
  }

  public void addArgumentTerm(final Term pArgumentTerm) {
    this.mArgumentTerms.add(pArgumentTerm);
  }
}
