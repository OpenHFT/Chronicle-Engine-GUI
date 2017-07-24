package queue.replication;


import net.openhft.chronicle.core.pool.ClassAliasPool;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Application {

    //private static final ExecutorService threadpool = Executors.newFixedThreadPool(2);

    public static void deleteFile(@NotNull File element) {
        if (element.isDirectory()) {
            for (@NotNull File sub : element.listFiles()) {
                deleteFile(sub);
            }
        }
        element.delete();
    }


    public static void addClass(Class aClass) {
        ClassAliasPool.CLASS_ALIASES.addAlias(aClass);
    }


}


