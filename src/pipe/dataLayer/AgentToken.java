package pipe.dataLayer;

public class AgentToken {

	String name;
	DataLayer agentLayer;
	
	public AgentToken(String _name, DataLayer _agentLayer){
		name = _name;
		agentLayer = _agentLayer;
	}
	
	public String getName(){
		return this.name;
	}
	
	public DataLayer getAgentDataLayer(){
		return this.agentLayer;
	}
}
