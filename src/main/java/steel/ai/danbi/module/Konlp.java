package steel.ai.danbi.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import steel.ai.danbi.vo.MorphemeVO;



/**
 * 형태소 분석 모듈
 * 
 * @author jinhoo.jang
 * @since 2020.01.17
 */
public class Konlp {
	private HashMap<String, String> morphemeDic;
	private HashMap<String, String[]> tagDictionary;
	private HashMap<String, String[]> tagSubDictionary;
	private HashMap<String, String[]> nnDictionary;
	private HashMap<String, String> lastTagDictionary;
	private HashMap<String, ArrayList<String>> nerDictionary;
	private HashMap<String, String> nerSynDictionary;
	
	
	/**
	 * 생성자
	 * 
	 * @param morphemeDic
	 * @param tagDictionary
	 * @param tagSubDictionary
	 * @param nnDictionary
	 * @param lastTagDictionary
	 * @param nerDictionary
	 * @param nerSynDictionary
	 * @param snDictionary
	 */
	public Konlp(
			HashMap<String, String> morphemeDic,
			HashMap<String, String[]> tagDictionary,
			HashMap<String, String[]> tagSubDictionary,
			HashMap<String, String[]> nnDictionary,
			HashMap<String, String> lastTagDictionary,
			HashMap<String, ArrayList<String>> nerDictionary,
			HashMap<String, String> nerSynDictionary,
			HashMap<String, String> snDictionary
		) {
		
		this.morphemeDic = morphemeDic;
		this.tagDictionary = tagDictionary;
		this.tagSubDictionary = tagSubDictionary;
		this.nnDictionary = nnDictionary;
		this.lastTagDictionary = lastTagDictionary;
		this.nerDictionary = nerDictionary;
		this.nerSynDictionary = nerSynDictionary;
	}
	
	
	/**
	 * 단어 분석 메소드
	 * 
	 * @param word
	 * @param morphVO
	 * @param isRecu(재귀 호출 여부)
	 * @return
	 */
	public void wordAnalyze(String word, MorphemeVO morphVO, boolean isRecu) {
		// 기분석 사전에 존재할 경우
		if(morphemeDic.containsKey(word)) {
			setExistMorpheme(word, morphVO);
			return;
		}
		// 동사, 명사, 부사등이 일치할 경우 (독립적으로 나올 수 있는)
		else if(tagDictionary.containsKey(word)  
					|| tagSubDictionary.containsKey(word)) {
			
			// 재귀 호출일 경우, 복합적으로 나올 수 있는 주 태그 중심
			if(isRecu) {
				if(tagDictionary.containsKey(word)) {
					setExistTag(word, morphVO);
				} else {
					setSubExistTag(word, morphVO);
				}
			}
			// 재귀 호출이 아닐 경우, 서브 태그를 우선으로 매핑
			else {
				if(tagSubDictionary.containsKey(word)) {
					setSubExistTag(word, morphVO);
				} else {
					setExistTag(word, morphVO);
				}
			}			
			return;
		}		
		// 기타 품사(보조)가 일치할 경우
		else if(lastTagDictionary.containsKey(word)) {
			setExistLastTag(word, morphVO);
			return;
		}
		word = stringReplace(word).trim();	// 데이터를 정제한 후 다시 요청		
				
		// 정제된 데이터를 다시 만들어진 사전에 존재할 경우 리턴
		if(morphemeDic.containsKey(word)) {
			setExistMorpheme(word, morphVO);		
		}
		// 동사, 명사, 부사등이 일치할 경우
		else if(tagDictionary.containsKey(word) 
				|| tagSubDictionary.containsKey(word)) {
			
			// 재귀 호출일 경우, 복합적으로 나올 수 있는 주 태그 중심
			if(isRecu) {
				if(tagDictionary.containsKey(word)) {
					setExistTag(word, morphVO);
				} else {
					setSubExistTag(word, morphVO);
				}
			}
			// 재귀 호출이 아닐 경우, 서브 태그를 우선으로 매핑
			else {
				if(tagSubDictionary.containsKey(word)) {
					setSubExistTag(word, morphVO);
				} else {
					setExistTag(word, morphVO);
				}
			}			
			return;
		}		
		else if(lastTagDictionary.containsKey(word)) {	// 기타 품사가 일치할 경우
			setExistLastTag(word, morphVO);
		}
		else if(getPattern(word).equals("SL")) {	// 사전에는 없고, 영어 혹은 영어숫자로만 되어 있을 경우
			setWordTag(word, "SL", morphVO);	// 패턴 체크(숫자)
		}/*
		else if(mixed(word)) {	// 영문/숫자/한글이 섞인 경우
			
		}*/
		else {
			String last = "";
			
			for(int i = 1; i < word.length(); i++) {	// 조사, 어미, 동사등에 값이 있을 경우 처리
				if(lastTagDictionary.containsKey(
						word.substring(word.length()-i, word.length()))) {
					
					last = word.substring(word.length()-i, word.length());
					String first = word.substring(0, word.length()-last.length());
					
					if(last.length() == 1) {	// 패턴으로 지정되어 있는 것은 세팅
						String lastStr = replaceNJongsung(first.substring(first.length()-1, first.length()));
						
						if(!lastStr.equals(first.substring(first.length()-1, first.length()))) {
							boolean success = setPatternTag(first.substring(0,first.length()-1)+lastStr, morphVO, last, "ㄴ");
							if(success)
								return;
						}
					} else {
						boolean success = setPatternTag(first, morphVO, last, "");
						if(success)
							return;
					}
					
					// 명사 혹은 복합명사일 경우 태그세팅
					first = chkNGetComplexNounNew(first, nnDictionary);
					
					// 복합명사 혹은 단일명사에 일치할 경우
					if(!first.contains(":UK") && first.contains(" ")) {
						setNounNLastTag(first, morphVO, last);		// (명사 or 복합명사) + (조사 or 어미조합)
						return;
					} 
				}
			}
			
			// 복합명사에 일치할 경우
			String nnList = chkNGetComplexNounNew(word, nnDictionary);
			if(!nnList.contains(":UK")) {
				setComplexNoun(nnList, morphVO);
				return;
			}
			
			if(last.length() > 0) {	// 조사, 어미, 동사등에 값이 있을 경우
				String nn = word.substring(0, word.length()-last.length());	// 일부 단어가 품사에 포함이 되면
				
				wordAnalyze(nn, morphVO, true);	// 재귀 호출
				setExistLastTag(last, morphVO);
			} else {
				for(int i = 1; i < word.length(); i++) {	// 복합명사, 동사 처리
					if(tagDictionary.containsKey(word.substring(word.length()-i, word.length()))) 
						last = word.substring(word.length()-i, word.length());
				}
				
				if(last.length() > 0) {
					String nn = word.substring(0, word.length()-last.length());		// 일부 단어가 품사에 포함이 되면
					
					wordAnalyze(nn, morphVO, true);	// 재귀 호출
					setExistTag(last, morphVO);						
				} else {
					setWordTag(word, getPattern(word), morphVO);	// 패턴 체크(숫자)
				}			
			}
		}		
	}
	
	
	/**
	 * tag 사전에 존재할 경우 세팅
	 * 
	 * @param word
	 * @return
	 */
	public void setExistTag(String word, MorphemeVO morphVO) {
		String[] temp = tagDictionary.get(word);
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
			
		tokenList.add(temp[0].trim());
		tagList.add(temp[1].trim());
		
		// 개체명이 있을 경우 등록
		if(nerDictionary.containsKey(word)) {
			HashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>> ();
			ArrayList<String> list = new ArrayList<String> ();
			
			if(morphVO.getNer() != null) {
				map = morphVO.getNer();
			}
			
			list = nerDictionary.get(word);
			map.put(nerSynDictionary.get(word), list);
			
			morphVO.setNer(map);
		}
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * sub tag 사전에 존재할 경우 세팅 (NER 없음)
	 * 
	 * @param word
	 * @return
	 */
	public void setSubExistTag(String word, MorphemeVO morphVO) {
		String[] temp = tagSubDictionary.get(word);
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
			
		tokenList.add(temp[0].trim());
		tagList.add(temp[1].trim());		
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * tag 사전에 존재할 경우 세팅
	 * 
	 * @param word
	 * @return
	 */
	public void setExistLastTag(String word, MorphemeVO morphVO) {
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
			
		tokenList.add(word.trim());
		tagList.add(lastTagDictionary.get(word.trim()));
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * 키워드와 태그를 파라미터로 받은 것을 세팅
	 * 
	 * @param word
	 * @param tag
	 */
	public void setWordTag(String word, String tag, MorphemeVO morphVO) {
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
			
		tokenList.add(word.trim());
		tagList.add(tag.trim());
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * 이미 분석된 사전을 그대로 가지고 온다
	 * 
	 * @param word
	 * @return
	 */
	public void setExistMorpheme(String word, MorphemeVO morphVO) {
		String[] morphs = morphemeDic.get(word).split(" ");
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
		
		for(String morph : morphs) {
			String[] value = morph.split("\\:");
			tokenList.add(value[0].trim());
			tagList.add(value[1].trim());
			
			morphVO.setToken(tokenList);
			morphVO.setTag(tagList);			
		}
	}
	
	
	/**
	 * 받침에 N을 제거하여 리턴
	 * 
	 * @param str
	 * @return
	 */
	public String replaceNJongsung(String str) {
		char test = str.charAt(0);
		
		 if(test >= 0xAC00) {
             char uniVal = (char) (test - 0xAC00);
             char jon = (char) (uniVal % 28);

             if(Integer.toHexString((char)jon).equals(Integer.toHexString(4))) {
            	 return String.valueOf((char)(test-4));
             }
		 }
		 
		 return str;
	}
	
		
	/**
	 * 명사 + 어미or조사 태그 세팅
	 * 
	 * @param word
	 * @return
	 */
	public void setNounNLastTag(String nnStr, MorphemeVO morphVO, String lastWord) {
		String[] nnArr = nnStr.split(" ");
		String complexNN = nnStr.replaceAll(" ", "").replaceAll(":NN", ""); 
		
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
		
		if(nnDictionary.containsKey(complexNN)) {
			tokenList.add(complexNN);
			tagList.add(nnDictionary.get(complexNN)[1]);
		} else {
			for(String nn : nnArr) {
				tokenList.add(nn.replaceAll(":NN", ""));
				tagList.add("NN");
			}
		}
			
		// 개체명이 있을 경우 등록
		if(nerDictionary.containsKey(complexNN)) {
			HashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>> ();
			ArrayList<String> list = new ArrayList<String> ();
			
			if(morphVO.getNer() != null) {
				map = morphVO.getNer();
			}
			
			list = nerDictionary.get(complexNN);
			map.put(nerSynDictionary.get(complexNN), list);
			morphVO.setNer(map);
		}
		
		tokenList.add(lastWord);
		tagList.add(lastTagDictionary.get(lastWord));
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * 다양한 패턴에 근거하여 최적화된 품사를 조합한다
	 * 
	 * @param word
	 * @return
	 */
	public boolean setPatternTag(String firstWord, MorphemeVO morphVO, String lastWord, String jongsung) {
		String lastTag = lastTagDictionary.get(lastWord);
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
		
		// 동사
		if(lastWord.equals("는데") && lastTag.equals("EM") 
					&& tagDictionary.containsKey(firstWord+"VV")) {
			
			tokenList.add(firstWord);
			tokenList.add(lastWord);

			tagList.add("VV");
			tagList.add(lastTagDictionary.get(lastWord));
			
			morphVO.setToken(tokenList);
			morphVO.setTag(tagList);
			
			return true;
		}
		// 명사
		else if (lastWord.equals("인데") && lastTag.equals("EM")) {
			tokenList.add(firstWord);			
			tokenList.add(lastWord);

			if(nnDictionary.containsKey(firstWord)) {
				tagList.add(nnDictionary.get(firstWord)[1]);
			} else {
				tagList.add("NN");
			}
			
			tagList.add(lastTagDictionary.get(lastWord));
			
			// 개체명이 있을 경우 등록
			if(nerDictionary.containsKey(firstWord)) {
				HashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>> ();
				ArrayList<String> list = new ArrayList<String> ();
				
				if(morphVO.getNer() != null) {
					map = morphVO.getNer();
				}
				
				list = nerDictionary.get(firstWord);
				map.put(nerSynDictionary.get(firstWord), list);
				
				morphVO.setNer(map);
			}
			
			morphVO.setToken(tokenList);
			morphVO.setTag(tagList);
			
			return true;
		}
		// 동사 혹은 명사
		else if (lastWord.equals("데") && tagDictionary.containsKey(firstWord)) {
			tokenList.add(tagDictionary.get(firstWord)[0]);			
			tokenList.add(jongsung + lastWord);

			tagList.add(tagDictionary.get(firstWord)[1]);
			tagList.add(lastTagDictionary.get(lastWord));
			
			// 개체명이 있을 경우 등록
			if(nerDictionary.containsKey(tagDictionary.get(firstWord)[0])) {
				HashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>> ();
				ArrayList<String> list = new ArrayList<String> ();
				
				if(morphVO.getNer() != null) {
					map = morphVO.getNer();
				}
				
				list = nerDictionary.get(firstWord);
				map.put(nerSynDictionary.get(firstWord), list);
				
				// ner 세팅
				morphVO.setNer(map);
			}
			
			morphVO.setToken(tokenList);
			morphVO.setTag(tagList);
			
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 복합명사를 세팅
	 * 
	 * @param word
	 * @return
	 */
	public void setComplexNoun(String nnStr, MorphemeVO morphVO) {
		String[] nnArr = nnStr.split(" ");
		String complexNN = nnStr.replaceAll(" ", "");
		
		ArrayList<String> tokenList = new ArrayList<String> ();
		ArrayList<String> tagList = new ArrayList<String> ();
		
		if(morphVO.getTag() != null) {
			tokenList = morphVO.getToken();
			tagList = morphVO.getTag();
		}
		
		for(String nn : nnArr) {
			tokenList.add(nn.replaceAll(":NN", ""));
			tagList.add("NN");
		}
		
		// 개체명이 있을 경우 등록
		if(nerDictionary.containsKey(complexNN)) {
			HashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>> ();
			ArrayList<String> list = new ArrayList<String> ();
			
			if(morphVO.getNer() != null) {
				map = morphVO.getNer();
			}
			
			list = nerDictionary.get(complexNN);
			map.put(nerSynDictionary.get(complexNN), list);
			
			// ner 세팅
			morphVO.setNer(map);
		}
		
		morphVO.setToken(tokenList);
		morphVO.setTag(tagList);
	}
	
	
	/**
	 * 패턴 체크 (숫자 여부)
	 * 
	 * @param word
	 * @return
	 */
	public String getPattern(String word) {
		if(StringUtils.isNumeric(word)){
			return "SN";
		}
		
		// 영어로만 되어 있을 경우.
		if(Pattern.matches("^[a-zA-Z]*$", word)){
			return "SL";
		}
		
		// 영어 혹은 영어+숫자로만 이루어져 있는지 체크
		if(Pattern.matches("^[a-zA-Z0-9]*$", word)){
			return "SL";
		}
		
		// 숫자 패턴으로 되어 있을 경우, 추후 만들어야 됨				
		return "UK";
	}
	
	
	/**
	 * 단어를 Language에 맞게 변환하여 맵에 저장
	 * 
	 * @param word
	 * @return
	 *//*
	public Map<String, String> langMap(String word) {
		Map<String, String> rtnMap = new HashMap<> ();
		String type = "";
		int startIdx = 0;
		
		for(int i = 0; i < word.length(); i++) {
			int ascii = word.charAt(i);		
		
			if(ascii >= 48 && ascii <= 57) {
 				
				if(type.length() == 0 || type.equals("N")) {
					type = "N";
				} 
				// 타입이 변경된 경우
				else if(type.length() > 0 && !type.equals("N")) {
					// 우선 바로 이전값 까지를 맵에 담는다
					rtnMap.put(word.substring(startIdx, i-1), type);
					
					type = "N";
					startIdx = i;
				}
			} else if(ascii >= 65 && ascii <= 90) { // 영문 대문자
				return "E";
			} else if(ascii >= 97 && ascii <= 122) { // 영문 대문자
				return "E";
			} else if(ascii>='가' && ascii<='힣') { // 한글
				return "H";
			}
			
			
		}
	}*/
	
	
	/**
	 * 특수문자를 제거하는 메소드
	 * 
	 * @param str
	 * @return
	 */
	public String stringReplace(String str){
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		return str.replaceAll(match, " ");
	}
	
	
	/**
	 * UK일 경우 Morph 변환
	 * @return
	 */
	public MorphemeVO setUKMorph(String word) {
		MorphemeVO morphVO = new MorphemeVO ();
		morphVO.setWord(word);
		
		morphVO.setTag(new ArrayList<String>(Arrays.asList("UK")));
		morphVO.setToken(new ArrayList<String>(Arrays.asList(stringReplace(word).trim())));
		morphVO.setNer(null);
		
		return morphVO;
	}
	
	
	/**
	 * 복합명사를 추출하는 메소드
	 * 
	 * @param word
	 * @param pureMap
	 * @return
	 */
	public String chkNGetComplexNoun(String word, HashMap<String, String[]> NNmap, int step) {
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		StringBuffer sb3 = new StringBuffer();
		
		int total1 = 0;
		int total2 = 0;
		int total3 = 0;
		
		if(NNmap.containsKey(word)) {
			return word + ":NN ";
		}
		
		if(word.length() <= 2) {
			return word + ":UK ";
		}
		
		int pos = word.length() / 2;
		
		String frontWord = word.substring(0, pos+step);
		String endWord = word.substring(pos+step, word.length());
		
		// 앞단어가 일치가 되었을 경우 일치된 단어는 그냥 사용
		if(NNmap.containsKey(frontWord)) {
			sb.append(frontWord + ":NN ");			
		} 
		else {
			sb.append(chkNGetComplexNoun(frontWord, NNmap, 0));
		}
		
		if(NNmap.containsKey(endWord)) {
			sb.append(endWord + ":NN "); 
		} else {
			sb.append(chkNGetComplexNoun(endWord, NNmap, 0));
		}
		
		String[] words = sb.toString().split("\\:");
		total1 = words.length;
		
		frontWord = word.substring(0, pos+1-step);
		endWord = word.substring(pos+1-step, word.length());
		
		// 앞단어가 일치가 되었을 경우 일치된 단어는 그냥 사용
		if(NNmap.containsKey(frontWord)) {
			sb2.append(frontWord + ":NN "); 
		} else {
			sb2.append(chkNGetComplexNoun(frontWord, NNmap, 0));
		}
		
		if(NNmap.containsKey(endWord)) {
			sb2.append(endWord + ":NN "); 
		} else {
			sb2.append(chkNGetComplexNoun(endWord, NNmap, 0));
		}
		
		words = sb2.toString().split("\\:");		
		total2 = words.length;
		
		if(pos > 1) {
			frontWord = word.substring(0, pos-1+step);
			endWord = word.substring(pos-1+step, word.length());
			
			// 앞단어가 일치가 되었을 경우 일치된 단어는 그냥 사용
			if(NNmap.containsKey(frontWord)) {
				sb3.append(frontWord + ":NN "); 
			} else {
				sb3.append(chkNGetComplexNoun(frontWord, NNmap, 0));
			}
			
			if(NNmap.containsKey(endWord)) {
				sb3.append(endWord + ":NN "); 
			} else {
				//System.out.println(word + " " + endWord);
				sb3.append(chkNGetComplexNoun(endWord, NNmap, 0));
			}
			
			words = sb3.toString().split("\\:");
			total3 = words.length;
			
			if(total3 < total2 && total3 < total1) {
				return sb3.toString();
			}
		}
		
		if(total2 < total1) {
			return sb2.toString();
		} else {
			return sb.toString();
		}
	}
	
	
	/**
	 * 복합명사를 추출하는 메소드 (조사가 있을 경우)
	 * 
	 * @param word
	 * @param pureMap
	 * @return
	 */
	public String chkNGetComplexNounNew(String word, HashMap<String, String[]> NNmap) {
		//System.out.println("NEW WORD=>" + word);

		StringBuffer sb = new StringBuffer();
		String first = "";
		
		if(NNmap.containsKey(word)) {
			return word + ":NN ";
		}
		
		if(word.length() <= 2) {
			return word + ":UK ";
		}
		
		for(int i = 1; i < word.length(); i++) {
			sb = new StringBuffer();
			
			// 단어가 포함되었을 경우
			if(NNmap.containsKey(word.substring(word.length()-1, word.length()))) {
				// 재귀로 체크 시작
				first = word.substring(0, word.length()-1);
				String last = word.substring(word.length()-1, word.length());
				
				if(!word.equals(last)) {
					String tempStr = chkNGetComplexNounNew(first, NNmap);
					
					if(tempStr.indexOf(":NN") > -1) {
						sb.append(first + ":NN " + last + ":NN");
					}
				}
			}
		}
		
		if(sb.length() == 0)
			sb.append(word + ":UK");
		
		return sb.toString();
	}
}
