package jp.mufg.examples.objectSerializationExamples;

import java.io.*;

/**
 * Created by daniels on 26/02/2015.
 */
public class ObjectWithoutEnum implements Serializable
{
    String someString;
    double someDouble;
    double someInt;

    public String getSomeString()
    {
        return someString;
    }

    public void setSomeString(String someString)
    {
        this.someString = someString;
    }

    public double getSomeDouble()
    {
        return someDouble;
    }

    public void setSomeDouble(double someDouble)
    {
        this.someDouble = someDouble;
    }

    public double getSomeInt()
    {
        return someInt;
    }

    public void setSomeInt(double someInt)
    {
        this.someInt = someInt;
    }
}