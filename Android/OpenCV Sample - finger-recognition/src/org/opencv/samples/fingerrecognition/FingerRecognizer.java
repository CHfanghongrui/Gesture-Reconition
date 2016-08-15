package org.opencv.samples.fingerrecognition;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

/** 手勢偵測-java版 */
public final class FingerRecognizer {
	public static BackgroundSubtractorMOG g_BG_Model;
	public static BackgroundSubtractorMOG2 g_BG_Model_2;
	
	private final static class GestureNode {
		public int lIndex;
		public int lDepth;
		public Point tPoint;
		
		public GestureNode(int lIndex, 
							int lDepth, 
							Point tPoint) {
			super();

			this.lIndex = lIndex;
			this.lDepth = lDepth;
			this.tPoint = tPoint;
		}
	}
	
	public final static class GestureReconition_Cfg {
		public final static int CONTOURS_NUM_TH = 70; // 100
		public final static int  DISTANCE_TH = 15;
		public final static int  DEPTH_TH_LOW = 5;
		public final static int  DEPTH_TH_HIGH = 80;
		public final static int FINGER_ANGLE_TH = 90;
		public final static int  RESIZE_SCALE = 0;
		
		/** \brief Contour Number threshold */
		public int lContour_NumTH;
		/** \brief distance threshold between node */
		public int lDistanceTH;
		/** \brief Depth TH LOW*/
		public int lDepthTH_LOW;
		/** \brief Depth TH HIGH */
		public int lDepthTH_HIGH;
		/** \brief Resize scale */
		public int lResizeScale;
		
		public static GestureReconition_Cfg instance() {
			return new GestureReconition_Cfg(CONTOURS_NUM_TH, 
											DEPTH_TH_HIGH, 
											DEPTH_TH_LOW, 
											DISTANCE_TH, 
											RESIZE_SCALE);
		}
		
		public GestureReconition_Cfg(int lContour_NumTH, 
									int lDepthTH_HIGH, 
									int lDepthTH_LOW, 
									int lDistanceTH, 
									int lResizeScale) {
			super();
			
			this.lContour_NumTH = lContour_NumTH;
			this.lDepthTH_HIGH = lDepthTH_HIGH;
			this.lDepthTH_LOW = lDepthTH_LOW;
			this.lDistanceTH = lDistanceTH;
			this.lResizeScale = lResizeScale;
		}
	}
	
