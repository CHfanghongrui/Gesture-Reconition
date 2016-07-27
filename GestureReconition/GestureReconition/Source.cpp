#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <opencv/cv.h>
#include "opencv2/highgui/highgui.hpp"
#include "GestureReconition.h"

using namespace std;
using namespace cv;


int testInsert()
{
	std::vector<int> myvector(3, 100);
	std::vector<int>::iterator it;

	myvector.insert(myvector.begin(), 200);

	myvector.insert(myvector.begin() + 2, 1000);

	it = myvector.begin();
	it = myvector.insert(it, 200);

	myvector.insert(it, 2, 300);

	// "it" no longer valid, get a new one:
	it = myvector.begin();

	std::vector<int> anothervector(2, 400);
	myvector.insert(it + 2, anothervector.begin(), anothervector.end());

	int myarray[] = { 501,502,503 };
	myvector.insert(myvector.begin(), myarray, myarray + 3);

	std::cout << "myvector contains:";
	for (it = myvector.begin(); it<myvector.end(); it++)
		std::cout << ' ' << *it;
	std::cout << '\n';

	return 0;
}

int main(int argc, char** argv)
{
	GestureReconition_Cfg tCfg;
	GestureReconition_Data tData;

	Mat tOutTest, tResize;
	Mat tSrcTest = imread("./SrcImg/hand_4.png", CV_LOAD_IMAGE_UNCHANGED);

	if (tSrcTest.empty())
	{
		printf("empty frame!\n");
		return -1;
	}

	printf("Load Default Configure\n");

	GR_LoadDefaultCfg(tCfg);

	printf("Gesture Reconition Start\n");

	double eTimeCost = 0;
	eTimeCost = (double)getTickCount();

	GestureReconition(tCfg, tSrcTest, tData);

	eTimeCost = ((double)getTickCount() - eTimeCost) / getTickFrequency();
	printf("Gesture Reconition's Time Cost is %.3f sec\n", eTimeCost);

	printf("There are %d fingers\n", tData.lFingerNum);
	printf("Gesture Reconition End\n");

	system("PAUSE");
	return 0;
}