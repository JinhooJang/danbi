package steel.ai.danbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import steel.ai.danbi.module.Dictionary;
import steel.ai.danbi.module.Konlp;
import steel.ai.danbi.module.KonlpNew;
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
	
	private KonlpNew ANALYZE;
	private DanbiConfigVO CONFIG;
	
	// 기분석된 형태소 사전
	protected Map<String, String> morphemeDic;
	// 개체명 사전
	protected Map<String, Set<String>> nerDictionary;
	// 개체명 동의어 사전
	protected Map<String, String> nerSynDictionary;
	// 태그 통합
	protected Map<String, Set<String>> tagDictionary;
	// 대표어 사전
	protected Map<String, String> synDictionary;
	// 숫자를 위한 사전
	protected Map<String, String> snDictionary;
	
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
		
		/**ANALYZE = new KonlpNew(
				morphemeDic, 
				tagDictionary,
				nerDictionary, 
				nerSynDictionary, 
				synDictionary
		);*/
		
		posTagger = new PosTagger(
				CONFIG,
				morphemeDic, 
				tagDictionary,
				nerDictionary, 
				nerSynDictionary, 
				synDictionary,
				snDictionary);
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
			
			morphemeDic = dictionary.setMorpheme(CONFIG.getDicPath() + "morpheme.dic");	// 기분석된 형태소 분석기
			
			// 한단어로 끝나는 사전 처리
			// 단어의 마지막에 붙는 태그 처리
			dictionary.setDictionary(CONFIG.getDicPath() + "em.dic", tagDictionary, synDictionary, "EM");	// 어미
			dictionary.setDictionary(CONFIG.getDicPath() + "js.dic", tagDictionary, synDictionary, "JS");	// 조사
			dictionary.setDictionary(CONFIG.getDicPath() + "nb.dic", tagDictionary, synDictionary, "NB");	// 의존
			dictionary.setDictionary(CONFIG.getDicPath() + "sf.dic", tagDictionary, synDictionary, "SF");	// 기호
			
			// 숫자를 처리하기 위한 처리
			dictionary.setDictionary(CONFIG.getDicPath() + "sn.dic", tagDictionary, synDictionary, "SN");
			
			dictionary.setDictionary(CONFIG.getDicPath() + "mm.dic", tagDictionary, synDictionary, "MM");	// 관형사
			dictionary.setDictionary(CONFIG.getDicPath() + "ic.dic", tagDictionary, synDictionary, "IC");	// 감탄사
			dictionary.setDictionary(CONFIG.getDicPath() + "ma.dic", tagDictionary, synDictionary, "MA");	// 부사
			dictionary.setDictionary(CONFIG.getDicPath() + "va.dic", tagDictionary, synDictionary, "VA");	// 형용사
			dictionary.setDictionary(CONFIG.getDicPath() + "vv.dic", tagDictionary, synDictionary, "VV");	// 동사
			//dictionary.setDictionary(CONFIG.getDicPath() + "vp.dic", tagNewDictionary, synDictionary, tagDictionary, "VP");	// 긍정지정사
			//dictionary.setDictionary(CONFIG.getDicPath() + "vn.dic", tagNewDictionary, synDictionary, tagDictionary, "VN");	// 부정지정사
			
			
			
			// 아직 태깅이 안된 사전
			dictionary.setDictionary(CONFIG.getDicPath() + "uu.dic", tagDictionary, synDictionary, "UU");	// 미분류
			dictionary.setDictionary(CONFIG.getDicPath() + "np.dic", tagDictionary, synDictionary, "NP");	// 대명사
			dictionary.setDictionary(CONFIG.getDicPath() + "nn.dic", tagDictionary, synDictionary, "NN");	// 명사
			
			for(String ner : CONFIG.getNerList()) {
				dictionary.setNerDictionary(CONFIG.getDicPath() + "ner/" + ner + ".dic", tagDictionary, nerDictionary, nerSynDictionary, ner);
			}			
			dictionary.setDictionary(CONFIG.getDicPath() + "cn.dic", tagDictionary, synDictionary, "CN");	// 복합명사
			
			// 유저 사전을 사용할지 여부
			if(CONFIG.isUserYn())
				dictionary.setDictionary(CONFIG.getDicPath() + "user.dic", tagDictionary, synDictionary, "USER");	// 유저사전
			
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
	 * 형태소 분석
	 */
	/* public List<HashMap<String, MorphemeVO>> morphemeAnalyze(String document) {
		if(document == null || document.trim().length() == 0) return null;
		
		//LOGGER.debug("document : " + document);
		List<HashMap<String, MorphemeVO>> morphemeMap = new ArrayList<HashMap<String, MorphemeVO>> ();
		List<HashMap<String, MorphemeVO>> sentenceMorphMap = null;
		
		// 구분자 특수문자는 공백으로 변경
		document = document.replaceAll(",", " ")
				.replaceAll("/", " ")
				.replaceAll("\\|", " ")
				.replaceAll("·", " ");				
		
		
		// 개행은 마침표로 변경
		document = document.trim()
				.replaceAll("\n", ".")
				.replaceAll("\r", ".");
		
		String[] sentences = document.split("\\.");
		
		//System.out.println(sentences.length + " sentences.");
		String sentence = "";
		
		// 문장별로 형태소 분석 수행
		for(int i = 0; i < sentences.length; i++) {
			sentence = sentences[i].toLowerCase().trim();
			if(sentence.length() == 0) continue;
			sentenceMorphMap = new ArrayList<HashMap<String, MorphemeVO>> ();
			
			// 문장을 다시, 단어로 자른다
			String[] word = sentence.trim().split(" ");
			for(int j = 0; j < word.length; j++) {
				//System.out.println(word[j].trim());
				if(word[j].trim().length() > 0 && word[j].trim().length() < 15) {				
					HashMap<String, MorphemeVO> map = new HashMap<String, MorphemeVO> ();
					MorphemeVO morphVO = new MorphemeVO ();
					morphVO.setWord(word[j]);
					ANALYZE.wordAnalyze(word[j], morphVO, false);
					
					// 2019.10.25 UK가 있을 경우, 모두 UK로 변환
					if(morphVO.getTag().contains("UK")) {
						morphVO = ANALYZE.setUKMorph(word[j]);
					}
					
					map.put(word[j], morphVO);
					sentenceMorphMap.add(map);
				}
			}
			//morphemeList.addAll(wordAnalyze(word[i]));
			
			// 한칸씩 띄어져 있는 단어들을 분석하여 복합명사일 경우 합친다
			morphemeMap.addAll(sentenceAnalyze(sentenceMorphMap));
		}
		
		return morphemeMap;
	} */
	
	
	/**
	 * 문장의 의미를 분석하여, 형태소를 재조립한다
	 * 
	 * @param sentenceMorphMap
	 * @return
	 */
	/* public List<HashMap<String, MorphemeVO>> 
				sentenceAnalyze(List<HashMap<String, MorphemeVO>> sentenceMorphMap) {
		
		List<HashMap<String, MorphemeVO>> rtnList = new ArrayList<HashMap<String, MorphemeVO>>();
		
		// 1차. 복합명사로 변경. 2018.06.26
		MorphemeVO beforeMorphVO = null;
		HashMap<String, MorphemeVO> tempMap = null;
		
		for(HashMap<String, MorphemeVO> wordMap : sentenceMorphMap) {
			for(String word : wordMap.keySet()) {
				MorphemeVO morphVO = wordMap.get(word);
				
				// 바로 전 단어가, 명사로 끝날 경우
				if(beforeMorphVO != null && beforeMorphVO.getWord().length() > 0 &&
						(beforeMorphVO.getTag().get(beforeMorphVO.getTag().size()-1).equals("NN") 
						|| beforeMorphVO.getTag().get(beforeMorphVO.getTag().size()-1).equals("CN"))) {
					
					// 첫번째 단어가 명사나 복합명사일 경우 합쳐본다
					if(morphVO.getTag().get(0).equals("NN") 
							|| morphVO.getTag().get(0).equals("CN")){
						
						// 합쳐졌을 때, 명사사전에 있는 경우...
						if(nnDictionary.containsKey(beforeMorphVO.getWord() + morphVO.getToken().get(0))) {
							
							// word map을 합친다
							beforeMorphVO.setWord(beforeMorphVO.getWord() + morphVO.getWord());
							ArrayList<String> tokenList = beforeMorphVO.getToken();
							ArrayList<String> tagList = beforeMorphVO.getTag();
							
							String token = tokenList.get(tokenList.size()-1) + morphVO.getToken().get(0);
							
							tokenList.set(tokenList.size()-1, token);
							tagList.set(tokenList.size()-1, "CN");
							
							for(int i = 0; i < morphVO.getToken().size(); i++) {
								if(i > 0) {
									tokenList.add(morphVO.getToken().get(i));
									tagList.add(morphVO.getTag().get(i));
								}
							}
							
							beforeMorphVO.setToken(tokenList);
							beforeMorphVO.setTag(tagList);
							
							continue;
						}						
					}
				}	
				// before를 세팅
				if(beforeMorphVO != null) {
					tempMap = new HashMap<String, MorphemeVO> ();
					tempMap.put(beforeMorphVO.getWord(), beforeMorphVO);
					rtnList.add(tempMap);
				}
				
				beforeMorphVO = wordMap.get(word);
			}
		}
		
		// 한문장이 끝날 경우
		if(beforeMorphVO != null) {
			tempMap = new HashMap<String, MorphemeVO> ();
			tempMap.put(beforeMorphVO.getWord(), beforeMorphVO);
			rtnList.add(tempMap);
		}
		
		return rtnList;
	} */
	
	
	/**
	 * 형태소 분석 결과를 pos tagging 한 것을 list map 형태로 보여준다
	 * @return
	 */
	public List<Map<String, String>> pos(String document) {
		if(CONFIG.getCoumpoundLevel() == 0) return posTagger.posTagging(document);
		else return arrangeList(posTagger.posTagging(document));
	}
	
	
	/**
	 * 연속된 데이터를 기반으로 NER과 복합명사등을 추가 태깅
	 * 
	 * @return
	 */
	public List<Map<String, String>> arrangeList(List<Map<String, String>> posList) {
		List<Map<String, String>> rtnList = new ArrayList<> ();
		
		String prevKwd = "";
		String prevTag = "";
		for(int i = 0; i < posList.size(); i++) {
			Map<String, String> posMap = posList.get(i);
			
			// 
			for(String k : posMap.keySet()) {
				String tag = posMap.get(k);
				
				// 이전값과 현재값이 모두 명사일 때
				if(prevTag.equals("NN") && tag.equals("NN")) {
					Map<String, String> newMap = new LinkedHashMap<> ();
					
					// 합친 명사가 사전에 있을 경우
					if(tagDictionary.containsKey(prevKwd + k)) {						
						Set<String> tagSet = tagDictionary.get(prevKwd + k);
						
						// 복합명사 우선
						if(tagSet.contains("CN")) newMap.put(prevKwd + k, "CN");
						else if(tagSet.contains("NN")) newMap.put(prevKwd + k, "CN");
					}
					
					String chgNerKwd = chgNerKwd(prevKwd + k);
					if(nerDictionary.containsKey(chgNerKwd)) {
						Set<String> nerSet = nerDictionary.get(prevKwd + k);
						
					}
				}
			}
			
			rtnList.add(posMap);
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
