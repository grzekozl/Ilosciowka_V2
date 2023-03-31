package ilosciowkaV2;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class mainFrame{
	
	private JTable theTable = new JTable();
	private JPanel thePanelExt;
	private JScrollPane thePane = new JScrollPane();
	private ButtonGroup radioGroup;
	private JRadioButton inks, toners, mouses, keyboards;
	private TableRowSorter<TableModel> sorter = null;
	private JFormattedTextField filterField;
	private boolean issueVal = false;
	private JButton showIssued;

	//Metoda ustawiajaca filtr na tabele
	private void filterTable(String filter){

		sorter = new TableRowSorter<TableModel>(theTable.getModel());//Tworzenei sortera
		sorter.setRowFilter(null); // Czyszczenie filtr 
		sorter.setRowFilter(RowFilter.regexFilter(filter)); // Ustawianie filtra
		theTable.setRowSorter(sorter);
	}

	// Metoda ustawiajaca nowa zawartosc dla tabeli
	private void setTheShittyTable(String tabela) {
		theTable.setRowSorter(null);
		theTable = DBConn.makeJTable(DBConn.getAll(tabela, issueVal));
		theTable.setName(tabela);
		theTable.setAutoCreateRowSorter(true);
		
		refreshTable();

	}
	
	//Metoda odswiezajaca tabele
	private void refreshTable() {
		thePane.setViewportView(theTable);
		filterField.setText(null);
	}	
	
	//Konstruktor
	public mainFrame(){
		//Etykieta aby opisac filtr
		JLabel filterLabel = new JLabel("Znajdź rekord zawierający:");

		//TextField to wprowadzenia filtru
		filterField = new JFormattedTextField();
			filterField.addKeyListener(filterFieldListener);
			filterField.setMinimumSize(new Dimension(150,25));
			filterField.setPreferredSize(new Dimension(150,25));

		//Tworzenie i ustawianie okna
		JFrame mainFrame = new JFrame();
			mainFrame.setTitle("IlosciowkaV2 | Grzegorz Kozłowski");
			mainFrame.setMinimumSize(new Dimension(500,600));
			mainFrame.setSize(600,750);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setLayout(new BorderLayout());
			mainFrame.setLocationRelativeTo(null);
			mainFrame.getContentPane().setBackground(Color.lightGray);
			
			// Donly panel okna z tabela
			thePanelExt = new JPanel(new FlowLayout()); 
			thePanelExt.setBackground(Color.lightGray);

			//Wydobywanie ofert do obiketow JTable a nastepne do JScrollPane
		// setTheShittyTable();
		refreshTable();
		thePanelExt.add(thePane);
		thePanelExt.add(filterLabel);
		thePanelExt.add(filterField);

		//Layout dla gornego menu
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,2,6,0);	
		// gbc.fill = GridBagConstraints.HORIZONTAL;

		Dimension buttsDim = new Dimension(150,25);

		// Panel topMenu dla przyciskow wyoru tabeli i opcji
		JPanel topMenu = new JPanel();
			topMenu.setMinimumSize(new Dimension(600, 250));
			topMenu.setPreferredSize(new Dimension(600, 100));
			topMenu.setBackground(Color.LIGHT_GRAY);
			topMenu.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			topMenu.setLayout(new GridBagLayout());
		
			JPanel radioButtPanel = new JPanel();
				radioButtPanel.setOpaque(false);
			//Przyciski radio dla odpowiednich tabeli
			inks = new JRadioButton("Tusze", true);
				inks.setOpaque(false);
			toners = new JRadioButton("Tonery");
				toners.setOpaque(false);
			mouses = new JRadioButton("Myszki");
				mouses.setOpaque(false);
			keyboards = new JRadioButton("Klawiatury");
				keyboards.setOpaque(false);

			radioButtPanel.add(inks,gbc);
			radioButtPanel.add(toners,gbc);
			radioButtPanel.add(mouses,gbc);
			radioButtPanel.add(keyboards,gbc);

			//Ustawianie zawartosci tabeli na tusze
			setTheShittyTable("tusze");

			gbc.gridwidth = 6;
			gbc.gridx = 0;
			gbc.gridy = 0;
				topMenu.add(radioButtPanel, gbc);

			//Wrzucanie przyciskow do grupy aby tylko jeden mogl byc wybrany jednoczesnie
			radioGroup = new ButtonGroup();
			radioGroup.add(inks);
			radioGroup.add(toners);
			radioGroup.add(mouses);
			radioGroup.add(keyboards);
		
			//Podpinanie zdarzen do kazdego z przyciskow tabel
			inks.addActionListener(inksAction);
			toners.addActionListener(tonersAction);
			mouses.addActionListener(mousesAction);
			keyboards.addActionListener(keyboardsAction);
			
			
			JButton refreshButt = new JButton("Odswiez tabele");
			refreshButt.setMinimumSize(buttsDim);
			refreshButt.setPreferredSize(buttsDim);
			JButton issueButt = new JButton("Wydaj");
			issueButt.setMinimumSize(buttsDim);
				issueButt.setPreferredSize(buttsDim);
			showIssued = new JButton("Pokaż wydane");
				showIssued.setMinimumSize(buttsDim);
				showIssued.setPreferredSize(buttsDim);
				showIssued.addActionListener(showIssuedAction);
				
			gbc.gridwidth = 2;

			gbc.gridx = 0;
			gbc.gridy = 1;
				topMenu.add(refreshButt, gbc);
				
			gbc.gridx = 2;
			gbc.gridy = 1;
				topMenu.add(issueButt, gbc);
				
			gbc.gridx = 4;
			gbc.gridy = 1;
				topMenu.add(showIssued, gbc);
				
			refreshButt.addActionListener(refreshButtAction);
			issueButt.addActionListener(issueButtAction);
		

			
		//Dodawanie paneli do okna
		mainFrame.add(topMenu, BorderLayout.NORTH);
		mainFrame.add(thePanelExt, BorderLayout.CENTER);
		
		mainFrame.setVisible(true);
		refreshTable();
		
	}
	
	//Sluchacz dla pola filtra - Sprwadz czy nie ma niepoprawnych znakow i wykonaj metoda filtrujaca tablice
	private final KeyListener filterFieldListener = new KeyListener(){

		@Override
		public void keyTyped(KeyEvent e) {
			// keyReleased(e);
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			// keyReleased(e);

		}

		@Override
		public void keyReleased(KeyEvent e) {

			
			String filterText = filterField.getText();
			String regex = "";

			for(int i = 0; i < filterText.length(); i++){
				regex = regex.concat("\\w");
			}

			if(filterText.matches(regex))
				filterTable(filterText);
			else{
				JOptionPane.showMessageDialog(null, "Używaj tylko liter i cyfr");
				filterField.setText(filterText.replaceAll("\\W", ""));
				keyReleased(e);
			}

		}

	};

	//Sluchacze dla przycisko od wyoru tabeli
		//Metoda standaryzujaca sluchacze dla radio
			private final void standarizeRadioListener(String input){
				setTheShittyTable(input.toLowerCase());
				refreshTable();
				filterField.setText(null);
				filterFieldListener.keyPressed(null);
			}


	//Akcja tusze
	private final ActionListener inksAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			standarizeRadioListener(((JRadioButton)ae.getSource()).getText());
	}};
	
	//Akcja tonery
	private final ActionListener tonersAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			standarizeRadioListener(((JRadioButton)ae.getSource()).getText());
	}};
	
	//Akcja myszki
	private final ActionListener mousesAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			standarizeRadioListener(((JRadioButton)ae.getSource()).getText());
	}};
	
	//Akcja klawiatury
	private final ActionListener keyboardsAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			standarizeRadioListener(((JRadioButton)ae.getSource()).getText());
	}};
	
	
	
	
	
	//Akcja przycisku odswiezenia
	private final ActionListener refreshButtAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			setTheShittyTable(theTable.getName());
			refreshTable();
			
		}};
		
	//Akcja przycisku do aktualizowania bazy
	private final ActionListener issueButtAction = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			issueThings.setItVisible(true);
		}};
		
	//Akcja przycisku do pokazania wydanych rzeczy
		private final ActionListener showIssuedAction = new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				issueVal = !issueVal;
				setTheShittyTable(theTable.getName());
				filterField.setText(null);
				filterFieldListener.keyPressed(null);

				if(issueVal)
					showIssued.setText("Pokaż stan");
				else
					showIssued.setText("Pokaż wydane");
			}};
	
}
