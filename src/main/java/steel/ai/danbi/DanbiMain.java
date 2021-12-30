package steel.ai.danbi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import steel.ai.danbi.vo.DanbiConfigVO;
import steel.ai.danbi.vo.MorphemeVO;


/**
 * 단비 형태소 분석기
 * 
 * @author steel
 * @since 2020.12.14
 */
public class DanbiMain {
	
	public void test() {
		DanbiConfigVO vo = new DanbiConfigVO ();
		
		// 사전이 위치한 경로
		vo.setDicPath("c:/Project/steel/database/dictionary/");
		// 사용할 NER 리스트 
		vo.setNerList("SCH,JOB,SKLC,SKLS,MAJ,TSK,NAT,LOC,LIC,MTR,CMP,SYN,BRD".split(","));
		// 사용자 사전을 사용할 것인지
		vo.setUserYn(true);
		// debug 모드 사용 여부
		vo.setDebug(false);
		vo.setRepresentative(false);
		vo.setCoumpoundLevel(1);
		// compound level 0(무리해서 추출 안함), 1(복합명사가 있을 경우 명사 연결 치환), 2(복합명사와 명사 둘다 뽑는다)
		/*String text = "우아하게 보여요";
		Map<String, String> testMap = new HashMap<> ();
		
		try {
			Danbi danbi = new Danbi(vo);
			List<MorphemeVO> danbiResult = danbi.pos(text);
			
			for(MorphemeVO morphVO : danbiResult) {
				for(int i = 0; i < morphVO.getWordList().size(); i++) {
					System.out.println(morphVO.getWordList().get(i) + "->" + morphVO.getPosTagList().get(i));
				}
			}
			//System.out.println(danbi.pos(text));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/
		
		try {
			Danbi danbi = new Danbi(vo);
			// jarvis 데이터 가져오기
			List<String> sentences = getTestData();
			//Collections.shuffle(sentences);
			System.out.println("sentences->" + sentences.size());
			Thread.sleep(5000);
			List<Map<String, String>> tagAll = new ArrayList<> ();
			int loop = 0;
			long startTime = System.currentTimeMillis();
			for(String sentence : sentences) {
				List<MorphemeVO> danbiResult = danbi.pos(sentence);
				for(MorphemeVO morphVO : danbiResult) {
					/*for(int i = 0; i < morphVO.getWordList().size(); i++) {
						//System.out.println(morphVO.getWordList().get(i) + "->" + morphVO.getPosTagList().get(i));						
					}*/
					System.out.println(sentence + "->" + morphVO.getTagStrList());
					//System.out.println(sentence + "->" + morphVO.getPosTagList());
				}
				
				//danbi.pos(sentence);
				if(++loop % 100 == 0) System.out.println("execute sentence => " + loop);
				Thread.sleep(5000);
			}
			long endTime = System.currentTimeMillis();
			//System.out.println(tagAll);
			findUkCount(tagAll, 5);
			//System.out.println(danbi.pos(text));
			System.out.println(endTime - startTime);
						
			//System.out.println(danbi.pos(text));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main( String[] args ) {
        DanbiMain danbi = new DanbiMain();
        danbi.test();        
    }
	
	
	public void findUkCount(List<Map<String, String>> tagAll, int showCount) {
		Map<String, Integer> ukCountMap = new HashMap<> ();
		for(Map<String, String> map : tagAll) {
			for(String key : map.keySet()) {
				if(map.get(key).equalsIgnoreCase("UK")) {
					int cnt = 1;
					if(ukCountMap.containsKey(key)) {
						cnt += ukCountMap.get(key);
					}
					ukCountMap.put(key, cnt);
				}
			}			
		}
		
		for(String key : ukCountMap.keySet()) {
			if(ukCountMap.get(key) >= showCount) {
				System.out.println(key + "," + ukCountMap.get(key));
			}
		}
	}
	
	
	public List<String> getTestData() {
		List<String> sentences = new ArrayList<> ();
		String line = "";
		// read file
		try {
			BufferedReader inFiles
				//= new BufferedReader(new InputStreamReader(new FileInputStream("c:/project/steel/database/raw-data/saibog/jarvis.txt"), "UTF8"));
				//= new BufferedReader(new InputStreamReader(new FileInputStream("D:/project/database/recruits_title.csv"), "UTF8"));
				//= new BufferedReader(new InputStreamReader(new FileInputStream("D:/project/database/jumpit_title.txt"), "UTF8"));
				= new BufferedReader(new InputStreamReader(new FileInputStream("c:/project/database/morph_test.txt"), "UTF8"));
			
			int count = 0;
			while((line = inFiles.readLine()) != null) {
				/*if(line.trim().length() > 0 && count++ > 0) {
					String[] temp = line.split(",");
					if(temp != null && temp.length == 2) {
						sentences.add(temp[1]);
					}
				}*/
				if(line.trim().length() > 0) sentences.add(line.trim());
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(line);
		}
		
		return sentences;
	}
}
