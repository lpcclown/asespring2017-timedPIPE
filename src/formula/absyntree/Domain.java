package formula.absyntree;
import formula.parser.Visitor;
public abstract class Domain {
	public int pos;
	public int domain_type; // 0 is IN, 1 is NIN
	public abstract void accept(Visitor v);
}
