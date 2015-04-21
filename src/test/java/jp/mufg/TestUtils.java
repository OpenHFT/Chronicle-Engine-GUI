package jp.mufg;

public class TestUtils
{
    public static double calculateAndPrintRuntime(long startTimeInNanoseconds)
    {
        long endNanoTime = System.nanoTime();

        long runtimeNanoSeconds = endNanoTime - startTimeInNanoseconds;

        double runtimeMilliseconds = (double)runtimeNanoSeconds / 1000000.0;

        double runtimeSeconds = runtimeMilliseconds / 1000.0;

        System.out.println("Runtime: " + runtimeNanoSeconds + " nanoseconds | "
                + runtimeMilliseconds + " milliseconds | " + runtimeSeconds + " seconds");

        return runtimeNanoSeconds;
    }
}