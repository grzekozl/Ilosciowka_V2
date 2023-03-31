package ilosciowkaV2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.NumberFormat;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;


public class issueThings {
	private static JFrame issueFrame;
	private Map<String, String[]> tablesHash;
	private JComboBox<String> modelChoice, colorChoice, tablesChoice;
	private JFormattedTextField amountField, roomField;
	private JLabel queryStatus;
	private JPanel mainPanel;
	
	//Metoda ustawiajaca czy okno maF byc widoczne czy nie
	static public void setItVisible(boolean state) {
		issueFrame.setVisible(state);
		issueFrame.setLocationRelativeTo(null);
	}
	
	// Metoda aktualizujaca JComboBox z modelami rzeczy i ewentualnie kolorami jezeli tusze zostana wybrane
	private void setModelChoice(String table) {
		
		DefaultComboBoxModel<String> comboBoxNew = (DefaultComboBoxModel<String>) modelChoice.getModel();
		comboBoxNew.removeAllElements();
		
		for(String val : tablesHash.get(table))
			comboBoxNew.addElement(val);

		if(table.equals("Tusz")){
			colorChoice.setEnabled(true);
			issueFrame.revalidate();
		}
		else
			colorChoice.setEnabled(false);
		
	}
	
	//Metoda z magią do przekształcania listy z hashmapami na tablice String
	private String[] ListToArray(List<LinkedHashMap<String, String>> list) {
		List<String> processList = new ArrayList<String>();
		String[] res = null;
		for(LinkedHashMap<String,String> lhm : list) {
			lhm.forEach((key, value) ->{
				processList.add(value);
			});
		}
		
		res = processList.toArray(new String[0]);
		
		return res;
	}
	
	//Metoda grupująca dane w tablicy
	private String[] groupArray(String[] input) {
		List<String> res = new ArrayList<String>();
		
		for(String data : input) 
			if(!res.contains(data))
				res.add(data);
		
		return res.toArray(new String[0]);
	}

