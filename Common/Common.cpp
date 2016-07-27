
#include "Common.h"


/*
Brief : 在圖形上畫點
Input :
a_tSrc : 輸入影像(RGB影像)
a_tDrawP : 要畫的點
a_lB : 藍色像素值
a_lG : 綠色像素值
a_lR : 紅色像素值
Return :
tBuffer : 畫完的結果
*/
Mat MyDrawPoint(const Mat a_tSrc, Point a_tDrawP, int a_lB, int a_lG, int a_lR)
{
	Mat tBuffer = a_tSrc.clone();
	tBuffer.at<Vec3b>(a_tDrawP.y, a_tDrawP.x)[0] = a_lB;
	tBuffer.at<Vec3b>(a_tDrawP.y, a_tDrawP.x)[1] = a_lG;
	tBuffer.at<Vec3b>(a_tDrawP.y, a_tDrawP.x)[2] = a_lR;
	return tBuffer;
}

/*
Brief : 已知兩點，求方向向量
Input :
a_tStartP : 開始點
a_tEndP : 終點
Return :
tBuffer : 方向向量
*/
Point2d GetVector(Point2d a_tStartP, Point2d a_tEndP)
{
	Point2d tBuffer;
	tBuffer.x = a_tEndP.x - a_tStartP.x;
	tBuffer.y = a_tEndP.y - a_tStartP.y;
	return tBuffer;
}

/*
Brief : 已知方向向量，求法向量
Input :
a_tDirP : 方向向量
Return :
tBuffer : 法向量
*/
Point2d GetLawVector(Point2d a_tVector)
{
	Point2d tBuffer;
	tBuffer.x = a_tVector.y;
	tBuffer.y = -a_tVector.x;
	return tBuffer;
}

// 兩點求距離
double GetDistance(Point2d a_tP1, Point2d a_tP2)
{
	double eLehgth;
	eLehgth = sqrt((a_tP1.x - a_tP2.x)*(a_tP1.x - a_tP2.x) + (a_tP1.y - a_tP2.y)*(a_tP1.y - a_tP2.y));
	return eLehgth;
}

void TruncateByThreshold(Mat &a_tSrc, const int a_lTH_High, const int lTH_Low)
{
	CvSize tSize;

	tSize = a_tSrc.size();

	for (int y = 0; y < tSize.height; y++)
	{
		for (int x = 0; x < tSize.width; x++)
		{
			if (a_tSrc.at<float>(y, x) > a_lTH_High)
			{
				a_tSrc.at<float>(y, x) = (float)a_lTH_High;
			}

			if (a_tSrc.at<float>(y, x) < lTH_Low)
			{
				a_tSrc.at<float>(y, x) = (float)lTH_Low;
			}
		}
	}
}

void HistogramCal(const Mat a_tSrc, int *a_plHistogram, int a_lSize)
{
	int lValue = 0;
	int lImgType = 0;
	int lImgDepth = 0;

	memset(a_plHistogram, 0, sizeof(int)*a_lSize);

	lImgType = a_tSrc.type();
	lImgDepth = a_tSrc.depth();

	if (lImgDepth == CV_8U)
	{
		for (int y = 0; y < a_tSrc.rows; y++)
		{
			for (int x = 0; x < a_tSrc.cols; x++)
			{
				lValue = (int)a_tSrc.at<unsigned char>(y, x);
				a_plHistogram[lValue]++;
			}
		}
	}
	else
	{
		for (int y = 0; y < a_tSrc.rows; y++)
		{
			for (int x = 0; x < a_tSrc.cols; x++)
			{
				lValue = (int)a_tSrc.at<float>(y, x);
				a_plHistogram[lValue]++;
			}
		}
	}
}

double DistanceCal(double a_eP1, double a_eP2)
{
	return cv::sqrt(a_eP1*a_eP1 + a_eP2*a_eP2);
}

void ImageEqualizeHistByBound(Mat &a_tSrc, const int a_lTH_High, const int lTH_Low)
{
	CvSize tSize;
	tSize = a_tSrc.size();
	double aeTmp[3] = {}, eTmpVal = 0;

	// modify image
	for (int i = 0; i < tSize.height; i++)
	{
		for (int j = 0; j < tSize.width; j++)
		{
			eTmpVal = a_tSrc.at<unsigned char>(i, j);
			aeTmp[0] = eTmpVal - lTH_Low;
			aeTmp[1] = a_lTH_High - lTH_Low;
			aeTmp[2] = aeTmp[0] / aeTmp[1] * 255;
			eTmpVal = aeTmp[2];
			a_tSrc.at<unsigned char>(i, j) = (unsigned char)eTmpVal;
		}
	}
}

void StandardDeviationCalImage(const int *a_plHistogram, const int a_lSize, double &a_eMean, double &a_eSigma)
{
	int lCnt = 0;
	double eSum = 0, eSumPixelVal = 0, eSumSTD = 0;

	for (int lCnt = 0; lCnt < a_lSize; lCnt++)
	{
		eSum += a_plHistogram[lCnt];
		eSumPixelVal += a_plHistogram[lCnt] * lCnt;
	}
	a_eMean = eSumPixelVal / eSum;

	eSumSTD = 0;
	for (int lCnt = 0; lCnt < a_lSize; lCnt++)
	{
		eSumSTD += (lCnt - a_eMean)*(lCnt - a_eMean);
	}
	eSumSTD /= eSum;

	a_eSigma = sqrt(eSumSTD);
}

void SetPoint(Mat img, Point start, Vec3b a_tColor)
{
	// set pixel
	img.at<Vec3b>(start) = a_tColor;
}

