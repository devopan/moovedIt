#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/mat.hpp>
#include <cmath>
#include <vector>
#include <math.h>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT jint JNICALL Java_pcdiy_moovedit_MainActivity_DetectMotion(JNIEnv*, jobject, jlong addr1Gray1, jlong addr2Gray2);

JNIEXPORT jint JNICALL Java_pcdiy_moovedit_MainActivity_DetectMotion(JNIEnv*, jobject, jlong addr1Gray1, jlong addr2Gray2)
{
    Mat& mGr1  = *(Mat*)addr1Gray1;
    Mat& mGr2  = *(Mat*)addr2Gray2;
    int totalDif1 = 0;
    int totalDif2 = 0;
	int cols0 = mGr1.cols;
	int rows0 = mGr1.rows;
	int cols1 = mGr2.cols;
	int rows1 = mGr2.rows;

	for(int i=1; i<rows0; i+=3){
		for(int j=1; j<cols0; j+=3){
			totalDif1 += (int)mGr1.at<uchar>(i,j);
		}
	}

	for(int k=1; k<rows1; k+=3){
		for(int l=1; l<cols1; l+=3){
			totalDif2 += (int)mGr2.at<uchar>(k,l);
		}
	}
	int totalDif = abs(totalDif1 - totalDif2);

    return totalDif;
}
}

