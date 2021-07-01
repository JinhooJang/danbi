package steel.ai.danbi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import steel.ai.danbi.vo.DanbiConfigVO;

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
		vo.setDicPath("C:/Project/steel/database/dictionary/");
		// 사용할 NER 리스트 
		vo.setNerList("SCH,JOB,SKLC,SKLS,MAJ,TSK,NAT,LOC,LIC,MTR,SYN".split(","));
		// 사용자 사전을 사용할 것인지
		vo.setUserYn(true);
		// debug 모드 사용 여부
		vo.setDebug(false);
		vo.setRepresentative(false);
		vo.setCoumpoundLevel(0);
		// compound level 0(무리해서 추출 안함), 1(복합명사가 있을 경우 명사 연결 치환), 2(복합명사와 명사 둘다 뽑는다)
		
		String text = "이를 어설프게 검토한다면 사양 변경 승인이 어려울 것으로 판단했습니다";
		
		try {
			Danbi danbi = new Danbi(vo);
			// jarvis 데이터 가져오기
			//List<String> sentences = getTestData();
			//List<Map<String, String>> tagAll = new ArrayList<> ();
			//for(String sentence : sentences) {
			//	tagAll.addAll(danbi.pos(sentence));				
			//}
			System.out.println(danbi.pos(text));
			//System.out.println(tagAll);
			//findUkCount(tagAll, 5);
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
				System.out.println(key + "->" + ukCountMap.get(key));
			}
		}
	}
	
	
	public List<String> getTestData() {
		List<String> sentences = new ArrayList<> ();
		// read file
		try {
			BufferedReader inFiles
				//= new BufferedReader(new InputStreamReader(new FileInputStream("c:/project/steel/database/raw-data/saibog/jarvis.txt"), "UTF8"));
			= new BufferedReader(new InputStreamReader(new FileInputStream("c:/project/steel/ytn_news.txt"), "UTF8"));
			
			String line = "";
			while((line = inFiles.readLine()) != null) {
				if(line.trim().length() > 0) {
					sentences.add(line.trim());
				}
			}
			
			inFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sentences;
	}
}
