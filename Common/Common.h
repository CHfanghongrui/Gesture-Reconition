#pragma once

#define MAX_PIXEL_VAL_8UC 255

#define PI 3.14159265

#define LOCAL_DEBUG_IMG_LOG 1


#include <opencv/cv.h>
#include "opencv2/highgui/highgui.hpp"
using namespace cv;

/// <summary> 求兩向量的夾角 </summary>
double GetTheta(Point2d a_tV1, Point2d a_tV2);

/// <summary> 求某向量長度 </summary>
double GetVectorLength(Point2d a_tIn);

/// <summary> 求某向量內積 </summary>
double Dot(Point2d a_tV1, Point2d a_tV2);

/// <summary> 在圖上畫點  </summary>
Mat MyDrawPoint(const Mat a_tSrc, Point a_tDrawP, int a_lB, int a_lG, int a_lR);

/// <summary> 已知兩點，求方向向量  </summary>
Point2d GetVector(Point2d a_tStartP, Point2d a_tEndP);

/// <summary> 已知方向向量，求法向量  </summary>
Point2d GetLawVector(Point2d a_tVector);

/// <summary> 兩點求距離  </summary>
double GetDistance(Point2d a_tP1, Point2d a_tP2);

/// <summary> 閥值限制 超過上限就已上限為主 下限亦同  </summary>
void TruncateByThreshold(Mat &a_tSrc, const int a_lTH_High, const int lTH_Low);

/// <summary> 圖片直方圖計算 >> 僅可float & uchar型態  </summary>
void HistogramCal(const Mat a_tSrc, int *a_plHistogram, int a_lSize);

/// <summary> 算離原點距離  </summary>
double DistanceCal(double a_eP1, double a_eP2);

/// <summary> 直方均值化 by 設定好的上下限  </summary>
void ImageEqualizeHistByBound(Mat &a_tSrc, const int a_lTH_High, const int lTH_Low);

/// <summary> 用直方圖算標準差跟均值  </summary>
void StandardDeviationCalImage(const int *a_plHistogram, const int a_lSize, double &a_eMean, double &a_eSigma);

/// <summary> 設定圖像顏色  </summary>
void SetPoint(Mat img, Point start, Vec3b a_tColor);

/// <summary> 將灰階圖轉乘彩圖 </summary>
Mat GrayToColor(const Mat a_tSrc);

/// <summary> 求座標是否超出畫面範圍 true 代表沒問題 </summary>
bool CheckBoundary(const Size a_tSize, const Point2d a_tPoint);

/// <summary> 兩矩逐元素相乘 >> 遮罩運算會用到 </summary>
void MatrixMulFloat(Mat a_tSrc, const Mat a_tMulMatrix);

/// <summary> 求Mat type </summary>
string getImgType(cv::Mat frame);

double FindVectorMultipleOfMeetPoint(Point2d a_tSrc1, Point2d a_tV1, Point2d a_tSrc2, Point2d a_tV2);

/// <summary> 求兩線段的交點 </summary>
Point2d FindMeetPoint(Point2d a_tSrc1, Point2d a_tV1, Point2d a_tSrc2, Point2d a_tV2);

