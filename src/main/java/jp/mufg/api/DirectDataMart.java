package jp.mufg.api;

public interface DirectDataMart extends DataMart {

    public boolean hasChanged();

    public String getTarget();
}
