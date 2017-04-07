package formula.absyntree;
import formula.parser.Visitor;
public class Index {
	public int pos;
	public Num n;
	public int int_val;
	
	public Index(int p, Num n){
		this.pos = p;
		this.n = n;
		this.int_val = Integer.parseInt(n.n);
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
