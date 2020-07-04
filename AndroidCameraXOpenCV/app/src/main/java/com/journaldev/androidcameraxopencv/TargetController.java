package com.journaldev.androidcameraxopencv;

import android.os.Debug;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TargetController {
    Queue<Target> listTargets = new LinkedList<Target>();
    ScoreController scoreController  = new ScoreController();

    int w, h;
    public void AddTargetLocal(Target t){
        TransformToImageCoordinates(t);
        listTargets.add(t);
    }

    public Target GetCurrentTarget(){
        return listTargets.peek();
    }

    public void RenderCurrentTarget(Mat mat){
        Target t = TransformToImageCoordinates(GetCurrentTarget());

        HudController.RenderTarget(mat, t);
    }

    public ScoreStatistic SetCurrentTargetCaptured(){
        Target t = listTargets.remove();
        scoreController.CalculateScore(t);
        return scoreController.SetupScore(GetCurrentTarget());

    }
    public boolean isColliding (Mat smallMat){
        int wS = smallMat.width();
        int hS = smallMat.height();
        Scalar s;
        // create black mat and draw fill circle white

        Target t = TransformToImageCoordinates(GetCurrentTarget(),wS,hS);
        Mat matZero = new Mat(new org.opencv.core.Size(wS,hS), smallMat.type(), new Scalar(0));//  cv::Size(1, 49), CV_64FC1);
        HudController.RenderFullTarget(matZero,t);

        Mat matMask =  matZero.mul(smallMat,1);

        // matZero.mul(smallMat,1);
        s = Core.sumElems(matMask);
//        Log.d("TAG", "sum: " + s);
//        return false;
        return s.val[0] > 8000;
//        return s.val[0] > 500;

        /*
        Target t = TransformToImageCoordinates(GetCurrentTarget(),wS,hS);

        for (int i = 0; i < gray.rows; i++)
        {
            for (int j = 0; j < gray.cols; j++)
            {
                byte pixel = gray.get(i, j);
            }
        }
        return true;*/
    }
    Target TransformToImageCoordinates(Target t){
        return new Target(t.x*w,t.y*h,t.r*h);
    }
    Target TransformToImageCoordinates(Target t,int w, int h){
        return new Target(t.x*w,t.y*h,t.r*h);
    }
    public void SetImageSpace(int w, int h){
        this.w = w;
        this.h = h;

    }
}

class Target{
    float x,y,r;

    public Target(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }
}