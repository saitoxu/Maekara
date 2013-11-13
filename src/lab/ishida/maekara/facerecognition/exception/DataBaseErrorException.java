package lab.ishida.maekara.facerecognition.exception;

/**
 * データベースに不整合性あった場合のエラー
 * @author Kignetsu
 *
 */
public class DataBaseErrorException extends Exception {
	public DataBaseErrorException(String message) {
		super(message);
	}

}
