package ilosciowkaV2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Demo{
	private static JFrame loginFrame;
	private static JTextField ipField, loginField, passwordField;
	private static JLabel ipLabel, loginLabel, passwordLabel;
	private static JButton loginButton;
	private static GridBagConstraints gbc;
	private static final Dimension fieldDim = new Dimension(115, 25);
	private static JCheckBox rememberServer = new JCheckBox("Zapamiętaj serwer");
	private static JCheckBox rememberLogin = new JCheckBox("Zapamiętaj login");

	//Tworzenei watkow od okien
	static void isFine(){

		Thread mainFrame = new Thread(){
			public void run(){
				new mainFrame();
			}
		};
		mainFrame.start();

		Thread issueThings = new Thread(){
			public void run(){
				new issueThings();
			}
		};
		saveCreds();
		issueThings.start();
		loginFrame.dispose();
	}
	
	//Co sie dzieje jezeli dane są nie poprawne
	static void isNotFine(){
		JOptionPane.showMessageDialog(null, ""
		+ "Błąd połączenia z bazą."
		+ "\n\n"
		+ "Upewnij się że host jest uruchomony, podłączony do sieci, ma działającą usługę SQL i dane logowania są poprawne.",
		"Błąd połączenia",
		0);
		
		ipField.setEditable(true);
		loginField.setEditable(true);
		passwordField.setEditable(true);
		loginButton.setEnabled(true);
		
	}

	//Metoda pobierajace zapisane dane
	static private HashMap<String, String> getSavedCreds(){ 
		String del = "";
		HashMap<String, String> res = new HashMap<String, String>() ;

		if(System.getProperty("os.name").contains("Windows")){
			del = "\\";
		}else if(System.getProperty("os.name").contains("Linux")){
			del = "/";
		}

		String usrHome = System.getProperty("user.home");
		String filePath = usrHome + del + "ilosciowkaV2" + del + "login";

	//Odczytywanie danych logowania z pliku
		File credFile = new File(filePath);
			if(credFile.exists()){
				try(BufferedReader credBuffReader = new BufferedReader(new FileReader(credFile));){

					String inFields[] = {"server","login"};
					String line;

					while((line = credBuffReader.readLine()) != null){
						for(String word : inFields)
							if(line.contains(word))
								res.put(word, line.split(":")[1].replaceAll(" ", ""));
					}
				}catch(IOException exc){exc.printStackTrace();}
			}

		return res;
	}

	static private void saveCreds(){
		//Akcja dla zaznaczonego pola zapamietanie serwera
		String del = null;

		if(System.getProperty("os.name").contains("Windows")){
			del = "\\";
		}else if(System.getProperty("os.name").contains("Linux")){
			del = "/";
		}


		String usrHome = System.getProperty("user.home");
		String filePath = usrHome + del + "ilosciowkaV2" + del + "login";
		File loginFile = new File(filePath);
		
		try(FileWriter loginFileWriter = new FileWriter(loginFile);){

		if(!loginFile.exists()){
			loginFile.mkdirs();
			loginFile.createNewFile();
		}

		String server="", login="";
		if(rememberServer.isSelected())
			server = ipField.getText();

		if(rememberLogin.isSelected())
			login = loginField.getText();
		

			loginFileWriter.write("server: " + server + "\nlogin: " + login); // + "\nlogin: " + loginField.getText());
		}catch(IOException exc){
			exc.printStackTrace();
		}	
		
	}

	public static void main(String args[]) {

		
		//Ustawianie okna
		loginFrame = new JFrame("Logowanie");
			loginFrame.setLayout(new GridBagLayout());
			loginFrame.setResizable(false);
			loginFrame.setMinimumSize(new Dimension(400,275));
			loginFrame.setLocationRelativeTo(null);
			loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			loginFrame.getContentPane().setBackground(Color.LIGHT_GRAY);

		//Ustawianei pzrycisku
		loginButton = new JButton("Zaloguj");
			loginButton.addActionListener(loginButtonListener);

		//Ustawienia etykiet
		ipLabel = new JLabel("Serwer");
		loginLabel = new JLabel("Login");
		passwordLabel = new JLabel("Hasło");
			
		//Pobieranie zapisanych danych.
		HashMap<String, String> fileData = getSavedCreds();


			

		//Ustawienia pol tekstowych
		 	// Serwer SQL
			ipField = new JTextField(fileData.get("server"));
				ipField.setPreferredSize(fieldDim);
				ipField.setToolTipText("Adres IP albo nazwa hosta");
				ipField.addKeyListener(loginKeyListener);

			//Login uzytkownika	
			loginField = new JTextField(fileData.get("login"));
				loginField.setPreferredSize(fieldDim);
				loginField.setToolTipText("Nazwa użytkownika");
				loginField.addKeyListener(loginKeyListener);
			//Haslo
			passwordField = new JPasswordField();
				passwordField.setPreferredSize(fieldDim);
				passwordField.setToolTipText("Hasło");
				passwordField.addKeyListener(loginKeyListener);

		//Przycisk do logowania
		loginButton = new JButton("Zaloguj");
		loginButton.addActionListener(loginButtonListener);
		
		rememberServer = new JCheckBox("Zapamiętaj serwer");
			rememberServer.setOpaque(false);

		rememberLogin = new JCheckBox("Zapamiętaj login");
			rememberLogin.setOpaque(false);

		//Sprawdzanie ktore dane nalezy zaznaczyc do zapamietania
			if(fileData.get("server") != null)
				rememberServer.setSelected(true);

			if(fileData.get("login") != null)
				rememberLogin.setSelected(true);
		//Zasady layoutu do dodawania elementow do okna
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,3,4,0);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		//Etykieta Serwer
		gbc.gridx = 0;
		gbc.gridy = 0;
			loginFrame.add(ipLabel, gbc);
		
		//Pole Serwer
		gbc.gridx = 1;
		gbc.gridy = 0;
			loginFrame.add(ipField, gbc);

		//CheckBox zapamietywania serwera
		gbc.gridx = 2;
		gbc.gridy = 0;
			loginFrame.add(rememberServer, gbc);

		//Etykieta login
		gbc.gridx = 0;
		gbc.gridy = 1;
			loginFrame.add(loginLabel, gbc);
			
		//Pole login
		gbc.gridx = 1;
		gbc.gridy = 1;
			loginFrame.add(loginField, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
			loginFrame.add(rememberLogin, gbc);

		//Etykieta hasla
		gbc.gridx = 0;
		gbc.gridy = 2;
			loginFrame.add(passwordLabel, gbc);
		
		//Pole hasla
		gbc.gridx = 1;
		gbc.gridy = 2;
			loginFrame.add(passwordField, gbc);

		//Przycisk zaloguj
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 3;
		loginFrame.add(loginButton, gbc);

		// Uruchamianie i odswiezanei okna
		loginFrame.setVisible(true);
		SwingUtilities.updateComponentTreeUI(loginFrame);


	}
	
	//Sluchacz do wcisniecia enter
	private static KeyListener loginKeyListener = new KeyListener(){

		@Override
		public void keyTyped(KeyEvent e) {
			// 10 - enter
			//Trzeba pobierac KeyChar i konwertowac na inta - getKeyCode nie dziala
			if((int)e.getKeyChar() == 10)
				loginButton.doClick(1);
		}

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {}
	};

	//Sluchacz dla przycisku logowania
	private static ActionListener loginButtonListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent ae){
			
			//Wylaczanie pol danych
			ipField.setEditable(false);
			loginField.setEditable(false);
			passwordField.setEditable(false);
			loginButton.setEnabled(false);

			String ip = ipField.getText();
			String username = loginField.getText();
			String password = passwordField.getText();

			//Jezeli pola nie sa puste - zaloguj
			//Jezeli pola sa puste - wyrzuc blad
			if(!ip.equals("") && !username.equals("") && !password.equals("")){
				new Thread(){
					public void run(){
						boolean x = DBConn.setConnection(ip, "Ilosciowka V2", username, password);
						
						if(x)
							Demo.isFine();
						else
							Demo.isNotFine();
					}
				}.start();
			}else{
				//blad
				JOptionPane.showMessageDialog(null, "Wprowadź poprawne dane logowania - puste pola.", "Błąd logowania", 0, null);

				ipField.setEditable(true);
				loginField.setEditable(true);
				passwordField.setEditable(true);
				loginButton.setEnabled(true);

			}



		}
	};
	
	
}
