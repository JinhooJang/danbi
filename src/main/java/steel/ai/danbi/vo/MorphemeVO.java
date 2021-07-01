package steel.ai.danbi.vo;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 형태소 Value Object
 * 
 * @author jinhoo.jang
 * @since 2018.06.04
 */
public class MorphemeVO {

	/** 단어 */
	private String word;
	/** 형태소 */
	private ArrayList<String> token;
	/** 태그 */
	private ArrayList<String> tag;
	/** NER */
	private HashMap<String, ArrayList<String>> ner;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public ArrayList<String> getToken() {
		return token;
	}
	public void setToken(ArrayList<String> token) {
		this.token = token;
	}
	public ArrayList<String> getTag() {
		return tag;
	}
	public void setTag(ArrayList<String> tag) {
		this.tag = tag;
	}
	public HashMap<String, ArrayList<String>> getNer() {
		return ner;
	}
	public void setNer(HashMap<String, ArrayList<String>> ner) {
		this.ner = ner;
	}
}