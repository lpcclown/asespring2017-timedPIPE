package formula.absyntree;
import pipe.dataLayer.abToken;
import formula.parser.Visitor;
public class Union extends SetExp{
	public Term t1, t2;
	
	public boolean isPostCond = false;

	public Union(int p, Term t1, Term t2){
		this.pos = p;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public void accept(Visitor v){
		v.visit(this);
	}
}
