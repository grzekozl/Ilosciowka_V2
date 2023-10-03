package ilosciowkaV2;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

public class addThings {
    private static JFrame frame;
    private JPanel amountPanel, mainPanel = new JPanel(), radioPanel = new JPanel(), tuszeCase = new JPanel(), restCase = new JPanel();
    private JRadioButton newRadioButton, editRadioButton;
    private ButtonGroup radioButtonGroup = new ButtonGroup();
    private JComboBox<String> whichTable,  itemModels;

    public static void setItVisible(boolean state){
        frame.setVisible(state);
    }

    //Fabrykant paneli z etykieta i polem
    private JPanel createPanelLabelField(String labelText, int fieldSize, NumberFormatter nf){
        JPanel res = null;
        JLabel name = new JLabel(labelText);
            name.setName("label");
        JFormattedTextField field = new JFormattedTextField(nf);
            field.setColumns(fieldSize);
            field.setName("field");

            res = new JPanel();
            res.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
                res.add(name, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
                res.add(field, gbc);


        return res;
    }

    //Konstruktor klasy
    addThings(){

        //Tworzenie okna
        frame = new JFrame("Dodawanie danych");
            frame.setPreferredSize(new Dimension(500, 700));
            frame.setVisible(false);
            frame.addWindowListener(frameAdapter);
            frame.setSize(500, 500);
			frame.setLayout(new GridBagLayout());
            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);

        //Ustawianei layoutu dla glownego panelu
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints(); // obiekt przez ktory dodawane elementy do panelu beda mialy swoje miejsce

        //RadioButton do wyboru nowego lub edycji istniejacego przedmiotu
        newRadioButton = new JRadioButton("Nowy", false);
        editRadioButton = new JRadioButton("Edytuj", false);
            radioButtonGroup.add(newRadioButton);
            radioButtonGroup.add(editRadioButton);

		radioPanel.add(newRadioButton);
		radioPanel.add(editRadioButton);

        //ComboBox dla wyboru tablicy
        String[] tableBoxData = {"Tusze", "Tonery", "Klawiatury", "Myszki"};
        whichTable = new JComboBox<String>(tableBoxData);
            whichTable.addItemListener(tableChoiceListener);

        //Formater dla pola ilosci przedmiotu
        NumberFormatter numForm = new NumberFormatter(NumberFormat.getInstance());
            numForm.setMinimum(1);
            numForm.setMaximum(Integer.MAX_VALUE);

        //Pole ilosci przedmiotu
        amountPanel = createPanelLabelField("Ilość: ", 5, numForm);
            amountPanel.setVisible(false);

        //ComboBox z modelami/producentami produktow do edycji
        itemModels = new JComboBox<String>();

		//Pole kolorow tuszy i ilosci reszty
        JFormattedTextField C, M, Y, B, BXL, amount;
        	C = new JFormattedTextField(numForm);
				C.setColumns(5);
			M = new JFormattedTextField(numForm);
				M.setColumns(5);
			Y = new JFormattedTextField(numForm);
				Y.setColumns(5);
			B = new JFormattedTextField(numForm);
				B.setColumns(5);
			BXL = new JFormattedTextField(numForm);
				BXL.setColumns(5);
			amount = new JFormattedTextField(numForm);
				amount.setColumns(5);

		//Dodawanie do panelu z wyborem tuszy
		tuszeCase.add(C);
		tuszeCase.add(M);
		tuszeCase.add(Y);
		tuszeCase.add(B);
		tuszeCase.add(BXL);
		        
        //padding pomiedzy elementami dla GridBagLayout
        // gbc.insets = new Insets(4, 4, 4, 4);

		//Dodawanie elementow do okna

		gbc.ipadx = 4;
		gbc.ipady = 6;

		gbc.weightx = 4;
		gbc.weighty = 3;

		gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.gridy = 0;
            mainPanel.add(radioPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        	mainPanel.add(whichTable, gbc); //ComboBox z wyborem tablicy

		gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridwidth = 4;
		gbc.gridheight = 3;
        gbc.gridx = 1;
        gbc.gridy = 2;
            mainPanel.add(amountPanel, gbc); //Pole ilosci
			mainPanel.add(tuszeCase, gbc);
            
        //Odswiezanie okna po dodaniu elementow
        SwingUtilities.updateComponentTreeUI(frame);
		System.out.println(whichTable.getSize().height);
    }


    //Zdarzenie zamkniecia okna dodawania elementu 
    private final WindowAdapter frameAdapter = new WindowAdapter(){
        @Override
        public void windowClosing(WindowEvent e){
            frame.setVisible(false);
        }
    };

    private final ItemListener tableChoiceListener = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
            System.out.println(e.getItem());
            if(!e.getItem().toString().equals("Tusze")){
                System.out.println("O KURWA TO NIE TUSZE");
                amountPanel.setVisible(true);
            }else{
                System.out.println("łuhuu.... tusze.....");
                amountPanel.setVisible(false);
            }
        }
    };
}
