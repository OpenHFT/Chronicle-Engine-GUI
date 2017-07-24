package queue4.externalizableObjects;
import java.util.*;

/**
 * The different data types that fields subscribed to.
 */
public enum FieldDataType
{
    // Ids should never change and must be unique, but enum declaration order can change.

    BOOLEAN((byte) 1),
    CHARACTER((byte) 2),
    STRING((byte) 3),
    INTEGER((byte) 4),
    LONG((byte) 5),
    FLOAT((byte) 6),
    DOUBLE((byte) 7),
    BYTES((byte) 8),
    DATE((byte) 9);


    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */
    private static Map<Byte, FieldDataType> _idMappings;


    /**
     * Custom id value (byte because we are unlikely to exceed 127 enum types).
     * Custom ids should never change, but enum declaration order can change.
     */
    private final byte _id;


    /**
     * Creates a new FieldDataType with the given id. Id must be unique.
     *
     * @param id Custom id for the enum - MUST be unique (only checked at runtime).
     * @throws Exception
     */
    FieldDataType(byte id)
    {
        _id = id;
    }


    /**
     * Initialise the id mapping map with all FieldDataTypes.
     * Returns immediately if the map has already been initialised.
     * <p>
     * Throws IllegalArgumentException if two types attempt to use the same id.
     */
    private static synchronized void initialiseIdMappings()
    {
        if (_idMappings != null)
        {
            return;
        }

        _idMappings = new HashMap<>();

        FieldDataType[] values = FieldDataType.values();

        for (int i = 0; i < values.length; i++)
        {
            FieldDataType fieldDataType = values[i];

            byte id = fieldDataType.getId();

            if (_idMappings.containsKey(id))
            {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + FieldDataType.class.getSimpleName());
            }

            _idMappings.put(fieldDataType.getId(), fieldDataType);
        }
    }


    /**
     * Returns the FieldDataType with the given id.
     * If no such FieldDataType exist null is returned.
     *
     * @param id Id to get matching FieldDataType for.
     * @return FieldDataType matching the id, or null of no such data type exist.
     */
    public static FieldDataType fromId(byte id)
    {
        initialiseIdMappings();

        return _idMappings.get(id);
    }


    /**
     * Get the equivalent FieldDataType of the object type.
     * If no matching FieldDataType exist null is returned.
     *
     * @param object Object to get matching FieldDataType from.
     * @return FieldDataType matching the type of the given object. Null is returned if no such match exists.
     */
    public static FieldDataType getFieldDataTypeFromObject(Object object)
    {
        if (object instanceof Double)
        {
            return DOUBLE;
        }
        else if (object instanceof Character)
        {
            return CHARACTER;
        }
        else if (object instanceof String)
        {
            return STRING;
        }
        else if (object instanceof Integer)
        {
            return INTEGER;
        }
        else if (object instanceof Long)
        {
            return LONG;
        }
        else if (object instanceof Float)
        {
            return FLOAT;
        }
        else if (object instanceof Boolean)
        {
            return BOOLEAN;
        }
        else if (object instanceof byte[])
        {
            return BYTES;
        }

        return null;
    }


    /**
     * @return Id for the FieldDataType
     */
    public byte getId()
    {
        return _id;
    }


    /**
     * Gets the default size in bytes for this data type.
     *
     * @return byte size for this data type. String returns 1 as that is the minimum byte size of a string.
     */
    public int getDataTypeByteSize()
    {
        switch (this)
        {
            case BOOLEAN:
                return 1; //Actually 1 bit
            case CHARACTER:
                return 2;
            case STRING:
                return 1; //Variable length, but we return the minimum
            case INTEGER:
                return 4; //This is a 32-bit int, we do not consider 64-bit
            case LONG:
                return 8; //Long is 64-bit
            case FLOAT:
                return 4;
            case DOUBLE:
                return 8; //Signed double
            case BYTES:
                return 1; //Variable length, but we return the minimum
            case DATE:
                return 8; //Signed double
            default:
                return 0; //If unknown return lowest
        }
    }
}
