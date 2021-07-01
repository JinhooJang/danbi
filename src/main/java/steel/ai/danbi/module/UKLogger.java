package steel.ai.danbi.module;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class UKLogger {

	
	public void ukLogger(String text) {
		BufferedWriter bw = null;
		String filePath = "uk_logger.log";
		
    	try {
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(filePath, true),	// true to append 
					StandardCharsets.UTF_8));	// set encoding utf-8
			
			bw.write(text);
			bw.newLine();
			bw.close();
		} catch(Exception e){
			System.out.println("ukLogger error : " + e.getMessage());
		}
	}
}
