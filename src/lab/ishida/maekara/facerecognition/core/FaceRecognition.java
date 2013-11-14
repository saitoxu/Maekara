package lab.ishida.maekara.facerecognition.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lab.ishida.maekara.facerecognition.core.ResultRecognition.ResultType;
import lab.ishida.maekara.facerecognition.exception.DataBaseErrorException;
import lab.ishida.maekara.facerecognition.model.CriminalAndComplainantDB;

import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * 顔認識を行うクラス.
 * 
 * 使い方<br>
 * recognitionメソッドで画像を入れてやると、ResultRecognitionオブジェクトが帰ってきます. 
 * このオブジェクトではいろいろな認識結果が入ってます.@see {@link ResultRecognition}
 * 
 * データベースにセットするときは各種setメソッドを使ってください.
 * 
 * 
 * 名称は以下で統一してます.
 * 
 * complainant : 怒っている人.
 * contents : 怒っている人の犯罪者に向けた告発文.
 * criminal : 犯罪者.
 * statements : 反省のための宣言文.
 * 
 * 
 * 使う名前は以下になってます． @see {@link FaceRecognition#NAME_ARRAY}
 * 
 * @author kingetsu
 *
 */
public class FaceRecognition {
	/** データベースリスト **/
	List<CriminalAndComplainantDB> db;

	
	// API キー
	private final String API_KEY = "https://face.p.mashape.com/account/limits";
	// API シークレット　キー
	private final String API_SECRET = "79ead5bc5cb4452e9115a2dd65c10541";
	// 学習済みの人たちの羅列
	private final String NAME_ARRAY = "goto,saito,kingetsu";
	// 名前空間
	private final String MY_NAMESPACE = "maekara";
	
	public FaceRecognition() {
		this.db = new ArrayList<CriminalAndComplainantDB>();
	}

	/**
	 * 告発者からの告発文の内容をデータベースに記録する
	 * @param criminalName 犯罪者の名前
	 * @param comploainantName 告発者の名前
	 * @param contents 告発内容
	 * @return 成功したらTrue
	 */
	public boolean setNameAndContents(String criminalName, String complainantName, String contents){

		CriminalAndComplainantDB obj = new CriminalAndComplainantDB();
		obj.setCiminalName(criminalName);
		obj.setComplainantName(complainantName);
		obj.setContents(contents);
		
		db.add(obj);

		return true;
	}

	/**
	 * 告発文を聞いた犯罪者が喋った反省文の内容をセットする
	 * @param criminalName 犯罪者の名前
	 * @param statement 犯罪者がつぶやいた反省文の内容
	 * @return 犯罪者の名前が見つかればDBにセットしてTrue，見つからなければFalse
	 */
	public boolean setComplaintAndStatements(String criminalName, String statements){

		for (CriminalAndComplainantDB comp : this.db) {
			if(comp.getCiminalName().equals(criminalName)){
				comp.setStatemenets(statements);
				return true;
			}
		}
		return false;
	}

	/**
	 * 認識器にかけてその顔写真の主が誰かを判定する.
	 * 犯罪者なら犯罪者にするべきお告げの内容を，告発者なら告発する内容を{@link ResultRecognition}オブジェクトで
	 * まとめて返す.
	 * @param imageFile 認識したい画像ファイル、最大縦横1024ピクセルまで
	 * @return ResultType ResultType列挙型を返す
	 * @throws UnirestException 画像認識APIが使えなかったときにエラー
	 * @throws JSONException JSONのパースエラー
	 * @return ResultRecognitionクラスのオブジェクト
	 * @throws DataBaseErrorException データベースに不整合性があった場合のエラー
	 * @throws UnirestException API接続エラー
	 * @throws JSONException JSONのパースに失敗時のエラー
	 * @see ResultRecognition
	 */
	public ResultRecognition recognition(File imageFile)throws UnirestException, JSONException, DataBaseErrorException{
		
//			faces/detect – detects faces in specified images, returns face tags (every tag has unique tag id - tid).
//			tags/save – saves specified face tags (by tid) with user specified user id(eg. mark@docs, where docs - data namespace name).
//			faces/train – checks changes for specified user ids (eg. new tags were added using tags/save or removed using tags/remove) and either creates/updates/removes face template for specified user id from data namespace.
			
		String status = ""; // 成功かどうか
		String tag = ""; // 認識結果
		String str = "";
		int confidence = 0; // 信頼率
		ResultType recognitionResultType = null;
		ResultRecognition result = null; // returnされるオブジェクト
			
		HttpResponse<JsonNode> request = Unirest.post("https://face.p.mashape.com/faces/recognize")
					  .header("X-Mashape-Authorization", "tHSQ0Z9Up4GkUxysekx5SNRHEgFobE8n")
					  .field("api_key", API_KEY)
				      .field("api_secret", API_SECRET)
					  .field("uids", NAME_ARRAY)
					  .field("namespace", MY_NAMESPACE)
					  .field("files", imageFile)
					  .field("limit", "1")
					  .asJson();
		// status 成功ならsuccess エラーならfailureがかえってくる
		status = request.getBody().getObject().getString("status");
		if (status.equals("success")){
			// tagは認識者の名前
			try{
				tag = request.getBody().getObject().getJSONObject("tags").getJSONObject("uids").getString("uid");
				// tag は name@namespace の形でAPIから返されるのでnameの部分だけ取り出す
				tag = tag.substring(0,tag.indexOf("@"));
				
				// 信頼性も取り出せる
				confidence = Integer.parseInt(request.getBody().getObject().getJSONObject("tags").getJSONObject("uids").getString("confidence"));
	
				// 探し出した名前に関してデータベースから犯罪者か告発者かを探す
				if (isComplainantExist(tag)){
					str = findStatements(tag);
					if(str == "") {
						recognitionResultType = ResultType.COMP_WITH_NO_STATEMENT;
					}else{
						recognitionResultType = ResultType.COMP_WITH_STATEMENT;
					}
				}else if(isCriminalExist(tag)){
					str = findContents(tag);
					if(str == "") {
						throw new DataBaseErrorException("犯罪者への告発文が登録されていません");
					}else{
						recognitionResultType = ResultType.COMP_WITH_STATEMENT;
					}
					// resultに返答をする
					result = new ResultRecognition(recognitionResultType, confidence, str, tag);
				}
			}catch(JSONException e){
				// 	認識結果として誰も帰ってこなかった場合
				result = new ResultRecognition(ResultType.NONE, 0, "");
			}
			
		}else{
			throw new DataBaseErrorException("APIの問題で認識結果が取り出せません");
		}
		
		return result;
	}
	
	/**
	 * 告発者向けた反省文が登録されているかを探す.
	 * @param criminal 犯罪者の名前
	 * @return 反省文内容.ないなら空文字列
	 */
	private String findStatements(String criminal){
		for (CriminalAndComplainantDB e : this.db) {
			if(e.getCiminalName().equals(criminal)) return e.getStatemenets();
		}
		return "";
	}
	
	/**
	 * 犯罪者へ向けた告発文が登録されているかを探す.
	 * @param complainant 告発者の名前
	 * @return 告発文内容.ないなら空文字列
	 */
	private String findContents(String complainant){
		for (CriminalAndComplainantDB e : this.db) {
			if(e.getComplainantName().equals(complainant)) return e.getContents();
		}
		return "";
	}
	
	/** 犯罪者リストに登録されているかを調べる */
	private boolean isCriminalExist(String criminal){
		for (CriminalAndComplainantDB e : this.db) {
			if(e.getCiminalName().equals(criminal)) return true;
		}
		return false;
	}
	
	/** 告発者リストに登録されているかを調べる */
	private boolean isComplainantExist(String complainant){
		for (CriminalAndComplainantDB e : this.db) {
			if(e.getComplainantName().equals(complainant)) return true;
		}
		return false;
	}
		
		
}
