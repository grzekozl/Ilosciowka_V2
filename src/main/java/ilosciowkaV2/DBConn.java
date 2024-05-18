package ilosciowkaV2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.sql.ResultSet;

public class DBConn{

	static private String baza, user, pass;

	//Mapa z danymi do wybierania danych z nazwami kolumn dla czesci ilosciowki
	static private HashMap<String, String[]> amounterMap = new HashMap<>();
		static private String[] tusze = {"model AS 'Model'", "color AS 'Kolor'", "printer AS 'Do Drukarki'", "amount AS 'Ilość'"};
		static private String[] tonery = {"what_printer AS 'Do Drukarki'", "model AS 'Model'", "amount AS 'Ilość'" };
		static private String[] klawiatury = {"manufacturer AS 'Producent'", "amount AS 'Ilość'"};
		static private String[] myszki = {"manufacturer AS 'Producent'", "amount AS 'Ilość'"};

	//Mapa z danymi do wybierania danych z nazwami kolumn dla czesci wydanych rzeczy
	static private HashMap<String, String[]> issuedMap = new HashMap<>();
		static private String[] tusze_Wydane = {"tusze.model AS Model", "tusze.color AS Kolor", "wydane_tusze.amount AS Ile", "wydane_tusze.[where] AS Gdzie", "wydane_tusze.[when] AS Kiedy"};
		static private String[] tonery_Wydane = {"tonery.model AS 'Model'", "wydane_tonery.amount AS 'Ile'", "wydane_tonery.[where] AS 'Gdzie'", "wydane_tonery.[when] AS 'Kiedy'"};
		static private String[] myszki_Wydane = {"myszki.manufacturer AS Producent", "wydane_myszki.amount AS Ile", "wydane_myszki.[where] AS Gdzie", "wydane_myszki.[when] AS Kiedy"};
		static private String[] klawiatury_Wydane = {"klawiatury.manufacturer AS Producent", "klawiatury.amount AS Ile", "wydane_klawiatury.[where] AS Gdzie", "wydane_klawiatury.[when] AS Kiedy"};

	static{
		amounterMap.put("tusze",		tusze);
		amounterMap.put("tonery",		tonery);
		amounterMap.put("klawiatury",	myszki);
		amounterMap.put("myszki",		klawiatury);

		issuedMap.put("tusze", 			tusze_Wydane);
		issuedMap.put("tonery",			tonery_Wydane);
		issuedMap.put("myszki", 		myszki_Wydane);
		issuedMap.put("klawiatury", 	klawiatury_Wydane);
	}

	//Metoda ustawiajaca dane polaczenia do bazy - z portem
	static public boolean setConnection(String ip, String port, String nazwaBazy, String uzytkownik, String haslo) {

		baza = "jdbc:sqlserver://" + ip + ":" + port + ";databaseName=" + nazwaBazy + ";characterEncoding=utf8;integratedSecurity=true;";
		user = uzytkownik;
		pass = haslo;

		DriverManager.setLoginTimeout(1);
		try(Connection Conn = DriverManager.getConnection(baza, uzytkownik, haslo)){
			return true;
		}catch(SQLException exc){
			error(exc);
			return false;
		}
	};

	//Metoda ustawiajaca dane polaczenia do bazy - bez portu
	static public boolean setConnection(String ip, String nazwaBazy, String uzytkownik, String haslo) {

		baza = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + nazwaBazy + ";?characterEncoding=utf8";
		user = uzytkownik;
		pass = haslo;

		DriverManager.setLoginTimeout(5);
		try(Connection Conn = DriverManager.getConnection(baza, uzytkownik, haslo)){
			return true;
		}catch(SQLException exc){
			error(exc);
			return false;
		}
	};

	/*
	  Metoda do przetwarzania ResultSet - wniku zapytania - na tablice LinkedHashMap
	  HashMap nie sortuje wdlug tego jak sa dodawane do neij elementy 
	  LinkedHashMap ma metody i sluchacza od sortowania i podstawowo sortuje welug dodanych elementow
	*/
	static private List<LinkedHashMap<String, String>> convertRSToList(ResultSet input){
		List<LinkedHashMap<String, String>> myList = new ArrayList<LinkedHashMap<String, String>>();
		
		try {
			//Wydobywanie danych i ustawianie ich dla kolejncyh Map w liscie
			while(input.next()) {
				LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
				for(int j = 1; j <= input.getMetaData().getColumnCount(); j++) {
					data.put(input.getMetaData().getColumnLabel(j), input.getString(j));
				}
				myList.add(data);
			}
			
		} catch (SQLException e) {error(e);}
	
		return myList;
	}
	
