package org.bj.deeplearning.tools;

/**
 * Created by benjaminhviid on 15/04/2017.
 */
public class Utils {

    private Utils(){}

    public static double map(double s, double a1, double a2, double b1, double b2)
    {
        return b1 + (s-a1)*(b2-b1)/(a2-a1);
    }

    public static double clamp (double value, double min, double max){
        return Math.max(min, Math.min(max, value));
    }

}
