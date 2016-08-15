package org.opencv.samples.fingerrecognition;

import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OrientateCameraView extends JavaCameraView {

	private int mCameraID = 0;
	private Boolean mTouchMoved = false;
	
	public OrientateCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected boolean initializeCamera(int width, int height) {
		boolean superReturn = super.initializeCamera(width, height);
		
		try {
			
			findCameraID();
			setCameraDisplayOrientation(mCameraID);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		if (mCamera != null) {
			setOnTouchListener(onTouchView);
		}
		else {
			setOnTouchListener(null);
		}
		
		return superReturn;
	}
	
	@Override
	protected void releaseCamera() {
		mCameraID = 0;
		
		setOnTouchListener(null);
		
		super.releaseCamera();
	}
	
	@Override
	protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth,
			int surfaceHeight) {
		
		Size frameSize = super.calculateCameraFrameSize(supportedSizes, accessor, surfaceWidth, surfaceHeight);
		
		findCameraID();
		
		int rotation = calculatePrefferredOrientation(mCameraID);
	    if (rotation%180 == 90) {
//	    	double height = frameSize.height;
//	    	frameSize.height = frameSize.width;
//	    	frameSize.width = height;
	    }
		
		return frameSize;
	}
	
	private void findCameraID() {
		if (mCameraIndex == CAMERA_ID_ANY) {
			mCameraID = 0;
		}
		else if (Camera.getNumberOfCameras() > 0) {
			int numOfCameras = Camera.getNumberOfCameras();
			Camera.CameraInfo info = new Camera.CameraInfo();
			for (int i = 0; i < numOfCameras; i ++) {
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && 
						mCameraIndex == CAMERA_ID_BACK) {
					
					mCameraID = i;
					break;
				}
				else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && 
						mCameraIndex == CAMERA_ID_FRONT) {
					
					mCameraID = i;
					break;
				}
			}
		}
	}
	
	private int calculatePrefferredOrientation(int cameraID) {
		if (cameraID >= Camera.getNumberOfCameras()) {
			return 0;
		}
		
		Camera.CameraInfo info = new Camera.CameraInfo();
	    Camera.getCameraInfo(cameraID, info);
	    
	    return calculatePrefferredOrientation(info);
	}
	
	private int calculatePrefferredOrientation(Camera.CameraInfo info) {
		if (info == null) {
			return 0;
		}
		
	    int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//	    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	    int degrees = 0;
	    switch (rotation) {
	    	case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
	    }

	    int result;
	    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	        result = (info.orientation + degrees) % 360;
	        result = (360 - result) % 360;  // compensate the mirror
	    }
	    else {  // back-facing
	        result = (info.orientation - degrees + 360) % 360;
	    }
	    
	    return result;
	}
	
	private void setCameraDisplayOrientation(int cameraID) {
		if (cameraID >= Camera.getNumberOfCameras()) {
			return;
		}
		
		Camera.CameraInfo info = new Camera.CameraInfo();
	    Camera.getCameraInfo(cameraID, info);
		
	    setCameraDisplayOrientation(info);
	}
	
	private void setCameraDisplayOrientation(Camera.CameraInfo info) {
		if (mCamera == null) {
			return;
		}
		if (info == null) {
			return;
		}
		
	    int result = calculatePrefferredOrientation(info);
//	    mCamera.setDisplayOrientation(result);
	}
	
	private View.OnTouchListener onTouchView = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mCamera == null) {
				return false;
			}
			
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mTouchMoved = false;
				break;
			case MotionEvent.ACTION_MOVE:
				mTouchMoved = true;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				
				if (mTouchMoved) {
				}
				
				
				
				break;
			default:
				break;
			}
			
			return false;
		}
	};
	
}
