package queue4.externalizableObjects;

/**
 * Created by cliveh on 10/05/2016.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * The suppliers of market data, which may include market data vendors as well as mathematical models.
 */
public enum MarketDataSupplier
{
    // Ids should never change and must be unique, but enum declaration order can change.

    BLOOMBERG(1),
    REUTERS(2),
    BROADWAY(3),
    ADEPT(4),
    DDP(5),
    ION(6),
    FILE(7);

    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */
    private static Map<Integer, MarketDataSupplier> _idMappings;

    /**
     * Custom id value (byte because we are unlikely to exceed 127 enum types).
     * Custom ids should never change, but enum declaration order can change.
     */
    private final int _id;

    /**
     * Creates a new MarketDataSupplier with the given id.
     *
     * @param id Custom id for the enum; MUST be unique (only checked at runtime).
     */
    private MarketDataSupplier(int id)
    {
        _id = id;
    }

    /**
     * Initialise the id mapping map with all MarketDataSupplier.
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

        MarketDataSupplier[] values = MarketDataSupplier.values();

        for (int i = 0; i < values.length; i++)
        {
            MarketDataSupplier marketDataSupplier = values[i];

            int id = marketDataSupplier.getId();

            if (_idMappings.containsKey(id))
            {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + MarketDataSupplier.class.getSimpleName());
            }

            _idMappings.put(marketDataSupplier.getId(), marketDataSupplier);
        }
    }

    /**
     * Returns the MarketDataSupplier with the given ID.
     *
     * @param id Id to get matching MarketDataSupplier for.
     * @return MarketDataSupplier matching the id, or null of no such data type exist.
     */
    public static MarketDataSupplier fromId(int id)
    {
        initialiseIdMappings();

        return _idMappings.get(id);
    }

    /**
     * @return Id for the MarketDataSupplier
     */
    public int getId()
    {
        return _id;
    }
}
