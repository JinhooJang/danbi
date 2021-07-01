package steel.ai.danbi.module;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 이름을 추출하는 모듈
 * 
 * @author jinhoo.jang
 * @since 2020.01.16
 */
public class ExtractName {
	private String dicPath;
	private HashMap<String, String> lastNmMap;
	private HashMap<String, String> nmMap;
	private HashMap<String, String> nickMap;

	
	/**
	 * 생성자
	 */
	public ExtractName(String dicPath) {
		this.dicPath = dicPath;
		
		setMap("lastNm", lastNmMap);	// 성을 로드한다
		setMap("nm", nmMap);			// 이름을 로드한다
		setMap("nick", nickMap);		// 별명을 로드한다
	}	
	
	public void setMap(String name, HashMap<String, String> map) {
		map = new HashMap<String, String> ();
		String fullPath = dicPath + name;
		
		try {
			System.out.println(fullPath + " reading...");
			BufferedReader inFiles
				= new BufferedReader(
					new InputStreamReader(new FileInputStream(fullPath), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0 && line.indexOf("//") == -1) {
					String[] values = line.toLowerCase().trim().split(",");
					ArrayList<String> list = null;
					
					// 단어를 사전에 추가한다
					
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 해당 센텐스에 이름이 있을 확률을 체크 한 후, 이름 가능성이 있을 경우 TRUE 리턴  
	 * @param sentence
	 */
	public boolean chkName(String sentence) {
		
		return false;
	}
	
}
