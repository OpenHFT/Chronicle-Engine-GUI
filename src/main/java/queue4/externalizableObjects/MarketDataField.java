package queue4.externalizableObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cliveh on 10/05/2016.
 */
public enum MarketDataField {
    //Ids should never change and must be unique, but enum declaration order can change.

    SUPPLIER(1),
    SOURCE(2),
    ID(3),
    LAST_UPDATED(4),
    BID_PRICE(5),
    BID_SIZE(6),
    ASK_PRICE(7),
    ASK_SIZE(8),
    MID_PRICE(9),
    VALUE_DATE(10),
    RATE(11),
    PREVIOUS_CLOSE(12),
    CTD_COUPON(13),
    CTD_CONVERSION_FACTOR(14),
    CTD_ISIN(15),
    CTD_MATURITY_DATE(16),
    LAST_DELIVERY_DATE(17),
    CTD_FIRST_COUPON_DATE(18),
    CTD_ISSUE_DATE(19),
    CTD_INTEREST_ACCRUAL_DATE(20),
    DELAYED_STREAM(21),
    ADJUSTMENT(22),
    CONVEXITY(23),
    LAST_PRICE(24),
    TIMESTAMP_SEC(25),
    TIMESTAMP_NANO(26),
    COUPON(27),
    MATURITY_DATE(28),
    ISSUE_DATE(29),
    YIELD_TO_MATURITY(30),
    ADJUSTED_PREVIOUS_CLOSE(31),
    SWAP_TYPE(32),
    CLEARING_HOUSE(33),
    ENABLED(34),
    BID_LEVELS(35),
    ASK_LEVELS(36),
    BID_PRICE_1(37),
    BID_SIZE_1(38),
    ASK_PRICE_1(39),
    ASK_SIZE_1(40),
    BID_PRICE_2(41),
    BID_SIZE_2(42),
    ASK_PRICE_2(43),
    ASK_SIZE_2(44),
    BID_PRICE_3(45),
    BID_SIZE_3(46),
    ASK_PRICE_3(47),
    ASK_SIZE_3(48),
    BID_PRICE_4(49),
    BID_SIZE_4(50),
    ASK_PRICE_4(51),
    ASK_SIZE_4(52),
    BID_PRICE_5(53),
    BID_SIZE_5(54),
    ASK_PRICE_5(55),
    ASK_SIZE_5(56),
    BID_PRICE_6(57),
    BID_SIZE_6(58),
    ASK_PRICE_6(59),
    ASK_SIZE_6(60),
    BID_PRICE_7(61),
    BID_SIZE_7(62),
    ASK_PRICE_7(63),
    ASK_SIZE_7(64),
    BID_PRICE_8(65),
    BID_SIZE_8(66),
    ASK_PRICE_8(67),
    ASK_SIZE_8(68),
    BID_PRICE_9(69),
    BID_SIZE_9(70),
    ASK_PRICE_9(71),
    ASK_SIZE_9(72),
    BID_PRICE_10(73),
    BID_SIZE_10(74),
    ASK_PRICE_10(75),
    ASK_SIZE_10(76),
    SECTOR(77),
    CUSIP(78),
    COUPON_FREQUENCY(79),
    DATED_DATE(80),
    CURRENCY(81),
    SPREAD_TO_BENCHMARK(82),
    BENCHMARK(83),
    PREVIOUS_YIELD_TO_MATURITY(84),
    BID_SPREAD_TO_BENCHMARK(85),
    ASK_SPREAD_TO_BENCHMARK(86),
    BID_YIELD_TO_MATURITY(87),
    ASK_YIELD_TO_MATURITY(88),
    TRADEWEB_BID_PRICE(89),
    TRADEWEB_ASK_PRICE(90),
    TRADEWEB_BID_SIZE(91),
    TRADEWEB_ASK_SIZE(92),
    SWAP_BOX_BID_PRICE(93),
    SWAP_BOX_ASK_PRICE(94),
    SWAP_BOX_BID_SIZE(95),
    SWAP_BOX_ASK_SIZE(96),
    BLENDED_MID_PRICE(97),
    SHOULD_PUBLISH(98),
    MID_PRICE_IS_VALID(99),
    SETTLEMENT_DATE(100),
    BINARY_SERIALIZATION(101),
    SPREAD_MODE(102),
    SPREAD_PRICE_TYPE(103),
    TIMESTAMP(104),
    BID_PRICE_1_SOURCE(105),
    BID_PRICE_1_SUPPLIER(106),
    ASK_PRICE_1_SOURCE(107),
    ASK_PRICE_1_SUPPLIER(108),
    TOLL(109),
    SMOOTHED_MID_PRICE(110),
    FORWARD_MID_PRICE(111),
    SMOOTHED_FORWARD_MID_PRICE(112),
    COMMENT(113),
    IMPLIED_CONVEXITY(114),
    MARKET_DATA_STORE(115),
    PREV_CLOSE_VALUE_REALTIME(116),
    TENOR_CURVE(117),
    BPV01(118),
    TENOR(119),
    IS_VALID(120),
    CONTRIBUTOR(121),
    OFFCL_CODE(122),
    FUTURE_CONV_FACTOR(123),
    FUTURE_UNDERLYING_REF(124),
    FRONT_TWIST(125),
    BACK_TWIST(126),
    PIVOT_CONVEXITY(127),
    CONVEXITY_ADJUSTMENT(128),
    ISIN(129),
    ION_CODE(130),
    ION_ACTIVE(131),
    ION_TENOR(132),
    CTD_TICKER(133),
    FIRST_COUPON_DATE(134),
    PENULTIMATE_COUPON_DATE(135),
    INTEREST_ACCRUAL_DATE(136),
    LEG_0_BID_PRICE(137),
    LEG_0_ASK_PRICE(138),
    LEG_1_BID_PRICE(139),
    LEG_1_ASK_PRICE(140),
    LEG_2_BID_PRICE(141),
    LEG_2_ASK_PRICE(142),
    LEG_0_INSTRUMENT_CODE(143),
    LEG_1_INSTRUMENT_CODE(144),
    LEG_2_INSTRUMENT_CODE(145);



    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */

    private static Map<Integer, MarketDataField> _idMappings;


    /**
     * Custom id value
     * Custom ids should never change, but enum declaration order can change.
     */

    private final int _id;


    /**
     * Creates a new MarketDataField with the given id. Id must be unique.
     *
     * @param id Custom id for the enum - MUST be unique (only checked at runtime).
     * @throws Exception
     */
    MarketDataField(int id)
    {
        _id = id;
    }


    /**
     * Initialise the id mapping map with all MarketDataField.
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

        MarketDataField[] values = MarketDataField.values();

        for (int i = 0; i < values.length; i++)
        {
            MarketDataField marketDataField = values[i];
            int id = marketDataField.getId();

            if (_idMappings.containsKey(id))
            {
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
    public static MarketDataField fromId(int id)
    {
        initialiseIdMappings();

        return _idMappings.get(id);
    }


    /**
     * @return Id for the MarketDataField
     */
    public int getId()
    {
        return _id;
    }
}
