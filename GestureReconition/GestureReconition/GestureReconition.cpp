#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <highgui.h>
#include <cxcore.h>
#include <vector>
#include <algorithm>
#include <math.h>       /* acos */

#include "Common.h"
#include "GestureReconition.h"
using namespace std;
using namespace cv;

#include "opencv2/core/core.hpp"
#include "opencv2/video/background_segm.hpp"
#include "opencv2/imgproc/imgproc_c.h"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/legacy/legacy.hpp"



BackgroundSubtractorMOG g_BG_Model(200, 5, 0.7, 10);
RNG rng(12345);



Mat ForegroundDetect(Mat a_tImgIn, bool a_bBG_Update, BackgroundSubtractorMOG a_CBG_Model)
{
	Mat tHSVImg, tSkinImg, tFG_Mask, tForeground;
	Scalar SkinBoundLow = Scalar(0, 58, 20);
	Scalar SkinBoundHigh = Scalar(50, 173, 230);

	cvtColor(a_tImgIn, tHSVImg, CV_BGR2HSV);

	// 篩選hsvImg在HSV顏色空間屬於膚色的區域
	inRange(tHSVImg, SkinBoundLow, SkinBoundHigh, tSkinImg);

	// 對skinImg進行膨脹&侵蝕
	dilate(tSkinImg, tSkinImg, Mat(), Point(-1, -1), 3);
	dilate(tSkinImg, tSkinImg, Mat(), Point(-1, -1), 3);
	erode(tSkinImg, tSkinImg, Mat(), Point(-1, -1), 3);

#if 0
	//update the model  (計算前景圖 tFG_Mask為二值化圖 第三個參數為學習率 設0則為背景相減)
	if (a_bBG_Update)
	{
		a_CBG_Model(a_tImgIn, tFG_Mask, 0.005);
	}
	else
	{
		a_CBG_Model(a_tImgIn, tFG_Mask, 0);
	}
#endif

	tForeground = tSkinImg.clone(); // for test

	return tForeground;
}

bool MatchId(vector<int> a_vlUsedID, int lId)
{
	int lLocalSize = a_vlUsedID.size();
	for (int i = 0; i < lLocalSize; i++)
	{
		if (a_vlUsedID[i] == lId)
		{
			return true;
		}
	}
	return false;
}

vector<int> FindMergeHullId(vector<Point > a_vtHull, vector<Point > a_vtContours)
{
	vector<int> vlHullId;
	bool bMatch = false;
	double eDistance = 0, eMin = 9999;
	Point tAvg;
	int lLocalSize = a_vtHull.size();

	for (int i = 0; i < lLocalSize; i++)
	{
		int lContourSize = a_vtContours.size();
		int lMinId = -1;
		eMin = 9999;
		for (int j = 0; j < lContourSize; j++)
		{
			eDistance = GetDistance(a_vtHull[i], a_vtContours[j]);

			if (eDistance < eMin)
			{
				eMin = eDistance;
				lMinId = j;
			}
		}
		vlHullId.push_back(lMinId);
	}

	return vlHullId;

}

vector<Point> MergeHull(vector<Point > a_vtHull)
{
	vector<Point> vtMergeHull;
	vector<int> vlUsedID;
	bool bMatch = false;
	double eDistance = 0;
	Point tAvg;
	int lCnt = 0;
	int lLocalSize = a_vtHull.size();

	for (int i = 0; i < lLocalSize; i++)
	{
		lCnt = 0;
		tAvg = Point(0, 0);
		bMatch = MatchId(vlUsedID, i);
		if (!bMatch)
		{
			for (int j = 0; j < lLocalSize; j++)
			{
				eDistance = GetDistance(a_vtHull[i], a_vtHull[j]);
				if (eDistance < DISTANCE_TH)
				{
					vlUsedID.push_back(j);
					tAvg.x += a_vtHull[j].x;
					tAvg.y += a_vtHull[j].y;
					lCnt++;
				}
			}
			tAvg.x /= lCnt;
			tAvg.y /= lCnt;

			vtMergeHull.push_back(tAvg);
		}


	}

	return vtMergeHull;
}