	//Metoda wstawiajaca do bazy nowe wydanie oraz odejmuje jeden od ilosci danego przedmiotu
	private void issueMethod(){
		//Ustawianie eykiety na wiadomosc ze uzytkownik ma czekac
		queryStatus.setText("<html><font color = 'yellow'>Przetwarzanie...</font></html>");
		//Mapa z poprawnymi nazwami olumn w bazie
		LinkedHashMap<String,String> dbNames = new LinkedHashMap<String,String>();
			dbNames.put("Tusz", "tusze");
			dbNames.put("Toner", "tonery");
			dbNames.put("Klawiatura", "klawiatury");
			dbNames.put("Myszka","myszki");
			
		//Pobieranie wartosci z pol
		String table = dbNames.get(tablesChoice.getSelectedItem().toString());
		String model = modelChoice.getSelectedItem().toString();
		String amount = String.valueOf(amountField.getValue());
		String color = (String) colorChoice.getSelectedItem();

		String room = roomField.isEnabled() ? "pokój " + String.valueOf(roomField.getValue()) : "bolków"; 

		System.out.println(model + " ||| " + table);

		String modelId = null;
		if(colorChoice.isEnabled())
			modelId = String.valueOf(DBConn.getIdOf(model, table, color));
		else
			modelId = String.valueOf(DBConn.getIdOf(model, table));
		
		String issueTable = "wydane_" + table;
			
		
		LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
			data.put("\"model\"", modelId);
			data.put("\"amount\"", String.valueOf(amount));
			data.put("\"where\"", "'" + room + "'");
		
			System.out.println(model);
			//Wątek który uruchamian zapytania
		Thread th = new Thread(){
			public void run(){
				try{
					if(DBConn.sendUpdate(table, amount, model))

					if(DBConn.sendInsert(issueTable, data))
						queryStatus.setText("<html><font color='green'>Wprowadzono dane</font></html>");
					else
						queryStatus.setText("<html><font color='red'>Uno problemo szefuńciu</font></html>");

					Thread.sleep(1000);
					queryStatus.setText(" ");
				}catch(InterruptedException exc){exc.printStackTrace();}
			}
		};
		th.start();		
	}
	
	
	
		
	
	
	
	
	//Konstruktor klasy
	issueThings(){
		//Przygotowywanie danych dla tworzenia elementów okna
			//Tablice do tworzenia ComboBoxow - maja zawierac modele/producentow 
			String[] tablesName = {"Tusz", "Toner", "Klawiatura", "Myszka"};
			
			String[] tusze = groupArray(ListToArray(DBConn.getSpecified("model", "tusze"))); 
			String[] tonery = ListToArray(DBConn.getSpecified("model", "tonery"));
			String[] klawiatury = ListToArray(DBConn.getSpecified("manufacturer", "klawiatury"));
			String[] myszki = ListToArray(DBConn.getSpecified("manufacturer", "myszki"));
			
		
			// Tworzenie okna
			issueFrame = new JFrame("Wydaj przedmiot");
			issueFrame.setResizable(true);
			issueFrame.setLocationRelativeTo(null);
			issueFrame.setVisible(false);
			issueFrame.getContentPane().setBackground(Color.lightGray);
			issueFrame.setSize(new Dimension(500,300));
			issueFrame.setResizable(false);


			//Sluchacz okna - co robic gdy okno zostanie zamkniete przez uzytkownika
			issueFrame.addWindowListener(issueThingsWindowListener);

			//Dodawanie tabel do HashMapy aby automatycznie ustawiac ComboBoxy 
			tablesHash = new HashMap<String,String[]>(); 
				tablesHash.put("Tusz", tusze);
				tablesHash.put("Toner", tonery);
				tablesHash.put("Klawiatura", klawiatury);
				tablesHash.put("Myszka", myszki);
		
			//Tablica z kolorami do jego wyboru
			// String[] colors = {"Czarny duży","Czarny mały","Żółty","Cyjan","Magenta"};
			
		//ComboBox z wyborem przedmiotu - toner/tusz...
		tablesChoice = new JComboBox<String>(tablesName);
			tablesChoice.setPreferredSize(new Dimension(100,25));
			tablesChoice.addItemListener(tabelsChoiceListener);	//Sluchacz wyboru przedmiotu aby aktualizowac modele w kolejnym ComboBox - modelem 
			// tablesChoice.setSelectedIndex(1);
		
		
		//ComboBox dla wyboru modeli lub prodecenta
		modelChoice = new JComboBox<String>(tusze);
			modelChoice.selectWithKeyChar((char) 'T');
			modelChoice.setPreferredSize(new Dimension(100,25));
			modelChoice.addItemListener(modelChoiceListener);
				
		//ComboBox dla wyboru koloru przy tuszach
		colorChoice = new JComboBox<String>();
			modelChoiceListener.itemStateChanged(null);
			colorChoice.setPreferredSize(new Dimension(100,25));
			
		//Wspólny panel dla wyboru modeli/prodeucenta oraz koloru
		JPanel modelColorPanel = new JPanel(new GridLayout(2,1));
			modelColorPanel.add(modelChoice);
			modelColorPanel.add(colorChoice);
			modelColorPanel.setOpaque(false);
		//Panel do wprowadzania ilosci
		JPanel amountPanel = new JPanel(new GridBagLayout());
	
		//Etykieta dla pola ilosc
			//Pole do wprowadzenia ilosci
				NumberFormatter numForm = new NumberFormatter(NumberFormat.getInstance());
				numForm.setMinimum(1);
				numForm.setMaximum(Integer.MAX_VALUE);
			amountField = new JFormattedTextField(numForm);
				amountField.setValue(1);
				amountField.setPreferredSize(new Dimension(45,25));
			//Etykieta
			JLabel amountLabel = new JLabel("Ilość: ");
				amountLabel.setPreferredSize(new Dimension(45,25));
				
			amountPanel.add(amountLabel);
			amountPanel.add(amountField);
			amountPanel.setOpaque(false);
			
		//Panel do wyboru pokoju
			//Panel
			JPanel roomPanel = new JPanel();
				roomPanel.setLayout(new GridBagLayout());

			//Etykieta
			JLabel roomLabel = new JLabel("Pokój: ");
				roomLabel.setPreferredSize(new Dimension(45,25));

			//Pole
			roomField = new JFormattedTextField(numForm);
				roomField.setValue(1);
				roomField.setPreferredSize(new Dimension(45,25));

			//Dodawanie etykiety i pola do panelu
			roomPanel.add(roomLabel);
			roomPanel.add(roomField);
			roomPanel.setOpaque(false);
			//CheckBox dla bolkowa
			JCheckBox bolkowRoomCBox = new JCheckBox("Bolków");
				bolkowRoomCBox.addItemListener(bolkowRoomCBoxListener);
				bolkowRoomCBox.setOpaque(false);
		//Przycisk do wydania
		JButton issueButton = new JButton("Wydaj");
			issueButton.addActionListener(issueButtonListener);
			
		//Etykieta z informacja o wsadzeniu do tablicy
		queryStatus = new JLabel(" ", SwingConstants.CENTER);

		//Dodawanie elementow do panelu glownego, a panel do okna		
		//Glowny panel dla okna
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setOpaque(false);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4,3,4,3);
			
