package ilosciowkaV2;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

public class addThings {
    private static JFrame frame;
    private JPanel mainPanel = new JPanel();
    private JButton addThingsButt, changeAddElement;
    private JTextField amountField = new JTextField();
    private JComboBox<String> whichTable;

    public static void setItVisible(boolean state){
        frame.setVisible(state);
    }

    //Konstruktor klasy
    addThings(){

        //Tworzenie okna
        frame = new JFrame("Dodawanie danych");
            frame.setPreferredSize(new Dimension(500, 700));
            frame.setVisible(false);
            frame.addWindowListener(frameAdapter);
            
        //Ustawianei layoutu dla glownego panelu
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); // obiekt przez ktory dodawane elementy do panelu beda mialy swoje miejsce

        String[] tableBoxData = {"Tusze", "Tonery", "Klawiatury", "Myszki"};
        JComboBox tableChoice = new JComboBox<>(tableBoxData);

        NumberFormatter numForm = new NumberFormatter(NumberFormat.getInstance());
            numForm.setMinimum(1);
            numForm.setMaximum(Integer.MAX_VALUE);
        JFormattedTextField amount = new JFormattedTextField(numForm);
        
    }

    private final WindowAdapter frameAdapter = new WindowAdapter(){
        @Override
        public void windowClosing(WindowEvent e){
            frame.setVisible(false);
        }

    };
}