	//Metoda na wypadek bledu - zapisuje dane do pliku oraz pokazuje okno z komunikatem
	static public void error(Exception exc) {
		String os = System.getProperty("os.name");
		char del = 0;
		
		
		if(os.contains("Windows")) 
			del = '\\';
		else if(os.contains("Linux"))
			del = '/';

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-HH-mm-ss");
		String filePath = String.join(Character.toString(del), System.getProperty("user.home"),"ilosciowkaV2", "blad" + sdf.format(new Date()) + ".txt");
		try {
			//Zapisywani bledu do pliku
			File excFile = new File(filePath);
			excFile.getParentFile().mkdirs();
			excFile.createNewFile();
			PrintWriter excPS = new PrintWriter(new FileWriter(excFile), true);
			exc.printStackTrace(excPS);
		} catch (IOException e) {JOptionPane.showMessageDialog(null, e.toString());}
		
		JOptionPane.showMessageDialog(null, "<html> Wystąpil bład. Plik z błędem zapisany został w: <br> " + filePath + " </html> ", "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	//Metoda wybierajaca wszystkie dane
	static public List<LinkedHashMap<String,String>> getAll(String table, boolean areIssued){
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;

		try (Connection Conn = DriverManager.getConnection(baza,user,pass)){
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			
			sqlRes = areIssued ? 
				stat.executeQuery("SELECT " + String.join(",", issuedMap.get(table)) + " FROM wydane_" + table +
				" JOIN " + table + " ON (wydane_" + table + ".model = " + table + ".Id) ORDER BY wydane_"+table+".[when] DESC;")
				:
				stat.executeQuery("SELECT " + String.join(",", amounterMap.get(table)) + " FROM " + table + " ORDER BY "+ table +".amount ASC;" );
			

			if(sqlRes.getWarnings() != null || sqlRes == null) {
				throw new SQLException();
			}
			
			res = convertRSToList(sqlRes);
			
		}catch(SQLException exc) {error(exc);}
		return res;
		
		
	}
	
	//Ta metoda moze byc wystarczajaca gdy where ustawione na null nie bedzie dodawac średnika
	static public List<LinkedHashMap<String, String>> getSpecified(String[] columns, String table, String where) {
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT " + String.join(",", columns) + " FROM " + table;
			query = (where != null) ? query + " WHERE " + where + ";" : query + ";";

			System.out.println(query);

			sqlRes = stat.executeQuery(query);
			
			return convertRSToList(sqlRes);
		} catch (SQLException e) {error(e);}
		
		return res;
	}

	//Wybieranie konkretnych danych - metoda dla pzrypadku gdy chcemy wyciagnac tylko jeden element (nie mozna utworzyc tablicy w argumencie)
	static public List<LinkedHashMap<String, String>> getSpecified(String column, String table, String where) {
		String[] arr = {column};
		return getSpecified(arr, table, where);
	}

	// Powyzsze metody z grupowaniem
	static public List<LinkedHashMap<String, String>> getSpecified(String[] columns, String table, String where, String groupBy) {
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT " + String.join(",", columns) + " FROM " + table + " ";
			query = (where != null) ? query + " WHERE " + where + ";" : (groupBy == null) ? query + ";": query;
			query = (groupBy != null) ? query + "GROUP BY " + groupBy : query + ";";

			System.out.println(query);

			sqlRes = stat.executeQuery(query);
			
			return convertRSToList(sqlRes);
		} catch (SQLException e) {error(e);}
		
		return res;
	}

	//Wybieranie konkretnych danych - metoda dla pzrypadku gdy chcemy wyciagnac tylko jeden element (nie mozna utworzyc tablicy w argumencie)
	static public List<LinkedHashMap<String, String>> getSpecified(String column, String table, String where, String groupBy) {
		String[] arr = {column};
		return getSpecified(arr, table, where, groupBy);
	}

	//Metoda wybierajaca wszystkie dane konkretnego modelu dla toneru, myszki i klawiatury (tusze ze wzgledu na swoja inna budowe w bzie wymagaja innego zapytania - innej metody)
	static public List<LinkedHashMap<String,String>> getDataOf(int id, String table){
		List<LinkedHashMap<String,String>> res = null;

		try(Connection conn = DriverManager.getConnection(baza, user, pass)){
			Statement stat = conn.createStatement();

			String query = "SELECT * FROM " + table + " WHERE id = " + id;

			ResultSet sqlRes = stat.executeQuery(query);
			res = convertRSToList(sqlRes);

		}catch(SQLException exc){error(exc);}

		return res;
	}

