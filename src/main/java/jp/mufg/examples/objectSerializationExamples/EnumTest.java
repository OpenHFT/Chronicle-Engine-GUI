package jp.mufg.examples.objectSerializationExamples;

import java.util.*;

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

    void writeObjectWithEnumExternalizableAndStringObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<String, Object> stringObjectMap);

    void writeObjectWithEnumExternalizableAndEnumObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<TestEnum, Object> stringObjectMap);
}