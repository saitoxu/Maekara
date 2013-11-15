package lab.ishida.maekara;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.mashape.unirest.http.HttpResponse;

/**
 * カメラプレビューを表示する {@link Activity} です。
 */
public class CameraActivity extends Activity {

	/** カメラのハードウェアを操作する {@link Camera} クラスです。 */
	private Camera mCamera;

	/** カメラのプレビューを表示する {@link SurfaceView} です。 */
	private SurfaceView mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mView = new SurfaceView(this);
		setContentView(mView);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		SurfaceHolder holder = mView.getHolder();
		holder.addCallback(surfaceHolderCallback);
		
	}
	
	/** カメラのコールバックです。 */
	private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// 生成されたとき
			// mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);

			int cameraId = 0;
			mCamera = Camera.open(cameraId);
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewCallback(previewCallback);
			// リスナをセット
			// mCamera.setFaceDetectionListener(faceDetectionListener);
			// 顔検出の開始
			// mCamera.startFaceDetection();
			try {
				// プレビューを表示する
				mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		


		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// 変更されたとき
			Camera.Parameters parameters = mCamera.getParameters();
			List<Camera.Size> previewSizes = parameters
					.getSupportedPreviewSizes();
			Camera.Size previewSize = previewSizes.get(0);
			parameters.setPreviewSize(previewSize.width, previewSize.height);
			// width, heightを変更する
			mCamera.setParameters(parameters);
			mCamera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// 破棄されたとき
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	};

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// 読み込む範囲
			int previewWidth = camera.getParameters().getPreviewSize().width;
			int previewHeight = camera.getParameters().getPreviewSize().height;

			// プレビューデータから Bitmap を生成
			Bitmap bmp = getBitmapImageFromYUV(data, previewWidth,
					previewHeight);
			// 読み込む際に16bitに減色
			// Options opts = new Options();
			// RBG_565を選択（透過ならARGB_444）
			// opts.inPreferredConfig = Bitmap.Config.RGB_565;
			// または、16bitのコピーを生成
			// Bitmap bmp2 = bmp.copy(Bitmap.Config.RGB_565, true);

			// 画像を回転させてる
			int deg = 270;

			Matrix mat = new Matrix();
			mat.postRotate(deg);

			int sw = bmp.getWidth();
			int sh = bmp.getHeight();

			Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, sw, sh, mat,
					true);

			// ギャラリーに反映
			// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
			// Uri.parse("file://"
			// + Environment.getExternalStorageDirectory())));
			//
			// Log.d("CAMERA", "test");
			// Log.d("WIDTH", "width = " + previewWidth);
			// Log.d("HEIGHT", "height = " + previewHeight);
			//
			// String filePath = "/mnt/sdcard/DCIM/Camera/20131112_224441.jpg";
			// Bitmap image = BitmapFactory.decodeFile(filePath);
			// Bitmap bmp2 = image.copy(Bitmap.Config.RGB_565, true);

			// 顔検出
			FaceDetector.Face faces[] = new FaceDetector.Face[10];
			FaceDetector detector = new FaceDetector(rotatedBmp.getWidth(),
					rotatedBmp.getHeight(), faces.length);
			// FaceDetector detector = new FaceDetector(previewWidth,
			// previewHeight, faces.length);
			int num = detector.findFaces(rotatedBmp, faces);

			Log.d("FACE_NUM", "num = " + num);
			// Log.d("FACE_DETECTION", "confidence = " +
			// Float.toString(faces[0].confidence()));

			if (num == 1) {
				// JPEGに変換してファイル保存
				try {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					rotatedBmp.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
					byte[] bytes = byteArrayOutputStream.toByteArray(); // 送信用bitmap
					Log.d("Request", "bytes = " + bytes);
					
					// 写真が無事とれているなら
					if (bytes != null){
					
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						//TODO: rotatedBmpをわたす
						bundle.putInt("key.photoData", 123456789);
					
						bundle.putByteArray("photo", bytes);
						
						intent.putExtras(bundle);
						// setResult() で bundle を載せた
						// 送るIntent dataをセットする
	
						// 第一引数は…Activity.RESULT_OK,
						// Activity.RESULT_CANCELED など
						setResult(RESULT_OK, intent);
						
						finish();
					}
					
//					Log.d("FACE_PATH", Environment.getExternalStorageDirectory().getPath());
					// finish() で終わらせて
					// Intent data を送る
				} catch (Exception e) {
					e.printStackTrace();
				}
				//
				// // finish();
				// // try {
				// // // インテント作成
				// // Intent intent = new Intent();
				// // intent.setClassName("lab.ishida.maekara",
				// // "lab.ishida.maekara.MainActivity");
				// // intent.putExtra("lab.ishida.maekara.filepath", Environment
				// // .getExternalStorageDirectory().getPath()
				// // + "/sample.jpg");
				// //
				// // // インテント発行
				// // startActivity(intent);
				// // } catch (ActivityNotFoundException e) {
				// // // このインテントに応答できるアクティビティがインストールされていない場合
				// // Toast.makeText(CameraActivity.this,
				// // "ActivityNotFoundException", Toast.LENGTH_LONG)
				// // .show();
				// // }
				// }

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
			YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width,
					height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
			byte[] jdata = baos.toByteArray();
			BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
			bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length,
					bitmapFatoryOptions);
			return bmp;
		}
	};
}