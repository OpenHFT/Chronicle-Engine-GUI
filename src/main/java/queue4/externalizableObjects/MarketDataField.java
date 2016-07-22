package queue4.externalizableObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cliveh on 10/05/2016.
 */
public enum MarketDataField {
    //Ids should never change and must be unique, but enum declaration order can change.

    SUPPLIER((byte) 1),
    SOURCE((byte) 2),
    ID((byte) 3),
    LAST_UPDATED((byte) 4),
    BID_PRICE((byte) 5),
    BID_SIZE((byte) 6),
    ASK_PRICE((byte) 7),
    ASK_SIZE((byte) 8),
    MID_PRICE((byte) 9),
    VALUE_DATE((byte) 10),
    RATE((byte) 11),
    PREVIOUS_CLOSE((byte) 12),
    CTD_COUPON((byte) 13),
    CTD_CONVERSION_FACTOR((byte) 14),
    CTD_ISIN((byte) 15),
    CTD_MATURITY_DATE((byte) 16),
    LAST_DELIVERY_DATE((byte) 17),
    CTD_FIRST_COUPON_DATE((byte) 18),
    CTD_ISSUE_DATE((byte) 19),
    CTD_INTEREST_ACCRUAL_DATE((byte) 20),
    DELAYED_STREAM((byte) 21),
    ADJUSTMENT((byte) 22),
    CONVEXITY((byte) 23),
    LAST_PRICE((byte) 24),
    RECEIVED_AT_SEC((byte) 25),
    RECEIVED_AT_NANO((byte) 26),
    COUPON((byte) 27),
    MATURITY_DATE((byte) 28),
    ISSUE_DATE((byte) 29),
    YIELD_TO_MATURITY((byte) 30),
    ADJUSTED_PREVIOUS_CLOSE((byte) 31),
    SWAP_TYPE((byte) 32),
    CLEARING_HOUSE((byte) 33),
    ENABLED((byte) 34),
    BID_LEVELS((byte) 35),
    ASK_LEVELS((byte) 36),
    BID_PRICE_1((byte) 37),
    BID_SIZE_1((byte) 38),
    ASK_PRICE_1((byte) 39),
    ASK_SIZE_1((byte) 40),
    BID_PRICE_2((byte) 41),
    BID_SIZE_2((byte) 42),
    ASK_PRICE_2((byte) 43),
    ASK_SIZE_2((byte) 44),
    BID_PRICE_3((byte) 45),
    BID_SIZE_3((byte) 46),
    ASK_PRICE_3((byte) 47),
    ASK_SIZE_3((byte) 48),
    BID_PRICE_4((byte) 49),
    BID_SIZE_4((byte) 50),
    ASK_PRICE_4((byte) 51),
    ASK_SIZE_4((byte) 52),
    BID_PRICE_5((byte) 53),
    BID_SIZE_5((byte) 54),
    ASK_PRICE_5((byte) 55),
    ASK_SIZE_5((byte) 56),
    BID_PRICE_6((byte) 57),
    BID_SIZE_6((byte) 58),
    ASK_PRICE_6((byte) 59),
    ASK_SIZE_6((byte) 60),
    BID_PRICE_7((byte) 61),
    BID_SIZE_7((byte) 62),
    ASK_PRICE_7((byte) 63),
    ASK_SIZE_7((byte) 64),
    BID_PRICE_8((byte) 65),
    BID_SIZE_8((byte) 66),
    ASK_PRICE_8((byte) 67),
    ASK_SIZE_8((byte) 68),
    BID_PRICE_9((byte) 69),
    BID_SIZE_9((byte) 70),
    ASK_PRICE_9((byte) 71),
    ASK_SIZE_9((byte) 72),
    BID_PRICE_10((byte) 73),
    BID_SIZE_10((byte) 74),
    ASK_PRICE_10((byte) 75),
    ASK_SIZE_10((byte) 76),
    SECTOR((byte) 77),
    CUSIP((byte) 78),
    COUPON_FREQUENCY((byte) 79),
    DATED_DATE((byte) 80),
    CURRENCY((byte) 81),
    SPREAD_TO_BENCHMARK((byte) 82),
    BENCHMARK((byte) 83),
    PREVIOUS_YIELD_TO_MATURITY((byte) 84),
    BID_SPREAD_TO_BENCHMARK((byte) 85),
    ASK_SPREAD_TO_BENCHMARK((byte) 86),
    BID_YIELD_TO_MATURITY((byte) 87),
    ASK_YIELD_TO_MATURITY((byte) 88),
    TRADEWEB_BID_PRICE((byte) 89),
    TRADEWEB_ASK_PRICE((byte) 90),
    TRADEWEB_BID_SIZE((byte) 91),
    TRADEWEB_ASK_SIZE((byte) 92),
    SWAP_BOX_BID_PRICE((byte) 93),
    SWAP_BOX_ASK_PRICE((byte) 94),
    SWAP_BOX_BID_SIZE((byte) 95),
    SWAP_BOX_ASK_SIZE((byte) 96),
    BLENDED_MID_PRICE((byte) 97),
    SHOULD_PUBLISH((byte) 98),
    MID_PRICE_IS_VALID((byte) 99),
    SETTLEMENT_DATE((byte) 100),
    BINARY_SERIALIZATION((byte) 101);

    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */

    private static Map<Byte, MarketDataField> _idMappings;

    /**
     * Custom id value
     * Custom ids should never change, but enum declaration order can change.
     */

    private final byte _id;

    /**
     * Creates a new MarketDataField with the given id. Id must be unique.
     *
     * @param id Custom id for the enum - MUST be unique (only checked at runtime).
     * @throws Exception
     */
    MarketDataField(byte id) {
        _id = id;
    }

    /**
     * Initialise the id mapping map with all MarketDataField.
     * Returns immediately if the map has already been initialised.
     * <p/>
     * Throws IllegalArgumentException if two types attempt to use the same id.
     */
    private static synchronized void initialiseIdMappings() {
        if (_idMappings != null) {
            return;
        }

        _idMappings = new HashMap<>();

        MarketDataField[] values = MarketDataField.values();

        for (int i = 0; i < values.length; i++) {
            MarketDataField marketDataField = values[i];
            byte id = marketDataField.getId();

            if (_idMappings.containsKey(id)) {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + MarketDataField.class.getSimpleName());
            }

            _idMappings.put(marketDataField.getId(), marketDataField);
        }
    }

    /**
     * Returns the MarketDataField with the given id.
     * If no such MarketDataField exist null is returned.
     *
     * @param id Id to get matching MarketDataField for.
     * @return MarketDataField matching the id, or null of no such data type exist.
     */
    public static MarketDataField fromId(byte id) {
        initialiseIdMappings();

        return _idMappings.get(id);
    }

    /**
     * @return Id for the MarketDataField
     */
    public byte getId() {
        return _id;
    }
}
