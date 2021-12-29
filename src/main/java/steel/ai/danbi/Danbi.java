package steel.ai.danbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import steel.ai.danbi.module.Dictionary;
import steel.ai.danbi.module.PosTagger;
import steel.ai.danbi.vo.DanbiConfigVO;
import steel.ai.danbi.vo.MorphemeVO;


/**
 * 단비 API 클래스
 * 
 * @author jinhoo.jang
 * @since 2021.07.01
 * 
 * @version 1.0.0 
 */
public class Danbi {
	
	protected String NEWLINE = System.getProperty("line.separator");
	
	private DanbiConfigVO CONFIG;
	
	protected Map<String, String> morphemeDic;			// 기분석된 형태소 사전
	protected Map<String, Set<String>> nerDictionary;	// 개체명 사전
	protected Map<String, String> nerSynDictionary;		// 개체명 동의어 사전
	protected Map<String, String> vvSynDictionary;		// 동사 동의어 사전
	protected Map<String, Set<String>> tagDictionary;	// 태그 통합
	protected Map<String, String> synDictionary;		// 대표어 사전
	protected Map<String, String> snDictionary;			// 숫자를 위한 사전
	
	// 종성 ㄱㄲㄳㄴㄵㄶㄷㄹㄺ ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ ㅁ ㅂ ㅄ ㅅ ㅆ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ
    private static final char[] JON = 
			    {0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 
			        0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 
			        0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 
			        0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 
			        0x314c, 0x314d, 0x314e};
    
