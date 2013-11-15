package lab.ishida.maekara;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lab.ishida.maekara.RequestHandler.Complaint;
import lab.ishida.maekara.facerecognition.core.FaceRecognition;

import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQUEST_CODE = 1;
	private static final String SEARCH_ACTION = "lab.ishida.maekara.SEARCH";
	private static final String APP_ID = "dj0zaiZpPUZHcnNZSUdGZHZwSCZzPWNvbnN1bWVyc2VjcmV0Jng9ZjE-";

	private TextView result;
	private FaceRecognition fRecognition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// テスト用のtextView
		result = (TextView) findViewById(R.id.textView1);

		// このボタンを押すと音声入力開始
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// インテント作成
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
							"RequestRecognition");

					// インテント発行
					startActivityForResult(intent, REQUEST_CODE);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合
					Toast.makeText(MainActivity.this,
							"ActivityNotFoundException", Toast.LENGTH_LONG)
							.show();
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
					intent.setClassName("lab.ishida.maekara", "lab.ishida.maekara.CameraActivity");

					// インテント発行
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合
					Toast.makeText(MainActivity.this,
							"ActivityNotFoundException", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 自分が投げたインテントであれば応答する
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
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
		}

		super.onActivityResult(requestCode, resultCode, data);
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
				fRecognition = new FaceRecognition();
				fRecognition.setNameAndContents(complaint.criminal,
						complaint.complainant, complaint.content);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}