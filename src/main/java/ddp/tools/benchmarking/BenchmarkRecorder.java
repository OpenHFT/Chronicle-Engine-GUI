package ddp.tools.benchmarking;

import java.util.*;

//TODO DS document
public class BenchmarkRecorder
{
    //TODO DS consider using sorted tree maps
    private static final Map<String, Long> _submitTimes = new HashMap<>();
    private static final Map<String, Long> _receivedTimes = new HashMap<>();

    private List<BenchmarkResult> _benchmarkResults = new ArrayList<>();
    private String _recordingName;

    public BenchmarkRecorder(List<String> keysToExpect)
    {
        //Pre-populate map with keys for (slightly) better performance
        keysToExpect.forEach(k -> {
            _submitTimes.put(k, null);
            _receivedTimes.put(k, null);
        });
    }

    public void startNewRecording(String recordingName)
    {
        //TODO DS add checks on whether currently recording
        _recordingName = recordingName;

        //TODO DS should not be necessary. Can we do this when calculating? Then cannot do check points...
        for (String key : _submitTimes.keySet())
        {
            _submitTimes.put(key, null);
            _receivedTimes.put(key, null);
        }
    }

    public void finishRecording() throws Exception
    {
        BenchmarkResult benchmarkResult = waitCalculateBenchmarks();
        benchmarkResult.printResults();

        _benchmarkResults.add(benchmarkResult);
    }

    public void recordKeySubmit(String key)
    {
        Long previous = _submitTimes.get(key);

        if(previous != null)
        {
            throw new RuntimeException("ALREADY RECORDED: " + key);
        }

        _submitTimes.put(key, System.nanoTime());
    }

    public void recordKeyReceived(String key)
    {
        long receivedTime = System.nanoTime();

        Long previous = _receivedTimes.get(key);

        if(previous != null)
        {
            throw new RuntimeException("ALREADY RECORDED: " + key);
        }

        _receivedTimes.put(key, receivedTime);
    }

    public BenchmarkResult waitCalculateBenchmarks() throws Exception
    {
        int noOfWaits = 100;
        int waitTime = 20;
        int waited = 0;

        boolean anyNullvalues = false;

        System.out.println(_receivedTimes.values().stream().anyMatch(v -> v == null));


        while ((anyNullvalues = _receivedTimes.values().stream().anyMatch(v -> v == null))
                && waited < noOfWaits)
        {
            waited++;
            System.out.println("Waiting...");
            Thread.sleep(waitTime);
        }

        if(anyNullvalues)
        {
            throw new Exception("NULL VALUES. WAITED (MS): " + noOfWaits * waitTime);
        }

        return calculateBenchmarks();
    }

    public BenchmarkResult calculateBenchmarks()
    {
        System.out.println("Calculating benchmarks...");

        BenchmarkResult benchmarkResult = new BenchmarkResult(_recordingName, _submitTimes.size());

        for (String key : _submitTimes.keySet())
        {
            //TODO DS null checks and warnings...
            Long submitTime = _submitTimes.get(key);
            Long receivedTime = _receivedTimes.get(key);

            Long roundTrip = receivedTime - submitTime;


            benchmarkResult.addRoundTrip(key, roundTrip);
        }

        return benchmarkResult;
    }
}