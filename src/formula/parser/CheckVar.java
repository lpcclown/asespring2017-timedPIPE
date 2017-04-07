/**
 * Check whether the variables from transition guard consistent with variables from all 
 * the arcs connected.
 */
package formula.parser;

import java.util.ArrayList;

import java.util.Iterator;

import pipe.dataLayer.Transition;
import pipe.dataLayer.Arc;;


public class CheckVar {
	private Transition myTransition;
	private ArrayList<Arc> myArcList = new ArrayList<Arc>();
	private ArrayList<String> myArcVarList = new ArrayList<String>();
	private ArrayList<String> myTranVarList = new ArrayList<String>();
	
	public CheckVar(Transition transition){
		this.myTransition = transition;
		myArcList = myTransition.getArcList();
		myTranVarList = myTransition.getTranVarList();
	}
	
	public boolean check() {
		// TODO Auto-generated method stub
		Iterator<Arc> arcIterator = myArcList.iterator();
		while(arcIterator.hasNext()){
			Arc thisArc = arcIterator.next();
			String thisvar = thisArc.getVar();
			if(!myArcVarList.contains(thisvar))myArcVarList.add(thisvar);
		}
		
		Iterator<String> tranVarIterator = myTranVarList.iterator();
		while(tranVarIterator.hasNext()){
			if(!myArcVarList.contains(tranVarIterator.next()))return false;
		}
		return true;
	}
	
}
