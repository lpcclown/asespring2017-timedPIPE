package analysis;

import formula.absyntree.In;
import org.apache.commons.io.IOUtils;
import pipe.dataLayer.DataLayer;

import java.io.*;

/**
 * Created by Maks on 7/11/2016.
 */
public abstract class AbstractAnalysisSupport implements ModelAnalysisSupport {

  @Override
  public String performTranslation(DataLayer pModel, String pPropertyFormula) {
    String translation = performTranslationImpl(pModel, pPropertyFormula);
    try (FileWriter fileWriter = new FileWriter(getModelFileName(pModel.pnmlName))) {
      fileWriter.write(translation);
      fileWriter.close();
    }
    catch (IOException ex) {
      System.out.println("Could not saved the translation to file.");
      ex.printStackTrace();
    }

    return translation;
  }

  @Override
  public String performVerification(DataLayer pModel, String pPropertyFormula) {
    try {
      //writer to a temp file
      File modelFile = new File(getModelFileName(pModel.pnmlName));
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(modelFile));
      bufferedWriter.write(performTranslationImpl(pModel, pPropertyFormula));
      bufferedWriter.close();

      String command = "";
      if (System.getProperty("os.name").startsWith("Windows")) {
        command = String.format(".\\scripts\\%s.bat", getScriptName());
      }
      else {
        command = String.format("./scripts/%s.sh", getScriptName());
      }

      command = String.format("%s %s", command, modelFile.getAbsolutePath());
      Process p = Runtime.getRuntime().exec(command);
      p.waitFor();
      System.out.println("Test 12/19/15");
      String str = null;
      BufferedReader is = new BufferedReader(new FileReader(getVerificationOutputFilename()));
      while ((str = is.readLine()) != null) {
        System.out.println(str);
      }

      return IOUtils.toString(new FileReader(getVerificationOutputFilename()));
    }
    catch (IOException | InterruptedException exception) {
      exception.printStackTrace();
    }

    return "Error.";
  }

  abstract public String performTranslationImpl(final DataLayer pModel, final String pPropertyFormula);
  abstract public String getModelFileName(final String pBaseName);
  abstract public String getVerificationOutputFilename();
  abstract public String getScriptName();
}
