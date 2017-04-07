package formula.parser;

import pipe.dataLayer.Token;
import pipe.dataLayer.abToken;

import java.util.HashMap;

public class SymbolTable {

	public HashMap<String, Symbol> table;

	public SymbolTable(){
		 table = new HashMap<>();
	}
	
	public SymbolTable(SymbolTable _table)
	{
		table = new HashMap<>(_table.table);
	}
	
	//only print when value is singleton type
	public void printSymTable()
	{
		System.out.println("----- symbol table size = "+table.size()+"-----");
		System.out.println("Key   Value");
		for(Symbol sym : table.values())
		{   
			//Modified by He - 8/5/15
 			if (sym.getType() != Symbol.TYPE.MULTI) {
 				System.out.println(sym.getKey()+"   "+ ((Token) sym.getBinder()).displayToken());
 			} else 
 			{
 				int nooftok = ((abToken) sym.getBinder()).getTokenCount();
 				for (int k = 0; k < nooftok; k++)
 				{
 					System.out.println(sym.getKey()+"   "+ ((Token) ((abToken) sym.getBinder()).getTokenbyIndex(k)).displayToken());
 				}
 			}
		}
	}
	
	public void addSymbolTable(SymbolTable _table)
	{
		table.putAll(_table.table);
	}
	
	public void removeSymbolTable(SymbolTable _table)
	{
		for(Symbol sym :_table.table.values())
		{
			this.table.remove(sym.key);
		}
	}

	public void insert(String key, Object b, Symbol.TYPE tp){
		Symbol symbol = new Symbol(key,b, tp);
		table.put(key, symbol);
	}
	
//	void insert(String key, abToken ab){
//		Symbol symbol = new Symbol(key,ab);
//		table.add(symbol);
//	}
	
	public Object lookup(String key){
		Symbol symbol = lookUpForSymbol(key);
		return symbol == null ? null : symbol.binder;
	}

	public Symbol lookUpForSymbol(final String pKey) {
		return table.get(pKey);
	}
	
	//added by He - 8/2/15
	public Symbol.TYPE getType(String key)
	{
		Symbol symbol = lookUpForSymbol(key);
		return symbol == null ? Symbol.TYPE.SINGLE : symbol.type;
	}
	
	public void update(String key, Object b, Symbol.TYPE tp){
		table.put(key, new Symbol(key,b, tp));
	}
	
	public void delete(String key){
		table.remove(key);
	}
	
	public boolean exist(String key){
		return table.containsKey(key);
	}
	
	public boolean containsToken(Object b)
	{
		return table.values().contains(b);
	}
	public void cleanTable(){
		table.clear();
	}
}
