package steel.ai.danbi.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import steel.ai.danbi.vo.DanbiConfigVO;
import steel.ai.danbi.vo.MorphemeVO;


/**
 * 새로 만드는 형태소 분석 모듈
 * 
 * @author steele
 * @since 2021.07.01
 */
public class PosTagger {
	
	protected DanbiConfigVO CONFIG;
	protected Map<String, String> morphemeDic;
	protected Map<String, Set<String>> nerDictionary;
	protected Map<String, String> nerSynDictionary;
	protected Map<String, Set<String>> tagDictionary;
	protected Map<String, String> synDictionary;
	protected Map<String, String> vvSynDictionary;
	protected Map<String, String> snDictionary;
	
	UKLogger ukLogger = new UKLogger();
	
	public PosTagger(DanbiConfigVO CONFIG,
			Map<String, String> morphemeDic,
			Map<String, Set<String>> tagDictionary,
			Map<String, Set<String>> nerDictionary,
			Map<String, String> vvSynDictionary,
			Map<String, String> nerSynDictionary,			
			Map<String, String> synDictionary,
			Map<String, String> snDictionary) {
		
		this.CONFIG = CONFIG;
		this.morphemeDic = morphemeDic;
		this.tagDictionary = tagDictionary;
		this.nerDictionary = nerDictionary;
		this.vvSynDictionary = vvSynDictionary;
		this.nerSynDictionary = nerSynDictionary;		
		this.synDictionary = synDictionary;
		this.snDictionary = snDictionary;
	}
	
	
	/**
	 * 형태소 태깅
	 * @param document
	 * @return
	 */
	public List<MorphemeVO> posTagging(String document) {
		List<MorphemeVO> posResult = new ArrayList<> ();
		if(document == null || document.trim().length() == 0) return posResult;
		
		if(CONFIG.isDebug()) System.out.println("document->" + document);
		
		// 구분자 특수문자는 공백으로 변경
		//document = document.replaceAll("[\\|\\[\\]\\(\\)\\<\\>\\\"-_·:~,㈜🙂?]", " ");
		//document = document.replaceAll("[\\|\\[\\]\\(\\)\\<\\>_\\-/㈜,·:~]", " ");
		document = document.replaceAll("[\\|\\[\\]\\(\\)\\<\\>_\\㈜,·:]", " ");
		if(CONFIG.isDebug()) System.out.println("document->" + document);
		
		// 개행값은 개행 문자로 변경
		document = document.trim()
				.replaceAll("\n", ". ")
				.replaceAll("\r", ". ")
				.replaceAll("요\\.", "요. ")
				.replaceAll("다\\.", "다. ");
		
		if(CONFIG.isDebug()) System.out.println("document->" + document);
		
		String[] sentences = document.toLowerCase().split("\\. ");
		
		// 문장별로 형태소 분석 수행
		for(String sentence : sentences) {
			if(CONFIG.isDebug()) System.out.println("sentence->" + sentence);
			if(sentence.length() == 0) continue;
			
			MorphemeVO vo = new MorphemeVO();
			List<String> wordList = new ArrayList<> ();
			List<List<Map<String, String>>> allPosTagList = new ArrayList<> ();
			
			// 문장을 다시, 단어로 자른다			
			String[] word = sentence.trim().split(" ");
			for(int j = 0; j < word.length; j++) {
				
				List<Map<String, String>> posTagList = new ArrayList<> (); 
				
				if(word[j].trim().length() > 0) {
					wordList.add(word[j].trim());
					
					Map<String,  Set<String>> backAnalyzed = backWardAnalyze(word[j]);
					
					if(chkUK(backAnalyzed)) {
						Map<String,  Set<String>> forwardAnalyzed = forwardAnalyze(word[j]);
						if(!chkUK(forwardAnalyzed)) {
							posTagList.addAll(arrange(forwardAnalyzed));
						} else {
							// 둘다 UK 있으면, backward 값으로
							posTagList.addAll(arrange(backAnalyzed));
						}
					} else {
						// 둘다 UK 있으면, backward 값으로
						posTagList.addAll(arrange(backAnalyzed));
					}
					
					allPosTagList.add(posTagList);
				}
			}
			
			vo.setWordList(wordList);
			vo.setPosTagList(allPosTagList);
			
			//morphemeList.addAll(wordAnalyze(word[i]));
			// 한칸씩 띄어져 있는 단어들을 분석하여 복합명사일 경우 합친다
			//morphemeMap.addAll(sentenceAnalyze(sentenceMorphMap));
			
			posResult.add(vo);
		}
		
		//System.out.println(document + "->" + posList);		
		return posResult;
	}
	
	
	/**
	 * UK값이 있는지 체크
	 * @param analyzed
	 * @return
	 */
	public boolean chkUK(Map<String,  Set<String>> analyzed) {
		if(analyzed != null && analyzed.size() > 0) {
			for(String _word : analyzed.keySet()) {
				if(analyzed.get(_word).contains("UK")) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * 값을 정배열 하면서, 올바른 품사를 선택한다
	 * 
	 * @param list
	 * @return
	 */
	public List<Map<String, String>> arrange(Map<String,  Set<String>> morphemeMap) {
		List<Map<String, String>> rtnList = new ArrayList<> ();
		Map<String, String> tempMap = new LinkedHashMap<> ();
		String lastTag = "";
		
		int loop = 0;
		for(String token : morphemeMap.keySet()) {
			loop++;
			Set<String> tagSet = morphemeMap.get(token);
			
			// tag는 앞뒤 tag를 기준으로 선택한다
			// 첫 품사일 경우 조사와 어미는 선택하지 않는다. 
			if(lastTag.length() == 0) {
				lastTag = firstTag(tagSet);
				tempMap.put(token, lastTag);
			}
			// 두번째 품사부터는 앞 단어를 기반으로 선택
			else {
				if(morphemeMap.size() == loop) {
					lastTag = otherTag(tagSet, lastTag, true);
				} else {
					lastTag = otherTag(tagSet, lastTag, false);
				}
				tempMap.put(token, lastTag);				
			}
		}
		
		// 복합명사
		String beforeToken = "";
		String beforeTag = "";
		for(String token : tempMap.keySet()) {
			Map<String, String> reArrangeMap = new LinkedHashMap<> ();
			
			if(tempMap.get(token).equals("NN") || tempMap.get(token).equals("CN")) {
				// 이전 명사와 현재 명사를 합쳤을 때 품사가 있을 경우
				if(beforeToken.length() > 0 && tagDictionary.containsKey(beforeToken + token)) {
					Set<String> tagSet = tagDictionary.get(beforeToken + token);
					
					if(tagSet.contains("CN") || tagSet.contains("NN")) {
						beforeToken = beforeToken + token;
						beforeTag = "CN";
						
						continue;
					}
				} else if(beforeTag.equals("NN") || beforeTag.equals("CN")) {
					reArrangeMap.put(beforeToken, beforeTag);					
				}
				
				if(tempMap.get(token).contains("CN")) beforeTag = "CN";
				else beforeTag = "NN"; 
				beforeToken = token;
				
			} else {
				if(beforeTag.equals("NN") || beforeTag.equals("CN")) reArrangeMap.put(beforeToken, beforeTag);
				reArrangeMap.put(token, tempMap.get(token));
				
				beforeToken = "";
				beforeTag = "";
			}
			
			if(reArrangeMap.size() > 0) rtnList.add(reArrangeMap);
			reArrangeMap = new LinkedHashMap<> ();
		}
		
		if(beforeToken.length() > 0) {
			Map<String, String> reArrangeMap = new LinkedHashMap<> ();
			reArrangeMap.put(beforeToken, beforeTag);
			
			rtnList.add(reArrangeMap);
		}
		
		return rtnList;
	}
	
	
	/**
	 * 첫번째 품사
	 * 
	 * @param tagSet
	 * @return
	 */
	public String firstTag(Set<String> tagSet) {
		// 우선 순위별
		if(tagSet.contains("CN")) return "CN";
		if(tagSet.contains("NN")) return "NN";
		if(tagSet.contains("MA")) return "MA";
		if(tagSet.contains("MM")) return "MM";
		
		
		for(String tag : tagSet) {
			if(tagSet.contains("EM")) continue;
			if(tagSet.contains("JS")) continue;
			
			return tag.toUpperCase();			
		}
		
		return "UK";
	}
	
	
	/**
	 * 첫번째 품사가 존재하는 경우
	 * @param tagSet
	 * @return
	 */
	public String otherTag(Set<String> tagSet, String prevTag, boolean isLast) {
		// 앞의 단어가 조사가 어울릴 경우
		if(isLast && ( prevTag.equals("CN") || prevTag.equals("NN") || prevTag.equals("NB"))) {
			if(tagSet.contains("JS")) return "JS";			
		}
		
		for(String tag : tagSet) {
			// 우선 순위별
			if(tag.equalsIgnoreCase("CN")) return "CN";
			if(tag.equalsIgnoreCase("NN")) return "NN";			
			
			return tag.toUpperCase();
		}
		
		return "UK";
	}
	
	
	
	/**
	 * 뒤부터 도는 분석
	 * 
	 * @param _word
	 * @return
	 */
	public Map<String,  Set<String>> backWardAnalyze(String _word) {
		Map<String,  Set<String>> morphemeMap = new LinkedHashMap<> ();
		List<Map<String, Object>> tempPosList = new ArrayList<> ();
		String word = changeTerm(_word);
		
		// 형태소가 분석되었다면...
		if(morphemeDic.containsKey(word)) {
			//System.out.println("morpheme->" + morphemeDic.get(word));
			String[] terms = morphemeDic.get(word).split(" ");
			for(String term : terms) {
				String[] kv = term.split(":");
				morphemeMap.put(kv[0], new HashSet<String>(Arrays.asList(kv[1])));
			}
			return morphemeMap;
		}
		
		// 다른 사전에 있다면
		if(tagDictionary.containsKey(word)) {
			morphemeMap.put(word, tagDictionary.get(word));			
			return morphemeMap;
		}
		
		// 패턴처리
		this.pattern(morphemeMap, word);
		if(morphemeMap.size() > 0) return morphemeMap;
		
		// 동사+어미 처리
		
		
		Set<String> prevPos = new HashSet<> ();
		int last = word.length();
		String lastToken = "";
		
		for(int i = 0; i < word.length(); i++) {
			String token = word.substring(last - (i+1), last);
			String chgToken = changeTerm(token);
			Set<String> posSet = chkPos(chgToken);
			
			if(posSet.size() > 0) {
				if(last - (i+1) == 0) {
					Map<String, Object> tempMap = new HashMap<> ();
					if(CONFIG.isRepresentative()) tempMap.put("token", changeTerm(token));
					else tempMap.put("token", token);
					tempMap.put("posSet", posSet);
					tempPosList.add(tempMap);
					break;
				}
				prevPos = posSet;
				lastToken = token;
				
				// 나머지 단어 조합으로 체크
				/*if(chkOtherToken(word.substring(0, last - token.length())) != null) {
					Map<String, Object> tempMap = new HashMap<> ();
					tempMap.put("token", token);
					tempMap.put("posSet", posSet);
					tempPosList.add(tempMap);					
					
					tempMap = new HashMap<> ();
					tempMap.put(word.substring(0, last - token.length()), 
							chkOtherToken(word.substring(0, last - token.length())));
					tempPosList.add(tempMap);
					break;
				}*/
			}
			
			// 마지막 태그를 토큰과 세팅
			if(posSet.size() == 0 && prevPos.size() > 0) {
				last -= lastToken.length();								
				//morphemeMap.put(lastToken, prevPos);
				
				Map<String, Object> tempMap = new HashMap<> ();
				if(CONFIG.isRepresentative()) tempMap.put("token", changeTerm(lastToken));
				else tempMap.put("token", lastToken);
				tempMap.put("posSet", prevPos);
				tempPosList.add(tempMap);
				
				i = -1;
				lastToken ="";				
				prevPos = new HashSet<> ();
			}
			
			if(last - (i+1) == 0) {
				Map<String, Object> tempMap = new HashMap<> ();
				if(CONFIG.isRepresentative()) tempMap.put("token", changeTerm(token));
				else tempMap.put("token", token);
				
				// 명사 추청 태그 NF
				if(prevPos.contains("JS") || prevPos.size() == 0) {
					tempMap.put("posSet", new HashSet<String>(Arrays.asList("NF")));
				}
				// 그외 UK
				else {
					tempMap.put("posSet", new HashSet<String>(Arrays.asList("UK")));					
				}				
				
				tempPosList.add(tempMap);

				
				//morphemeMap.put(token, new HashSet<String>(Arrays.asList("UK")));
				//System.out.println("WARNING[" + _word + "->" + posMap + "]");
				//ukLogger.ukLogger(_word);
				break;
			}
		}
		
		//System.out.println(tempPosList.size());
		
		if(tempPosList.size() > 0) {
			for(int i = tempPosList.size()-1; i >= 0; i--) {
				//System.out.println("i->" + i);
				Map<String,  Object> tempMap = tempPosList.get(i);
				morphemeMap.put((String)tempMap.get("token"), (Set<String>)tempMap.get("posSet"));
			}
		}
		
		//System.out.println(_word + "-->" + morphemeMap);
		return morphemeMap;
	}
	
	
	/**
	 * 정방향 형태소 분석
	 * 
	 * @param _word
	 * @return
	 */
	public Map<String,  Set<String>> forwardAnalyze(String _word) {
		Map<String,  Set<String>> morphemeMap = new LinkedHashMap<> ();
		String word = changeTerm(_word);
		
		Set<String> prevPos = new HashSet<> ();
		int first = 0;
		String lastToken = "";
		
		for(int i = 0; i < word.length(); i++) {
			String token = word.substring(first, i+1);
			String chgToken = changeTerm(token);
			Set<String> posSet = chkPos(chgToken);
			
			if(posSet.size() > 0) {
				// 마지막이라면
				if(i+1 == word.length()) {
					if(CONFIG.isRepresentative()) morphemeMap.put(changeTerm(token), posSet);
					else morphemeMap.put(token, posSet);
					break;
				}
				
				// 마지막이 아닐경우
				prevPos = posSet;
				lastToken = token;
				
				/*// 나머지 단어 조합으로 체크
				if(chkOtherToken(word.substring(first + token.length(), word.length())) != null) {
					morphemeMap.put(word.substring(first + token.length(), word.length()), 
							chkOtherToken(word.substring(first + token.length(), word.length())));
					break;
				}*/
			}
			
			// 마지막 태그를 토큰과 세팅
			if(posSet.size() == 0 && prevPos.size() > 0) {
				first += lastToken.length();				
				if(CONFIG.isRepresentative()) morphemeMap.put(changeTerm(lastToken), prevPos);
				else morphemeMap.put(lastToken, prevPos);
				
				i = first-1;
				lastToken ="";				
				prevPos = new HashSet<> ();
			}
			
			if(i+1 == word.length()) {
				if(CONFIG.isRepresentative()) morphemeMap.put(changeTerm(token), new HashSet<String>(Arrays.asList("UK")));
				else morphemeMap.put(token, new HashSet<String>(Arrays.asList("UK")));
				
				//morphemeMap.put(token, new HashSet<String>(Arrays.asList("UK")));
				//System.out.println("WARNING[" + _word + "->" + posMap + "]");
				ukLogger.ukLogger(_word);
				break;
			}
		}
		
		//System.out.println("forwardAnalyze : " + _word + "->" + morphemeMap);
		return morphemeMap;
	}
	
	
	/**
	 * 품사 체크
	 * 
	 * @param token
	 * @return
	 */
	public Set<String> chkPos(String token) {
		Set<String> posSet = new HashSet<> ();
		
		if(nerDictionary.containsKey(token)) {
			posSet.addAll(nerDictionary.get(token));
		}
		if(tagDictionary.containsKey(token)) {
			posSet.addAll(tagDictionary.get(token));
		}
		if(snDictionary.containsKey(token)) {
			posSet.addAll(Arrays.asList(snDictionary.get(token)));
		}
		if(StringUtils.isNumeric(token)) {
			posSet.add("SN");
		}
		// 영어로만 되어 있을 경우.
		if(Pattern.matches("^[a-zA-Z]*$", token)){
			posSet.add("SL");
		}
		// 한자로만 되어 있을 때
		boolean isHanja = true;
		for(int i = 0; i < token.length(); i++) {
			int charAt = (int)token.charAt(i);
			
			if((charAt >= '\u2E80' && charAt <= '\u2EFF') || 
				(charAt >= '\u3400' && charAt <= '\u4DB5') ||
				(charAt >= '\u4E00' && charAt <= '\u9FBF')) {				
			} else {
				isHanja = false;
				break;
			}
		}
		if(isHanja) posSet.add("SH");
		
		//System.out.println(token + "->" + posSet);
		return posSet;
	}
	
	
	/**
	 * 대표어로 치환
	 * 
	 * @param term
	 * @return
	 */
	public String changeTerm(String term) {
		/*if(tagDictionary.containsKey(term)) return term;
		if(morphemeDic.containsKey(term)) return term;*/
		if(synDictionary.containsKey(term)) return synDictionary.get(term);
		if(nerSynDictionary.containsKey(term)) return nerSynDictionary.get(term);
		if(vvSynDictionary.containsKey(term)) return vvSynDictionary.get(term);
		
		return term;
	}	
	
	
	/**
	 * 나머지 토큰으로 체크
	 * @return
	 */
	public Set<String> chkOtherToken(String otherToken) {
		String chgToken = changeTerm(otherToken);
		if(tagDictionary.containsKey(chgToken)) return tagDictionary.get(chgToken);
		return null;
	}
	
	
	/**
	 * 접미사 처리
	 */
	public void pattern(Map<String,  Set<String>> morphemeMap, String word) {
		for(int i = 0; i < word.length(); i++) {
			String subWord = word.substring(word.length()-i, word.length());
			String preWord = word.substring(0, word.length()-i);
			
			// 명사 + 접미사 처리
			this.nnSuffix(morphemeMap, preWord, subWord);
			if(morphemeMap.size() > 0) return;
			
			// 동사 + 어미 처리
			this.vvem(morphemeMap, preWord, subWord);
			if(morphemeMap.size() > 0) return;
			
			// 명사 + 조사 처리
			this.nnjs(morphemeMap, preWord, subWord);
			if(morphemeMap.size() > 0) return;
			
			// 명사 + 어미 처리
			this.nnem(morphemeMap, preWord, subWord);
			if(morphemeMap.size() > 0) return;
		}
	}
	
	
	/**
	 * 명사 + 접미사 처리
	 * 
	 * @param morphemeMap
	 * @param preWord
	 * @param subWord
	 */
	public void nnSuffix(Map<String,  Set<String>> morphemeMap, String preWord, String subWord) {
		// 접미사로 되어 있을 경우
		if(tagDictionary.containsKey(subWord) 
				&& tagDictionary.get(subWord).contains("XSN")
				&& tagDictionary.containsKey(preWord)) {
			
			Set<String> tagSet = tagDictionary.get(preWord);
			if(tagSet.contains("CN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("CN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("XSN")));
			} else if (tagSet.contains("NN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("NN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("XSN")));
			}				
		}
	}
	
	
	/**
	 * 동사 + 어미 처리
	 * 
	 * @param morphemeMap
	 * @param preWord
	 * @param subWord
	 */
	public void vvem(Map<String,  Set<String>> morphemeMap, String preWord, String subWord) {
		// 어미로 되어 있을 경우
		if(tagDictionary.containsKey(subWord) 
				&& tagDictionary.get(subWord).contains("EM")
				&& vvSynDictionary.containsKey(preWord)) {
			
			morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("VV")));
			morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("EM")));
		}
	}
	
	
	/**
	 * 명사 + 어미 처리
	 * 
	 * @param morphemeMap
	 * @param preWord
	 * @param subWord
	 */
	public void nnem(Map<String,  Set<String>> morphemeMap, String preWord, String subWord) {
		// 어미로 되어 있을 경우
		if(tagDictionary.containsKey(subWord) 
				&& tagDictionary.get(subWord).contains("EM")
				&& tagDictionary.containsKey(preWord)) {
			
			Set<String> tagSet = tagDictionary.get(preWord);
			
			if(tagSet.contains("CN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("CN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("EM")));
			} else if (tagSet.contains("NN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("NN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("EM")));
			}	
		}
	}
	
	
	/**
	 * 명사 + 조사
	 * 
	 * @param morphemeMap
	 * @param preWord
	 * @param subWord
	 */
	public void nnjs(Map<String,  Set<String>> morphemeMap, String preWord, String subWord) {
		// 조사로 되어 있을 경우
		if(tagDictionary.containsKey(subWord) 
				&& tagDictionary.get(subWord).contains("JS")
				&& tagDictionary.containsKey(preWord)) {
			
			Set<String> tagSet = tagDictionary.get(preWord);
			if(tagSet.contains("CN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("CN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("JS")));
			} else if (tagSet.contains("NN")) {
				morphemeMap.put(preWord, new HashSet<String>(Arrays.asList("NN")));
				morphemeMap.put(subWord, new HashSet<String>(Arrays.asList("JS")));
			}				
		}
	}
}
