package jp.mufg.examples.objectSerializationExamples;

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