	//Metoda wybierajaca ID pdoanego elementu
	static public int getIdOf(String model, String table, String color) {
		int res = 0;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)){
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String columnName = null;
			switch(table) {
				case "tusze":
				case "tonery":
					columnName = "model";
					break;
				case "klawiatury":
				case "myszki":
					columnName = "manufacturer";
					break;
			} 

			String query = "SELECT Id FROM " + table + " WHERE " + columnName + " LIKE '%" + model + "%';";
			query = (color!=null) ? query.subSequence(0, query.length()-1) + " AND color LIKE '%" + color + "%';":query;

			ResultSet sqlRes = stat.executeQuery(query);
			
			sqlRes.first();
			res = sqlRes.getInt(1);
			
		} catch(SQLException e) {error(e);}
		
		return res;
	}

	//Metoda tworzaca JTable z listy wynikow z SQL
	static public final JTable makeJTable(List<LinkedHashMap<String, String>> input) {
		JTable res = null;
		String[] labels = new String[input.get(0).keySet().size()];
		Object[][] data = null;

		// Ustawianie nazw kolumn w tablicy
		for(int i = 0; i < labels.length; i++) {
			labels[i] = (String) input.get(0).keySet().toArray()[i];		
		}
		
		////Ile mamy rekordow
		final int rows = input.size();
			
		//Ile mamy kolumn
		final int cols = input.get(0).keySet().size();

		data = new Object[rows][cols];
		
		//Magia do tworzenia dwu wymiarowej tablicy aby dodac dane do JTable
		int i = 0;
		for(HashMap<String, String> hm : input) {
			for(int j = 0; j < cols;j++) 
				if(labels[j].equals("Ile") || labels[j].equals("Ilość"))
					data[i][j] = Integer.valueOf(hm.get(labels[j]));
				else
				data[i][j] = hm.get(labels[j]);
			i++;
		}
		
		//Nadpisywanie metody getColumnClass w model utablicy aby ta zwracala poprawne typy danych w kolumnie
		DefaultTableModel resModel = new DefaultTableModel(data, labels){
			@Override
			public Class<?> getColumnClass(int col) {
			
				Class<?> retVal = Object.class;

				if(getRowCount() > 0)
					retVal =  getValueAt(0, col).getClass();
					
				return retVal;
			}
		};

		//Tworzenie JTable z wylaczona opcja edytowania - nadpisanie metody isCellEditable unimozliwia wprowadzanie zmian, ale pozwala na zaznaczenie wiersza
		res = new JTable(resModel) {
			 	
			private static final long serialVersionUID = -6294684695385804289L;

			@Override
			public boolean isCellEditable(int row, int column){  
		          return false;  
		      }			
		};
		
		//Ustawienia JTable przed jego zwroceniem
		res.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		return res;
	}
	
	//Metoda wstawiajaca dane do bazy
	static public final boolean sendInsert(String table, LinkedHashMap<String,String> data) {
		boolean returnVal = false;
		StringBuilder query = new StringBuilder("INSERT INTO dbo." + table + "(" + String.join(",", data.keySet().toArray(new String[0])) + ")" 
				+ " VALUES (" + String.join(",", data.values().toArray(new String[0])) + ");");

		while(query.indexOf("[") != -1)
			query = query.replace(query.indexOf("["), query.indexOf("[")+1, "(");
		
		while(query.indexOf("]") != -1)
			query = query.replace(query.indexOf("]"), query.indexOf("]")+1, ")");		
		
		try (Connection Conn = DriverManager.getConnection(baza,user,pass)){
			PreparedStatement stat = Conn.prepareStatement(query.toString());
			
			if(stat.executeUpdate() > 0)
				returnVal = true;
			else
				returnVal = false;
			
		} catch (Exception e) {error(e);}
		return returnVal;
	}
	
	//Metoda aktualizujaca dane w bazie
	static public final boolean sendUpdate(String table, String amount, String ofWhat){
		String modOrManu = 
			table.equalsIgnoreCase("tusze") || table.equalsIgnoreCase("tonery") ? "model" :
			table.equalsIgnoreCase("myszki") || table.equalsIgnoreCase("klawiatury") ? "manufacturer" : null;

		if(!modOrManu.isEmpty()){
			String query = "UPDATE dbo." + table + " SET amount = amount - " + amount + " WHERE " + modOrManu + " LIKE '%" + ofWhat + "%';";
			String[] returnCols = {"amount"};
			try(Connection Conn = DriverManager.getConnection(baza, user, pass)){
				Statement stat = Conn.createStatement();
				stat.executeUpdate(query, returnCols);
				
			}catch (SQLException exc){error(exc);}

			return true;
		}
		return false;
	}
}
