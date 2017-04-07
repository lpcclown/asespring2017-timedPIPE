package formula.parser;

import pipe.dataLayer.Token;
import pipe.dataLayer.abToken;

public class Symbol {

	public enum TYPE {
		SINGLE, //Single valued
		MULTI //Multi valued i.e. set variable
	}
	String key;
	TYPE type;    //used to indicate a set variable
	Object binder;
//	Token binding = null;
//	abToken abBinding = null;
	
	public Symbol(String key, Object b, TYPE tp){
		this.key = key;
		this.binder = b;
		this.type =tp;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public Object getBinder(){
		return this.binder;
	}
	
	//added by He - 8/2/15
	public TYPE getType()
	{
		return type;
	}
//	public Symbol(String key, Token b){
//		type = 0;
//		this.key = key;
//		this.binding = b;
//	}
//	
//	public Symbol(String key, abToken ab){
//		type = 1;
//		this.key = key;
//		this.abBinding = ab;
//	}
}
