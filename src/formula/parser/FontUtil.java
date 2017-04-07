package formula.parser;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * <p>Title: FOL Editor</p>
 *
 * <p>Description: This is an FOL editor to be used for SAM</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: SCIS - School of Computing and Information Sciences - FIU</p>
 *
 * @author Gonzalo Argote-Garcia
 * @version 1.0
 */
public class FontUtil {
    private FontUtil() {
    }
    private static final String FONT_SYMBOL_NAME = "Logic.ttf";
    public static Font loadDefaultTLFont(){
        Font font = null;
//        try{
            // Create the edit view where to enter the text
//            InputStream stream = FontUtil.class.getResourceAsStream(FONT_SYMBOL_NAME);
//            Font font = Font.createFont(Font.PLAIN, stream);
        	font = new Font(null,font.PLAIN,20);
//            retFont= font.deriveFont(0,20f/*20f*/);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        return font;
    }

}