Mat GrayToColor(const Mat a_tSrc)
{
	double eMin = 0, eMax = 0;
	Size tSizeSrc;
	tSizeSrc = a_tSrc.size();
	Mat tDrawImgBuffer = Mat::zeros(tSizeSrc, CV_8UC3);
	vector<Mat> vtChannel;

	vtChannel.push_back(a_tSrc);
	vtChannel.push_back(a_tSrc);
	vtChannel.push_back(a_tSrc);

	merge(vtChannel, tDrawImgBuffer);
	return tDrawImgBuffer;
}

bool CheckBoundary(const Size a_tSize, const Point2d a_tPoint)
{
	if (a_tPoint.x < 0 || a_tPoint.y < 0)
	{
		return false;
	}

	if (a_tPoint.x >= a_tSize.width || a_tPoint.y >= a_tSize.height)
	{
		return false;
	}
	return true;
}

string getImgType(cv::Mat frame)
{
	int imgTypeInt = frame.type();
	int numImgTypes = 28; // 7 base types, with 4 channel options each (C1, ..., C4)
	int enum_ints[] = { CV_8UC1,  CV_8UC2,  CV_8UC3,  CV_8UC4, CV_8SC1,  CV_8SC2,  CV_8SC3,  CV_8SC4, CV_16UC1, CV_16UC2, CV_16UC3, CV_16UC4, CV_16SC1, CV_16SC2, CV_16SC3, CV_16SC4, CV_32SC1, CV_32SC2, CV_32SC3, CV_32SC4, CV_32FC1, CV_32FC2, CV_32FC3, CV_32FC4, CV_64FC1, CV_64FC2, CV_64FC3, CV_64FC4 };
	string enum_strings[] = { "CV_8U",  "CV_8UC1",  "CV_8UC2",  "CV_8UC3",  "CV_8UC4", "CV_8SC1",  "CV_8SC2",  "CV_8SC3",  "CV_8SC4", "CV_16UC1",  "CV_16UC2",  "CV_16UC3",  "CV_16UC4", "CV_16SC1", "CV_16SC2", "CV_16SC3", "CV_16SC4", "CV_32SC1", "CV_32SC2", "CV_32SC3", "CV_32SC4", "CV_32FC1", "CV_32FC2", "CV_32FC3", "CV_32FC4", "CV_64FC1", "CV_64FC2", "CV_64FC3", "CV_64FC4" };
	for (int i = 0; i<numImgTypes; i++)
	{
		if (imgTypeInt == enum_ints[i]) return enum_strings[i];
	}
	return "unknown image type";
}

/// <summary> 求交點對應的向量係數 for Line 1 </summary>
double FindVectorMultipleOfMeetPoint(Point2d a_tSrc1, Point2d a_tV1, Point2d a_tSrc2, Point2d a_tV2)
{
	double eT1 = 0;

	// 求交點對應的向量係數 for Line 1
	eT1 = a_tSrc2.x * a_tV2.y - a_tSrc2.y * a_tV2.x - a_tSrc1.x * a_tV2.y + a_tSrc1.y * a_tV2.x;
	eT1 /= a_tV1.x * a_tV2.y - a_tV1.y * a_tV2.x;

	return eT1;
}

/// <summary> 求兩線段的交點 </summary>
Point2d FindMeetPoint(Point2d a_tSrc1, Point2d a_tV1, Point2d a_tSrc2, Point2d a_tV2)
{
	Point2d tMeetPoint;
	double eT1 = 0;

	eT1 = FindVectorMultipleOfMeetPoint(a_tSrc1, a_tV1, a_tSrc2, a_tV2);

	// 求交點(current point 沿著向量與路徑的交點)
	tMeetPoint.x = a_tSrc1.x + a_tV1.x * eT1;
	tMeetPoint.y = a_tSrc1.y + a_tV1.y * eT1;

	return tMeetPoint;
}

void MatrixMulFloat(Mat a_tSrc, const Mat a_tMulMatrix)
{
	int lTypeSrc = a_tSrc.type();
	int lTypeMulMatrix = a_tMulMatrix.type();
	int TypeSet = CV_32FC1;


	if (lTypeSrc == lTypeMulMatrix && lTypeSrc == TypeSet)
	{	// 必須同型態 且為CV_32FC1
		for (int y = 0; y < a_tSrc.rows; y++)
		{
			for (int x = 0; x < a_tSrc.cols; x++)
			{
				a_tSrc.at<float>(y, x) = a_tSrc.at<float>(y, x) * a_tMulMatrix.at<float>(y, x);
			}
		}
	}
	else
	{
		printf("Not Same Type or Worng Type");
	}
}

/// <summary> 求某向量內積 </summary>
double Dot(Point2d a_tV1, Point2d a_tV2)
{
	double eOut = 0;

	eOut = a_tV1.x * a_tV2.x + a_tV1.y * a_tV2.y;
	return eOut;
}

/// <summary> 求某向量長度 </summary>
double GetVectorLength(Point2d a_tIn)
{
	double eOut = 0;
	eOut = sqrt(a_tIn.x * a_tIn.x + a_tIn.y * a_tIn.y);
	return eOut;
}

/// <summary> 求兩向量的夾角 </summary>
double GetTheta(Point2d a_tV1, Point2d a_tV2)
{
	double eTheta = 0;

	eTheta = Dot(a_tV1, a_tV2);
	eTheta /= GetVectorLength(a_tV1) * GetVectorLength(a_tV2);
	eTheta = acos(eTheta) * 180.0 / PI;
	return eTheta;
}


