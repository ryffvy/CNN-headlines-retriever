
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.text.SimpleDateFormat;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * CNN Top Articles Retriever
 * 
 * Purpose: A Java program to retrieve top headlines from https://newsapi.org/s/cnn-api
 * and save them in a CSV file. If CSV file already exist, then just updates it with new entries.
 * 
 * @author Daniyar Ospanov
 * Date: 12/12/17
 */

public class Main {

	public static final String CSV_HEADER = "Source,Author,Title,URL,Published At";
	public static String csvName;
	private static final String KEY = "4c3ec6538a85499a9afdd28fe65ded8c";
	
	public static void main(String[] args) {
		csvName = generateFileName();
		makeRequest();
	}
	
	
	/**
	 * Generate a name for CSV file
	 * 
	 * @return Generated name
	 */
	public static String generateFileName() {
		return "top_headlines_" + (new SimpleDateFormat("MMMMM_dd_yyyy").format(new Date())) + ".csv";		
	}

	
	/**
	 * Make an HTTP request and handle the response
	 * 
	 */
	public static void makeRequest() {
		
		try{
			URL url = new URL("https://newsapi.org/v2/top-headlines?sources=cnn&apiKey=" + KEY);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");			
			
			InputStream inStream = connection.getInputStream();			
			JSONParser jsParser = new JSONParser();
			
			// Parse JSON data 
			JSONObject jsData = (JSONObject)jsParser.parse(new InputStreamReader(inStream,"UTF-8"));						
			JSONArray jsArray = (JSONArray)(jsData.get("articles"));
			DataEntry[] entrySet = parseJSON(jsArray);
			
			connection.disconnect();
			
			saveData(entrySet);
		}
		catch (Exception e) {
			System.out.println("HTTP Request Error: " + e);
		}
	}

	
	/**
	 * Parse JSON data and save into DataEntry array
	 * 
	 * @param jsArray		Array of data entries in JSON format
	 * @return DataEntry[]	Array of data entries as DataEntry class entities		
	 */
	public static DataEntry[] parseJSON(JSONArray jsArray) {		
		DataEntry [] dataSet = new DataEntry[jsArray.size()];
		
		for(int i = 0; i < jsArray.size(); i++) {
			JSONObject entry = (JSONObject)jsArray.get(i);
			JSONObject source = (JSONObject) entry.get("source");
			DataEntry dEntry = new DataEntry();

			dEntry.sourceName = (String)source.get("name");
			dEntry.author = (String)entry.get("author");
			dEntry.title = (String)entry.get("title");
			dEntry.url = (String)entry.get("url");
			dEntry.publishedAt =  (String)entry.get("publishedAt");
			
			dataSet[i] = dEntry;
		}
		
		return dataSet;
	}
	
	
	/**
	 * Saves data to CSV file
	 * 
	 * @param data	Data entries
	 */
	public static void saveData(DataEntry[] data) {	
		String csvPath = "output/" + csvName;
		File csvFile = new File(csvPath);
		
		if (csvFile.exists()) {
			updateCSV(data, csvFile);
		}
		else{
			createCSV(csvFile, data);
		}
	}

	
	/**
	 * Creates new CSV file and save data entries in it
	 * 
	 * @param csvFile	File path
	 * @param data		Data entries to be saved
	 */
	public static void createCSV(File csvFile, DataEntry[] data) {
		System.out.println("Creating " + csvFile.getName() + "...");
		csvFile.getParentFile().mkdirs();
		
		writeToCSV(csvFile, data, false);
	}
	
	
	/**
	 * Updates CSV file by writing new entries
	 * 
	 * @param data		New data
	 * @param csvFile	File path to CSV file
	 */
	public static void updateCSV(DataEntry[] data, File csvFile) {		
		System.out.println(csvName + " found. Updating...");		
		ArrayList<DataEntry> csvData = new ArrayList<DataEntry>();
		
		csvData = parseCSV(csvFile);		
		addNewData(csvFile, data, csvData);
	}
	
	
	/**
	 * Parses CSV file
	 * 
	 * @param csvFile	File path to CSV file
	 * @return Data entries extracted from the CSV file
	 */
	public static ArrayList<DataEntry> parseCSV(File csvFile){
		ArrayList<DataEntry> data = new ArrayList<DataEntry>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			
			// Stop if header does not match with preset
			if (!(br.readLine().equals(CSV_HEADER))) {
				br.close();
				return null;
			}
			
			String line = "";
			DataEntry entry;

			while((line = br.readLine()) != null) {
				String[] entryData = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				
				if (entryData.length == 5) {
					entry = new DataEntry();
					
					entry.sourceName = entryData[0].substring(1, entryData[0].length()-1);
					entry.author = entryData[1].substring(1, entryData[1].length()-1);
					entry.title = entryData[2].substring(1, entryData[2].length()-1);
					entry.url = entryData[3].substring(1, entryData[3].length()-1);
					entry.publishedAt = entryData[4].substring(1, entryData[4].length()-1);
					
					data.add(entry);		
				}
				else {
					System.out.println("Error: Wrong number of columns found in " + csvName);
					break;
				}
			}
			
			br.close();
		}
		catch(Exception e) {
			System.out.println("CSV parse error: " + e);
		}

		return data;
	}
	
	
	/**
	 * Finds which entries are new and update CSV
	 * 
	 * @param csvFile	File path to CSV
	 * @param data		Data from server
	 * @param csvData	Data from CSV file
	 */
	public static void addNewData(File csvFile, DataEntry[] data, ArrayList<DataEntry> csvData) {
		
		for (int i = 0; i < data.length; i++) {
			if (csvData.contains(data[i])) {
				data[i] = null;
			}
		}
		
		writeToCSV(csvFile, data, true);
	}


	/**
	 * Adds DataEntry instances to the CSV file.
	 * 
	 * @param csvFile	File path 
	 * @param data		Data to be written to the file
	 * @param update	True if need to update existing file. False otherwise.
	 */
	public static void writeToCSV(File csvFile, DataEntry[] data, Boolean update) {
		try {			
			Integer addCounter = 0;	
			FileWriter writer = new FileWriter(csvFile, update);
			
			if (!update) {
				writer.append(CSV_HEADER.toString());
				writer.append("\n");
			}
			
			for(int i = 0; i < data.length; i++) {
				
				if (data[i] != null) {
					
					writer.append(data[i].getCSVSourceName());
					writer.append(",");
					
					writer.append(data[i].getCSVAuthor());
					writer.append(",");
					
					writer.append(data[i].getCSVTitle());
					writer.append(",");

					writer.append(data[i].getCSVUrl());
					writer.append(",");

					writer.append(data[i].getCSVPublishedAt());
					writer.append("\n");
					
					addCounter++;
				}
			}
			
			writer.flush();
			writer.close();
			System.out.println(Integer.toString(addCounter) + " entries added.");
			
		} catch (IOException e) {
			// Print error
			System.out.println("CSV Write Error: " + e);
		}
	}
	

}
