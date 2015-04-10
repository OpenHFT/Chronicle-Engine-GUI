package jp.mufg.chronicle.map.size;

import java.io.*;

/**
 * Created by daniels on 10/04/2015.
 */
public class FillLargeMapsMain
{
    public static void main(String[] args) throws IOException
    {
//        System.out.println("Filling map with small strings...");
//        System.out.println("Successful: " + FillLargeMaps.fillLargeMapWithSmallStrings(false));
//        System.out.println("Finished map with small strings...");
//
//        System.out.println("Press any key to continue...");
//        System.in.read();

        System.out.println("Filling map with doubles...");
        System.out.println("Successful: " + FillLargeMaps.fillLargeMapWithDoubles(false));
        System.out.println("Finished map with doubles...");

        System.out.println("Done!");
    }
}