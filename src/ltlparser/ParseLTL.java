package ltlparser;

import ltlparser.errormsg.ErrorMsg;
import ltlparser.ltlabsyntree.LogicSentence;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class ParseLTL {

  public LogicSentence absyn;
  private ErrorMsg errorMsg;

  public ParseLTL(java.io.File filename, ErrorMsg err) {
    errorMsg = err;

    try (InputStream inputStream = new FileInputStream(filename)) {
      parser parser = new parser(new Yylex(inputStream, errorMsg), errorMsg);
      if (!errorMsg.anyErrors) {
        absyn = (LogicSentence) parser.parse().value;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ParseLTL(String parseStr, ErrorMsg err) {
    errorMsg = err;
    try (Reader reader = new StringReader(parseStr)) {
      parser parser = new parser(new Yylex(reader, errorMsg));
      if (!errorMsg.anyErrors) {
        absyn = (LogicSentence) parser.parse().value;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

