package lab.ishida.maekara.facerecognition.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lab.ishida.maekara.facerecognition.core.ResultRecognition.ResultType;
import lab.ishida.maekara.facerecognition.model.ComplainantNameAndContents;
import lab.ishida.maekara.facerecognition.model.CriminalNameAndContents;

import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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
	
	public FaceRecognition() {
		this.criminalList = new ArrayList<CriminalNameAndContents>();
		this.complainantList = new ArrayList<ComplainantNameAndContents>();
	}

	/**
	 * 告発者からの告発文の内容をデータベースに記録する
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
	 * 告発文を聞いた犯罪者が喋った反省文の内容をセットする
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
	 * 認識器にかけてその顔写真の主が誰かを判定する.
	 * 犯罪者なら犯罪者にするべきお告げの内容を，告発者なら告発する内容を{@link ResultRecognition}オブジェクトで
	 * まとめて返す.
	 * @param imageFile 認識したい画像ファイル
	 * @return ResultType ResultType列挙型を返す
	 * @throws UnirestException 画像認識APIが使えなかったときにエラー
	 * @throws JSONException JSONのパースエラー
	 * @return ResultRecognitionクラスのオブジェクト
	 * @see ResultRecognition
	 */
	public ResultRecognition recognition(File imageFile)throws UnirestException, JSONException{
		
//			faces/detect – detects faces in specified images, returns face tags (every tag has unique tag id - tid).
//			tags/save – saves specified face tags (by tid) with user specified user id(eg. mark@docs, where docs - data namespace name).
//			faces/train – checks changes for specified user ids (eg. new tags were added using tags/save or removed using tags/remove) and either creates/updates/removes face template for specified user id from data namespace.
			
		String status; // 成功かどうか
		String tag; // 認識結果
		int confidence; // 信頼率
		ResultRecognition result = null; // returnされるオブジェクト
		ResultType recognitionResultType = null;
			
		HttpResponse<JsonNode> request = Unirest.post("https://face.p.mashape.com/faces/recognize?api_key=%3Capi_key%3E&api_secret=%3Capi_secret%3E")
					  .header("X-Mashape-Authorization", "tHSQ0Z9Up4GkUxysekx5SNRHEgFobE8n")
					  .field("uids", "all")
					  .field("namespace", "MyNamespace")
					  .field("detector", "Aggressive")
					  .field("attributes", "all")
					  .field("files", new File("<file goes here>"))
					  .field("limit", "1")
					  .asJson();
		// status 成功ならsuccess エラーならfailureがかえってくる
		status = request.getBody().getObject().getString("status");
		if (status.equals("success")){
			tag = request.getBody().getObject().getJSONObject("tags").getJSONObject("uids").getString("uid");
			// tag は name@namespace の形でAPIから返されるので編集する
			
			
			
			confidence = Integer.parseInt(request.getBody().getObject().getJSONObject("tags").getJSONObject("uids").getString("confidence"));

			// tagから@の前を切り取る処理
			
			// 名前に関してデータベースから犯罪者か告発者かを探す
			
			// 画像が犯罪者なら犯罪者にあてた告発文をつけて返す
			
			// 画像が告発者なら犯罪者からの反省文をつけて返す
				// 反省文が既にセットされている場合
			
				// ない場合
			
			
			// resultに返答をする
			result = new ResultRecognition(recognitionResultType, confidence, "");
			
			
		}else{
			
		}
		
		return result;
		
		// getbody
		//System.out.println(response.getBody().getJSONObject(0).getString("condition"));
		
	}
	
	/**
	 * 告発者向けた反省文が登録されているかを探す.
	 * @param criminal 犯罪者の名前
	 * @return 反省文内容.ないなら空文字列
	 */
	private String findStatements(String criminal){
		// TODO implementation
		return "";
	}
	
	/**
	 * 犯罪者へ向けた告発文が登録されているかを探す.
	 * @param complainant 告発者の名前
	 * @return 反省文内容.ないなら空文字列
	 */
	private String findContents(String complainant){
		// TODO implementation
		return "";
	}
		
		
}
