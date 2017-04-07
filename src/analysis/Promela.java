package analysis;

import pipe.dataLayer.DataLayer;

import java.io.*;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */

public class Promela implements ModelAnalysisSupport {
  private static final String MODULE_NAME = "Translation from Petri Net to Promela";
  public static final String TRANSLATED_FILE_NAME = "proModel.pr";

  private PNTranslator mTranslator;

  @Override
  public String getModuleTitle() {
    return MODULE_NAME;
  }

  @Override
  public String performTranslation(final DataLayer pModel, final String pPropertyFormula) {
    StringWriter writer = new StringWriter();
    try {
      mTranslator.translate(pModel, pPropertyFormula, writer);
      FileWriter fileWriter = new FileWriter(TRANSLATED_FILE_NAME);
      fileWriter.write(writer.toString());
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return writer.toString();
  }

  @Override
  public String performVerification(final DataLayer pModel, final String pPropertyFormula) {
    StringBuilder s = new StringBuilder();

    try {
//      myPromela promela = new myPromela(pModel, pPropertyFormula);
//      s.append(promela.getPromela()).append("\n");

      //writer to a temp file
      File fmodel = new File(TRANSLATED_FILE_NAME);
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fmodel));
      mTranslator.translate(pModel, pPropertyFormula, bufferedWriter);
//      bufferedWriter.write(promela.getPromela());
      bufferedWriter.close();

      //call spin to verify the model
//				   System.out.println(System.getProperty("os.name"));
      String command = "sh ./scripts/VerificationInSpin.sh";
      if (System.getProperty("os.name").startsWith("Windows")) {
        command = ".\\scripts\\WinVerificationInSpin.bat";
      }
      Process p = Runtime.getRuntime().exec(String.format("%s %s", command, TRANSLATED_FILE_NAME));
      InputStream output = new BufferedInputStream(p.getInputStream());
      p.waitFor();
      System.out.println("Test 12/19/15");

      BufferedReader bufferedReader = new BufferedReader(new FileReader("SpinOutput.txt"));
      String t;
      while ((t = bufferedReader.readLine()) != null) {
        s.append(t).append("\n");
      }
    }
    catch (IOException | InterruptedException exception) {
      exception.printStackTrace();
      s.append("Error occured: " + exception.getMessage());
    }

    return s.toString();
  }

  public PNTranslator getTranslator() {
    return mTranslator;
  }

  public void setTranslator(final PNTranslator pTranslator) {
    mTranslator = pTranslator;
  }
}
