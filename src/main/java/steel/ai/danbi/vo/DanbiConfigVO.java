package steel.ai.danbi.vo;


/**
 * 설정값을 읽기 위한 Value Object
 * 
 * @author jinhoo.jang
 * @since 2020.04.21
 */
public class DanbiConfigVO {
	
	/** 사전 경로 */
	private String dicPath;
	/** NER 리스트 */
	private String[] nerList;
	/** 사용자 사전 사용 여부 */
	private boolean userYn;
	/** debug 모드 */
	private boolean debug;	
	/** 대표어 치환 모드 */
	private boolean representative;
	/** 분해 레벨 */
	private int coumpoundLevel;
	
	public String getDicPath() {
		return dicPath;
	}
	public void setDicPath(String dicPath) {
		this.dicPath = dicPath;
	}	
	public String[] getNerList() {
		return nerList;
	}
	public void setNerList(String[] nerList) {
		this.nerList = nerList;
	}
	public boolean isUserYn() {
		return userYn;
	}
	public void setUserYn(boolean userYn) {
		this.userYn = userYn;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public boolean isRepresentative() {
		return representative;
	}
	public void setRepresentative(boolean representative) {
		this.representative = representative;
	}
	public int getCoumpoundLevel() {
		return coumpoundLevel;
	}
	public void setCoumpoundLevel(int coumpoundLevel) {
		this.coumpoundLevel = coumpoundLevel;
	}	
}
