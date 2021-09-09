package steel.ai.danbi.vo;

import java.util.List;
import java.util.Map;


/**
 * 형태소 Value Object
 * 
 * @author jinhoo.jang
 * @since 2021.09.09
 */
public class MorphemeVO {

	/** 원본 단어 리스트 */
	private List<String> wordList;
	
	/** pos, ner tagging */
	private List<List<Map<String, String>>> posTagList;
	
	/** tag string list */
	private List<List<String>> tagStrList;

	public List<String> getWordList() {
		return wordList;
	}

	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}

	public List<List<Map<String, String>>> getPosTagList() {
		return posTagList;
	}

	public void setPosTagList(List<List<Map<String, String>>> posTagList) {
		this.posTagList = posTagList;
	}

	public List<List<String>> getTagStrList() {
		return tagStrList;
	}

	public void setTagStrList(List<List<String>> tagStrList) {
		this.tagStrList = tagStrList;
	}
}