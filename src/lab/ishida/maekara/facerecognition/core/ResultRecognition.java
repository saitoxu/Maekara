package lab.ishida.maekara.facerecognition.core;

import java.io.File;

import lab.ishida.maekara.facerecognition.model.ComplainantNameAndContents;




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
	
	public enum ResultType{
		/** 犯罪者の顔 */
		CRIMINAL,
		/** 告発者の顔でその人にむけた反省文が帰ってきている状態 */
		COMP_WITH_STATEMENT,
		/** 告発者の顔だが、まだ犯罪者がその人にむけた反省文を述べていない状態 */
		COMP_WITH_NO_STATEMENT,
		/** だれでもない */
		NONE,
		/** その他の認識エラー **/
		ERROR,
	}
	
	/**
	 * コンストラクタ
	 * @param recognitionResult
	 * @param confidence
	 * @param str 反省文か告発文
	 */
	public ResultRecognition(ResultType recognitionResultType, int confidence, String str) {
		
		this.recognitionResultType = recognitionResultType;
		
		switch (recognitionResultType) {
			case CRIMINAL:
				this.confidence = confidence;
				break;
				
			case COMP_WITH_STATEMENT:
				this.confidence = confidence;
				break;
	
			case COMP_WITH_NO_STATEMENT:
				this.confidence = confidence;
				break;
				
			default:
				break;
		}
	}
	
	/**
	 * コンストラクタ
	 * @param recognitionResult
	 * @param confidence
	 * @param str 反省文か告発文
	 */
	public ResultRecognition(ResultType recognitionResultType, int confidence) {
		
		this.recognitionResultType = recognitionResultType;
		
		switch (recognitionResultType) {
			case CRIMINAL:
				this.confidence = confidence;
				break;
				
			case COMP_WITH_STATEMENT:
				this.confidence = confidence;
				break;
	
			case COMP_WITH_NO_STATEMENT:
				this.confidence = confidence;
				break;
				
			default:
				break;
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

}
