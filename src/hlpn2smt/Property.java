package hlpn2smt;

import pipe.dataLayer.Token;

public class Property {

	String placeName;
	Token tok;
	RelationType type;
	Operator op;
	public enum RelationType {
		CONJUNCTION, DISJUNCTION
	}
	
	public enum Operator {
		EQ, NEQ
	}
	
	public Property(String _name, Token _tok, RelationType _type, Operator _op){
		placeName = _name;
		tok = _tok;
		this.type = _type;
		this.op = _op;
	}
	
	public String getPlaceName(){
		return this.placeName;
	}
	
	public Token getToken() {
		return tok;
	}
	
	public RelationType getRelationType(){
		return type;
	}
	
	public Operator getOperator(){
		return op;
	}
}
