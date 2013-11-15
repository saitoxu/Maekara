package lab.ishida.maekara;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
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
    
    /**
     * 画面の向きを取得する(縦ならtrue)
     */
    private boolean isPortrait() {
        return (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }
    
    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
//            Log.d("onFaceDetection", "顔検出数:" + faces.id);
            // 何かする
        }
    };
 
    /** カメラのコールバックです。 */
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 生成されたとき
//            mCamera = Camera.open();
            
            // nexus 用
            int cameraId = 0;
            mCamera = Camera.open(cameraId);
            
            Log.d("onFaceDetection", "顔検出数:" +mCamera.getParameters().getMaxNumDetectedFaces());
            
//            // リスナをセット
//            mCamera.setFaceDetectionListener(faceDetectionListener);
//            
//            // 顔検出の開始
//            mCamera.startFaceDetection();
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
            
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
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