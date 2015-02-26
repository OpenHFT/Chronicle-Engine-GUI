package jp.mufg.chronicle.queue.testclasses;

import java.util.*;

/**
 * Created by daniels on 26/02/2015.
 */
public interface EnumTestInterface
{
    void writeObjectWithEnum(ObjectWithEnum objectWithEnum);

    void writeObjectWithoutEnum(ObjectWithoutEnum objectWithoutEnum);

    void writeObjectWithoutEnumDataValueClass(ObjectWithoutEnumDataValueClass objectWithoutEnumDataValueClass);

    void writeObjectWithoutEnumExternalizable(ObjectWithoutEnumExternalizable objectWithoutEnumExternalizable);

    void writeObjectWithEnumExternalizable(ObjectWithEnumExternalizable objectWithEnumExternalizable);

    void writeObjectWithEnumExternalizableAndStringObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<String, Object> stringObjectMap);

    void writeObjectWithEnumExternalizableAndStringDoubleMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<String, Double> stringDoubleMap);

    void writeObjectWithEnumExternalizableAndEnumObjectMap(ObjectWithEnumExternalizable objectWithEnumExternalizable, Map<TestEnum, Object> stringObjectMap);

    void writeObjectWithMapDataValueClass(MapFieldDataValueClass mapFieldDataValueClass);
}