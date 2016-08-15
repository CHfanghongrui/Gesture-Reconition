package org.opencv.samples.fingerrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.samples.fingerrecognition.FingerRecognizer.GestureReconition_Cfg;
import org.opencv.samples.fingerrecognition.R;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import tools.userinterface.UserInterfaceTool;

@SuppressLint({ "NewApi", "InlinedApi" })
public class FdActivity extends Activity implements CvCameraViewListener2 {
	
	private static final Boolean useNativeAPI = true;
	
    private static final String    TAG                 = "OCVSample::Activity";

    private enum ViewMode {
    	/** 原圖 */
    	Original,
    	/** 前景圖 */
    	Foreground,
    	/** 輪廓 */
    	Polygon_Contour,
    	/** 結果 */
    	Result
    }
    private ViewMode				mViewMode = ViewMode.Original;
    
    private Mat                    	mRgba;
    
    private Button               	mItemPreviewSwitch;
    private Button               	mItemPreviewOrig;
    private Button               	mItemPreviewForeground;
    private Button               	mItemPreviewPolygon_Contour;
    private Button               	mItemPreviewResult;
    
    private CameraBridgeViewBase  	mOpenCvCameraView;
    private int 					cameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private long					cameraStartSystemTime = 0;
    
    private boolean isLearning = true;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("finger_recognizer_jni");
                    
                    FingerRecognizer.g_BG_Model = new BackgroundSubtractorMOG(200, 5, 0.7, 10);
                    isLearning = true;
                    
                    cameraStartSystemTime = System.currentTimeMillis();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    /** 進入點 */
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // 設定畫面
        setContentView(R.layout.face_detect_surface_view);
        
        int height = UserInterfaceTool.getScreenHeightPixels(this);
        View fl_camera = findViewById(R.id.fl_camera);
        UserInterfaceTool.setViewSize(fl_camera, ViewGroup.LayoutParams.MATCH_PARENT, (int) (height*0.9));
        View ll_tools = findViewById(R.id.ll_tools);
        UserInterfaceTool.setViewSize(ll_tools, ViewGroup.LayoutParams.MATCH_PARENT, (int) (height*0.1));
        
        // OpenCV Camera
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        mOpenCvCameraView.setCameraIndex(cameraIndex);
//        mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
        
        // 按鈕
        mItemPreviewSwitch = (Button) findViewById(R.id.btn_sw);
        mItemPreviewOrig = (Button) findViewById(R.id.btn_orig);
        mItemPreviewForeground = (Button) findViewById(R.id.btn_foreground);
        mItemPreviewPolygon_Contour = (Button) findViewById(R.id.btn_polygon_contour);
        mItemPreviewResult = (Button) findViewById(R.id.btn_result);
        
        if (Camera.getNumberOfCameras() <= 1) {
        	mItemPreviewSwitch.setVisibility(View.GONE);
        }
        mItemPreviewSwitch.setOnClickListener(onClickButton);
        mItemPreviewOrig.setOnClickListener(onClickButton);
        mItemPreviewForeground.setOnClickListener(onClickButton);
        mItemPreviewPolygon_Contour.setOnClickListener(onClickButton);
        mItemPreviewResult.setOnClickListener(onClickButton);
        
        // 打印app版本
        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        
        PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
	        tv_version.setText(pinfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        	mOpenCvCameraView.disableFpsMeter();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        // 有把OpenCV編譯好的lib copy進project裡，不需安裝OpenCV Manager
        if (OpenCVLoader.initDebug()) {
        	System.loadLibrary("finger_recognizer_jni");
            
        	FingerRecognizer.g_BG_Model = getDefaultBGSubtractorMOG();
        	FingerRecognizer.g_BG_Model_2 = getDefaultBGSubtractorMOG2();
        	
//        	mOpenCvCameraView.setRotation(90);
        	
        	isLearning = true;
        	cameraStartSystemTime = System.currentTimeMillis();
            mOpenCvCameraView.enableView();
            mOpenCvCameraView.enableFpsMeter();
        }
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return false;
    }
    
