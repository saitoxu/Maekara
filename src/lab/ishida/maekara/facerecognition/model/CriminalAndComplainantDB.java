package lab.ishida.maekara.facerecognition.model;

public class CriminalAndComplainantDB {
	/**
	 * 犯罪者の名前
	 */
	String ciminalName;
	
	/**
	 * 告発者の名前
	 */
	String complainantName;
	
	/**
	 * 告発文の内容
	 */
	String contents;
	
	/**
	 * 反省文の内容
	 */
	String statemenets;

	
	public String getCiminalName() {
		return ciminalName;
	}

	public void setCiminalName(String criminalName) {
		this.ciminalName = criminalName;
	}

	public String getComplainantName() {
		return complainantName;
	}

	public void setComplainantName(String complainantName) {
		this.complainantName = complainantName;
	}

	/**
	 * 反省文を取り出す
	 * @return String 反省文の内容
	 */
	public String getStatemenets() {
		return statemenets;
	}

	/**
	 * 反省文をセット
	 * @param statemenets 反省文の内容
	 */
	public void setStatemenets(String statemenets) {
		this.statemenets = statemenets;
	}

	/**
	 * 犯罪者の名前を入れる
	 * @param name 犯罪者の名前
	 */
	public void setCriminalName(String name) {
		this.ciminalName = name;
	}

	/**
	 * 告発内容を取得する
	 * @return 告発内容
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * 告発内容をセット
	 * @param contents 告発内容
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}


}