			gbc.fill = GridBagConstraints.NORTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
				mainPanel.add(tablesChoice, gbc);
				
			gbc.gridx = 1;
			gbc.gridy = 0;
				mainPanel.add(modelChoice, gbc);
		
			gbc.gridx = 2;
			gbc.gridy = 0;
				mainPanel.add(amountPanel, gbc);
				
			gbc.gridx = 3;
			gbc.gridy = 0;
				mainPanel.add(roomPanel,gbc);
				
			gbc.gridx = 1;
			gbc.gridy = 1;
				mainPanel.add(colorChoice, gbc);
				
			gbc.gridx = 3;
			gbc.gridy = 1;
				mainPanel.add(bolkowRoomCBox, gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 2;
			gbc.gridx = 1;
			gbc.gridy = 2;
				mainPanel.add(issueButton, gbc);

			gbc.gridwidth = 4;
			gbc.gridx = 0;
			gbc.gridy = 3;
				mainPanel.add(queryStatus, gbc);

		//Dodawanie panelu do okna i odswiezanie okna
		issueFrame.add(mainPanel);
		SwingUtilities.updateComponentTreeUI(issueFrame);
	}

	//Obiekty do sluchaczy dla obiektow
	//Sluchacz dla wyboru przedmiotu, aby zmieniac modele i ustawiać widoczność wyboru koloru
	private final ItemListener tabelsChoiceListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			@SuppressWarnings("unchecked")
			String choice = (String) ((JComboBox<String>)e.getSource()).getSelectedItem();
			
			setModelChoice(choice);
		}};
		
	//Sluchasz dla wyboru modelu/producenta. Gdy wybierany jest tusz, trzeba ustawic odpowiednie kolory aby moc pozniej wstawic odpowiednie dane
	private final ItemListener modelChoiceListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(tablesChoice.getSelectedItem().toString().equals("Tusz")) {
				String chosedModel = (String)modelChoice.getSelectedItem();
				List<LinkedHashMap<String,String>> res = DBConn.getSpecified("color", "tusze", "model LIKE '%" + chosedModel + "%'");
				
				DefaultComboBoxModel<String> cbm = (DefaultComboBoxModel<String>) colorChoice.getModel();
				cbm.removeAllElements();
				
				for(LinkedHashMap<String, String> i : res) {
					i.forEach((key, value) -> {
						cbm.addElement(value);
					});
				}
				issueFrame.revalidate();
				
			}
		}
	};
		
	//Sluchacz dla okna, aby nie zamykal programu a znikal
	private final WindowAdapter issueThingsWindowListener = new WindowAdapter() {
			@Override
		public void windowClosing(WindowEvent we) {
			issueFrame.setVisible(false);
		}
	};
	
	//Sluchacz dla checkboxa dla bolkowa
	private final ItemListener bolkowRoomCBoxListener = new ItemListener(){

		@Override
		public void itemStateChanged(ItemEvent e) {

			if(e.getStateChange() ==1 )
				roomField.setEnabled(false);
			else
				roomField.setEnabled(true);
				

		}

	};
	
	//Sluchacz dla przycisku do wydawania
	private final ActionListener issueButtonListener = new ActionListener() {


		@Override
		public void actionPerformed(ActionEvent e) {
			issueMethod();
		}
			
	};


}