    private View.OnClickListener onClickButton = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == mItemPreviewSwitch) {
	            if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK) {
	            	cameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT;
	            }
	            else {
	            	cameraIndex = CameraBridgeViewBase.CAMERA_ID_BACK;
	            }
	            mOpenCvCameraView.disableView();
	            mOpenCvCameraView.setCameraIndex(cameraIndex);
	            
	            isLearning = true;
	            
	            cameraStartSystemTime = System.currentTimeMillis();
	            FingerRecognizer.g_BG_Model = getDefaultBGSubtractorMOG();
	            FingerRecognizer.g_BG_Model_2 = getDefaultBGSubtractorMOG2();
	            mOpenCvCameraView.enableView();
	        }
	        else if (v == mItemPreviewOrig) {
	            mViewMode = ViewMode.Original;
	        } 
	        else if (v == mItemPreviewForeground) {
	            mViewMode = ViewMode.Foreground;
	        } 
	        else if (v == mItemPreviewPolygon_Contour) {
	            mViewMode = ViewMode.Polygon_Contour;
	        } 
	        else if (v == mItemPreviewResult) {
	            mViewMode = ViewMode.Result;
	        }
		}
	};
    
	/** OpenCV Camera callback */
	
    public void onCameraViewStarted(int width, int height) {
    	mRgba = new Mat(height, width, CvType.CV_8UC4);
//    	mRgba = new Mat(width, height, CvType.CV_8UC4);
    	
    	cameraStartSystemTime = System.currentTimeMillis();
    }
    
    public void onCameraViewStopped() {
        mRgba.release();
    }
    
    /** 每個frame呼叫 */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	Mat imgBuffer = inputFrame.rgba();
    	if (false) {
    		return inputFrame.rgba();
    	}
    	
    	Boolean learnBG = isLearning;
    	long currentSystemTime = System.currentTimeMillis();
    	if (currentSystemTime-cameraStartSystemTime >= 500 && learnBG) {
    		isLearning = false;
    		learnBG = false;
    		
    		runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(FdActivity.this, "Ready", Toast.LENGTH_LONG).show();
				}
			});
    		
    	}
    	Log.e("TOTAL TIME", "total="+(currentSystemTime-cameraStartSystemTime)/1000.0);
    	
//    	Mat mRgbaT = imgBuffer.t();
//    	Core.flip(imgBuffer.t(), mRgbaT, 1);
//    	Imgproc.resize(mRgbaT, mRgbaT, imgBuffer.size());
//    	imgBuffer = mRgbaT;
    	
        if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) {
        	// 前鏡頭，先對mat做鏡像
        	Core.flip(imgBuffer, imgBuffer, 1);
        }
        else {
//        	Core.flip(imgBuffer, imgBuffer, 1);
        }
        // Convert RGBA format to RGB
        Imgproc.cvtColor(imgBuffer, imgBuffer, Imgproc.COLOR_RGBA2RGB);
        
        // useNativeAPI: 是否使用c-code來處理Mat
        
        final ViewMode viewMode = mViewMode;
        switch (viewMode) {
		case Foreground: {
			if (useNativeAPI) {
				nativeForegroundDetect(imgBuffer.getNativeObjAddr(), mRgba.getNativeObjAddr(), learnBG);
			}
			else {
				FingerRecognizer.ForegroundDetect(imgBuffer, mRgba, false, FingerRecognizer.g_BG_Model);
			}
			break;
		}
		case Polygon_Contour: {
			if (useNativeAPI) {
				nativeFindPolygonAndContour(imgBuffer.getNativeObjAddr(), mRgba.getNativeObjAddr(), learnBG);
			}
			else {
				FingerRecognizer.FindPolygonAndContour(imgBuffer, mRgba, GestureReconition_Cfg.instance());
			}
			break;
		}
		case Result: {
			if (useNativeAPI) {
				nativeDrawResult(imgBuffer.getNativeObjAddr(), mRgba.getNativeObjAddr(), learnBG);
			}
			else {
				FingerRecognizer.DrawResult(imgBuffer, mRgba, GestureReconition_Cfg.instance());
			}
			break;
		}
		default: {
			imgBuffer.copyTo(mRgba);
			
			int fingerNumber = nativeFindFingers(imgBuffer.getNativeObjAddr(), learnBG);
//	        Log.e("Finger Number", ""+fingerNumber);
			String strFrame = "Finger Num = "+fingerNumber;
	    	Core.putText(mRgba, strFrame, new Point(20, 20), 1, 1.0, new Scalar(255, 0, 0), 2);
			break;
		}
		}
        
        return mRgba;
    }
    
    /** 初始化背景模型 */
    private BackgroundSubtractorMOG getDefaultBGSubtractorMOG() {
    	return new BackgroundSubtractorMOG(200, 5, 0.7, 10);
//    	return new BackgroundSubtractorMOG(200, 5, 0.9, 10);
    }
    /** 初始化背景模型 */
    private BackgroundSubtractorMOG2 getDefaultBGSubtractorMOG2() {
    	return new BackgroundSubtractorMOG2(20, 16, false);
    }
    
    // JNI native functions
    /** 計算手指數目 */
    private native int nativeFindFingers(long matAddrRGB, boolean learnBG);
    /** 產生前景二值化Mat */
    private native void nativeForegroundDetect(long matAddrRGB, long matAddrDescriptor, boolean learnBG);
    /** 找輪廓 */
    private native void nativeFindPolygonAndContour(long matAddrRGB, long matAddrDescriptor, boolean learnBG);
    /** 計算手指數目-結果圖 */
    private native void nativeDrawResult(long matAddrRGB, long matAddrDescriptor, boolean learnBG);
}
