package com.journaldev.androidcameraxopencv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HudController {
    public static void RenderTarget(Mat mat, Target t){
        Imgproc.circle(mat,new Point(t.x, t.y), (int)t.r,new Scalar(0,255,0),3);
    }
    public static void RenderFullTarget(Mat mat, Target t){
        Imgproc.circle(mat,new Point(t.x, t.y), (int)t.r,new Scalar(255),-1);
    }
}
