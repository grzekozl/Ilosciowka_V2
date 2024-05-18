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
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;


public class issueThings {
	private static JFrame issueFrame;
	private JComboBox<String> modelChoice, colorChoice, tablesChoice;
	private JFormattedTextField amountField, roomField;
	private JLabel queryStatus;
	private JPanel mainPanel;
	
	
	//Metoda ustawiajaca czy okno maF byc widoczne czy nie
	static public void setItVisible(boolean state) {
		issueFrame.setVisible(state);
		issueFrame.setLocationRelativeTo(null);
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
	
	//Metoda wstawiajaca do bazy nowe wydanie oraz odejmuje jeden od ilosci danego przedmiotu
	private void issueMethod(){
		//Ustawianie eykiety na wiadomosc ze uzytkownik ma czekac
		queryStatus.setText("<html><font color = 'yellow'>Przetwarzanie...</font></html>");

		//Mapa z poprawnymi nazwami kolumn w bazie
		LinkedHashMap<String,String> dbNames = new LinkedHashMap<String,String>();
			dbNames.put("Tusz", "tusze");
			dbNames.put("Toner", "tonery");
			dbNames.put("Klawiatura", "klawiatury");
			dbNames.put("Myszka","myszki");
			
		//Pobieranie wartosci z pol
		String table = dbNames.get(tablesChoice.getSelectedItem().toString());
		String model = modelChoice.getSelectedItem().toString();
		String amount = String.valueOf(amountField.getValue());
		String color = colorChoice.isEnabled() ? (String)colorChoice.getSelectedItem() : null;
		String room = roomField.isEnabled() ? "pokój " + roomField.getValue() : "bolków";
		int modelId = DBConn.getIdOf(model, table, color);
		String issueTable = "wydane_" + table;
			
		//HashMapa z canymi do zapytania SQL
		LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
			data.put("\"model\"", ("'"+String.valueOf(modelId))+"'");
			data.put("\"amount\"", ("'"+String.valueOf(amount))+"'");
			data.put("\"where\"", "'" + room + "'");
		
			//Wątek który uruchamia zapytania
		Thread th = new Thread(){
			public void run(){
				//Sprawdzanie czy ilosc an stanie w bazie pozwala na wydanie zadanej ilosci
				DBConn.getDataOf(modelId, table).forEach(sqlData -> {

					//Jezeli ilosc w bazie jest mniejsza od wydawanej
					try{
						if(Integer.parseInt(sqlData.get("amount")) < Integer.parseInt(amount)){
							int differance = Integer.parseInt(amount) - Integer.parseInt(sqlData.get("amount"));
							int confirm = JOptionPane.showConfirmDialog(null, "Zabraknie "+ differance + ". Czy chcesz wydać mimo to?", "Brakująca ilość", JOptionPane.OK_CANCEL_OPTION, 0 , null);
								/*
								* 0 - OK
								* 2 - Cancel
								*/

							//Warunek jezeli uzytkownik zgodzil sie na wydanie resztki sprzetu
							if(confirm == 0){
								//Wprowadzanie danych - ma byc wykonywane gdy ilosc elementu jest rowna lub wieksza ilosci wydawanej
								DBConn.sendUpdate(table, sqlData.get("amount"), model); // Zmniejszanie ilosci do 0 na bazie
				
								if(DBConn.sendInsert(issueTable, data)) // Wstawianie danych do tabeli wydanych
									queryStatus.setText("<html><font color='green'>Wprowadzono dane</font></html>");
								else
									queryStatus.setText("<html><font color='red'>Uno problemo szefuńciu</font></html>");
								Thread.sleep(3000);
								queryStatus.setText(" ");
							}else if(confirm == 2){
								queryStatus.setText("<html><font color=yellow>Nie wydano, potrzeba wydania byla "+ amount +", zabrakło "+ differance +"</font></html>");
								Thread.sleep(3000);
								queryStatus.setText(" ");
							}
						}else{
							DBConn.sendUpdate(table, amount, model); // Zmniejszanie ilosci do 0 na bazie
							
							if(DBConn.sendInsert(issueTable, data)) // Wstawianie danych do tabeli wydanych
							queryStatus.setText("<html><font color='green'>Wprowadzono dane</font></html>");
							else
							queryStatus.setText("<html><font color='red'>Uno problemo szefuńciu</font></html>");
							
							Thread.sleep(3000);
							queryStatus.setText(" ");
								
						}
					}catch(InterruptedException exc){DBConn.error(exc);}
				});
			}
		};
		th.start();		
	}
	
	//Konstruktor klasy
	issueThings(){
		//Przygotowywanie danych dla tworzenia elementów okna
			//Tablice do tworzenia ComboBoxow - maja zawierac modele/producentow 

			String[] tablesName = {"Tusz", "Toner", "Klawiatura", "Myszka"};

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
		
		//ComboBox z wyborem przedmiotu - toner/tusz...
		tablesChoice = new JComboBox<String>(tablesName);
			tablesChoice.setPreferredSize(new Dimension(100,25));
			tablesChoice.addItemListener(tabelsChoiceListener);	//Sluchacz wyboru przedmiotu aby aktualizowac modele w kolejnym ComboBox - modelem 
			// tablesChoice.setSelectedIndex(1);
		
		//TODO: Dodanie etykiety nad wyborem modelu zawierającej drukarki do których materiał pasuje
		JLabel modelLabel = new JLabel();
			modelLabel.setText("SEX");

		//ComboBox dla wyboru modeli lub prodecenta
		modelChoice = new JComboBox<String>();
			// modelChoice.selectWithKeyChar((char) 'T');
			modelChoice.setPreferredSize(new Dimension(100,25));
			modelChoice.addItemListener(modelChoiceListener);
				
		//ComboBox dla wyboru koloru przy tuszach
		colorChoice = new JComboBox<String>();
			modelChoiceListener.itemStateChanged(null);
			colorChoice.setPreferredSize(new Dimension(100,25));
			
		//Panel do wprowadzania ilosci
		JPanel amountPanel = new JPanel(new GridBagLayout());
	
		
		//Pole do wprowadzenia ilosci
			//Format dla pola
			NumberFormatter numForm = new NumberFormatter(NumberFormat.getInstance());
			numForm.setMinimum(1);
			numForm.setMaximum(Integer.MAX_VALUE);

		amountField = new JFormattedTextField(numForm);
			amountField.setValue(1);
			amountField.setPreferredSize(new Dimension(45,25));

		////Etykieta dla pola ilosc
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
			roomField = new JFormattedTextField();
				roomField.setValue("1");
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
		mainPanel.setBackground(Color.gray);
		mainPanel.setOpaque(false);
		issueFrame.setBackground(Color.gray);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4,3,4,3);
			gbc.fill = GridBagConstraints.NORTH;
			
			gbc.gridx = 1;
			gbc.gridy = 0;
				mainPanel.add(modelLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
				mainPanel.add(tablesChoice, gbc);
				
			gbc.gridx = 1;
			gbc.gridy = 1;
				mainPanel.add(modelChoice, gbc);
		
			gbc.gridx = 2;
			gbc.gridy = 1;
				mainPanel.add(amountPanel, gbc);
				
			gbc.gridx = 3;
			gbc.gridy = 1;
				mainPanel.add(roomPanel,gbc);
				
			gbc.gridx = 1;
			gbc.gridy = 2;
				mainPanel.add(colorChoice, gbc);
				
			gbc.gridx = 3;
			gbc.gridy = 2;
				mainPanel.add(bolkowRoomCBox, gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 2;
			gbc.gridx = 1;
			gbc.gridy = 3;
				mainPanel.add(issueButton, gbc);

			gbc.gridwidth = 4;
			gbc.gridx = 0;
			gbc.gridy = 4;
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
			
			if(!e.getItem().toString().equals("Tusz"))
				colorChoice.setEnabled(false);
			else
				colorChoice.setEnabled(true);

			String table = 
				(e.getItem().toString().equals("Tusz")) ? "tusze" :
				(e.getItem().toString().equals("Toner")) ? "tonery" :
				(e.getItem().toString().equals("Klawiatura")) ? "klawiatury" :
				(e.getItem().toString().equals("Myszka")) ? "myszki" : null ;

			String group = 
				(e.getItem().toString().equals("Tusz") || e.getItem().toString().equals("Toner")) ? "model" :
				(e.getItem().toString().equals("Klawiatura") || e.getItem().toString().equals("Myszka")) ? "manufacturer" : null ;

			String[] columns = 
				(e.getItem().toString().equals("Tusz")) ? new String[]{"model","printer"}  :
				(e.getItem().toString().equals("Toner")) ? new String[]{"model","what_printer"} :
				(e.getItem().toString().equals("Klawiatura") || e.getItem().toString().equals("Myszka")) ? new String[]{"manufacturer"}
				 : null ;

			String[] newData = ListToArray(DBConn.getSpecified(columns, table, null));

			for (String data : newData){
				System.out.println(data);
			}

			DefaultComboBoxModel<String> defModelChoiceMod = (DefaultComboBoxModel<String>)modelChoice.getModel();
			defModelChoiceMod.removeAllElements();

			for(String val : newData){
				defModelChoiceMod.addElement(val);
			}

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

				if(tablesChoice.getSelectedItem().toString().equals("Tusz") || tablesChoice.getSelectedItem().toString().equals("Toner")){


				}

				SwingUtilities.updateComponentTreeUI(issueFrame);
				
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


