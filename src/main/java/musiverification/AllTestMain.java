package musiverification;

import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by peter.lawrey on 16/07/2015.
 */
public class AllTestMain {
    public static void main(String[] args) throws Throwable {
        System.out.println("Start tests on " + new Date());

        final List<Failure> failures = new ArrayList<>();
        RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                failures.add(failure);
                System.err.println(failure);
                failure.getException().printStackTrace();
            }
        });
        for (Class testClass : new Class[]{
                ReplicationTest.class,
                RemoteSubscriptionModelPerformanceTest.class,
                RemoteSubscriptionModelPerformance2Test.class,
                SubscriptionModelTest.class,
                SubscriptionModelPerformanceTest.class,
                SubscriptionModelFilePerKeyPerformanceTest.class,
//                ManyMapsTest.class,
//                ManyMapsFilePerKeyTest.class
        }) {
            System.out.println("\n=====================");
            System.out.println("\t" + testClass);
            System.out.println("=====================");
            new JUnit4Builder().runnerForClass(testClass).run(runNotifier);
        }
//        if (failures.size() == 1) {
//            System.out.println("Got the expected number of failures, 1");
//        } else {
        if (!failures.isEmpty()) {
            System.out.println("\n" +
                    "***************************\n" +
                    "\tFAILED TESTS\n" +
                    "***************************");

            for (Failure failure : failures) {
                System.err.println(failure);
                failure.getException().printStackTrace();
            }
            System.exit(-1);
        }
        System.out.println("Finished tests on " + new Date());
    }
}
