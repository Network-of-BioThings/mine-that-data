package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

import net.minidev.json.JSONObject;
import test.util.CSVIterator;

public class Csv2JsonConverter {
	private boolean hasHeader = false;
	private boolean hasID = false;
	private long idCounter = 1;
	private boolean writeMetadata = false;
	private int idPos = -1;
	String[]headers;
	String indexName = null;
	String typeName = null;
	String indexAction = "index";
	
	CSVIterator csvIterator;
	
	public Csv2JsonConverter(String filename, String indexAction, String indexName, String typeName, String[]headers, boolean writeMetadata, int idPos, char delimiter, char quoteCharacter) throws Exception{
		this.csvIterator = new CSVIterator(new BufferedReader(new FileReader(filename)), delimiter, quoteCharacter);
		this.hasID = idPos < 0;
		this.idPos = idPos;
		this.hasHeader = headers != null;
		this.headers = headers;
		this.indexName = indexName;
		this.typeName = typeName;
		this.writeMetadata = writeMetadata;
		this.indexAction = indexAction;
	}
	
	public void convert(String outputJsonFilename) throws Exception{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outputJsonFilename));
		} catch (Exception e) {
			return;
			// TODO: handle exception
		}
		if(headers == null){
			headers = this.csvIterator.next();
		}
		String[]nextLine;
		JSONObject metadata, indexMetadata, data;
		while(this.csvIterator.hasNext()){
			nextLine = this.csvIterator.next();
			if(nextLine.length != headers.length){
				throw new Exception("line length different from header length: \n"+Arrays.toString(nextLine)+"\n"+Arrays.toString(headers));
			}
			if(writeMetadata){
				metadata = new JSONObject();
				indexMetadata = new JSONObject();
				if(indexName != null){
					indexMetadata.put("_index", indexName);
				}
				if(typeName != null){
					indexMetadata.put("_type", typeName);
				}
				if(hasID){
					indexMetadata.put("_id", nextLine[idPos]);
				} else {
					indexMetadata.put("_id", Long.toString(this.idCounter));
					this.idCounter++;
				}
				metadata.put(this.indexAction, indexMetadata);
				writer.write(metadata.toJSONString());
				writer.write("\n");
			}
			data = new JSONObject();
			for (int i = 0; i < nextLine.length; i++) {
				data.put(headers[i], nextLine[i]);
			}
			writer.write(data.toJSONString());
			writer.write("\n");
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		String path = "";
		String csvFilename = path + "";
		String indexAction = "index";
		String indexName = "";
		String typeName = "";
		String[]headers = null;//{"",""};
		boolean writeMetadata = true;
		int idPos = 1;//???
		char delimiter = ',';
		char quoteCharacter = '"';

		String outputFilename = path + "";
		
		Csv2JsonConverter converter = new Csv2JsonConverter(csvFilename, indexAction, indexName, typeName, headers, writeMetadata, idPos, delimiter, quoteCharacter);
		converter.convert(outputFilename);
	}
	
}
