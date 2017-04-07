package analysis;

import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.DataLayer;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */

public class Maude extends AbstractAnalysisSupport {
  private static final String MODULE_NAME = "Translation from Petri Net to Maude";
  private static final String TRANSLATED_FILE_NAME = "%s.maude";
  private static final String VERIFICTION_OUTPUT = "MaudeOutput.txt";
  private static final String DEFAULT_BASE = "hlpnModel";
  private static final String SCRIPT_NAME = String.format("maude%srun_maude", System.getProperty("file.separator"));

  @Override
  public String getModuleTitle() {
    return MODULE_NAME;
  }

  @Override
  public String performTranslationImpl(DataLayer pModel, String pPropertyFormula) {
    HLPN2Maude translator = new HLPN2Maude(pModel, "");
//    HLPN2Maude translator = new HLPN2Maude(pModel, pPropertyFormula);
    return translator.sMaude;
  }

  @Override
  public String getModelFileName(String pBaseName) {
    String baseName = pBaseName;
    if (StringUtils.isBlank(baseName)) {
      baseName = DEFAULT_BASE;
    }

    return String.format(TRANSLATED_FILE_NAME, baseName);
  }

  @Override
  public String getVerificationOutputFilename() {
    return VERIFICTION_OUTPUT;
  }

  @Override
  public String getScriptName() {
    return SCRIPT_NAME;
  }
}
