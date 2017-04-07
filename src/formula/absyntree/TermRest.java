package formula.absyntree;

import formula.parser.Visitor;

public class TermRest {

    public int pos;
    public Term t;
    public String str = "";

    //z3
    public String z3str = "";
    public String placeName = "";
    public boolean isPostCond;

    public TermRest(int p, Term t) {
        this.pos = p;
        this.t = t;
    }

    public void accept(Visitor v) {
        v.visit(this);
    }
}
