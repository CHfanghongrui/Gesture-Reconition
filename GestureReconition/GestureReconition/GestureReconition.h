#pragma once

using namespace std;
using namespace cv;
#include "opencv2/core/core.hpp"

#define CONTOURS_NUM_TH 70 // 100

#define DISTANCE_TH 15

#define DEPTH_TH_LOW 5

#define DEPTH_TH_HIGH 80

#define FINGER_ANGLE_TH 90

#define RESIZE_SCALE 5

#define DEBUG_LOG_IMG 1

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
void GestureReconition(const GestureReconition_Cfg a_tCfg, const Mat a_tSrc, GestureReconition_Data &a_tData);