int GetFingerNum(vector<GestureNode> a_vtNodeList)
{
	int lFingerNum = 0;
	int lLastIndex = 0;
	int lNextIndex = 0;
	int lSize = 0;
	double eAngle = 0;

	Point tVectorLast, tVectorNext;

	lSize = a_vtNodeList.size();
	for (int i = 0; i < lSize; i++)
	{
		lLastIndex = (i != 0) ? i - 1 : lSize - 1;
		lNextIndex = (i < lSize-1) ? i + 1 : 0;

		tVectorLast = Point(a_vtNodeList[lLastIndex].tPoint.x - a_vtNodeList[i].tPoint.x, a_vtNodeList[lLastIndex].tPoint.y - a_vtNodeList[i].tPoint.y);
		tVectorNext = Point(a_vtNodeList[lNextIndex].tPoint.x - a_vtNodeList[i].tPoint.x, a_vtNodeList[lNextIndex].tPoint.y - a_vtNodeList[i].tPoint.y);

		eAngle = GetTheta(tVectorLast, tVectorNext);

		if (a_vtNodeList[lLastIndex].lDepth > 0 && a_vtNodeList[lNextIndex].lDepth > 0 && eAngle < 90)
		{
			lFingerNum++;
		}
	}

	return lFingerNum;
}

