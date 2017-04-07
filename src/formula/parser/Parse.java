package formula.parser;

import formula.absyntree.Sentence;

import java.io.Reader;
import java.io.StringReader;

public class Parse {

	  public Sentence absyn;
	  private ErrorMsg errorMsg;

	  public Parse(String source, ErrorMsg err) {
	    errorMsg = err;
//	    java.io.InputStream inp;
//	    try {inp = new java.io.FileInputStream(filename);
//	    } catch (java.io.FileNotFoundException e) {
//	      throw new Error("File not found: " + filename);
//	    }
			Reader inp = new StringReader(source);
	    parser parser = new parser(new Yylex(inp, errorMsg), errorMsg);
	    try {
	      absyn = (Sentence) parser./*debug_*/parse().value;
	    } catch (Throwable e) {
	      e.printStackTrace();
	      throw new Error(e.toString());
	    } 
	    finally {
	      try {inp.close();} catch (java.io.IOException e) {}
	    }
	  }
	}
