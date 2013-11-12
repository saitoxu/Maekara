package lab.ishida.maekara.facerecognition.model;

/**
 * 犯罪者のデータベース
 * @author kingetsu
 *
 */
public class CriminalNameAndContents {

	String name;
	String contents;
	
	public CriminalNameAndContents() {
		name = null;
		contents = null;
	}

	public String getName() {
		return name;
	}

	/**
	 * 犯罪者の名前を入れる
	 * @param name 犯罪者の名前
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getContents() {
		return contents;
	}

	/**
	 * 犯罪内容を入れる
	 * @param contents 犯罪内容を入れる
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}


}
