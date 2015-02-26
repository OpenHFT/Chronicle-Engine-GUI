package jp.mufg.examples.objectSerializationExamples;

import java.util.*;

/**
 * Created by daniels on 26/02/2015.
 */
public class EnumTestImpl implements EnumTest
{
    @Override
    public void writeObjectWithEnum(ObjectWithEnum objectWithEnum)
    {
        System.out.println("writeObjectWithEnum");
        System.out.println(objectWithEnum);
    }

    @Override
    public void writeObjectWithoutEnum(ObjectWithoutEnum objectWithoutEnum)
    {
        System.out.println("writeObjectWithoutEnum");
        System.out.println(objectWithoutEnum);
    }

    @Override
    public void writeObjectWithoutEnumDataValueClass(ObjectWithoutEnumDataValueClass objectWithoutEnumDataValueClass)
    {
        System.out.println("writeObjectWithoutEnumDataValueClass");
        System.out.println(objectWithoutEnumDataValueClass);
    }

    @Override
    public void writeObjectWithoutEnumExternalizable(ObjectWithoutEnumExternalizable objectWithoutEnumExternalizable)
    {
        System.out.println("writeObjectWithoutEnumExternalizable");
        System.out.println(objectWithoutEnumExternalizable);
    }

    @Override
    public void writeObjectWithEnumExternalizable(ObjectWithEnumExternalizable objectWithEnumExternalizable)
    {
        System.out.println("writeObjectWithEnumExternalizable");
        System.out.println(objectWithEnumExternalizable);
    }

    @Override
    public void writeObjectWithEnumExternalizableAndStringObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<String, Object> stringObjectMap)
    {
        System.out.println("writeObjectWithEnumExternalizableAndStringObjectMap");
        System.out.println(objectWithEnumExternalizable);
        System.out.println(stringObjectMap);
    }

    @Override
    public void writeObjectWithEnumExternalizableAndEnumObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<TestEnum, Object> enumObjectMap)
    {
        System.out.println("writeObjectWithEnumExternalizableAndStringObjectMap");
        System.out.println(objectWithEnumExternalizable);
        System.out.println(enumObjectMap);
    }
}