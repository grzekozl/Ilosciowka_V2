package ilosciowkaV2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import java.sql.ResultSet;

public class DBConn{

	static private String baza, user, pass;

	static private HashMap<String, String[]> amounterMap = new HashMap<>();
		static private String[] tusze = {"model AS 'Model'", "color AS 'Kolor'", "printer AS 'Do Drukarki'", "amount AS 'Ilość'"};
		static private String[] tonery = {"what_printer AS 'Do Drukarki'", "model AS 'Model'", "amount AS 'Ilość'" };
		static private String[] klawiatury = {"manufacturer AS 'Producent'", "amount AS 'Ilość'"};
		static private String[] myszki = {"manufacturer AS 'Producent'", "amount AS 'Ilość'"};

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

		issuedMap.put("tusze", 		tusze_Wydane);
		issuedMap.put("tonery",		tonery_Wydane);
		issuedMap.put("myszki", 		myszki_Wydane);
		issuedMap.put("klawiatury", 	klawiatury_Wydane);
	}

	static final public boolean ISSUED = true;	// parametr ze chcemy wydobyc wydane
	static final public boolean NOT_ISSUED = false;// parametr ze nie chcemy wydobwyac wydanych

	static public boolean setConnection(String ip, String port, String nazwaBazy, String uzytkownik, String haslo) {

		baza = "jdbc:sqlserver://" + ip + ":" + port + ";databaseName=" + nazwaBazy + ";characterEncoding=utf8;integratedSecurity=true;";
		user = uzytkownik;
		pass = haslo;

		DriverManager.setLoginTimeout(1);
		try(Connection Conn = DriverManager.getConnection(baza, uzytkownik, haslo)){
			return true;
		}catch(SQLException exc){
			exc.printStackTrace();
			return false;
		}
	};

	static public boolean setConnection(String ip, String nazwaBazy, String uzytkownik, String haslo) {

		baza = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + nazwaBazy + ";?characterEncoding=utf8";
		user = uzytkownik;
		pass = haslo;

		DriverManager.setLoginTimeout(5);
		try(Connection Conn = DriverManager.getConnection(baza, uzytkownik, haslo)){
			return true;
		}catch(SQLException exc){
			exc.printStackTrace();
			return false;
		}
	};

	/*
	 * Metoda do przetwarzania ResultSet - wniku zapytania - na tablice LinkedHashMap
	 * HashMap nie sortuje wdlug tego jak sa dodawane do neij elementy 
	 * LinkedHashMap ma metody i sluchacza od sortowania i podstawowo sortuje welug dodanych elementow
	 * 
	 * @return Liste LinkedHashMap / tablice LinkedHashmap
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
			
		} catch (SQLException e) {e.printStackTrace();}
	
		return myList;
	}
	
	static public void error(Exception exc) {
		String os = System.getProperty("os.name");
		char del = 0;
		
		if(os.contains("Windows")) 
			del = '\\';
		else if(os.contains("Linux"))
			del = '/';

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-HH:mm");
		String filePath = String.join(Character.toString(del), System.getProperty("user.home"),"ilosciowkaV2", "blad" + sdf.format(new Date()) + ".txt");
		File excFile = new File(filePath);
		FileWriter FW = null;
		
		try {
			//Zapisywani bledu do pliku
			excFile.getParentFile().mkdirs();
			excFile.createNewFile();
			FW = new FileWriter(excFile);
			FW.write(exc.getMessage());
			exc.printStackTrace();
			FW.flush();
			FW.close();

		} catch (IOException e) {JOptionPane.showMessageDialog(null, e.toString());}
		
		JOptionPane.showMessageDialog(null, "Wystąpil bład. Plik z błędem zapisany został w \n " + filePath, "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	static public List<LinkedHashMap<String,String>> getAll(String table, boolean areIssued){
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;

		try (Connection Conn = DriverManager.getConnection(baza,user,pass)){
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			
			sqlRes = areIssued ? 
				stat.executeQuery("SELECT " + String.join(",", issuedMap.get(table)) + " FROM wydane_" + table +
				" JOIN " + table + " ON (wydane_"+table+".model = " + table + ".Id) ORDER BY wydane_"+table+".[when] DESC;")
				:
				stat.executeQuery("SELECT " + String.join(",", amounterMap.get(table)) + " FROM " + table + " ORDER BY "+ table +".amount ASC;" );
			

			if(sqlRes.getWarnings() != null || sqlRes == null) {
				System.out.println(sqlRes.getWarnings().getMessage());
				throw new SQLException();
			}
			
			res = convertRSToList(sqlRes);
			
		}catch(SQLException exc) {error(exc);}
		return res;
		
		
	}

	static public List<LinkedHashMap<String, String>> getSpecified(String[] columns, String table) {
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_READ_ONLY);
			
			sqlRes = stat.executeQuery("SELECT " + String.join(",", columns) + " FROM " + table + ";");
			
			if(sqlRes.getWarnings() != null) {
				System.out.println(sqlRes.getWarnings().getMessage());
				return null;
			}
			
			return convertRSToList(sqlRes);
		} catch (SQLException e) {error(e);}
		return res;
	}
	
	static public List<LinkedHashMap<String, String>> getSpecified(String[] columns, String table, String where) {
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			
			sqlRes = stat.executeQuery("SELECT " + String.join(",", columns) + " FROM " + table + " WHERE " + where + ";");
			
			return convertRSToList(sqlRes);
		} catch (SQLException e) {error(e);}
		
		return res;
	}
	
	static public List<LinkedHashMap<String, String>> getSpecified(String column, String table) {
		List<LinkedHashMap<String, String>> res = null;
		ResultSet sqlRes = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
			Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			
			sqlRes = stat.executeQuery("SELECT " + column + " FROM " + table + ";");
			
			if(sqlRes.getWarnings() != null) {
				System.out.println(sqlRes.getWarnings().getMessage());
				throw new SQLException();
			}
			
			return convertRSToList(sqlRes);

		} catch (SQLException e) {error(e);}
		
		return res;
	}
	
	static public List<LinkedHashMap<String, String>> getSpecified(String column, String table, String where) {

		List<LinkedHashMap<String, String>> res = null;
		
		try(Connection Conn = DriverManager.getConnection(baza,user,pass)) {
				Statement stat = Conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
				
				ResultSet sqlRes = stat.executeQuery("SELECT " + column + " FROM " + table + " WHERE " + where + " ;");
				return convertRSToList(sqlRes);


		} catch (SQLException e) {error(e);}
		
		return res;
	}
	
	static public int getIdOf(String thing, String table) {
		Integer res = null;
		
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
			String query = "SELECT Id FROM " + table + " WHERE " + columnName + " LIKE '%" + thing + "%';";
			ResultSet sqlRes = stat.executeQuery(query);
			
			sqlRes.first();
			res = sqlRes.getInt(1);
			
		} catch(SQLException e) {error(e);}
		
		return res;
	}	
	
	static public int getIdOf(String thing, String table, String color) {
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
			String query = "SELECT Id FROM " + table + " WHERE " + columnName + " LIKE '%" + thing + "%' AND color LIKE '%" + color + "%';";
			ResultSet sqlRes = stat.executeQuery(query);
			
			sqlRes.first();
			res = sqlRes.getInt(1);
			
		} catch(SQLException e) {error(e);}
		
		return res;
	}

	static public final JTable makeJTable(List<LinkedHashMap<String, String>> input) {
		JTable res = null;
		String[] labels = new String[input.get(0).keySet().size()];
		String[][] data = null;


		// Ustawianie nazw kolumn w tablicy
		for(int i = 0; i < labels.length; i++) {
			labels[i] = (String) input.get(0).keySet().toArray()[i];		
		}
		
		////Ile mamy rekordow
		final int rows = input.size();
			
		//Ile mamy kolumn
		final int cols = input.get(0).keySet().size();

		data = new String[rows][cols];
		
		//Magia do tworzenia dwu wymiarowej tablicy aby dodac dane do JTable
		int i = 0;
		for(HashMap<String, String> hm : input) {
			for(int j = 0; j < cols;j++) {
				data[i][j] = hm.get(labels[j]);
			}
			i++;
		}
		
		//Tworzenie JTable z wylaczona opcja edytowania - nadpisanie metody isCellEditable unimozliwia wprowadzanie zmian, ale pozwala na zaznaczenie wiersza
		res = new JTable(data, labels) {
			 	
			private static final long serialVersionUID = -6294684695385804289L;

			public boolean isCellEditable(int row, int column){  
		          return false;  
		      }
		};
		
		//Ustawienia JTable przed jego zwroceniem
		res.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// res.setAutoCreateRowSorter(true);
		
		return res;
	}
	
	static public final boolean sendInsert(String table, LinkedHashMap<String,String> data) {
		StringBuilder query = new StringBuilder("INSERT INTO dbo." + table + "(" + String.join(",", data.keySet().toArray(new String[0])) + ")" 
				+ " VALUES " + data.values().toString() + ";");
		
		while(query.indexOf("[") != -1)
			query = query.replace(query.indexOf("["), query.indexOf("[")+1, "(");
		
		while(query.indexOf("]") != -1)
			query = query.replace(query.indexOf("]"), query.indexOf("]")+1, ")");		
		
		try (Connection Conn = DriverManager.getConnection(baza,user,pass)){
			PreparedStatement stat = Conn.prepareStatement(query.toString());
			
			if(stat.executeUpdate() > 0)
				return true;
			else
				return false;
			
		} catch (Exception e) {e.printStackTrace();}
		return true;
	}
	
	static public final boolean sendUpdate(String table, String amount, String ofWhat){
		String modOrManu = "";

		if(table.equalsIgnoreCase("tusze") || table.equalsIgnoreCase("tonery"))
			modOrManu = "model";
		else if (table.equalsIgnoreCase("myszki") || table.equalsIgnoreCase("klawiatury"))
			modOrManu = "manufacturer";

			if(!modOrManu.isEmpty()){
				String query = "UPDATE dbo." + table + " SET amount = amount - " + amount + " WHERE " + modOrManu + " LIKE '%" + ofWhat + "%'";
				String[] returnCols = {"amount"};
			try(Connection Conn = DriverManager.getConnection(baza, user, pass)){
				Statement stat = Conn.createStatement();
				stat.executeUpdate(query, returnCols);
				
			}catch (SQLException exc){error(exc);}
		}
		return true;
	}
}
