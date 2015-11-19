package ddp.tools.benchmarking;

import ddp.api.datahub.*;
import ddp.tools.utils.*;

import java.net.*;
import java.util.*;

public class StringStringBenchmarker implements AutoCloseable
{
    private static int _noOfKeys = 50;
    private static int _noOfRuns = 100; //100_000_000;
    private static DataHubConfiguration _dataHubConfiguration;
    private static DataHubClient _dataHubClient;
    private static DataStorePublisher<String, String> _dataStorePublisher;

    private static List<String> _keys;
    private static final String mapUri = "/ddp/data/live/test-map-2";
//    private static final String hostname = "DDP-PROD-UK2";
    private static final String hostname = "localhost";
    private static final int port = 5850;
    private final BenchmarkRecorder _benchmarkRecorder;

    public static void main(String[] args) throws Exception
    {
        StringStringBenchmarker stringStringBenchmarker = new StringStringBenchmarker();
        stringStringBenchmarker.start();
        stringStringBenchmarker.close();
    }

    public StringStringBenchmarker() throws UnknownHostException
    {
        _keys = new ArrayList<>();

        for (int i = 0; i < _noOfKeys; i++)
        {
            String key = "Key" + i;
            _keys.add(key);
        }

        _benchmarkRecorder = new BenchmarkRecorder(_keys);

        _dataHubConfiguration = new DataHubConfiguration(hostname, port);

        _dataHubClient = new DataHubClient(_dataHubConfiguration);
        _dataStorePublisher = _dataHubClient.getDataStorePublisher(mapUri, String.class, String.class);

        Thread subscriberThread = new Thread(() -> {
            _dataStorePublisher.registerTopicSubscriber(String.class, String.class, (k, v) -> {
                _benchmarkRecorder.recordKeyReceived(k);
            }, false);
        });

        subscriberThread.start();
    }

    private void start() throws Exception
    {
        //Hack to connect and limit "performance" impact of initial connection
        int size = _dataStorePublisher.size();
        //TODO DS check why it takes longer with random string. Could have something to do with contention on read in event?
//        String twoMbString = StringGenerator.createStringOfSize(2048, 'x');
//        String twoMbString = StringGenerator.createRandomStringOfSize(2048).toLowerCase();
        String twoMbString = StringGenerator.createRandomStringOfSize(2048);

        System.out.println(twoMbString);

        for (int i = 0; i < _noOfRuns; i++)
        {
            _benchmarkRecorder.startNewRecording(i + "");

            for (String key : _keys)
            {
                _benchmarkRecorder.recordKeySubmit(key);

                _dataStorePublisher.put(key, twoMbString);

                Thread.sleep(20); //Approximately 50 updates per second
            }

            _benchmarkRecorder.finishRecording();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (_dataStorePublisher != null)
        {
            _dataStorePublisher.close();
        }

        if (_dataHubClient != null)
        {
            _dataHubClient.close();
        }
    }
}