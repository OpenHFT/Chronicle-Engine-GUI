package ddp.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestUtils
{
    /**
     * Calculates the runtime in nanoseconds from the given start time in nanoseconds.
     * Prints the runtime in nanos, millis and seconds.
     *
     * @param startTimeInNanoseconds Start time in nanos.
     * @return Runtime in nanos.
     */
    public static long calculateAndPrintRuntime(long startTimeInNanoseconds)
    {
        return calculateAndPrintRuntime(startTimeInNanoseconds, 1);
    }

    public static long calculateAndPrintRuntime(long startTimeInNanoseconds, int count) {
        long endNanoTime = System.nanoTime();

        long runtimeNanoSeconds = endNanoTime - startTimeInNanoseconds;

        double runtimeMilliseconds = (double) runtimeNanoSeconds / 1000000.0;

        double runtimeSeconds = runtimeMilliseconds / 1000.0;

        System.out.printf("For %,d tests, Runtime: %,d nanoseconds | %.1f milliseconds | %.3f seconds%s%n",
                count, runtimeNanoSeconds / count, runtimeMilliseconds / count, runtimeSeconds / count, count > 1 ? " average" : "");

        return runtimeNanoSeconds;
    }

    /**
     * Loads the given file into a string.
     *
     * @param fileName Name of the resource to load.
     * @return String value of the text file.
     * @throws java.io.IOException
     */
    public static String loadSystemResourceFileToString(String fileName) throws IOException, URISyntaxException
    {
        URL testFileUrl = ClassLoader.getSystemResource(fileName);
        URI testFileUri = testFileUrl.toURI();

        final StringBuilder stringBuilder = new StringBuilder();
        Files.lines(Paths.get(testFileUri)).forEach(stringBuilder::append);

        return stringBuilder.toString();
    }

    /**
     * Loads the given system resource and assumes it is a csv file with a key and value column. These key/value pairs
     * are loaded into a map.
     *
     * @param resourcePath Path to resource file that is to be loaded into a map.
     * @return Map loaded with key/value pairs from the resource file.
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Map<String, Double> loadSystemResourceKeyValueCsvFileToMap(String resourcePath) throws IOException, URISyntaxException
    {
        URL testFileUrl = ClassLoader.getSystemResource(resourcePath);
        URI testFileUri = testFileUrl.toURI();

        Map<String, Double> results = new HashMap<>();

        Files.lines(Paths.get(testFileUri)).forEach(x -> {
            String[] strings = x.split(",");

            results.put(strings[0], Double.parseDouble(strings[1]));
        });

        return results;
    }

    /**
     * @param extension
     * @param stringToWrite
     * @throws IOException
     */
    public static void saveTestFileToDisk(String extension, String stringToWrite) throws IOException
    {
        Files.write(Paths.get("./test" + extension), stringToWrite.getBytes());
    }

    public static void deleteTestFile(String extension) throws IOException
    {
        deleteFile(Paths.get("./test" + extension).toString());
    }

    public static void deleteFile(String path) throws IOException
    {
        try
        {
            Files.deleteIfExists(Paths.get(path));
        }
        catch (Exception e)
        {
            System.err.println(String.format("Failed to delete file '%s'. Exception: %s", path, e));
        }
    }

    public static void createDirectoryIfNotExists(String directoryName)
    {
        File directory = new File(directoryName);

        if (!directory.exists())
        {

            System.out.println("Creating directory: " + directoryName);

            boolean result = false;

            try
            {
                directory.mkdir();
                result = true;
            }
            catch (SecurityException se)
            {
                System.out.println("Could not create directory '%s'.");
            }

            if (result)
            {
                System.out.println("DIR created");
            }
        }
    }
}