package steel.ai.danbi.module;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 사전 클래스
 * 
 * @author jinhoo.jang
 * @since 2018.06.05
 */
public class Dictionary {
	
	/*public Dictionary() {
		// 형태소 사전 세팅
		this.setMorpheme(morphemeMap, "C:/Project/AI/SINABRO/02.Data/00.Dictionary/morpheme.dic");
	}*/
	
	
	/**
	 * 사전 세팅
	 * @param fullPath
	 */
	public HashMap<String, String> setMorpheme(String fullPath) {
		File file = new File(fullPath);
		HashMap<String, String> map = new HashMap<String, String> ();
		
		// read file
		try {
			System.out.println(file.getAbsoluteFile() + " reading...");
			BufferedReader inFiles
				= new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0 && line.indexOf("//") == -1) {
					String[] value = line.trim().split(",");
					map.put(value[0], value[1]);
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	
	/**
	 * 사전 세팅
	 * 
	 * @param fullPath
	 * @param tagDic
	 */
	public void setDictionary(String fullPath, 
								Map<String, Set<String>> tagDic, 
								Map<String, String> synDictionary, 
								String tag) {
		
		try {
			File file = new File(fullPath);
			BufferedReader inFiles = new BufferedReader(new InputStreamReader(
									new FileInputStream(file.getAbsolutePath()), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0 && line.indexOf("//") == -1) {
					String[] value = line.toLowerCase().split(",");
					
					// 동의어 존재시
					if(value.length > 1) {
						for(int i = 0; i < value.length-1; i++) {
							/*
							 * if(synDictionary.containsKey(value[i+1])) { System.out.println(value[i+1] +
							 * " duplicated. -> " + synDictionary.get(value[i+1])); } else {
							 * synDictionary.put(value[i+1], value[0]); }
							 */
							synDictionary.put(value[i+1], value[0]);
						}
					} 
					
					// 태그는 기본적으로 대표값만 세팅한다 (동의어 세팅 안함)
					Set<String> tagSet = new HashSet<> ();
					if(tagDic.containsKey(value[0])) {
						tagSet.addAll(tagDic.get(value[0]));
					}
					tagSet.add(tag);
					tagDic.put(value[0], tagSet);
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
	
	/**
	 * 개체명 세팅
	 * 
	 * @param fullPath
	 * @param tagDic
	 */
	public void setNerDictionary(String fullPath, 
								Map<String, Set<String>> tagDic, 
								Map<String, Set<String>> nerDic,
								Map<String, String> nerSynDic, 
								String tag) {
		
		try {
			File file = new File(fullPath);
			BufferedReader inFiles	= new BufferedReader(new InputStreamReader(
									new FileInputStream(file.getAbsolutePath()), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0 && line.indexOf("//") == -1) {
					String[] value = line.toLowerCase().trim().split(",");
					
					// 동의어 존재시 ner 동의어 사전에 처리
					if(value.length > 1) {
						for(int i = 0; i < value.length-1; i++) {
							nerSynDic.put(value[i+1], value[0]);
						}
					} 
					
					// NER 데이터는 기본적으로, 기본품사는 NN으로 세팅
					Set<String> tagSet = new HashSet<> ();
					if(tagDic.containsKey(value[0])) {
						tagSet.addAll(tagDic.get(value[0]));
					}
					tagSet.add("NN");							
					tagDic.put(value[0], tagSet);
					
					Set<String> nerTagSet = new HashSet<> ();
					if(nerDic.containsKey(value[0])) {
						nerTagSet.addAll(nerDic.get(value[0]));
					}
					nerTagSet.add(tag.toUpperCase());							
					nerDic.put(value[0], nerTagSet);
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 사전 세팅
	 * 
	 * @param fullPath
	 * @param tagDic
	 */
	public void setLastTagDictionary(String fullPath, Map<String, String> tagDic, String tag) {
		File file = new File(fullPath);
		//HashMap<String, String> map = new HashMap<String, String> ();
		
		// read file
		try {
			//System.out.println(file.getAbsoluteFile() + " reading...");
			BufferedReader inFiles
				= new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0 && line.indexOf("//") == -1) {
					tagDic.put(line.trim(), tag);
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 사전의 데이터를 읽어와서 리턴
	 * 
	 * @param personality
	 * @param type
	 * @return
	 */
	public List<String> list(String filePath, String personality, String type, int page, int size) {
		 List<String> list = new ArrayList<String> ();
		 int cnt = 0;
		 int offset = 0;
		 
		 if(page > 0)
			 offset = page * size; 
		 		 
		 try {
				System.out.println(filePath + " reading...");
				BufferedReader inFiles
					= new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
				
				String line = "";
				while((line = inFiles.readLine()) != null) {
					if(line.trim().length() > 0 && line.indexOf("//") == -1) {
						if(size > 0 && list.size() >= size)
							break;
						
						if(cnt >= offset)
							list.add(line.toLowerCase().trim());
						cnt++;
					}
				}
				
				inFiles.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		 return list;
	}
	
	
	/**
	 * 키워드 어레이로 사전을 저장한다
	 * 
	 * @param kwdArr
	 * @param filePath
	 * @return
	 */
	public boolean saveDictionary(String[] kwdArr, String filePath) {
		BufferedWriter bw;		
		String NewLine = System.getProperty("line.separator");
		
		try {   
			bw = new BufferedWriter(
					new OutputStreamWriter(
						new FileOutputStream(
						filePath, false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			// 종료
			for(String kwd : kwdArr) {
				if(kwd.trim().length() > 0)
					bw.write(kwd + NewLine);
			}
			
			bw.close();
		}catch(IOException e){
			System.err.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 유저별 데이터를 처리하고, 금일 총 수정 개수를 리턴한다
	 * 
	 * @param editList
	 * @param type
	 * @param personality
	 * @param user
	 * @return
	 */
	public HashMap<String, Integer> saveLogByUser(List<String> editList, String type, String personality, String filePath, String name) {
		StringBuffer sb = new StringBuffer();
		String NEWLINE = System.getProperty("line.separator");
		
		// 우선 파일을 읽는다
		SimpleDateFormat todayFormat = new SimpleDateFormat("yyyyMMdd");
		Date today = new Date();
		String fullPath = filePath + todayFormat.format(today) + "_" + name + ".log";
		File f = new File(fullPath);
		
		System.out.println("로그저장위치 : " + fullPath);
		// 파일이 존재하지 않으면, 추가한다.
		if(!f.exists()) {
			if(type.equals("S") || type.equals("M")) {
				sb.append("<" + type + getCharPersonality(personality) + ">");
			} else {
				sb.append("<" + type + ">");
			}
			for(int i = 0; i < editList.size(); i++) {
				if(i > 0)
					sb.append(",");
				sb.append(editList.get(i));
			}
			sb.append(NEWLINE);			
		}
		// 파일이 존재하면, 우선 읽어들인다.
		else {
			HashMap<String, HashMap<String, String>> saveMap = getSaveLog(fullPath);
			sb = mergeEditData(saveMap, editList, type, getCharPersonality(personality));
		}
		
		// 파일을 저장한다
		if(sb.length() > 0) {
			BufferedWriter bw;
			try {   
				bw = new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
								fullPath, false),	// true to append 
								StandardCharsets.UTF_8));	// set encoding utf-8
				
				bw.write(sb.toString());
				bw.close();
			}catch(IOException e){
				System.err.println(e.getMessage());
			}
		}
		
		// 처리한 개수를 리턴한다
		return logToCntMap(fullPath);
	}
	
	
	/**
	 * 저장된 로그 파일을 읽어온다
	 * 
	 * @param fullPath
	 * @return
	 */
	public HashMap<String, HashMap<String, String>> getSaveLog(String fullPath) {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>> ();
		try {
			System.out.println(fullPath + " reading...");
			BufferedReader inFiles
					= new BufferedReader(new InputStreamReader(new FileInputStream(fullPath), "UTF8"));
		
			String line = "";
			HashMap<String, String> map;
			String[] temp;
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0) {
					map = new HashMap<String, String> ();
					// 수동 및 금칙어 사전
					if(line.indexOf("<M") > -1 || line.indexOf("<S") > -1) {
						System.out.println(line.substring(1,3) + " " + line.substring(4));
						temp = line.substring(4).split(",");
						for(String kwd : temp)							
							map.put(kwd, "");
						
						result.put(line.substring(1,3), map);
					} 
					// 공통 금칙어 사전
					else if(line.indexOf("<C>") > -1) {
						System.out.println(line.substring(1,2) + " " + line.substring(3));
						temp = line.substring(3).split(",");
						for(String kwd : temp)							
							map.put(kwd, "");
						
						result.put(line.substring(1,2), map);
					}
					
				}
			}
				
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("get save log size : " + result.size());
		return result;
	}
	
	
	/**
	 * 세이브 로그와 수정된 edit log를 병합한다
	 * 
	 * @param saveMap
	 * @param editList
	 * @param type
	 * @param personality
	 * @return
	 */
	public StringBuffer mergeEditData(HashMap<String, HashMap<String, String>> saveMap, 
			List<String> editList, String type, String personality) {
		String NEWLINE = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		HashMap<String, String> kwdMap;
		
		String key = type.toUpperCase();
		if(type.equals("S") || type.equals("M"))
			key += personality;
		
		// save map에 있으면 병합
		if(saveMap.containsKey(key)) {
			kwdMap = saveMap.get(key);
		} else {
			kwdMap = new HashMap<String, String> ();
		}
		
		for(String editKwd : editList) {
			kwdMap.put(editKwd.trim(), "");				
		}
		saveMap.put(key, kwdMap);
		
		// shape string
		for(String dicType : saveMap.keySet()) {
			boolean flag = false;
			kwdMap = saveMap.get(dicType);
			sb.append("<" + dicType + ">");
			
			for(String kwd : kwdMap.keySet()) {
				if(flag)
					sb.append(",");
				sb.append(kwd);
				flag = true;
			}
			sb.append(NEWLINE);
		}
		
		System.out.println("[" + sb.toString() + "]");
		return sb;
	}
	
	
	/**
	 * 최종 로그를 맵으로 리턴
	 * 
	 * @param logStr
	 * @return
	 */
	public HashMap<String, Integer> logToCntMap(String fullPath) {
		HashMap<String, Integer> rtnMap = new HashMap<String, Integer> ();
		
		try {
			System.out.println(fullPath + " reading...");
			BufferedReader inFiles
					= new BufferedReader(new InputStreamReader(new FileInputStream(fullPath), "UTF8"));		
			String line = "";			
			
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0) {
					// 수동 및 금칙어 사전
					if(line.indexOf("<M") > -1 || line.indexOf("<S") > -1) {
						rtnMap.put(line.substring(1,3), line.substring(4).split(",").length);
					} 
					// 공통 금칙어 사전
					else if(line.indexOf("<C>") > -1) {
						rtnMap.put(line.substring(1,2), line.substring(3).split(",").length);
					}					
				}
			}
				
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rtnMap;
	}
	
	
	
	
	
	/**
	 * 분류값을 약어로 리턴(R~C)
	 * 
	 * @param personality
	 * @return
	 */
	public String getCharPersonality(String personality) {
		// 현실형
		if(personality.equalsIgnoreCase("engin") 
				|| personality.equalsIgnoreCase("realistic")) {
			return "R";
		} 
		// 탐구형
		else if(personality.equalsIgnoreCase("rnd") 
				|| personality.equalsIgnoreCase("investigative")) {
			return "I";
		} 
		// 예술형
		else if(personality.equalsIgnoreCase("art") 
				|| personality.equalsIgnoreCase("artistic")) {
			return "A";
		} 
		// 사회형
		else if(personality.equalsIgnoreCase("society") 
				|| personality.equalsIgnoreCase("social")) {
			return "S";
		} 
		// 진취형
		else if(personality.equalsIgnoreCase("leader") 
				|| personality.equalsIgnoreCase("enterprising")) {
			return "E";
		} 
		// 관습형
		else if(personality.equalsIgnoreCase("desk") 
				|| personality.equalsIgnoreCase("conventional")) {
			return "C";
		}
		
		return "";
	}
}