    public final static void ForegroundDetect(Mat a_tImgIn, 
    										Mat a_tImgOut, 
    										Boolean a_bBG_Update, 
    										BackgroundSubtractorMOG a_CBG_Model) {
    	Mat tHSVImg = new Mat(), 
    			tSkinImg = new Mat(), 
    			tFG_Mask = new Mat();
//    	Scalar SkinBoundLow = new Scalar(0, 58, 20);
//    	Scalar SkinBoundHigh = new Scalar(50, 173, 230);
    	
    	Scalar SkinBoundLow = new Scalar(0, 0.28*255, 0);
        Scalar SkinBoundHigh = new Scalar(25, 0.68*255, 255);
    	
    	Imgproc.cvtColor(a_tImgIn, tHSVImg, Imgproc.COLOR_RGB2HSV);
    	
    	// 篩選hsvImg在HSV顏色空間屬於膚色的區域
    	Core.inRange(tHSVImg, SkinBoundLow, SkinBoundHigh, tSkinImg);
    	tHSVImg.release();
    	
    	// 對skinImg進行膨脹&侵蝕
    	Imgproc.dilate(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
    	Imgproc.dilate(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
    	Imgproc.erode(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
    	
    	if (false) {
    	if (a_bBG_Update) {
//    		a_CBG_Model.apply(a_tImgIn, tFG_Mask, 0.005);
    		a_CBG_Model.apply(tSkinImg, tSkinImg, 0.005);
    	}
    	else {
//    		a_CBG_Model.apply(a_tImgIn, tFG_Mask, 0);
    		a_CBG_Model.apply(tSkinImg, tSkinImg, 0);
    	}
    	}
    	
    	tSkinImg.copyTo(a_tImgOut);
    	tSkinImg.release();
    	tFG_Mask.release();
    }
    
    private final static Boolean useBackground = true;
    public final static void ForegroundDetect2(Mat a_tImgIn, 
												Mat a_tImgOut, 
												Boolean a_bBG_Update, 
												BackgroundSubtractorMOG2 a_CBG_Model) {
		Mat tHSVImg = new Mat(), 
				tSkinImg = new Mat(), 
				tFG_Mask = new Mat();
		//Scalar SkinBoundLow = new Scalar(0, 58, 20);
		//Scalar SkinBoundHigh = new Scalar(50, 173, 230);
		
		Scalar SkinBoundLow = new Scalar(0, 0.28*255, 0);
		Scalar SkinBoundHigh = new Scalar(25, 0.68*255, 255);
		
		Imgproc.cvtColor(a_tImgIn, tHSVImg, Imgproc.COLOR_RGB2HSV);
		
		// 篩選hsvImg在HSV顏色空間屬於膚色的區域
		Core.inRange(tHSVImg, SkinBoundLow, SkinBoundHigh, tSkinImg);
		tHSVImg.release();
		
		// 對skinImg進行膨脹&侵蝕
		Imgproc.dilate(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
		Imgproc.dilate(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
		Imgproc.erode(tSkinImg, tSkinImg, new Mat(), new Point(-1, -1), 3);
		
		if (useBackground) {
			if (a_bBG_Update) {
				a_CBG_Model.apply(a_tImgIn, tFG_Mask, 0.001);	// 最後一個參數給0代表不再更新背景 給負數代表給opencv自行決定學習率
			}
			else {
				a_CBG_Model.apply(a_tImgIn, tFG_Mask, 0);	// 最後一個參數給0代表不再更新背景 給負數代表給opencv自行決定學習率
			}
			for (int y = 0; y < tSkinImg.height(); y++)
			{
				for (int x = 0; x < tSkinImg.width(); x++)
				{
					if (tSkinImg.get(y, x)[0] > 0 && tSkinImg.get(y, x)[1] > 0 && tSkinImg.get(y, x)[2] > 0) {	// 膚色對
						if (tFG_Mask.get(y, x)[0] == 0 && tFG_Mask.get(y, x)[1] == 0 && tFG_Mask.get(y, x)[2] == 0)
						{	// 非高斯前景
							tSkinImg.put(y, x, 0, 0, 0);
						}
					}
				}
			}
		}
		
		tSkinImg.copyTo(a_tImgOut);
		tSkinImg.release();
		tFG_Mask.release();
	}
    
    @SuppressWarnings("unused")
	private final static void resize(Mat a_tSrc, Mat a_tDest, double scale) {
    	Mat tResize = new Mat();
    	Imgproc.resize(a_tSrc, tResize, new Size(a_tSrc.width()*scale, a_tSrc.height()*scale));
    	tResize.copyTo(a_tDest);
    	tResize.release();
    }
    
    public final static void FindPolygonAndContour(Mat a_tSrc, 
    												Mat a_tDest, 
    												GestureReconition_Cfg a_tCfg) {
    	
    	Mat tForeground = new Mat();
    	ForegroundDetect(a_tSrc, tForeground, false, g_BG_Model);
    	
//    	resize(a_tSrc, a_tSrc, 1.0/4.0);
    	
    	Mat tBinary = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Mat hierarchy = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	int lMaxContoursNum = 0, lMaxContoursId = 0;
    	int lGlobalSize = 0;

    	Mat tContour = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	Scalar color3 = new Scalar(0, 255, 255);
    	Scalar color4 = new Scalar(255, 255, 0);

    	// Get Binary image using Threshold
    	Imgproc.threshold(tForeground, tBinary, 100, 255, Imgproc.THRESH_BINARY);

    	// Find contours
    	Imgproc.findContours(tBinary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

    	lGlobalSize = contours.size();

    	// coordinate type hull
    	ArrayList<MatOfPoint> hull = new ArrayList<MatOfPoint>(lGlobalSize);
    	for(int j = 0; j < lGlobalSize; j ++){
    		hull.add(new MatOfPoint());
        }
    	
    	// Int type hull
    	ArrayList<MatOfInt> hullsI = new ArrayList<MatOfInt>(lGlobalSize);
    	for(int j = 0; j < lGlobalSize; j ++){
    		hullsI.add(new MatOfInt());
        }
    	
    	// Find the convex hull object for each contour
    	for (int i = 0; i < lGlobalSize; i++)
    	{
    		int lNum = contours.get(i).toList().size();
    		if (lNum < a_tCfg.lContour_NumTH)
    		{	// 輪廓點太少 > 不予計算
    			continue;
    		}

    		if (lNum > lMaxContoursNum)
    		{
    			lMaxContoursNum = lNum;
    			lMaxContoursId = i;
    		}
    		
    		// find int type hull (output is the index of contours)
    		Imgproc.convexHull(contours.get(i), hullsI.get(i), false);
    		
    		// find hull (output is the coordinate)
    		int[] intlist = hullsI.get(i).toArray();
    		ArrayList<Point> l = new ArrayList<Point>();
            for (int j = 0; j < intlist.length; j ++) {
                l.add(contours.get(i).toList().get(hullsI.get(i).toList().get(j)));
            }
            hull.get(i).fromList(l);
            
    	}
    	if (lMaxContoursId >= hull.size()) {
    		tContour.copyTo(a_tDest);
    		
        	tContour.release();
        	tBinary.release();
        	hierarchy.release();
        	tForeground.release();
    		return;
    	}
    	
    	Imgproc.drawContours(tContour, contours, lMaxContoursId, color3, 1, 8, new Mat(), 0, new Point());
    	Imgproc.drawContours(tContour, hull, lMaxContoursId, color4, 1, 8, new Mat(), 0, new Point());

    	tContour.copyTo(a_tDest);
    	tContour.release();
    	tBinary.release();
    	hierarchy.release();
    	tForeground.release();
    	
    }
    
    public final static void DrawResult(Mat a_tSrc, 
										Mat a_tDest, 
										GestureReconition_Cfg a_tCfg) {

    	Mat tForeground = new Mat();
    	ForegroundDetect(a_tSrc, tForeground, false, g_BG_Model);
    	
//    	resize(a_tSrc, a_tSrc, 1.0/4.0);
    	
    	Mat tBinary = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Mat hierarchy = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	MatOfPoint vtMergeHull;
    	MatOfInt vtMergeHullId;
    	ArrayList<GestureNode> vtNodeList = new ArrayList<FingerRecognizer.GestureNode>();
    	int lMaxContoursNum = 0, lMaxContoursId = 0;
    	int lGlobalSize = 0;
    	int lLocalSize = 0;
    	
    	Mat tFinger = Mat.zeros(tForeground.size(), CvType.CV_8UC3);
    	Scalar color3 = new Scalar(0, 255, 255);
    	Scalar color4 = new Scalar(255, 255, 0);

    	// Get Binary image using Threshold
    	Imgproc.threshold(tForeground, tBinary, 100, 255, Imgproc.THRESH_BINARY);

    	// Find contours
    	Imgproc.findContours(tBinary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

    	lGlobalSize = contours.size();

    	// coordinate type hull
    	ArrayList<MatOfPoint> hull = new ArrayList<MatOfPoint>(lGlobalSize);
    	for(int j = 0; j < lGlobalSize; j ++){
    		hull.add(new MatOfPoint());
    	}

    	// Int type hull
    	ArrayList<MatOfInt> hullsI = new ArrayList<MatOfInt>(lGlobalSize);
    	for(int j = 0; j < lGlobalSize; j ++){
    		hullsI.add(new MatOfInt());
    	}
    	
    	// Convexity Local defects  
    	MatOfInt4 DefectsLocal = new MatOfInt4();

    	// Find the convex hull object for each contour
    	for (int i = 0; i < lGlobalSize; i++)
    	{
    		int lNum = contours.get(i).toList().size();
    		if (lNum < a_tCfg.lContour_NumTH)
    		{	// 輪廓點太少 > 不予計算
    			continue;
    		}

    		if (lNum > lMaxContoursNum)
    		{
    			lMaxContoursNum = lNum;
    			lMaxContoursId = i;
    		}

    		// find int type hull (output is the index of contours)
    		Imgproc.convexHull(contours.get(i), hullsI.get(i), false);

    		//find hull (output is the coordinate)
    		int[] intlist = hullsI.get(i).toArray();
    		ArrayList<Point> l = new ArrayList<Point>();
    		for (int j = 0; j < intlist.length; j ++) {
    			l.add(contours.get(i).toList().get(hullsI.get(i).toList().get(j)));
    		}
    		hull.get(i).fromList(l);

    	}
    	if (lMaxContoursId >= hull.size()) {
    		String strFrame = "Finger Num = 0";
        	Core.putText(tFinger, strFrame, new Point(20, 20), 1, 1.0, new Scalar(255, 255, 0), 2);
        	
    		tFinger.copyTo(a_tDest);
    		tBinary.release();
    		hierarchy.release();
    		tFinger.release();
    		tForeground.release();
    		return;
    	}
    	if(hull.get(lMaxContoursId).toList().size() <= 2) {
    		String strFrame = "Finger Num = 0";
        	Core.putText(tFinger, strFrame, new Point(20, 20), 1, 1.0, new Scalar(255, 255, 0), 2);
        	
    		tFinger.copyTo(a_tDest);
    		tBinary.release();
    		hierarchy.release();
    		tFinger.release();
    		tForeground.release();
    		return;
    	}
    	
    	// merge Hull's points which is too close
    	vtMergeHull = MergeHull(hull.get(lMaxContoursId));

    	// find merge Hull's index of Contours list
    	vtMergeHullId = FindMergeHullId(vtMergeHull, contours.get(lMaxContoursId));
    	
    	// Draw contours + hull results
    	Imgproc.drawContours(tFinger, contours, lMaxContoursId, color3, 1, 8, new Mat(), 0, new Point());
    	Imgproc.drawContours(tFinger, hull, lMaxContoursId, color4, 1, 8, new Mat(), 0, new Point());
    	
    	if(vtMergeHullId.toList().size() <= 2) {
    		String strFrame = "Finger Num = 0";
        	Core.putText(tFinger, strFrame, new Point(20, 20), 1, 1.0, new Scalar(255, 255, 0), 2);
        	
    		tFinger.copyTo(a_tDest);
    		tBinary.release();
    		hierarchy.release();
    		tFinger.release();
    		tForeground.release();
    		return;
    	}
    	
    	// get convexity Defects Local  
    	Imgproc.convexityDefects(contours.get(lMaxContoursId), vtMergeHullId, DefectsLocal);
//    	Imgproc.convexityDefects(contours.get(lMaxContoursId), hullsI.get(lMaxContoursId), DefectsLocal);
    	
    	// push Merged Hull into Node List
    	lLocalSize = vtMergeHullId.toList().size();
    	for (int i = 0; i < lLocalSize; i++) {
    		int lIndex = vtMergeHullId.toList().get(i);
    		GestureNode tNodeTmp = new GestureNode(lIndex, 
    									0, 
    									contours.get(lMaxContoursId).toList().get(lIndex));
    		vtNodeList.add(tNodeTmp);
    	}

    	int lIndexLocal = 0;
    	// push Defect points into Node List
    	while (lIndexLocal < (int)DefectsLocal.toList().size()) {
    		int lStartIndex = DefectsLocal.toList().get(lIndexLocal);
    		Point tStartP = contours.get(lMaxContoursId).toList().get(lStartIndex); // point of the contour where the defect begins  
    		int lEndIndex = DefectsLocal.toList().get(lIndexLocal+1);
    		Point tEndP = contours.get(lMaxContoursId).toList().get(lEndIndex); // point of the contour where the defect ends  
    		int lFarIndex = DefectsLocal.toList().get(lIndexLocal+2);
    		Point tFarP = contours.get(lMaxContoursId).toList().get(lFarIndex);// the farthest from the convex hull point within the defect  
    		int lDepth = DefectsLocal.toList().get(lIndexLocal+3) / 256; // distance between the farthest point and the convex hull  

    		if (lDepth > a_tCfg.lDepthTH_LOW && lDepth < a_tCfg.lDepthTH_HIGH) {
    			Core.line(tFinger, tStartP, tFarP, new Scalar(0, 255, 0), 2);
    			Core.line(tFinger, tEndP, tFarP, new Scalar(0, 255, 0), 2);
    			Core.circle(tFinger, tStartP, 4, new Scalar(255, 0, 100), 2);
    			Core.circle(tFinger, tEndP, 4, new Scalar(255, 0, 100), 2);
    			Core.circle(tFinger, tFarP, 4, new Scalar(100, 0, 255), 2);
    			
    			lLocalSize = vtNodeList.size();
    			for (int i = 0; i < lLocalSize; i++) {
    				if (lEndIndex == vtNodeList.get(i).lIndex) {
    					GestureNode tNodeTmp = new GestureNode(lFarIndex, lDepth, tFarP);
    					vtNodeList.add(i+1, tNodeTmp);
    					break;
    				}
    			}
    		}
    		lIndexLocal += 4;
    	}
    	
    	String strFrame = "Finger Num = "+GetFingerNum(vtNodeList);
    	Core.putText(tFinger, strFrame, new Point(20, 20), 1, 1.0, new Scalar(255, 255, 0), 2);
    	
    	tFinger.copyTo(a_tDest);
    	
    	tBinary.release();
    	hierarchy.release();
    	tFinger.release();
    	tForeground.release();
    	
    }
    
    private static Boolean MatchId(ArrayList<Integer> a_vlUsedID, int lId) {
    	for (int usedID : a_vlUsedID) {
    		if (usedID == lId) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private static MatOfInt FindMergeHullId(MatOfPoint a_vtHull, MatOfPoint a_vtContours) {
    	ArrayList<Integer> vlHullId = new ArrayList<Integer>();
    	double eDistance = 0, eMin = 9999;
    	
    	for (Point hull : a_vtHull.toList()) {
    		int lMinId = -1;
    		eMin = 9999;
    		for (Point contours : a_vtContours.toList()) {
    			eDistance = Common.GetDistance(hull, contours);

    			if (eDistance < eMin) {
    				eMin = eDistance;
    				lMinId = a_vtContours.toList().indexOf(contours);
    			}
    			
//    			if (eDistance < 10) {
//    				vlHullId.add(lMinId);
//    				break;
//    			}
    		}
    		vlHullId.add(lMinId);
    	}

    	MatOfInt moi_vlHullId = new MatOfInt();
    	moi_vlHullId.fromList(vlHullId);
    	return moi_vlHullId;
    }

    private static MatOfPoint MergeHull(MatOfPoint a_vtHull) {
    	ArrayList<Point> vtMergeHull = new ArrayList<Point>();
    	ArrayList<Integer> vlUsedID = new ArrayList<Integer>();
    	Boolean bMatch = false;
    	double eDistance = 0;
    	Point tAvg;
    	int lCnt = 0;
    	int lLocalSize = a_vtHull.toList().size();
    	
    	for (int i = 0; i < lLocalSize; i++) {
    		lCnt = 0;
    		tAvg = new Point(0, 0);
    		bMatch = MatchId(vlUsedID, i);
    		if (!bMatch) {
    			for (int j = 0; j < lLocalSize; j++) {
    				eDistance = Common.GetDistance(a_vtHull.toList().get(i), a_vtHull.toList().get(j));
    				if (eDistance < GestureReconition_Cfg.DISTANCE_TH) {
    					vlUsedID.add(j);
    					tAvg.x += a_vtHull.toList().get(j).x;
    					tAvg.y += a_vtHull.toList().get(j).y;
    					lCnt++;
    				}
    			}
    			tAvg.x /= lCnt;
    			tAvg.y /= lCnt;

    			vtMergeHull.add(tAvg);
    		}
    		
    	}
    	
    	MatOfPoint mop_vtMergeHull = new MatOfPoint();
    	mop_vtMergeHull.fromList(vtMergeHull);
    	return mop_vtMergeHull;
    }
    
    private static int GetFingerNum(ArrayList<GestureNode> a_vtNodeList) {
    	int lFingerNum = 0;
    	int lLastIndex = 0;
    	int lNextIndex = 0;
    	int lSize = 0;
    	double eAngle = 0;

    	Point tVectorLast, tVectorNext;

    	lSize = a_vtNodeList.size();
    	for (int i = 0; i < lSize; i++) {
    		lLastIndex = (i != 0) ? i - 1 : lSize - 1;
    		lNextIndex = (i < lSize-1) ? i + 1 : 0;

    		tVectorLast = new Point(a_vtNodeList.get(lLastIndex).tPoint.x - a_vtNodeList.get(i).tPoint.x, a_vtNodeList.get(lLastIndex).tPoint.y - a_vtNodeList.get(i).tPoint.y);
    		tVectorNext = new Point(a_vtNodeList.get(lNextIndex).tPoint.x - a_vtNodeList.get(i).tPoint.x, a_vtNodeList.get(lNextIndex).tPoint.y - a_vtNodeList.get(i).tPoint.y);

    		eAngle = Common.GetTheta(tVectorLast, tVectorNext);

    		if (a_vtNodeList.get(lLastIndex).lDepth > 0 && a_vtNodeList.get(lNextIndex).lDepth > 0 && eAngle < 90) {
    			lFingerNum++;
    		}
    	}

    	return lFingerNum;
    }
    
    private final static class Common {
    	
    	// 兩點求距離
    	public static double GetDistance(Point p1, Point p2) {
        	return Math.sqrt(Math.pow((p1.x-p2.x), 2)+Math.pow((p1.y-p2.y), 2));
        }
    	
    	/// <summary> 求某向量內積 </summary>
    	public static double Dot(Point a_tV1, Point a_tV2) {
    		return (a_tV1.x * a_tV2.x + a_tV1.y * a_tV2.y);
    	}

    	/// <summary> 求某向量長度 </summary>
    	public static double GetVectorLength(Point a_tIn) {
    		return Math.sqrt(a_tIn.x * a_tIn.x + a_tIn.y * a_tIn.y);
    	}

    	/// <summary> 求兩向量的夾角 </summary>
    	public static double GetTheta(Point a_tV1, Point a_tV2) {
        	double eTheta = 0;
        	
        	eTheta = Dot(a_tV1, a_tV2);
        	eTheta /= GetVectorLength(a_tV1) * GetVectorLength(a_tV2);
        	eTheta = Math.acos(eTheta) * 180.0 / Math.PI;
        	return eTheta;
        }
    	
    }
    
}
