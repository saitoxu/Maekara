package lab.ishida.maekara;

import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

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
			mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
			mCamera.setDisplayOrientation(90);
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
			
			Log.d("CAMERA", "test");
			// あとはBitmapを好きに使う。
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