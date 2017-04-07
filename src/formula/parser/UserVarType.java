package formula.parser;

public class UserVarType{

	public String userVariable;
	public String powerSetVariable;
	public String placeName;
	
	UserVarType(String uv, String pv, String pn){
		this.userVariable = uv;
		this.powerSetVariable = pv;
		this.placeName = pn;
	}
	
	UserVarType(){
		
	}
	
	public String getUserVariable(){
		return this.userVariable;
	}
	
	public String getPlaceName(){
		return this.placeName;
	}
}
