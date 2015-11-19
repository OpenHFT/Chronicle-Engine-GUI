package ddp.tools.benchmarking;

import java.time.*;

public class BenchmarkResult
{
    private static final double NANOS_PER_MS = 1_000_000.0;

    private long _roundTripSumInNanos = 0;
    private long _maxRoundTripInNanos = 0;
    private Object _maxRoundTripKey;

    private long _minRoundTripInNanos = 0;
    private Object _minRoundTripKey;

    private String _resultName;
    private final int _noOfKeys;

    public BenchmarkResult(String resultName, int noOfKeys)
    {
        _resultName = resultName;
        _noOfKeys = noOfKeys;
    }

    public void addRoundTrip(Object key, Long roundTripInNanos)
    {
        if (roundTripInNanos < 0)
        {
            throw new RuntimeException("ROUND TRIP CANNOT BE NEGATIVE!");
        }

        _roundTripSumInNanos += roundTripInNanos;

        if (roundTripInNanos > _maxRoundTripInNanos)
        {
            _maxRoundTripInNanos = roundTripInNanos;
            _maxRoundTripKey = key;
        }
        else if (roundTripInNanos < _minRoundTripInNanos || _minRoundTripInNanos == 0)
        {
            _minRoundTripInNanos = roundTripInNanos;
            _minRoundTripKey = key;
        }

//        System.out.println(key + " : " + roundTripInNanos / NANOS_PER_MS);
    }

    public long getAvgLatencyInNanos()
    {
        return _roundTripSumInNanos / 2 / _noOfKeys;
    }

    public long getAvgRoundTripInNanos()
    {
        return _roundTripSumInNanos / _noOfKeys;
    }

    public long getRoundTripSumInNanos()
    {
        return _roundTripSumInNanos;
    }

    public long getMaxRoundTripInNanos()
    {
        return _maxRoundTripInNanos;
    }

    public Object getMaxRoundTripKey()
    {
        return _maxRoundTripKey;
    }

    public long getMinRoundTripInNanos()
    {
        return _minRoundTripInNanos;
    }

    public Object getMinRoundTripKey()
    {
        return _minRoundTripKey;
    }

    public int getNoOfKeys()
    {
        return _noOfKeys;
    }

    public String getResultName()
    {
        return _resultName;
    }

    public void printResults()
    {
        System.out.println("Results '" + _resultName + "' at " + Instant.now());
        System.out.println("####################################");
        System.out.println("Average roundtrip in nanos/ms: " + getAvgRoundTripInNanos() + " / " + getAvgRoundTripInNanos() / NANOS_PER_MS);
//        System.out.println("Average roundtrip in ms/: " + getAvgRoundTripInNanos() / NANOS_PER_MS);
        System.out.println("Average latency in nanos/ms: " + getAvgLatencyInNanos() + " / " + getAvgLatencyInNanos() / NANOS_PER_MS);
//        System.out.println("Average latency in ms: " + getAvgLatencyInNanos() / NANOS_PER_MS);
        System.out.println("Max roundtrip in nanos/ms (" + getMaxRoundTripKey() + "): " + getMaxRoundTripInNanos() + " / " + getMaxRoundTripInNanos() / NANOS_PER_MS);
        System.out.println("Min roundtrip in nanos/ms (" + getMinRoundTripKey() + "): " + getMinRoundTripInNanos() + " / " + getMinRoundTripInNanos() /  NANOS_PER_MS);
        System.out.println("####################################");
    }

    @Override
    public String toString()
    {
        return "BenchmarkResult{" +
                "_roundTripSumInNanos=" + _roundTripSumInNanos +
                ", _maxRoundTripInNanos=" + _maxRoundTripInNanos +
                ", _maxRoundTripKey=" + _maxRoundTripKey +
                ", _minRoundTripInNanos=" + _minRoundTripInNanos +
                ", _minRoundTripKey=" + _minRoundTripKey +
                ", _resultName='" + _resultName + '\'' +
                ", _noOfKeys=" + _noOfKeys +
                '}';
    }
}