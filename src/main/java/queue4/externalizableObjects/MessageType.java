package queue4.externalizableObjects;

import java.util.*;

/**
 * The message type that can be communicated from the server to the client via data manger
 */
public enum MessageType
{
    VALUATION_ENVIRONMENT_SAVE_ERROR(1);


    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */
    private static Map<Integer, MessageType> _idMappings;


    /**
     * Custom id value (byte because we are unlikely to exceed 127 enum types).
     * Custom ids should never change, but enum declaration order can change.
     */
    private final int _id;


    /**
     * Creates a new DealType with the given id. Id must be unique.
     *
     * @param id Custom id for the enum - MUST be unique (only checked at runtime).
     * @throws Exception
     */
    MessageType(int id)
    {
        _id = id;
    }


    /**
     * Initialise the id mapping map with all Index; returns immediately if the map has already been initialised.
     *
     * @throws IllegalArgumentException if two types attempt to use the same id.
     */
    private static synchronized void initialiseIdMappings()
    {
        if (_idMappings != null)
        {
            return;
        }

        _idMappings = new HashMap<>();

        MessageType[] values = MessageType.values();

        for (int i = 0; i < values.length; i++)
        {
            MessageType index = values[i];
            int id = index.getId();

            if (_idMappings.containsKey(id))
            {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + MessageType.class.getSimpleName());
            }

            _idMappings.put(index.getId(), index);
        }
    }


    /**
     * Returns the DealType with the given id.
     * If no such DealType exist null is returned.
     *
     * @param id Id to get matching DealType for.
     * @return DealType matching the id, or null of no such data type exist.
     */
    public static MessageType fromId(int id)
    {
        initialiseIdMappings();

        return _idMappings.get(id);
    }


    /**
     * @return Id for the DealType.
     */
    public int getId()
    {
        return _id;
    }
}