    PosTagger posTagger;
	
	
    /**
     * 생성자
     * 
     * @param DICPATH
     * @throws Exception
     */
	public Danbi(DanbiConfigVO CONFIG) throws Exception {
		this.CONFIG = CONFIG;
		this.reloadDictionary();
		
		posTagger = new PosTagger(
				CONFIG,
				morphemeDic, 
				tagDictionary,
				nerDictionary, 
				vvSynDictionary,
				nerSynDictionary, 
				synDictionary,
				snDictionary
		);
	}
	
	
	/**
	 * 사전 재기동
	 */
	public boolean reloadDictionary() {
		try {
			Dictionary dictionary = new Dictionary();
			tagDictionary = new HashMap<> ();
			nerDictionary = new HashMap<> ();
			nerSynDictionary = new HashMap<> ();
			snDictionary = new HashMap<> ();
			synDictionary = new HashMap<> ();
			vvSynDictionary = new HashMap<> ();
			
			morphemeDic = dictionary.setMorpheme(CONFIG.getDicPath() + "morpheme.dic");	// 기분석된 형태소 분석기
			
			// 한단어로 끝나는 사전 처리
			// 단어의 마지막에 붙는 태그 처리
			dictionary.setDictionary(CONFIG.getDicPath() + "em.dic", tagDictionary, synDictionary, "EM", true);	// 어미
			dictionary.setDictionary(CONFIG.getDicPath() + "xsn.dic", tagDictionary, synDictionary, "XSN", true);	// 접미사
			dictionary.setDictionary(CONFIG.getDicPath() + "js.dic", tagDictionary, synDictionary, "JS", true);	// 조사
			dictionary.setDictionary(CONFIG.getDicPath() + "nb.dic", tagDictionary, synDictionary, "NB", true);	// 의존
			dictionary.setDictionary(CONFIG.getDicPath() + "sf.dic", tagDictionary, synDictionary, "SF", true);	// 기호
			
			// 숫자를 처리하기 위한 처리
			dictionary.setDictionary(CONFIG.getDicPath() + "sn.dic", tagDictionary, synDictionary, "SN", true);
			
			dictionary.setDictionary(CONFIG.getDicPath() + "mm.dic", tagDictionary, synDictionary, "MM", true);	// 관형사
			dictionary.setDictionary(CONFIG.getDicPath() + "ic.dic", tagDictionary, synDictionary, "IC", true);	// 감탄사
			dictionary.setDictionary(CONFIG.getDicPath() + "ma.dic", tagDictionary, synDictionary, "MA", true);	// 부사
			dictionary.setDictionary(CONFIG.getDicPath() + "va.dic", tagDictionary, synDictionary, "VA", true);	// 형용사
			dictionary.setDictionary(CONFIG.getDicPath() + "vv.dic", tagDictionary, vvSynDictionary, "VV", true);	// 동사
			//dictionary.setDictionary(CONFIG.getDicPath() + "vp.dic", tagNewDictionary, synDictionary, tagDictionary, "VP");	// 긍정지정사
			//dictionary.setDictionary(CONFIG.getDicPath() + "vn.dic", tagNewDictionary, synDictionary, tagDictionary, "VN");	// 부정지정사
			
			// 아직 태깅이 안된 사전
			dictionary.setDictionary(CONFIG.getDicPath() + "uu.dic", tagDictionary, synDictionary, "UU", true);	// 미분류
			dictionary.setDictionary(CONFIG.getDicPath() + "np.dic", tagDictionary, synDictionary, "NP", true);	// 대명사
			dictionary.setDictionary(CONFIG.getDicPath() + "nn.dic", tagDictionary, synDictionary, "NN", true);	// 명사
			
			for(String ner : CONFIG.getNerList()) {
				dictionary.setNerDictionary(CONFIG.getDicPath() + "ner/" + ner + ".dic", tagDictionary, nerDictionary, nerSynDictionary, ner);
			}			
			dictionary.setDictionary(CONFIG.getDicPath() + "cn.dic", tagDictionary, synDictionary, "CN", true);	// 복합명사
			
			// 유저 사전을 사용할지 여부
			if(CONFIG.isUserYn())
				dictionary.setDictionary(CONFIG.getDicPath() + "user.dic", tagDictionary, synDictionary, "USER", true);	// 유저사전
			
			dictionary.setLastTagDictionary(CONFIG.getDicPath() + "sn.dic", snDictionary, "SN");
			//dictionary.setLastTagDictionary(DICPATH + "suf.dic", lastTagDictionary, "SF");	// 접미			
						
		} catch(Exception e) {
			System.out.println("reloadDictionary error : " + e.getMessage());
			return false;
		}
		
		// 사전 세팅 로깅
		this.showSetDictionary(CONFIG.isDebug());
		
		return true;
	}
	
	
	/**
	 * 형태소 분석 결과를 pos tagging 한 것을 list map 형태로 보여준다
	 * @return
	 */
	public List<MorphemeVO> pos(String document) {
		if(CONFIG.getCoumpoundLevel() == 0) return posTagger.posTagging(document);
		else return arrangeList(posTagger.posTagging(document));
		//return posTagger.posTagging(document);
	}
	
	
	/**
	 * 연속된 데이터를 기반으로 NER과 복합명사등을 추가 태깅
	 * 
	 * @return
	 */
	public List<MorphemeVO> arrangeList(List<MorphemeVO> posList) {
		List<MorphemeVO> rtnList = new ArrayList<> ();
		
		for(int i = 0; i < posList.size(); i++) {
			MorphemeVO posVO = posList.get(i);
			List<List<Map<String, String>>> posTagList = posVO.getPosTagList();
			List<List<String>> allTagStrList = new ArrayList<> ();
			
			for(int j = 0; j < posVO.getWordList().size(); j++) {
				String word = posVO.getWordList().get(j);
				List<Map<String, String>> posTagListByWord = posTagList.get(j);
				
				//System.out.println("[ " + posTagListByWord + " ]");
				
				// 명사가 연속으로 있을 경우, 그리고 명사를 사전으로 찾았을 때 복합명사가 있을 경우 복합명사로 변경
				for(Map<String, String> tagMap : posTagListByWord) {
					// 우선 연속적인 명사가 있고, NER 사전에 없을 경우 복합명사 처리
					boolean isNer = false;
					boolean isAllNN = true;
					StringBuffer words = new StringBuffer();
					for(String tagWord : tagMap.keySet()) {
						if(!tagMap.get(tagWord).equals("NN") && !tagMap.get(tagWord).equals("CN")) {
							isNer = false;
							isAllNN = false;
							break;
						}
						String chgNerKwd = chgNerKwd(word);
						if(nerDictionary.containsKey(chgNerKwd)) isNer = true;
						words.append(tagWord);
					}
					
					// 명사가 있고, NER이 없으며, 복합명사 사전에 있을 경우 치환
					if(!isNer && isAllNN && posTagListByWord.size() > 1 
							&& tagDictionary.containsKey(words.toString())
							&& tagDictionary.get(words.toString()).contains("CN")) {
						
						List<String> tagStrList = new ArrayList<> ();
						tagStrList.add(word + "=" + "CN");
						allTagStrList.add(tagStrList);
						break;
					} else {
						for(String tagWord : tagMap.keySet()) {
							word = tagWord;				
						
							List<String> tagStrList = new ArrayList<> ();
							StringBuffer posStr = new StringBuffer();
							String tagStr = tagMap.get(word);
							
							//System.out.println("tagStr==>" + tagStr);
							
							// tag에서 NER을 가져온다
							String chgNerKwd = chgNerKwd(word);
							if(nerDictionary.containsKey(chgNerKwd)) {
								Set<String> nerSet = nerDictionary.get(chgNerKwd);
								for(String ner : nerSet) {
									if(posStr.length() > 0) posStr.append(",");
									posStr.append("NER-" + ner);				
								}
								
								tagMap.put(word, tagStr + "," + posStr.toString());	
							}
							
							tagStrList.add(word + "=" + tagMap.get(word));
							allTagStrList.add(tagStrList);
						}
					}
				}				
			}
			
			posVO.setTagStrList(allTagStrList);			
			rtnList.add(posVO);
		}
		
		return rtnList;
	}
	
	
	/**
	 * 사전 세팅 현황을 보여준다
	 */
	public void showSetDictionary(boolean debug) {
		if(!debug) return;
		
		Map<String, Integer> tagCount = new LinkedHashMap<> ();
		tagCount.put("hashing", morphemeDic.size());
		tagCount.put("SN", snDictionary.size());
		
		for(String kwd : tagDictionary.keySet()) {
			Set<String> tagSet = tagDictionary.get(kwd);
			
			for(String tag : tagSet) {
				int count = 1;
				if(tagCount.containsKey(tag)) {
					count += tagCount.get(tag);
				}
				tagCount.put(tag, count);
			}
		}
		
		for(String kwd : nerDictionary.keySet()) {
			Set<String> tagSet = nerDictionary.get(kwd);
			
			for(String tag : tagSet) {
				int count = 1;
				if(tagCount.containsKey("NER-" + tag)) {
					count += tagCount.get("NER-" + tag);
				}
				tagCount.put("NER-" + tag, count);
			}
		}
		
		for(String tag : tagCount.keySet()) {
			System.out.println(tag + "->" + tagCount.get(tag));
		}
	}
	
	
	/**
	 * NER 대표어가 있을 경우 치환
	 * @param kwd
	 * @return
	 */
	public String chgNerKwd(String kwd) {
		if(nerSynDictionary.containsKey(kwd)) return nerSynDictionary.get(kwd);
		return kwd;
	}
}
