package pipe.dataLayer.converter;

import pipe.dataLayer.Parameter;
import pipe.dataLayer.PetriNetObject;

import java.util.HashMap;
import java.util.Map;

public class PTNConverterManager {


  private static PTNConverterManager sInstance = new PTNConverterManager();

  private final Map<String, PTNObjectConverter> sConverters = new HashMap<>();

  private PTNConverterManager() {
    sConverters.put(PTNElement.PLACE.getValue(), new PlaceConverter());
    sConverters.put(PTNElement.TRANSITION.getValue(), new TransitionConverter());
    sConverters.put(PTNElement.ARC.getValue(), new ArcConverter());
    sConverters.put(PTNElement.DEFINITION.getValue(), new ParameterConverter());
    sConverters.put(PTNElement.LABELS.getValue(), new AnnotationNoteConverter());
  }

  public static PTNConverterManager getInstance() {
    return sInstance;
  }

  public PTNObjectConverter getConverterFor(final String pName) {
    if (sConverters.containsKey(pName)) {
      return sConverters.get(pName);
    }
    return null;
  }

  public AnnotationNoteConverter getAnnotationConverter() {
    return (AnnotationNoteConverter) getConverterFor(PTNElement.LABELS.getValue());
  }

  public ParameterConverter getParameterConverter() {
    return (ParameterConverter) getConverterFor(PTNElement.DEFINITION.getValue());
  }

  public PlaceConverter getPlaceConverter() {
    return (PlaceConverter) getConverterFor(PTNElement.PLACE.getValue());
  }

  public TransitionConverter getTransitionConverter() {
    return (TransitionConverter) getConverterFor(PTNElement.TRANSITION.getValue());
  }

  public ArcConverter getArcConverter() {
    return (ArcConverter) getConverterFor(PTNElement.ARC.getValue());
  }
}
