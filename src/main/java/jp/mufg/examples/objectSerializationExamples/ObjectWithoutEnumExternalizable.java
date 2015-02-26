package jp.mufg.examples.ObjectWithEnumExamples;

import java.io.*;

/**
 * Created by daniels on 26/02/2015.
 */
public class ObjectWithoutEnumExternalizable implements Externalizable
{
    String someString;
    double someDouble;
    int someInt;

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

    public int getSomeInt()
    {
        return someInt;
    }

    public void setSomeInt(int someInt)
    {
        this.someInt = someInt;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(someString);
        out.writeDouble(someDouble);
        out.writeInt(someInt);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.someString = in.readUTF();
        this.someDouble = in.readDouble();
        this.someInt = in.readInt();
    }
}