void FingerNumCal(Mat a_tSrc, const GestureReconition_Cfg a_tCfg, GestureReconition_Data &a_tData)
{
	Mat tBinary;
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	vector<Point> vtMergeHull;
	vector<int> vtMergeHullId;
	vector<GestureNode> vtNodeList;
	Point tCenter = Point(0,0);
	int lMaxContoursNum = 0, lMaxContoursId = 0;
	int lGlobalSize = 0;
	int lLocalSize = 0;

#if DEBUG_LOG_IMG> 0
	Mat tContour = Mat::zeros(a_tSrc.size(), CV_8UC3);
	Mat tHull = Mat::zeros(a_tSrc.size(), CV_8UC3);
	Mat tHullMerge = Mat::zeros(a_tSrc.size(), CV_8UC3);
	Mat tFinger = Mat::zeros(a_tSrc.size(), CV_8UC3);
	Scalar color3 = Scalar(0, 255, 255);
	Scalar color4 = Scalar(255, 255, 0);
#endif

	// Get Binary image using Threshold
	threshold(a_tSrc, tBinary, 100, 255, THRESH_BINARY);

#if DEBUG_LOG_IMG > 0
	imwrite("./tBinary.png", tBinary);
#endif

	// Find contours
	findContours(tBinary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

	lGlobalSize = contours.size();

#if DEBUG_LOG_MSG > 0
	printf("contours.size = %d\n", lGlobalSize);
#endif

	// coordinate type hull
	vector<vector<Point> >hull(lGlobalSize);

	// Int type hull  
	vector<vector<int>> hullsI(lGlobalSize);

	// Convexity Local defects  
	vector<Vec4i > DefectsLocal;

	// Find the convex hull object for each contour
	for (int i = 0; i < lGlobalSize; i++)
	{
		int lNum = contours[i].size();
		if (lNum < a_tCfg.lContour_NumTH)
		{	// 輪廓點太少 > 不予計算
			continue;
		}

		if (lNum > lMaxContoursNum)
		{
			lMaxContoursNum = lNum;
			lMaxContoursId = i;
		}

		// find hull (output is the coordinate)
		convexHull(Mat(contours[i]), hull[i], false);

		// find int type hull (output is the index of contours)
		convexHull(Mat(contours[i]), hullsI[i], false);
	}

#if DEBUG_LOG_IMG> 0
	lLocalSize = hull[lMaxContoursId].size();
	drawContours(tContour, contours, lMaxContoursId, color3, 1, 8, vector<Vec4i>(), 0, Point());
	drawContours(tContour, hull, lMaxContoursId, color4, 1, 8, vector<Vec4i>(), 0, Point());
	imwrite("./Contour.png", tContour);
	tHull = tContour.clone();
	for (int i = 0; i < lLocalSize; i++)
	{
		circle(tHull, Point(hull[lMaxContoursId][i]), 4, Scalar(0, 155, 100), 2);
	}
	imwrite("./Hull.png", tHull);
#endif

	// merge Hull's points which is too close
	vtMergeHull = MergeHull(hull[lMaxContoursId]);

	// find merge Hull's index of Contours list
	vtMergeHullId =  FindMergeHullId(vtMergeHull, contours[lMaxContoursId]);

#if DEBUG_LOG_IMG> 0
	lLocalSize = vtMergeHull.size();
	tHullMerge = tContour.clone();
	tCenter = Point(0, 0);
	for (int i = 0; i < lLocalSize; i++)
	{
		circle(tHullMerge, Point(vtMergeHull[i]), 4, Scalar(0, 155, 100), 2);
		tCenter.x += vtMergeHull[i].x;
		tCenter.y += vtMergeHull[i].y;
	}
	tCenter.x /= lLocalSize;
	tCenter.y /= lLocalSize;
	circle(tHullMerge, Point(tCenter), 5, Scalar(0, 255, 255), 2);
	imwrite("./tHullMerge.png", tHullMerge);
#endif

	// Draw contours + hull results
	drawContours(tFinger, contours, lMaxContoursId, color3, 1, 8, vector<Vec4i>(), 0, Point());
	drawContours(tFinger, hull, lMaxContoursId, color4, 1, 8, vector<Vec4i>(), 0, Point());

	// get convexity Defects Local  
	convexityDefects(Mat(contours[lMaxContoursId]), vtMergeHullId, DefectsLocal);

#if DEBUG_LOG_MSG > 0
	printf("vtMergeHullId number = %d\n", vtMergeHullId.size());
	printf("DefectsLocal number = %d\n", DefectsLocal.size());
#endif

	// push Merged Hull into Node List
	lLocalSize = vtMergeHullId.size();
	for (int i = 0; i < lLocalSize; i++)
	{
		GestureNode tNodeTmp;
		tNodeTmp.lIndex = vtMergeHullId[i];
		tNodeTmp.lDepth = 0;
		tNodeTmp.tPoint = contours[lMaxContoursId][tNodeTmp.lIndex];
		vtNodeList.push_back(tNodeTmp);
	}

	int lIndexLocal = 0;
	// push Defect points into Node List
	while (lIndexLocal < (int)DefectsLocal.size())
	{
		int lStartIndex = DefectsLocal[lIndexLocal].val[0];
		Point tStartP(contours[lMaxContoursId][lStartIndex]); // point of the contour where the defect begins  
		int lEndIndex = DefectsLocal[lIndexLocal].val[1];
		Point tEndP(contours[lMaxContoursId][lEndIndex]); // point of the contour where the defect ends  
		int lFarIndex = DefectsLocal[lIndexLocal].val[2];
		Point tFarP(contours[lMaxContoursId][lFarIndex]);// the farthest from the convex hull point within the defect  
		int lDepth = DefectsLocal[lIndexLocal].val[3] / 256; // distance between the farthest point and the convex hull  

		if (lDepth > a_tCfg.lDepthTH_LOW && lDepth < a_tCfg.lDepthTH_HIGH)
		{
#if DEBUG_LOG_IMG> 0
			line(tFinger, tStartP, tFarP, CV_RGB(0, 255, 0), 2);
			line(tFinger, tEndP, tFarP, CV_RGB(0, 255, 0), 2);
			circle(tFinger, tStartP, 4, Scalar(255, 0, 100), 2);
			circle(tFinger, tEndP, 4, Scalar(255, 0, 100), 2);
			circle(tFinger, tFarP, 4, Scalar(100, 0, 255), 2);
#endif

			lLocalSize = vtNodeList.size();
			for (int i = 0; i < lLocalSize; i++)
			{
				if (lEndIndex == vtNodeList[i].lIndex)
				{
					GestureNode tNodeTmp;
					tNodeTmp.lIndex = lFarIndex;
					tNodeTmp.lDepth = lDepth;
					tNodeTmp.tPoint = tFarP;
					vtNodeList.insert(vtNodeList.begin()+ i +1, tNodeTmp);
					break;
				}
			}
		}
		lIndexLocal++;
	}

	a_tData.lFingerNum = GetFingerNum(vtNodeList);
	a_tData.vtNodeList = vtNodeList;

#if DEBUG_LOG_IMG> 0
	char strFrame[256];
	sprintf(strFrame, "Finger Num = %d", a_tData.lFingerNum);
	putText(tFinger, strFrame, cvPoint(20, 20), 1, 1, CV_RGB(255, 255, 0), 2, 16);
	imwrite("./Finger.png", tFinger);
#endif
}



void GestureReconition(const GestureReconition_Cfg a_tCfg, const Mat a_tSrc, GestureReconition_Data &a_tData)
{
	Mat tForeground, tResize;
	Mat tSrcBuffer = a_tSrc.clone();

	tForeground = ForegroundDetect(tSrcBuffer, false, g_BG_Model);
#if DEBUG_LOG_IMG > 0
	imwrite("./Foreground.png", tForeground);
#endif
	if (a_tCfg.lResizeScale)
	{
		resize(tForeground, tResize, Size(tForeground.cols / 5, tForeground.rows / 5));
	}
	else
	{
		tResize = tForeground.clone();
	}
#if DEBUG_LOG_IMG > 0
	imwrite("./TestOut_hand_Resize.png", tResize);
#endif
	FingerNumCal(tResize, a_tCfg, a_tData);
}

void GR_LoadDefaultCfg(GestureReconition_Cfg &a_tCfg)
{
	a_tCfg.lContour_NumTH = CONTOURS_NUM_TH;
	a_tCfg.lDepthTH_HIGH = DEPTH_TH_HIGH;
	a_tCfg.lDepthTH_LOW = DEPTH_TH_LOW;
	a_tCfg.lDistanceTH = DISTANCE_TH;
	a_tCfg.lResizeScale = RESIZE_SCALE;
}
