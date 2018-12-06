package ddp.tools.benchmarking;

import java.util.*;

//TODO DS document
public class StringGenerator
{

    /**
     * Create a string of the given size in KB consisting of the given character repeated.
     *
     * @param stringSizeInKb Size of the generated string in KB.
     * @param character      Character to repeat in string.
     * @return Generated string.
     */
//    public static String createStringOfSize(int stringSizeInKb, char character)
//    {
//        //1 KB = 1024 bytes and each Java char is 2 bytes in size
//        int noOfChars = stringSizeInKb * 1024 / 2;
//
//        StringBuilder sb = new StringBuilder(noOfChars);
//        for (int i = 0; i < noOfChars; i++)
//        {
//            sb.append(character);
//        }
//        return sb.toString();
//    }

    public static String createStringOfSize(int stringSizeInKb, char character)
    {
        //1 KB = 1024 bytes and each Java char is 2 bytes in size
        int noOfChars = stringSizeInKb * 1024 / 2;

        Random random = new Random();

        StringBuilder sb = new StringBuilder(noOfChars);
        for (int i = 0; i < noOfChars; i++)
        {

            String s = random.nextInt(10) + "";
            sb.append(s);
//            if(i % 2 == 0)
//            {
////                System.out.println("a");
//                sb.append('a');
//            }
//            else
//            {
////                System.out.println("b");
//                sb.append('b');
//            }

//            sb.append(character);
        }
        return sb.toString();
    }

    public static String createRandomStringOfSize(int stringSizeInKb)
    {
        //1 KB = 1024 bytes and each Java char is 2 bytes in size
        int noOfChars = stringSizeInKb * 1024 / 2;

        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();

        Random random = new Random();

        char[] resultCharArray = new char[noOfChars];

        for (int i = 0; i < noOfChars; i++)
        {
            int charIndex = random.nextInt(chars.length);
            resultCharArray[i] = chars[charIndex];
        }

        return new String(resultCharArray);

//        StringBuilder salt = new StringBuilder(resultCharArray);
//        Random rnd = new Random();
//        while (salt.length() < 18) {
//            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
//            salt.append(SALTCHARS.charAt(index));
//        }
//        String saltStr = salt.toString();
//        return saltStr;
    }
}