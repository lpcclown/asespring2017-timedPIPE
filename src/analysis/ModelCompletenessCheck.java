package analysis;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;
import pipe.dataLayer.Arc;

public class ModelCompletenessCheck {
	
	public DataLayer dataLayer;
	private Place[] places;
	private Transition[] transitions;
	private Arc[] arcs;
	private String message;
	private boolean result = true;
	
	public boolean getResult(){
		return result;
	}
	
	public String getMessage(){
		return message;
	}
	
	public ModelCompletenessCheck(DataLayer dl){
		dataLayer = dl;
		
		places = dl.getPlaces();
		transitions = dl.getTransitions();
		arcs = dl.getArcs();
		
		message = "Undefined ";
		
		message += "\nPlaces: ";
		if(!checkPlaceType())result = false;
		message += "\nTransitions: ";
		if(!checkTransitions())result = false;
		message += "\nArcs: ";
		if(!checkArcs())result = false;

	}
	
	boolean checkPlaceType(){
		boolean re = true;
		
		if(places != null){
		for(int i = 0; i < places.length; i++){
			if(places[i].getDataType() == null){
				re = false;
				this.message = this.message + " "+places[i].getName() + ";";
			}
		}
		}
		return re;
	}
	
	boolean checkTransitions(){
		boolean re = true;
		if(transitions != null){
			for(int i = 0; i < transitions.length; i++){
				if(transitions[i].getFormula() == null){
					re = false;
					this.message = this.message + " "+transitions[i].getName() + ";";
				}
			}
		}
		return re;
	}
	
	boolean checkArcs(){
		boolean re = true;
		if(arcs != null){
			for(int i = 0; i < arcs.length; i++){
				if(arcs[i].getVar().equals("")){
					re = false;
					this.message = this.message + " "+arcs[i].getName() + ";";
				}
			}
		}
		return re;
	}
}

