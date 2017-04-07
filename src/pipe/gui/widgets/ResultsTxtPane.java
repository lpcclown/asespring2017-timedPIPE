package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import pipe.dataLayer.PNMatrix;
import pipe.dataLayer.PetriNetObject;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.FileBrowser;

/**
 * This class is used by the analysis modules to display the results
 * of their analysis as Plain text.
 * Copied from ResultsHTMLPane.java
 * 
 * @author Zhuo Sun

 * @throws RuntimeException if the parameter to the constructor is null
 * and a temporary file cannot be created. 
 * 
 * Changes:
 * 1) setContentType is changed to text/plain
 * 2) Remove HyperlinkListener
 */

public class ResultsTxtPane
        extends JPanel {
   
   JEditorPane results;
   File defaultPath;
   boolean tempDefaultPath;
   Clipboard clipboard = this.getToolkit().getSystemClipboard();
   ButtonBar copyAndSaveButtons;
   
   
   public ResultsTxtPane(String path) {
      super(new BorderLayout());
      
      // Change Jan 14, 2007 by David Patterson
      // When you have drawn a net and not saved it, the path field is null 
      // at this point.
      if ( path == null ){
         tempDefaultPath = true;
         try { 
            defaultPath = File.createTempFile(  "PIPE", ".xml" ).getParentFile();
         } catch (IOException e) {
            throw new RuntimeException("Cannot create temp file. " +
                    "Save net before running analysis modules." );
         }
      }	else {
         tempDefaultPath = false;
         defaultPath = new File(path);
         if(defaultPath.isFile()){
            defaultPath = defaultPath.getParentFile();
         }
      }
      
      results = new JEditorPane();
      results.setEditable(false);
      results.setMargin(new Insets(5,5,5,5));
      results.setContentType("text/plain");
      JScrollPane scroller=new JScrollPane(results);
      scroller.setPreferredSize(new Dimension(400,300));
      scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
      this.add(scroller);

      copyAndSaveButtons = 
               new ButtonBar(new String[]{"Copy","Save"},
                             new ActionListener[]{CopyHandler,SaveHandler});     
      copyAndSaveButtons.setButtonsEnabled(false);
      this.add( copyAndSaveButtons, BorderLayout.PAGE_END);
      this.setBorder(new TitledBorder(new EtchedBorder(),"Results"));
   }
   
   
   public void setText(String text) {
      results.setText(text);
      results.setCaretPosition(0); // scroll to top
   }
   
   
   public String getText() {
      return results.getText();
   }
   
   
   //<pere>
   public void setEnabled(boolean b) {
      copyAndSaveButtons.setEnabled(b);
      // provisional
      ((ButtonBar) this.getComponent(1)).getComponent(0).setEnabled(b);
      ((ButtonBar) this.getComponent(1)).getComponent(1).setEnabled(b);      
   }
   //</pere>
   

   private ActionListener CopyHandler = new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         StringSelection data = new StringSelection(results.getText());
         try {
            clipboard.setContents(data,data);
         } catch (IllegalStateException e) {
            System.out.println("Error copying to clipboard, seems it's busy?");
         }
      }
   };
   
   
   private ActionListener SaveHandler=new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         try{
            FileBrowser fileBrowser = 
                    new FileBrowser("Promela file","pml", defaultPath.getPath());
            String destFN = fileBrowser.saveFile();
            if (!destFN.toLowerCase().endsWith(".pml")) {
               destFN += ".pml";
            }
            FileWriter writer=new FileWriter(new File(destFN));
            String output = results.getText();
            writer.write(output);
            writer.close();
         } catch (Exception e) {
            System.out.println("Error saving Promela to file");
         }
      }
   };
   
   

   
}
