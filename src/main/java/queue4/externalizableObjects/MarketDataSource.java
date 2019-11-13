package queue4.externalizableObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cliveh on 10/05/2016.
 */
public enum MarketDataSource
{
    // Ids should never change and must be unique, but enum declaration order can change.

    CME(1),
    CBOT(2),
    ICAP_19901(3),
    IBA(4),
    TULLETT_PREBON_SMKR(5),
    ESPEED(6),
    BROKERTEC(7),
    BLOOMBERG(8),
    FEDERAL_RESERVE_BANK_NY(9),
    REUTERS(10),
    PCA_MODEL(11),
    SWAP_RATE_MODEL(12),
    BUTTERFLY_MODEL(13),
    TREASURY_SPREAD_MODEL(14),
    TRADER(15),
    MARKET_DATA_MODEL(16),
    FUTURES_MODEL(17),
    MARKET_IMPLIED_MODEL(18),
    MARKET_RATES_MODEL(19),
    GOLDMAN_SACHS(20),
    SOCIETE_GENERAL(21),
    WELLS_FARGO(22),
    BARCLAYS_BANK(23),
    RBS(24),
    CREDIT_SUISSE(25),
    CREDIT_AGRICOLE(26),
    TD_SECURITIES(27),
    CITADEL(28),
    BMO_CAPITAL_MARKETS(29),
    MARKET_STANDARD_SWAP_CURVE_MODEL(30),
    TRADEWEB(31),
    MANUAL(32),
    BROKER_SWAP_RATE_MODEL(33),
    BROADWAY(34),
    TRADX(35),
    SWAP_BOX(36),
    CME_MODEL(37),
    ADEPT(38),
    TRADEWEB_IMPLIED_YIELD_SPREAD_MODEL(39),
    SWAP_BOX_SPREAD_MODEL(40),
    TRADEWEB_SINGLE_YIELD_SPREAD_MODEL(41),
    BLOOMBERG_CBBT(42),
    MORNING_SETUP(43),
    LIVE_MORNING_SETUP(44),
    TRADITION(45),
    ICAP_GCMN(46),
    ICAP_ICAP(47),
    EMMI(48), // European Money Markets Institute
    BLOOMBERG_CMPN(49),
    BLOOMBERG_TPRA(50),
    BLOOMBERG_TRPU(51),
    WMBA(52),
    DRIVER_SWAP_RATE_MODEL(53),
    TULLETT_PREBON_TTKL(54),
    ICE_FUTURES_EUROPE(55),
    TENOR_CURVE(56),
    ECB(57),
    LIBOR_SWAP_RATE_MODEL(58),
    ION(59),
    FORWARD_SPREAD_MODEL(60),
    EUREX(61),
    LIBOR_OIS_FULL_FUT_MODEL(62),
    LIBOR_OIS_SHORT_FUT_MODEL(63),
    LIBOR_LIBOR_FULL_FUT_MODEL(64),
    LIBOR_LIBOR_SHORT_FUT_MODEL(65),
    OUTPUT_VALUATION_ENVIRONMENT(66),
    FORT(67);


    /**
     * Cache mapping the custom ids with the Eum types to ensure fast reverse lookup.
     * Lazily loaded.
     */
    private static Map<Integer, MarketDataSource> _idMappings;


    /**
     * Custom id value.
     * Custom ids should never change, but enum declaration order can change.
     */
    private final int _id;


    /**
     * Creates a new MarketDataSource with the given id. Id must be unique.
     *
     * @param id Custom id for the enum - MUST be unique (only checked at runtime).
     * @throws Exception
     */
    MarketDataSource(int id)
    {
        _id = id;
    }


    /**
     * Initialise the id mapping map with all MarketDataSource; returns immediately if the map has already been initialised.
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

        MarketDataSource[] values = MarketDataSource.values();

        for (int i = 0; i < values.length; i++)
        {
            MarketDataSource marketDataSource = values[i];

            int id = marketDataSource.getId();

            if (_idMappings.containsKey(id))
            {
                throw new IllegalArgumentException("The specified id '" + id + "' cannot be used for multiple enums of type: " + MarketDataSource.class.getSimpleName());
            }

            _idMappings.put(marketDataSource.getId(), marketDataSource);
        }
    }


    /**
     * Returns the MarketDataSource with the given id.
     * If no such MarketDataSource exist null is returned.
     *
     * @param id Id to get matching MarketDataSource for.
     * @return MarketDataSource matching the id, or null of no such data type exist.
     */
    public static MarketDataSource fromId(int id)
    {
        initialiseIdMappings();

        return _idMappings.get(id);
    }


    /**
     * @return Id for the MarketDataSource
     */
    public int getId()
    {
        return _id;
    }
}