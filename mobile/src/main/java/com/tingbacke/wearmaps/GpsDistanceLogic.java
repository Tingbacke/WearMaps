package com.tingbacke.wearmaps;


public class GpsDistanceLogic {

    public float distance;


    public GpsDistanceLogic(float distance) {

        this.distance = distance;

    }

    //kollar hur många meter det är mellan dig och något annat för att ge dig ett nummer mellan 1 o 7 för att trigga vad fan du vill

    public int eventLaunch() {
        int event = (int) distance;
        int cases = 0;


        if (event <= 100 && event >= 0) {


            cases = 1;
        } else if (event <= 200 && event >= 100) {
            cases = 2;
        } else if (event <= 500 && event >= 200) {
            cases = 3;
        } else if (event <= 1000 && event >= 500) {
            cases = 4;
        } else if (event <= 2000 && event >= 1000) {
            cases = 5;
        } else if (event <= 4000 && event >= 2000) {
            cases = 6;
        } else if (event <= 10000 && event >= 4000) {
            cases = 7;
        }
        return cases;
    }


}
