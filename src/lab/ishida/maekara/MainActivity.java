package lab.ishida.maekara;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Future;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lab.ishida.maekara.RequestHandler.Complaint;
import lab.ishida.maekara.facerecognition.core.FaceRecognition;
import lab.ishida.maekara.facerecognition.core.ResultRecognition;
import lab.ishida.maekara.facerecognition.core.ResultType;
import lab.ishida.maekara.facerecognition.exception.DataBaseErrorException;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.request.HttpRequest;

public class MainActivity extends FragmentActivity implements
		TextToSpeech.OnInitListener, LoaderCallbacks<JSONObject> {

    private static final String KEY_URL_STR = "urlStr";
    private static final String KEY_URL_POST = "urlPOST";
	private static final int CODE_SPEECH_RECOGNITION = 1;
	private static final int CODE_SPEECH_STATEMENT = 3;
	private static final int CODE_CAMERA = 2;

	private static final String SEARCH_ACTION = "lab.ishida.maekara.SEARCH";
	private static final String APP_ID = "dj0zaiZpPUZHcnNZSUdGZHZwSCZzPWNvbnN1bWVyc2VjcmV0Jng9ZjE-";
	
	private TextView result;
	private FaceRecognition fRecognition;
	// 音声合成用
	TextToSpeech tts = null;
	// 画像認識用
	FaceRecognition face = null;
	// 画像
	Bitmap photo = null;
	
	// API キー
	private final String API_KEY = "122dd14be1c44bcb90d1243979defbda";
	// API シークレット　キー
	private final String API_SECRET = "79ead5bc5cb4452e9115a2dd65c10541";
	// 学習済みの人たちの羅列
	private final String NAME_ARRAY = "saito,kingetsu";
	// 名前空間
	private final String MY_NAMESPACE = "maekara";
	

	/**
	 * 話す
	 * 
	 * @param str
	 *            話す内容
	 * @param isWait
	 *            話し終わるまで待つのか
	 */
	private void speak(String str) {
		tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * 話し終わるのを待つ
	 */
	private void waitSpeaking() {
		boolean speakingEnd = tts.isSpeaking();
		do {
			speakingEnd = tts.isSpeaking();
		} while (speakingEnd);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    // Bundleにはパラメータを保存する（1）

		tts = new TextToSpeech(this, this);
		face = new FaceRecognition();

		setContentView(R.layout.activity_main);
		// テスト用のtextView
		result = (TextView) findViewById(R.id.textView1);

		// このボタンを押すと音声入力開始
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				speak("お前の、不満を、述べたまえ");
				waitSpeaking();

				try {
					// インテント作成
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
							"RequestRecognition");

					// インテント発行
					startActivityForResult(intent, CODE_SPEECH_RECOGNITION);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合
					Toast.makeText(MainActivity.this,
							"ActivityNotFoundException There is no recognizer",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// このボタンを押すと顔認識開始
		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// インテント作成
					Intent intent = new Intent();
					intent.setClassName("lab.ishida.maekara",
							"lab.ishida.maekara.CameraActivity");

					// インテント発行
					// startActivity(intent);
					startActivityForResult(intent, CODE_CAMERA);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合
					// Toast.makeText(MainActivity.this,
					// "ActivityNotFoundException There is no camera",
					// Toast.LENGTH_LONG)
					// .show();
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 自分が投げたインテントであれば応答する
		if (requestCode == CODE_SPEECH_RECOGNITION && resultCode == RESULT_OK) {

			speak("それは、絶対許せんな。天罰を下してやる！");

			// 結果文字列リスト
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			// 文字列結果が複数帰ってきた場合、どう選ぶかが問題
			// for (int i = 0; i < results.size(); i++) {
			for (int i = 0; i < 0; i++) {
				// Toast.makeText(this, results.get(i),
				// Toast.LENGTH_SHORT).show();
				Log.d("Request", "result = " + results.get(i));
			}

			// Yahoo!日本語形態素解析に結果を投げる
			try {
				// Yahoo!に投げるURL
				// results.add(0, "斎藤です金月さんご飯おごって下さい");
				String url = "http://jlp.yahooapis.jp/MAService/V1/parse?"
						+ "appid=" + APP_ID + "&sentence="
						+ URLEncoder.encode(results.get(0), "UTF-8")
						+ "&results=ma";
				HttpGet searchRequest = new HttpGet(new URI(url));
				RestTask task = new RestTask(this, SEARCH_ACTION);
				task.execute(searchRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 話し終わるのを待つ
			waitSpeaking();
			Log.d("Request", "end speaking");

			// カメラモード
		} else if (requestCode == CODE_CAMERA && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Log.d("Request", "result = " + extras.getInt("key.photoData"));
			byte[] jdata = extras.getByteArray("photo");
			Log.d("Request", "photo = " + jdata);
//			BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
//			bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
//			photo = BitmapFactory.decodeByteArray(jdata, 0, jdata.length,
//					bitmapFatoryOptions);
		    // main.xmlのGUIにはGraphicsViewがないため、
	        // Viewクラスを継承したGraphicsViewを自分で作成
	        // 画面に登録する
			if (jdata != null){
				// debug用
				// setContentView(new GraphicsViewTest(this));
				Log.d("Request", "jdata exists");
				File f = new File(getCacheDir(), "cache.bmp");
				Log.d("Request", f.getPath());
				if (f.exists()){
					Log.d("Request", "deleted tmp file");
				    f.delete();
				}
				FileOutputStream fos = null;
				ResultRecognition result = null;
				try {
					if(f.createNewFile()){
						Log.d("Request", "created new file");
						fos = new FileOutputStream(f);
						fos.write(jdata);
//						result = fRecognition.recognition(f);
						
						if(f!=null) Log.d("Request-file","file"+f.toString());
						
						Bundle args = new Bundle(1);
						String url = "http://api.skybiometry.com/fc/account/limits.json";
						String post = "api_key="+API_KEY+"&api_secret=" + API_SECRET;
				        args.putString(KEY_URL_STR, url);
				        args.putString(KEY_URL_POST, post);
				        getSupportLoaderManager().initLoader(0, args, this);
						
						
						fos.flush();
						fos.close();
						
					}else{
						Log.d("Request", "failed");
						
					}

					

				} catch (Exception e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				
			}
		}else if (requestCode == CODE_SPEECH_STATEMENT && resultCode == RESULT_OK){
			//反省文
			
			// 結果文字列リスト
						ArrayList<String> results = data
								.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

						// 文字列結果が複数帰ってきた場合、どう選ぶかが問題
						// for (int i = 0; i < results.size(); i++) {
						for (int i = 0; i < 0; i++) {
							// Toast.makeText(this, results.get(i),
							// Toast.LENGTH_SHORT).show();
							Log.d("Request", "result = " + results.get(i));
						}

						// Yahoo!日本語形態素解析に結果を投げる
						try {
							// Yahoo!に投げるURL
							// results.add(0, "斎藤です金月さんご飯おごって下さい");
							String url = "http://jlp.yahooapis.jp/MAService/V1/parse?"
									+ "appid=" + APP_ID + "&sentence="
									+ URLEncoder.encode(results.get(0), "UTF-8")
									+ "&results=ma";
							HttpGet searchRequest = new HttpGet(new URI(url));
							RestTask task = new RestTask(this, SEARCH_ACTION);
							task.execute(searchRequest);
						} catch (Exception e) {
							e.printStackTrace();
						}

						// 話し終わるのを待つ
						waitSpeaking();
						Log.d("Request", "end speaking");
			
		}
		super.onActivityResult(requestCode, resultCode, data);
		
		
	}

	// Viewクラスを継承して、自分で作成したGraphicsViewTest
    private class GraphicsViewTest extends View {
        // ↓これはeclipseが自動的に「記載をしろ！」と警告を出してくる。
        // 警告にそって記載する。
        public GraphicsViewTest(Context context) {
            super(context);
            // TODO 自動生成されたコンストラクター・スタブ
        }

        // onDrawをオーバーライドして描画処理を作成する。
        @Override
        protected void onDraw(Canvas canvas) {
            //そのリソース群からhato/hatostrというファイル名を指定して
            //画像描画用クラス(Bitmapクラス)に変換する
            Bitmap img0 = photo;

            //その画像描画用クラスをdrawBitmapで描画します。
            //引数４つ目にPaintクラスがあるためnewで渡しておきます。
            canvas.drawBitmap(img0,0,0,new Paint());
        }
    }

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(SEARCH_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public void onDestroy() {
		
		Log.d("Request", "tts shutdown");

		// ttsのシャットダウン
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
			result.setText(response);

			try {
				// ここでSAXを使ってXMLを解析
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser p = factory.newSAXParser();
				RequestHandler parser = new RequestHandler();
				// 解析処理の実行
				p.parse(new InputSource(new StringReader(response)), parser);
				Complaint complaint = parser.getParsedComplaint();
				Log.d("COMPLAINANT", complaint.complainant);
				Log.d("CRIMINAL", complaint.criminal);
				Log.d("CONTENT", complaint.content);

				// DBに告発者の名前、犯罪者の名前、内容を保存
				fRecognition.setNameAndContents(complaint.criminal,
						complaint.complainant, complaint.content);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 音声認識の初期設定用メソッド
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// 音声合成の設定を行う

			float pitch = 1.0f; // 音の高低
			float rate = 1.0f; // 話すスピード
			// Locale locale = Locale.US; // 対象言語のロケール
			// ※ロケールの一覧表
			// http://docs.oracle.com/javase/jp/1.5.0/api/java/util/Locale.html

			tts.setPitch(pitch);
			tts.setSpeechRate(rate);
		}
	}

	@Override
	public Loader<JSONObject> onCreateLoader(int arg0, Bundle bundle) {
	      String urlStr = bundle.getString(KEY_URL_STR);
	      String urlPOST = bundle.getString(KEY_URL_POST);
	        if (! TextUtils.isEmpty(urlStr)) {
	            return new AsyncFetchJSONLoader(getApplication(), urlStr, urlPOST);
	        }
	        return null;
	}

	@Override
	public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
		// status 成功ならsuccess エラーならfailureがかえってくる
		String status;
		try {
			
			status = data.getString("status");
			Log.d("status::::", status);
			if(status != null){
				speak("おまえは犯罪者だ！");
				waitSpeaking();
				return;
			}
			status = data.getString("status");
			String tag = "";
			int confidence = 0;
			String str = "";
			Log.d("status", status);
			if (status.equals("success")){
				// tagは認識者の名前
				try{
					tag = data.getJSONObject("tags").getJSONObject("uids").getString("uid");
					// tag は name@namespace の形でAPIから返されるのでnameの部分だけ取り出す
					tag = tag.substring(0,tag.indexOf("@"));
					
					// 信頼性も取り出せる
					confidence = Integer.parseInt(data.getJSONObject("tags").getJSONObject("uids").getString("confidence"));
		
					// 探し出した名前に関してデータベースから犯罪者か告発者かを探す
					if (fRecognition.isComplainantExist(tag)){
						str = fRecognition.findStatements(tag);
						if(str == "") {
							speak("まだ反省文が来ていない");
							waitSpeaking();
						}else{
							speak("反省していました。こう言っていましたよ。");
							speak(str);
							speak("天罰はくだりました。");
							waitSpeaking();
							
						}
					}else if(fRecognition.isCriminalExist(tag)){
						str = fRecognition.findContents(tag);
						if(str == "") {
							throw new DataBaseErrorException("犯罪者への告発文が登録されていません");
						}else{
							speak("ちょっとあなた、こういう文句が来ています");
							speak(str);
							speak("反省文を述べなさい");
							waitSpeaking();
							// インテント作成
							Intent intent = new Intent(
									RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
							intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
									RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
							intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
									"RequestRecognition");

							// インテント発行
							startActivityForResult(intent, CODE_SPEECH_STATEMENT);
						}
					}
				}catch(JSONException e){
					Log.d("LOG", "JSON parse error");
				} catch (DataBaseErrorException e) {
					Log.d("LOG", "DataBaseErrorException");
				}
				
			}else{
				try {
					throw new DataBaseErrorException("APIの問題で認識結果が取り出せません");
				} catch (DataBaseErrorException e) {
					Log.d("LOG", "APIの問題で認識結果が取り出せません");
				}
			}
		} catch (JSONException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		
		
	}

	@Override
	public void onLoaderReset(Loader<JSONObject> arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
}