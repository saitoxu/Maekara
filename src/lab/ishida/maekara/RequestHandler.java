package lab.ishida.maekara;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 告発者からの告発を構文解析するクラス
 * 「斎藤です。後藤さん、コーヒーの値段を安くしてください。」という
 * フォーマットで告発されるので、「です」と「さん」で分けて、
 * 告発者の名前、犯罪者の名前、告発内容に分ける。
 */
public class RequestHandler extends DefaultHandler {
	public class Complaint {
		public String complainant; /* 告発者 */
		public String criminal; /* 犯罪者 */
		public String content; /* 告発内容 */
	}

	private StringBuffer buf;
	private ArrayList<String> wordList = new ArrayList<String>(); /* 出てきたwordをとりあえず突っ込んでおく */
	private Complaint complaint;
	private boolean onlyWordList = true; /* 2つ目のword_listを無視する */

	// wordListからcomlaintを作成
	public Complaint getParsedComplaint() {
		String temp = "";
		for (String word : wordList) {
			if (word.equals("です")) {
				complaint.complainant = temp;
				temp = "";
			} else if (word.equals("さん")) {
				complaint.criminal = temp;
				temp = "";
			} else {
				temp += word;
			}
			// Log.d("WORD", "word: " + word);
		}
		complaint.content = temp;

		return complaint;
	}

	// 各要素の始まりで呼び出される
	@Override
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if ("word_list".equals(name) && onlyWordList) {
			complaint = new Complaint();
			onlyWordList = false;
		} else if ("surface".equals(name)) {
			buf = new StringBuffer();
		}
	}

	// 各要素の終わりに呼び出される
	@Override
	public void endElement(String uri, String name, String qName) {
		if ("surface".equals(name)) {
			wordList.add(buf.toString());
		}

		buf = null;
	}

	// 要素内のキャラクタデータについて呼び出される
	@Override
	public void characters(char ch[], int start, int length) {
		// バッファが初期化されていなければ何もしない
		if (buf != null) {
			for (int i = start; i < start + length; i++) {
				buf.append(ch[i]);
			}
		}
	}
}