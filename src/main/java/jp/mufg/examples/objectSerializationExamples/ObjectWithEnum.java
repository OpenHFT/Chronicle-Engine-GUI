package jp.mufg.examples.objectSerializationExamples;

import java.io.*;

/**
 * Created by daniels on 26/02/2015.
 */
public class ObjectWithEnum implements Serializable
{
    String someString;
    TestEnum testEnum;

    public String getSomeString()
    {
        return someString;
    }

    public void setSomeString(String someString)
    {
        this.someString = someString;
    }

    public TestEnum getTestEnum()
    {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum)
    {
        this.testEnum = testEnum;
    }
}