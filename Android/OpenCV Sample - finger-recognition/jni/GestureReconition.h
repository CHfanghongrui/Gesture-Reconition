#pragma once

using namespace std;
using namespace cv;
#include <opencv2/core/core.hpp>
#include <opencv2/video/background_segm.hpp>
#include <errno.h>
#include <android/log.h>

#define  LOG_TAG    "OCV:libfinger_recognizer_jni"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


#define CONTOURS_NUM_TH 70 // 100

#define DISTANCE_TH 15

#define DEPTH_TH_LOW 5

#define DEPTH_TH_HIGH 80

#define FINGER_ANGLE_TH 90

#define RESIZE_SCALE 0 //5

#define DEBUG_LOG_IMG 0 //1

#define DEBUG_LOG_MSG 0

typedef struct
{
	/** \brief Contours index */
	int lIndex;

	/** \brief 0: Hull else: Depth of defect */
	int lDepth;

	/** \brief coordinate */
	Point tPoint;

}GestureNode;

typedef struct
{
	/** \brief Finger Number */
	int lFingerNum;

	/** \brief Gesture Type */
	int lGestureType;

	/** \brief Node List of hand */
	vector<GestureNode> vtNodeList;

}GestureReconition_Data;

typedef struct
{
	/** \brief Contour Number threshold */
	int lContour_NumTH;

	/** \brief distance threshold between node */
	int lDistanceTH;

	/** \brief Depth TH LOW*/
	int lDepthTH_LOW;

	/** \brief Depth TH HIGH */
	int lDepthTH_HIGH;

	/** \brief Resize scale */
	int lResizeScale;

}GestureReconition_Cfg;

/** \brief Load Default Configure */
void GR_LoadDefaultCfg(GestureReconition_Cfg &a_tCfg);

/** \brief Gesture Reconition */
void GestureReconition(const GestureReconition_Cfg a_tCfg,
						const Mat a_tSrc,
						GestureReconition_Data &a_tData,
						bool a_bUseBackground,
						bool a_bLearn,
						double a_eLearnRate);


// C.C.C.
extern BackgroundSubtractorMOG g_BG_Model;
extern BackgroundSubtractorMOG2 g_BG_Model_2;

/** Filter foreground region */
Mat ForegroundDetect(Mat a_tImgIn, bool a_bBG_Update, BackgroundSubtractorMOG a_CBG_Model);
Mat ForegroundDetect2(Mat a_tImgIn, // 源圖
					bool a_bUseBackground, // 是否要train background model
					bool a_bBG_Update, // 是否要使用源圖更新model
					double a_eLearnRate, // model學習率
					BackgroundSubtractorMOG2 a_CBG_Model); // background model

Mat FindPolygonAndContour(Mat a_tSrc, const GestureReconition_Cfg a_tCfg);

Mat DrawResult(Mat a_tSrc, const GestureReconition_Cfg a_tCfg);
// C.C.C.
