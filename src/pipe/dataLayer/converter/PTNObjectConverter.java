package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;

public interface PTNObjectConverter<T extends PetriNetObject> {
	/**
	 * This method will convert the P/T Net Object to DOM element so that it can be serialized
	 * @param pPTNObject
	 * @return
	 */
	Element toElement(final T pPTNObject, final Document pDocument);
	
	/**
	 * This method will convert the DOM element to underlying P/T Net object. 
	 * @param pElement
	 * @return
	 */
	T toPTNObject(final Element pElement, final DataLayer pDataLayer);
}
