package queue4.externalizableObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cliveh on 10/05/2016.
 */
public enum MarketDataType {
    // Ids should never change and must be unique, but enum declaration order can change.

    DEPOSIT_RATE(1),
    EURODOLLAR_FUTURES(2),
    FRA(3),
    BOND(4),
    SWAP_RATE(5),
    SPREAD_OVER(6),
    OIS_RATE(7),
    BASIS_SWAP_RATE(8),
    ADJUSTMENT(9),
    CONFIGURATION(10),
    SWAP_BUTTERFLY(11),
    SWAP_CURVE_SPREAD(12),
    SWAP_VALUATION_ENVIRONMENT(13);

    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup; lazily loaded.
     */
    private static Map<Integer, MarketDataType> _idMappings;

    /**
     * Custom id value (byte because we are unlikely to exceed 127 enum types).
     * Custom ids should never change, but enum declaration order can change.
     */
    private final int _id;

    /**
     * Creates a new MarketDataSupplier with the given id.
     *
     * @param id Custom id for the enum; this MUST be unique (only checked at runtime).
     */
    private MarketDataType(int id) {
        _id = id;
    }

    /**
     * Initialise the id mapping map with all MarketDataSupplier.
     * Returns immediately if the map has already been initialised.
     * <p/>
     * Throws IllegalArgumentException if two types attempt to use the same id.
     */
    private static synchronized void initialiseIdMappings() {
        if (_idMappings != null) {
            return;
        }

        _idMappings = new HashMap<>();

        MarketDataType[] values = MarketDataType.values();

        for (int i = 0; i < values.length; i++) {
            MarketDataType marketDataType = values[i];

            int id = marketDataType.getId();

            if (_idMappings.containsKey(id)) {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + MarketDataSupplier.class.getSimpleName());
            }

            _idMappings.put(marketDataType.getId(), marketDataType);
        }
    }

    /**
     * Returns the MarketDataSupplier with the given ID.
     *
     * @param id Id to get matching MarketDataSupplier for.
     * @return MarketDataSupplier matching the id, or null of no such data type exist.
     */
    public static MarketDataType fromId(int id) {
        initialiseIdMappings();

        return _idMappings.get(id);
    }

    /**
     * @return The ID.
     */
    public int getId() {
        return _id;
    }
}
