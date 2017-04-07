package analysis;

import pipe.dataLayer.DataLayer;

import java.io.IOException;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */
public interface ModelAnalysisSupport {
  String getModuleTitle();

  String performTranslation(final DataLayer pModel, final String pPropertyFormula);

  String performVerification(final DataLayer pModel, final String pPropertyFormula);
}
