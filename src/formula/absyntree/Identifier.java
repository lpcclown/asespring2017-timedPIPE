package formula.absyntree;
import formula.parser.Visitor;

public class Identifier {
	  public String key;
	  
	  public Identifier(String as) {
	    key=as;
	  }
		public void accept(Visitor v){
			v.visit(this);
		}
}
