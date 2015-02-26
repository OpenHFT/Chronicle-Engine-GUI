package jp.mufg.examples.ObjectWithEnumExamples;

/**
 * Created by daniels on 26/02/2015.
 */
public interface EnumTest
{
    void writeObjectWithEnum(ObjectWithEnum objectWithEnum);

    void writeObjectWithoutEnum(ObjectWithoutEnum objectWithoutEnum);

    void writeObjectWithoutEnumDataValueClass(ObjectWithoutEnumDataValueClass objectWithoutEnumDataValueClass);

    void writeObjectWithoutEnumExternalizable(ObjectWithoutEnumExternalizable objectWithoutEnumExternalizable);

    void writeObjectWithEnumExternalizable(ObjectWithEnumExternalizable objectWithEnumExternalizable);
}