package jp.mufg.examples.objectSerializationExamples;

import java.io.*;

/**
 * Created by daniels on 26/02/2015.
 */
public class ObjectWithEnumExternalizable implements Externalizable
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(someString);
        out.writeObject(testEnum);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.someString = in.readUTF();
        this.testEnum = (TestEnum) in.readObject();
    }
}