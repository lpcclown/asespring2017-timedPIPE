package formula.absyntree;
import formula.parser.Visitor;
public abstract class Quantifier {
	public int pos;
	
	public abstract void accept(Visitor v);
}
