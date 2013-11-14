package lab.ishida.maekara;

import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

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
			mCamera = Camera.open();
			try {
				// プレビューをセットする
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
}