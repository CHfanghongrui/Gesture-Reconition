#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <opencv/cv.h>
#include <opencv2/highgui/highgui.hpp>
#include "GestureReconition.h"


using namespace std;
using namespace cv;


bool common_should_use_bg = true;
double common_learning_rate = 0.5;


extern "C" {
JNIEXPORT jint JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeFindFingers(JNIEnv *, jlong, jboolean);
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeForegroundDetect(JNIEnv *, jlong, jlong, jboolean);
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeFindPolygonAndContour(JNIEnv *, jlong, jlong, jboolean);
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeDrawResult(JNIEnv *, jlong, jlong, jboolean);

/** 算手指數目 */
JNIEXPORT jint JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeFindFingers(JNIEnv * jenv, jlong addrRGB, jboolean learnBG) {
	Mat& mRgb = *(Mat*)addrRGB;
	if (mRgb.empty()) {
		return 0;
	}

	GestureReconition_Cfg tCfg;
	GestureReconition_Data tData;

	try {
		GR_LoadDefaultCfg(tCfg);

		bool shouldLearnBG = learnBG;
		GestureReconition(tCfg, mRgb, tData, common_should_use_bg, shouldLearnBG, common_learning_rate);
	}
	catch(cv::Exception& e)
	{
		LOGE("nativeFindFingers caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if(!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	}
	catch (...)
	{
		LOGE("nativeFindFingers caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, "Unknown exception in JNI code of FingerRecognizer.nativeFindFingers()");
	}

	return tData.lFingerNum;
}

/** 產生膚色的前景圖 */
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeForegroundDetect(JNIEnv * jenv, jlong addrRGB, jlong addrDescriptor, jboolean learnBG) {
	Mat& mRgb = *(Mat*)addrRGB;
	if (mRgb.empty()) {
		return;
	}
	Mat& pMatDesc = *(Mat*)addrDescriptor;

	try {
		Mat tForeground;
		Mat tSrcBuffer = mRgb.clone();

		bool shouldLearnBG = learnBG;
//		tForeground = ForegroundDetect(tSrcBuffer, true, g_BG_Model);
		tForeground = ForegroundDetect2(tSrcBuffer, common_should_use_bg, shouldLearnBG, common_learning_rate, g_BG_Model_2);
		tForeground.copyTo(pMatDesc);

	}
	catch(cv::Exception& e)
	{
		LOGE("nativeForegroundDetect caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if(!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	}
	catch (...)
	{
		LOGE("nativeForegroundDetect caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, "Unknown exception in JNI code of FingerRecognizer.nativeForegroundDetect()");
	}

}

/** 抓輪廓&包覆輪廓的多邊形 */
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeFindPolygonAndContour(JNIEnv * jenv, jlong addrRGB, jlong addrDescriptor, jboolean learnBG) {
	Mat& mRgb = *(Mat*)addrRGB;
	if (mRgb.empty()) {
		return;
	}
	Mat& pMatDesc = *(Mat*)addrDescriptor;

	try {
		GestureReconition_Cfg tCfg;
		GR_LoadDefaultCfg(tCfg);

		Mat tForeground, tHull;
		Mat tSrcBuffer = mRgb.clone();

		bool shouldLearnBG = learnBG;
//		tForeground = ForegroundDetect(tSrcBuffer, true, g_BG_Model);
		tForeground = ForegroundDetect2(tSrcBuffer, common_should_use_bg, shouldLearnBG, common_learning_rate, g_BG_Model_2);

		tHull = FindPolygonAndContour(tForeground, tCfg);
		tHull.copyTo(pMatDesc);

	}
	catch(cv::Exception& e)
    {
        LOGE("nativeFindPolygonAndContour caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGE("nativeFindPolygonAndContour caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of FingerRecognizer.nativeFindPolygonAndContour()");
    }

}

/** 取得計算手指數的結果圖 */
JNIEXPORT void JNICALL Java_org_opencv_samples_fingerrecognition_FdActivity_nativeDrawResult(JNIEnv * jenv, jlong addrRGB, jlong addrDescriptor, jboolean learnBG) {
	Mat& mRgb = *(Mat*)addrRGB;
	if (mRgb.empty()) {
		return;
	}
	Mat& pMatDesc = *(Mat*)addrDescriptor;

	try {
		GestureReconition_Cfg tCfg;
		GR_LoadDefaultCfg(tCfg);

		Mat tForeground, tResult;
		Mat tSrcBuffer = mRgb.clone();

		bool shouldLearnBG = learnBG;
//		tForeground = ForegroundDetect(tSrcBuffer, true, g_BG_Model);
		tForeground = ForegroundDetect2(tSrcBuffer, common_should_use_bg, shouldLearnBG, common_learning_rate, g_BG_Model_2);

		tResult = DrawResult(tForeground, tCfg);
		tResult.copyTo(pMatDesc);

	}
	catch(cv::Exception& e)
    {
        LOGE("nativeDrawResult caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGE("nativeDrawResult caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of FingerRecognizer.nativeDrawResult()");
    }

}

}
