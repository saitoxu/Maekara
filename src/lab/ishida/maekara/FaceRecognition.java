package lab.ishida.maekara;

import java.util.ArrayList;
import java.util.List;

/**
 * 顔認識を行うクラス
 * @author kingetsu
 *
 */
public class FaceRecognition {
	/** 犯罪者の名前リスト **/
	List<CriminalNameAndContents> criminalList;
	/** 告発者の名前リスト **/
	List<ComplainantNameAndContents> complainantList;
	enum FaceResult{
		/**
		 * 犯罪者
		 */
		CRIMINAL,
		/**
		 * 告発者
		 */
		COMPLAINANT,
		/**
		 * だれでもない
		 */
		NONE,
	}


	public FaceRecognition() {
		this.criminalList = new ArrayList<CriminalNameAndContents>();
		this.complainantList = new ArrayList<ComplainantNameAndContents>();

	}

	/**
	 * 文句をDBにセットする
	 * @param criminalName 犯罪者の名前
	 * @param comploainantName 告発者の名前
	 * @param contents 告発内容
	 * @return 成功したらTrue
	 */
	public boolean setNameAndContents(String criminalName, String comploainantName, String contents){

		CriminalNameAndContents criminalDB  = new CriminalNameAndContents();
		criminalDB.setContents(contents);
		criminalDB.setName(criminalName);
		
		ComplainantNameAndContents compDB = new ComplainantNameAndContents();
		compDB.setName(comploainantName);

		criminalList.add(criminalDB);
		complainantList.add(compDB);

		return true;
	}

	/**
	 * 犯罪者の反省文をセットする
	 * @param name 犯罪者の名前
	 * @param statement 犯罪者がつぶやいた反省文の内容
	 * @return 犯罪者の名前が見つかればDBにセットしてTrue，見つからなければFalse
	 */
	public boolean setComplaint(String name, String statements){

		for (ComplainantNameAndContents comp : complainantList) {
			if(comp.getName().equals(name)){
				comp.setStatemenets(statements);
				return true;
			}
		}
		return false;
	}

	/**
	 * 認識器にかける
	 * @param picture 写真データのバイナリデータ
	 * @return ResultRecognitionクラスのオブジェクト
	 */
	public ResultRecognition recognition(String picture){
		return null;
	}

}
