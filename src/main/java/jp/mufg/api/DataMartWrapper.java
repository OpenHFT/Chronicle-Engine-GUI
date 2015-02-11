package jp.mufg.api;

public interface DataMartWrapper {
    boolean runOnce();

    boolean onIdle();

    String getTarget();
}
