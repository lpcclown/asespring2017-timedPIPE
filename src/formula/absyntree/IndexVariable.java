package formula.absyntree;
import formula.parser.Visitor;
public class IndexVariable extends Variable{

	public Identifier i;
	public Index idx;
	public int index = 0;

	public IndexVariable(int p, Identifier i, Index idx){
		this.pos = p;
		this.i = i;
		this.idx = idx;
		this.key = i.key;
		this.index = idx.int_val;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
