package lab.ishida.maekara.facerecognition.core;

import lab.ishida.maekara.facerecognition.exception.DataBaseErrorException;


/**
 * 認識結果を返すクラス.
 * 
 * https://www.skybiometry.comのAPIを用いて認識を行う.
 * APIはunirestライブラリを用いて取得する.
 * 参照ライブラリーにunirest-java-1.2.5.jarを追加しておくこと.
 * 
 * @author Kignetsu
 *
 */
public class ResultRecognition {
	
	private String contents = null; // 告発文
	private String statements = null; // 反省文
	private ResultType recognitionResultType = null; // 認識結果
	private int confidence = 0;
	private String name; // 認識した人の名前

	/**
	 * コンストラクタ
	 * @param recognitionResult
	 * @param confidence
	 * @param str 反省文か告発文
	 * @throws DataBaseErrorException データが多すぎて整合性が取れない場合
	 */
	public ResultRecognition(ResultType recognitionResultType, int confidence, String str, String name) throws DataBaseErrorException {
		
		this.name = name;
		this.recognitionResultType = recognitionResultType;
		
		switch (recognitionResultType) {
			case CRIMINAL:
				this.confidence = confidence;
				this.contents = str;
				break;
				
			case COMP_WITH_STATEMENT:
				this.confidence = confidence;
				this.statements = str;
				break;
		
			default:
				throw new DataBaseErrorException("データ過多");
		}
	}
	
	/**
	 * コンストラクタ
	 * @param recognitionResult
	 * @param confidence
	 * @throws DataBaseErrorException データベースのデータが足りてない時のエラー
	 */
	public ResultRecognition(ResultType recognitionResultType, int confidence, String name) throws DataBaseErrorException {
		this.name = name;
		this.recognitionResultType = recognitionResultType;
		
		switch (recognitionResultType) {
			case COMP_WITH_NO_STATEMENT:
				this.confidence = confidence;
				break;
	
			case NONE:
				this.name = "";
				this.confidence = confidence;
				break;
				
			default:
				throw new DataBaseErrorException("データ欠損");
		}
	}
	
	/**
	 * 告発文を返す
	 * @return
	 */
	public String getContents(){
		return this.contents;
	}
	
	/**
	 * 認識結果を返す
	 * @return ResultType列挙型のいずれか
	 */
	public ResultType getRecognitionResult(){
		return this.recognitionResultType;
	}
	
	/**
	 * 反省文を返す
	 * @return
	 */
	public String getStatements(){
		return this.statements;
	}

	public int getConfidence() {
		return confidence;
	}

	/**
	 * 認識された名前について取得する
	 * @return 名前
	 */
	public String getName() {
		return name;
	}

}
