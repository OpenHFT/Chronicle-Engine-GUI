package musiverification;

import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter.lawrey on 16/07/2015.
 */
public class AllTestMain {
    public static void main(String[] args) throws Throwable {
        final List<Failure> failures = new ArrayList<>();
        RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                failures.add(failure);
                System.err.println(failure);
            }
        });
        for (Class testClass : new Class[]{
                FailingTest.class,
                SubscriptionModelTest.class,
                ReplicationTest.class,
                SubscriptionModelPerformanceTest.class,
                SubscriptionModelFilePerKeyPerformanceTest.class,
                RemoteSubscriptionModelPerformanceTest.class,
                ManyMapsTest.class,
                ManyMapsFilePerKeyTest.class
        }) {
            System.out.println("\n=====================");
            System.out.println("\t" + testClass);
            System.out.println("=====================");
            new JUnit4Builder().runnerForClass(testClass).run(runNotifier);
        }
        if (failures.size() == 1) {
            System.out.println("Got the expected number of failures, 1");
        } else {
            for (Failure failure : failures) {
                System.err.println(failure);
            }
            System.exit(-1);
        }
    }
}
