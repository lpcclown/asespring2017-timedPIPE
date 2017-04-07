package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public abstract class DefaultPTNObjectConverter<T extends PetriNetObject> implements PTNObjectConverter<T> {

	protected double toDouble(String pValue) {
		if (isBlank(pValue)) {
			return 0;
		}
		
		return Double.parseDouble(pValue);
	}

	protected int toInt(String pValue) {
		if (isBlank(pValue)) {
			return 0;
		}
		
		return Integer.parseInt(pValue);
	}

	protected boolean isBlank(String pValue) {
		return pValue == null || pValue.length() == 0;
	}
	
	

}
