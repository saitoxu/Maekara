package lab.ishida.maekara.facerecognition.model;

/**
 * 文句を言っている人のDB
 * @author kingetsu
 *
 */
public class ComplainantNameAndContents {

	/**
	 * 文句を言っている人
	 */
	String name;
	/**
	 * 反省文の内容
	 */
	String statemenets;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
