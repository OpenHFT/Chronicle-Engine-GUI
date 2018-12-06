package jp.mufg.api;

public interface DirectDataMart extends DataMart {

    boolean hasChanged();

    String getTarget();
}
