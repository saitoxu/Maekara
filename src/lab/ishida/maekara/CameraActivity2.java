package lab.ishida.maekara;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;

public class CameraActivity2 extends Activity implements CvCameraViewListener {
	// カメラビューのインスタンス
	// CameraBridgeViewBase は JavaCameraView/NativeCameraView のスーパークラス
	private CameraBridgeViewBase mCameraView;
	private Mat mOutputFrame;

	// ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected)
	// public abstract class BaseLoaderCallback implements
	// LoaderCallbackInterface
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			// 読み込みが成功したらカメラプレビューを開始
			case LoaderCallbackInterface.SUCCESS:
				mCameraView.enableView();
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_view);
		// カメラビューのインスタンスを変数にバインド
		mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
		// リスナーの設定 (後述)
		mCameraView.setCvCameraViewListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 非同期でライブラリの読み込み/初期化を行う
		// static boolean initAsync(String Version, Context AppContext,
		// LoaderCallbackInterface Callback)
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	@Override
	public void onPause() {
		if (mCameraView != null) {
			mCameraView.disableView();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// カメラプレビュー開始時に呼ばれる
		mOutputFrame = new Mat(height, width, CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		// カメラプレビュー終了時に呼ばれる
		mOutputFrame.release();
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		return inputFrame;
	}
}