package pipe.gui.widgets;

import formula.parser.FontUtil;
import pipe.dataLayer.Place;
import pipe.gui.CreateGui;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LTLFormulaPanel extends JPanel {
  public JTextField m_textField;
  public Font m_font;
  //private Transition myTransition;
  private String mInitialFormula;

  public LTLFormulaPanel(String pInitialFormula) {
    super();
    mInitialFormula = pInitialFormula;
    initialize();
  }

  public LTLFormulaPanel(boolean isDoubleBuffered) {
    super(isDoubleBuffered);
    initialize();
  }

  public LTLFormulaPanel(LayoutManager layout) {
    super(layout);
    initialize();
  }

  public LTLFormulaPanel(LayoutManager layout, boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
    initialize();
  }


  protected void initialize() {
    // TODO Auto-generated method stub
    this.setLayout(null);
    this.setSize(665, 280);
    this.setPreferredSize(this.getSize());

    JScrollPane scrollFieldTextArea = new JScrollPane();
    scrollFieldTextArea.setSize(655, 46);
    m_textField = new JTextField(mInitialFormula);
    m_font = FontUtil.loadDefaultTLFont();
    m_textField.setFont(m_font);
    m_textField.setSize(655, 25);
    m_textField.setBorder(null);
    add(scrollFieldTextArea);
    scrollFieldTextArea.getViewport().add(m_textField);
    scrollFieldTextArea.getHorizontalScrollBar().setVisibleAmount(4);
    scrollFieldTextArea.setLocation(5, 5);

    //Now the labels and the buttons
    int curY = 53;
    int curX = 5;

    // Connective Symbols
    JLabel connectiveSymbols = new JLabel("Connective Symbols:");
    connectiveSymbols.setSize(120, 13);
    add(connectiveSymbols);
    connectiveSymbols.setLocation(curX, curY + 5);
    curX += connectiveSymbols.getWidth() + 5;
    JButton btConAnd = createButton('\u2227', curX, curY);
    curX += btConAnd.getWidth();
    JButton btConOr = createButton('\u2228', curX, curY);
    curX += btConOr.getWidth();
    JButton btConNot = createButton('\u00AC', curX, curY);
    curX += btConNot.getWidth();
    JButton btConImplies = createButton('\u2192', curX, curY);
    curX += btConImplies.getWidth();
    JButton btConDoubleImplies = createButton('\u2194', curX, curY);
    // Relational Symbols
    curX = 5;
    curY = curY + btConDoubleImplies.getHeight() + 2;
    JLabel relationSymbols = new JLabel("Relational Symbols:");
    relationSymbols.setSize(112, 13);
    add(relationSymbols);
    relationSymbols.setLocation(curX, curY + 5);
    curX += relationSymbols.getWidth() + 5;
    JButton btRelEqual = createButton('=', curX, curY);
    curX += btRelEqual.getWidth();
    JButton btRelNotEqual = createButton('\u2260', curX, curY);
    curX += btRelNotEqual.getWidth();
    JButton btRelGreater = createButton('>', curX, curY);
    curX += btRelGreater.getWidth();
    JButton btRelLess = createButton('<', curX, curY);
    curX += btRelLess.getWidth();
    JButton btRelGreaterOrEqual = createButton('\u2265', curX, curY);
    curX += btRelGreaterOrEqual.getWidth();
    JButton btRelLessOrEqual = createButton('\u2264', curX, curY);
    curX += btRelLessOrEqual.getWidth();
    // Arithmetic Symbols
    curX = 5;
    curY = curY + btRelLessOrEqual.getHeight() + 2;
    JLabel arithmeticSymbols = new JLabel("Arithmetic Symbols:");
    arithmeticSymbols.setSize(120, 13);
    add(arithmeticSymbols);
    arithmeticSymbols.setLocation(curX, curY + 5);
    curX += arithmeticSymbols.getWidth() + 5;
    JButton btArithPlus = createButton('+', curX, curY);
    curX += btArithPlus.getWidth();
    JButton btArithMinus = createButton('-', curX, curY);
    curX += btArithMinus.getWidth();
    JButton btArithMul = createButton('*', curX, curY);
    curX += btArithMul.getWidth();
    JButton btArithMod = createButton('%', curX, curY);
    curX += btArithMod.getWidth();
    JButton btArithDiv = createButton('/', curX, curY);
    curX += btArithDiv.getWidth();
    // Predicate Logic Symbols
    curX = 5;
    curY = curY + btArithPlus.getHeight() + 2;
    JLabel firstOrderSymbols = new JLabel("Predicate Logic Symbols:");
    firstOrderSymbols.setSize(120, 13);
    add(firstOrderSymbols);
    firstOrderSymbols.setLocation(curX, curY + 5);
    curX += firstOrderSymbols.getWidth() + 5;
    JButton btFOForAll = createButton('\u2200', curX, curY);
    curX += btFOForAll.getWidth();
    JButton btFOExists = createButton('\u2203', curX, curY);
    curX += btFOExists.getWidth();

    JButton btFONotExists = createButton('\u2204', curX, curY);
    curX += btFONotExists.getWidth();
    JButton btFOScope = createButton('\u22C5', curX, curY);
    // Set Symbols
    curX = 5;
    curY = curY + btFOForAll.getHeight() + 2;
    JLabel setSymbols = new JLabel("Other Symbols:");
    setSymbols.setSize(86, 13);
    add(setSymbols);
    setSymbols.setLocation(curX, curY + 5);
    curX += setSymbols.getWidth() + 5;
    JButton btSetBelongs = createButton('\u2208', curX, curY);
    curX += btSetBelongs.getWidth();
    JButton btSetNotBelongs = createButton('\u2209', curX, curY);
//        curX += btSetNotBelongs.getWidth();
//        JButton btSetEmpty = createButton('\uFA52',curX,curY);
//        curX += btSetEmpty.getWidth();
//        JButton btSetSubset = createButton('\uFA53',curX,curY);
//        curX += btSetSubset.getWidth();
//        JButton btSetNotSubset = createButton('\uFA54',curX,curY);
//        curX += btSetNotSubset.getWidth();
//        JButton btSetSubsetOrSet = createButton('\uFA55',curX,curY);
//        curX += btSetSubsetOrSet.getWidth();
    curX += btSetNotBelongs.getWidth();
    JButton btSetUnion = createButton('\u222A', curX, curY);
    curX += btSetUnion.getWidth();
    JButton btSetDifference = createButton('\u2216', curX, curY);
    curX += btSetDifference.getWidth();
    JButton btSetParenL = createButton('(', curX, curY);
    curX += btSetParenL.getWidth();
    JButton btSetParenR = createButton(')', curX, curY);
    curX += btSetParenL.getWidth();
//        JButton btSetEmpty = createButton('\u2205',curX,curY);

    curX = 5;
    curY = curY + btSetBelongs.getHeight() + 2;
    JLabel Symbols = new JLabel("Additional Symbols:");
    Symbols.setSize(86, 13);
    add(Symbols);
    Symbols.setLocation(curX, curY + 5);
    curX += Symbols.getWidth() + 5;
    JButton SymbolsSetSquare = createButton('\u25A1', curX, curY);
    curX += SymbolsSetSquare.getWidth();
    JButton SymbolsSetLozenge = createButton('\u25CA', curX, curY);
    curX += SymbolsSetLozenge.getWidth();
    JButton SymbolsSetCircle = createButton('\u25CB', curX, curY);
    curX += SymbolsSetCircle.getWidth();
    JButton SymbolsSetUntil = createButton('\u222A', curX, curY);
    curX += SymbolsSetUntil.getWidth();


    Place[] places = CreateGui.getModel().getPlaces();
    String[] placeNames = new String[places.length];
    for (int i = 0; i< places.length; i++) {
      placeNames[i] = places[i].getName();
    }

    curY += SymbolsSetSquare.getHeight()+5;
    JLabel placesLabel = new JLabel("Select Place to Insert: ");
    placesLabel.setSize(new Dimension(150, 25));
    placesLabel.setLocation(5, curY);
    add(placesLabel);
    final JComboBox<String> placesBox = new JComboBox<>(placeNames);
    placesBox.setSize(this.getWidth() - placesLabel.getWidth() - 5, placesLabel.getHeight());
    placesBox.setLocation(placesLabel.getWidth() + 5, curY);
    placesBox.setFocusable(false);
    placesBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
       buttonPressed(placesBox.getSelectedItem().toString());
      }
    });
    add(placesBox);

  }

  private JButton createButton(char code, int x, int y) {
    final JButton but = new JButton("" + code);
    but.setFont(m_font);
    but.setSize(56, 30);
    but.setPreferredSize(but.getSize());
    but.setFocusable(false);
    but.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        buttonPressed(but.getText());
      }
    });
    add(but);
    but.setLocation(x, y);
    return but;
  }

  private void buttonPressed(String code) {
    StringBuffer curText = new StringBuffer(m_textField.getText());
    int caretPos = m_textField.getCaretPosition();
    if (m_textField.getSelectedText() != null) {
      int start = m_textField.getSelectionStart();
      int end = m_textField.getSelectionEnd();
      curText.replace(start, end, code);
      caretPos = start;
    }
    else {
      curText.insert(caretPos, code);
    }

    m_textField.setText(curText.toString());
    m_textField.setCaretPosition(caretPos + code.length());
  }
}
