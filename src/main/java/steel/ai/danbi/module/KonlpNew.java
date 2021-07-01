package steel.ai.danbi.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import steel.ai.danbi.vo.MorphemeVO;



/**
 * 형태소 분석 모듈
 * 
 * @author jinhoo.jang
 * @since 2020.01.17
 */
public class KonlpNew {
	private Map<String, String> morphemeDic;
	private Map<String, Set<String>> tagDictionary;
	private Map<String, Set<String>> nerDictionary;
	private Map<String, String> nerSynDictionary;
	private Map<String, String> snDictionary;
	
	
	/**
	 * 생성자
	 * 
	 * @param morphemeDic
	 * @param tagDictionary
	 * @param nerDictionary
	 * @param nerSynDictionary
	 * @param snDictionary
	 */
	public KonlpNew(
			Map<String, String> morphemeDic,
			Map<String, Set<String>> tagDictionary,			
			Map<String, Set<String>> nerDictionary,
			Map<String, String> nerSynDictionary,
			Map<String, String> snDictionary) {
		
		this.morphemeDic = morphemeDic;
		this.tagDictionary = tagDictionary;
		this.nerDictionary = nerDictionary;
		this.nerSynDictionary = nerSynDictionary;
		this.snDictionary = snDictionary;
	}
	
	
	public void analyze() {
		
	}
}
