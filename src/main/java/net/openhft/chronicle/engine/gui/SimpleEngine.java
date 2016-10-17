package net.openhft.chronicle.engine.gui;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.engine.SimpleEngineMain;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.AbstractMarshallable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rob Austin.
 */
public class SimpleEngine {

    private static VanillaAssetTree ASSET_TREE = createEngine();

    public static VanillaAssetTree assetTree() {
        return ASSET_TREE;
    }


    static class MarketData extends AbstractMarshallable {
        double open;
        double high;
        double low;
        double close;
        double volume;
        double adjClose;

        public MarketData(double open, double high, double low, double close,
                          double volumnMillions,
                          double volumeThousands,
                          double volume, double adjClose) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume + (volumeThousands * 1000) + (volumnMillions * 1_000_000L);
            this.adjClose = adjClose;
        }
    }


    private static VanillaAssetTree createEngine() {
        VanillaAssetTree assetTree = null;
        try {
            assetTree = SimpleEngineMain.tree();
        } catch (IOException e) {
            throw Jvm.rethrow(e);
        }


        MapView<Integer, Double> intView = assetTree.acquireMap("/my/numbers", Integer.class, Double
                .class);

        for (int i = 0; i < 100; i++) {
            intView.put(i, Double.valueOf(i));
        }

        {
            MapView<Date, MarketData> map = assetTree.acquireMap("/shares/APPL", Date.class,
                    MarketData
                    .class);

            SimpleDateFormat sd = new SimpleDateFormat("dd MMM yyyy");

            try {
                map.put(sd.parse("7 Oct 2016"), new MarketData(114.31, 114.56, 113.51, 114.06, 24, 358, 400, 114.06));
                map.put(sd.parse("6 Oct 2016"), new MarketData(113.70, 114.34, 113.13, 113.89, 28, 779, 300, 113.89));
                map.put(sd.parse("5 Oct 2016"), new MarketData(113.40, 113.66, 112.69, 113.05, 21, 453, 100, 113.05));
                map.put(sd.parse("4 Oct 2016"), new MarketData(113.06, 114.31, 112.63, 113.00, 29, 736, 800, 113.00));
                map.put(sd.parse("3 Oct 2016"), new MarketData(112.71, 113.05, 112.28, 112.52, 21, 701, 800, 112.52));
                map.put(sd.parse("30 Sep 2016"), new MarketData(112.46, 113.37, 111.80, 113.05, 36, 379, 100, 113.05));
                map.put(sd.parse("29 Sep 2016"), new MarketData(113.16, 113.80, 111.80, 112.18, 35, 887, 000, 112.18));
                map.put(sd.parse("28 Sep 2016"), new MarketData(113.69, 114.64, 113.43, 113.95, 29, 641, 100, 113.95));
                map.put(sd.parse("27 Sep 2016"), new MarketData(113.00, 113.18, 112.34, 113.09, 24, 607, 400, 113.09));
                map.put(sd.parse("26 Sep 2016"), new MarketData(111.64, 113.39, 111.55, 112.88, 29, 869, 400, 112.88));
                map.put(sd.parse("23 Sep 2016"), new MarketData(114.42, 114.79, 111.55, 112.71, 52, 481, 200, 112.71));
                map.put(sd.parse("22 Sep 2016"), new MarketData(114.35, 114.94, 114.00, 114.62, 31, 74, 000, 114.62));
                map.put(sd.parse("21 Sep 2016"), new MarketData(113.85, 113.99, 112.44, 113.55, 36, 3, 200, 113.55));
                map.put(sd.parse("20 Sep 2016"), new MarketData(113.05, 114.12, 112.51, 113.57, 34, 514, 300, 113.57));
                map.put(sd.parse("19 Sep 2016"), new MarketData(115.19, 116.18, 113.25, 113.58, 47, 23, 000, 113.58));
                map.put(sd.parse("16 Sep 2016"), new MarketData(115.12, 116.13, 114.04, 114.92, 79, 886, 900, 114.92));
                map.put(sd.parse("15 Sep 2016"), new MarketData(113.86, 115.73, 113.49, 115.57, 89, 983, 600, 115.57));
                map.put(sd.parse("14 Sep 2016"), new MarketData(108.73, 113.03, 108.60, 111.77, 110, 888, 700, 111.77));
                map.put(sd.parse("13 Sep 2016"), new MarketData(107.51, 108.79, 107.24, 107.95, 62, 176, 200, 107.95));
                map.put(sd.parse("12 Sep 2016"), new MarketData(102.65, 105.72, 102.53, 105.44, 45, 292, 800, 105.44));
                map.put(sd.parse("9 Sep 2016"), new MarketData(104.64, 105.72, 103.13, 103.13, 46, 557, 000, 103.13));
                map.put(sd.parse("8 Sep 2016"), new MarketData(107.25, 107.27, 105.24, 105.52, 53, 2, 000, 105.52));
                map.put(sd.parse("7 Sep 2016"), new MarketData(107.83, 108.76, 107.07, 108.36, 42, 364, 300, 108.36));
                map.put(sd.parse("6 Sep 2016"), new MarketData(107.90, 108.30, 107.51, 107.70, 26, 880, 400, 107.70));
                map.put(sd.parse("2 Sep 2016"), new MarketData(107.70, 108.00, 106.82, 107.73, 26, 802, 500, 107.73));
                map.put(sd.parse("1 Sep 2016"), new MarketData(106.14, 106.80, 105.62, 106.73, 26, 701, 500, 106.73));
                map.put(sd.parse("31 Aug 2016"), new MarketData(105.66, 106.57, 105.64, 106.10, 29, 662, 400, 106.10));
                map.put(sd.parse("30 Aug 2016"), new MarketData(105.80, 106.50, 105.50, 106.00, 24, 863, 900, 106.00));
                map.put(sd.parse("29 Aug 2016"), new MarketData(106.62, 107.44, 106.29, 106.82, 24, 970, 300, 106.82));
                map.put(sd.parse("26 Aug 2016"), new MarketData(107.41, 107.95, 106.31, 106.94, 27, 766, 300, 106.94));
                map.put(sd.parse("25 Aug 2016"), new MarketData(107.39, 107.88, 106.68, 107.57, 25, 86, 200, 107.57));
                map.put(sd.parse("24 Aug 2016"), new MarketData(108.57, 108.75, 107.68, 108.03, 23, 675, 100, 108.03));
                map.put(sd.parse("23 Aug 2016"), new MarketData(108.59, 109.32, 108.53, 108.85, 21, 257, 700, 108.85));
                map.put(sd.parse("22 Aug 2016"), new MarketData(108.86, 109.10, 107.85, 108.51, 25, 820, 200, 108.51));
                map.put(sd.parse("19 Aug 2016"), new MarketData(108.77, 109.69, 108.36, 109.36, 25, 368, 100, 109.36));
                map.put(sd.parse("18 Aug 2016"), new MarketData(109.23, 109.60, 109.02, 109.08, 21, 984, 700, 109.08));
                map.put(sd.parse("17 Aug 2016"), new MarketData(109.10, 109.37, 108.34, 109.22, 25, 356, 000, 109.22));
                map.put(sd.parse("16 Aug 2016"), new MarketData(109.63, 110.23, 109.21, 109.38, 33, 794, 400, 109.38));
                map.put(sd.parse("15 Aug 2016"), new MarketData(108.14, 109.54, 108.08, 109.48, 25, 868, 200, 109.48));
                map.put(sd.parse("12 Aug 2016"), new MarketData(107.78, 108.44, 107.78, 108.18, 18, 660, 400, 108.18));
                map.put(sd.parse("11 Aug 2016"), new MarketData(108.52, 108.93, 107.85, 107.93, 27, 484, 500, 107.93));
                map.put(sd.parse("10 Aug 2016"), new MarketData(108.71, 108.90, 107.76, 108.00, 24, 8, 500, 108.00));
                map.put(sd.parse("9 Aug 2016"), new MarketData(108.23, 108.94, 108.01, 108.81, 26, 315, 200, 108.81));
                map.put(sd.parse("8 Aug 2016"), new MarketData(107.52, 108.37, 107.16, 108.37, 28, 37, 200, 108.37));
                map.put(sd.parse("5 Aug 2016"), new MarketData(106.27, 107.65, 106.18, 107.48, 40, 553, 400, 107.48));
                map.put(sd.parse("4 Aug 2016"), new MarketData(105.58, 106.00, 105.28, 105.87, 27, 408, 700, 105.87));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        {
            MapView<String, String> map = assetTree.acquireMap("/my/demo", String.class, String.class);
            MapView<String, String> mapView = assetTree.acquireMap("/my/demo", String.class, String.class);
            mapView.put("AED", "United Arab Emirates dirham");
            mapView.put("AFN", "Afghan afghani");
            mapView.put("ALL", "Albanian lek");
            mapView.put("AMD", "Armenian dram");
            mapView.put("ANG", "Netherlands Antillean guilder");
            mapView.put("AOA", "Angolan kwanza");
            mapView.put("ARS", "Argentine peso");
            mapView.put("AUD", "Australian dollar");
            mapView.put("AWG", "Aruban florin");
            mapView.put("AZN", "Azerbaijani manat");
            mapView.put("BAM", "Bosnia and Herzegovina convertible mark");
            mapView.put("BBD", "Barbados dollar");
            mapView.put("BDT", "Bangladeshi taka");
            mapView.put("BGN", "Bulgarian lev");
            mapView.put("BHD", "Bahraini dinar");
            mapView.put("BIF", "Burundian franc");
            mapView.put("BMD", "Bermudian dollar");
            mapView.put("BND", "Brunei dollar");
            mapView.put("BOB", "Boliviano");
            mapView.put("BOV", "Bolivian Mvdol (funds code)");
            mapView.put("BRL", "Brazilian real");
            mapView.put("BSD", "Bahamian dollar");
            mapView.put("BTN", "Bhutanese ngultrum");
            mapView.put("BWP", "Botswana pula");
            mapView.put("BYN", "Belarusian ruble");
            mapView.put("BYR", "Belarusian ruble");
            mapView.put("BZD", "Belize dollar");
            mapView.put("CAD", "Canadian dollar");
            mapView.put("CDF", "Congolese franc");
            mapView.put("CHE", "WIR Euro (complementary currency)");
            mapView.put("CHF", "Swiss franc");
            mapView.put("CHW", "WIR Franc (complementary currency)");
            mapView.put("CLF", "Unidad de Fomento (funds code)");
            mapView.put("CLP", "Chilean peso");
            mapView.put("CNY", "Chinese yuan");
            mapView.put("COP", "Colombian peso");
            mapView.put("COU", "Unidad de Valor Real (UVR) (funds code)[7]");
            mapView.put("CRC", "Costa Rican colon");
            mapView.put("CUC", "Cuban convertible peso");
            mapView.put("CUP", "Cuban peso");
            mapView.put("CVE", "Cape Verde escudo");
            mapView.put("CZK", "Czech koruna");
            mapView.put("DJF", "Djiboutian franc");
            mapView.put("DKK", "Danish krone");
            mapView.put("DOP", "Dominican peso");
            mapView.put("DZD", "Algerian dinar");
            mapView.put("EGP", "Egyptian pound");
            mapView.put("ERN", "Eritrean nakfa");
            mapView.put("ETB", "Ethiopian birr");
            mapView.put("EUR", "Euro");
            mapView.put("FJD", "Fiji dollar");
            mapView.put("FKP", "Falkland Islands pound");
            mapView.put("GBP", "Pound sterling");
            mapView.put("GEL", "Georgian lari");
            mapView.put("GHS", "Ghanaian cedi");
            mapView.put("GIP", "Gibraltar pound");
            mapView.put("GMD", "Gambian dalasi");
            mapView.put("GNF", "Guinean franc");
            mapView.put("GTQ", "Guatemalan quetzal");
            mapView.put("GYD", "Guyanese dollar");
            mapView.put("HKD", "Hong Kong dollar");
            mapView.put("HNL", "Honduran lempira");
            mapView.put("HRK", "Croatian kuna");
            mapView.put("HTG", "Haitian gourde");
            mapView.put("HUF", "Hungarian forint");
            mapView.put("IDR", "Indonesian rupiah");
            mapView.put("ILS", "Israeli new shekel");
            mapView.put("INR", "Indian rupee");
            mapView.put("IQD", "Iraqi dinar");
            mapView.put("IRR", "Iranian rial");
            mapView.put("ISK", "Icelandic króna");
            mapView.put("JMD", "Jamaican dollar");
            mapView.put("JOD", "Jordanian dinar");
            mapView.put("JPY", "Japanese yen");
            mapView.put("KES", "Kenyan shilling");
            mapView.put("KGS", "Kyrgyzstani som");
            mapView.put("KHR", "Cambodian riel");
            mapView.put("KMF", "Comoro franc");
            mapView.put("KPW", "North Korean won");
            mapView.put("KRW", "South Korean won");
            mapView.put("KWD", "Kuwaiti dinar");
            mapView.put("KYD", "Cayman Islands dollar");
            mapView.put("KZT", "Kazakhstani tenge");
            mapView.put("LAK", "Lao kip");
            mapView.put("LBP", "Lebanese pound");
            mapView.put("LKR", "Sri Lankan rupee");
            mapView.put("LRD", "Liberian dollar");
            mapView.put("LSL", "Lesotho loti");
            mapView.put("LYD", "Libyan dinar");
            mapView.put("MAD", "Moroccan dirham");
            mapView.put("MDL", "Moldovan leu");
            mapView.put("MGA", "Malagasy ariary");
            mapView.put("MKD", "Macedonian denar");
            mapView.put("MMK", "Myanmar kyat");
            mapView.put("MNT", "Mongolian tögrög");
            mapView.put("MOP", "Macanese pataca");
            mapView.put("MRO", "Mauritanian ouguiya");
            mapView.put("MUR", "Mauritian rupee");
            mapView.put("MVR", "Maldivian rufiyaa");
            mapView.put("MWK", "Malawian kwacha");
            mapView.put("MXN", "Mexican peso");
            mapView.put("MXV", "Mexican Unidad de Inversion (UDI) (funds code)");
            mapView.put("MYR", "Malaysian ringgit");
            mapView.put("MZN", "Mozambican metical");
            mapView.put("NAD", "Namibian dollar");
            mapView.put("NGN", "Nigerian naira");
            mapView.put("NIO", "Nicaraguan córdoba");
            mapView.put("NOK", "Norwegian krone");
            mapView.put("NPR", "Nepalese rupee");
            mapView.put("NZD", "New Zealand dollar");
            mapView.put("OMR", "Omani rial");
            mapView.put("PAB", "Panamanian balboa");
            mapView.put("PEN", "Peruvian Sol");
            mapView.put("PGK", "Papua New Guinean kina");
            mapView.put("PHP", "Philippine peso");
            mapView.put("PKR", "Pakistani rupee");
            mapView.put("PLN", "Polish złoty");
            mapView.put("PYG", "Paraguayan guaraní");
            mapView.put("QAR", "Qatari riyal");
            mapView.put("RON", "Romanian leu");
            mapView.put("RSD", "Serbian dinar");
            mapView.put("RUB", "Russian ruble");
            mapView.put("RWF", "Rwandan franc");
            mapView.put("SAR", "Saudi riyal");
            mapView.put("SBD", "Solomon Islands dollar");
            mapView.put("SCR", "Seychelles rupee");
            mapView.put("SDG", "Sudanese pound");
            mapView.put("SEK", "Swedish krona/kronor");
            mapView.put("SGD", "Singapore dollar");
            mapView.put("SHP", "Saint Helena pound");
            mapView.put("SLL", "Sierra Leonean leone");
            mapView.put("SOS", "Somali shilling");
            mapView.put("SRD", "Surinamese dollar");
            mapView.put("SSP", "South Sudanese pound");
            mapView.put("STD", "São Tomé and Príncipe dobra");
            mapView.put("SVC", "Salvadoran colón");
            mapView.put("SYP", "Syrian pound");
            mapView.put("SZL", "Swazi lilangeni");
            mapView.put("THB", "Thai baht");
            mapView.put("TJS", "Tajikistani somoni");
            mapView.put("TMT", "Turkmenistani manat");
            mapView.put("TND", "Tunisian dinar");
            mapView.put("TOP", "Tongan paʻanga");
            mapView.put("TRY", "Turkish lira");
            mapView.put("TTD", "Trinidad and Tobago dollar");
            mapView.put("TWD", "New Taiwan dollar");
            mapView.put("TZS", "Tanzanian shilling");
            mapView.put("UAH", "Ukrainian hryvnia");
            mapView.put("UGX", "Ugandan shilling");
            mapView.put("USD", "United States dollar");
            mapView.put("UYU", "Uruguayan peso");
            mapView.put("UZS", "Uzbekistan som");
            mapView.put("VEF", "Venezuelan bolívar");
            mapView.put("VND", "Vietnamese dong");
            mapView.put("VUV", "Vanuatu vatu");
            mapView.put("WST", "Samoan tala");
            mapView.put("XAF", "CFA franc BEAC");
            mapView.put("XCD", "East Caribbean dollar");
            mapView.put("XDR", "Special drawing rights");
            mapView.put("XOF", "CFA franc BCEAO");
            mapView.put("XPD", "Palladium (one troy ounce)");
            mapView.put("XPF", "CFP franc (franc Pacifique)");
            mapView.put("XPT", "Platinum (one troy ounce)");
            mapView.put("XSU", "SUCRE");
            mapView.put("XTS", "Code reserved for testing purposes");
            mapView.put("XUA", "ADB Unit of Account");
            mapView.put("XXX", "No currency");
            mapView.put("YER", "Yemeni rial");
            mapView.put("ZAR", "South African rand");
            mapView.put("ZMW", "Zambian kwacha");
            mapView.put("ZWL", "Zimbabwean dollar A/10");
            mapView.put("Code", "Currency");
            mapView.put("AED", "United Arab Emirates dirham");
            mapView.put("AFN", "Afghan afghani");
            mapView.put("ALL", "Albanian lek");
            mapView.put("AMD", "Armenian dram");
            mapView.put("ANG", "Netherlands Antillean guilder");
            mapView.put("AOA", "Angolan kwanza");
            mapView.put("ARS", "Argentine peso");
            mapView.put("AUD", "Australian dollar");
            mapView.put("AWG", "Aruban florin");
            mapView.put("AZN", "Azerbaijani manat");
            mapView.put("BAM", "Bosnia and Herzegovina convertible mark");
            mapView.put("BBD", "Barbados dollar");
            mapView.put("BDT", "Bangladeshi taka");
            mapView.put("BGN", "Bulgarian lev");
            mapView.put("BHD", "Bahraini dinar");
            mapView.put("BIF", "Burundian franc");
            mapView.put("BMD", "Bermudian dollar");
            mapView.put("BND", "Brunei dollar");
            mapView.put("BOB", "Boliviano");
            mapView.put("BOV", "Bolivian Mvdol (funds code)");
            mapView.put("BRL", "Brazilian real");
            mapView.put("BSD", "Bahamian dollar");
            mapView.put("BTN", "Bhutanese ngultrum");
            mapView.put("BWP", "Botswana pula");
            mapView.put("BYN", "Belarusian ruble");
            mapView.put("BYR", "Belarusian ruble");
            mapView.put("BZD", "Belize dollar");
            mapView.put("CAD", "Canadian dollar");
            mapView.put("CDF", "Congolese franc");
            mapView.put("CHF", "Swiss franc");
            mapView.put("CLP", "Chilean peso");
            mapView.put("CNY", "Chinese yuan");
            mapView.put("COP", "Colombian peso");
            mapView.put("CRC", "Costa Rican colon");
            mapView.put("CUC", "Cuban convertible peso");
            mapView.put("CUP", "Cuban peso");
            mapView.put("CVE", "Cape Verde escudo");
            mapView.put("CZK", "Czech koruna");
            mapView.put("DJF", "Djiboutian franc");
            mapView.put("DKK", "Danish krone");
            mapView.put("DOP", "Dominican peso");
            mapView.put("DZD", "Algerian dinar");
            mapView.put("EGP", "Egyptian pound");
            mapView.put("ERN", "Eritrean nakfa");
            mapView.put("ETB", "Ethiopian birr");
            mapView.put("EUR", "Euro");
            mapView.put("FJD", "Fiji dollar");
            mapView.put("FKP", "Falkland Islands pound");
            mapView.put("GBP", "Pound sterling");
            mapView.put("GEL", "Georgian lari");
            mapView.put("GHS", "Ghanaian cedi");
            mapView.put("GIP", "Gibraltar pound");
            mapView.put("GMD", "Gambian dalasi");
            mapView.put("GNF", "Guinean franc");
            mapView.put("GTQ", "Guatemalan quetzal");
            mapView.put("GYD", "Guyanese dollar");
            mapView.put("HKD", "Hong Kong dollar");
            mapView.put("HNL", "Honduran lempira");
            mapView.put("HRK", "Croatian kuna");
            mapView.put("HTG", "Haitian gourde");
            mapView.put("HUF", "Hungarian forint");
            mapView.put("IDR", "Indonesian rupiah");
            mapView.put("ILS", "Israeli new shekel");
            mapView.put("INR", "Indian rupee");
            mapView.put("IQD", "Iraqi dinar");
            mapView.put("IRR", "Iranian rial");
            mapView.put("ISK", "Icelandic króna");
            mapView.put("JMD", "Jamaican dollar");
            mapView.put("JOD", "Jordanian dinar");
            mapView.put("JPY", "Japanese yen");
            mapView.put("KES", "Kenyan shilling");
            mapView.put("KGS", "Kyrgyzstani som");
            mapView.put("KHR", "Cambodian riel");
            mapView.put("KMF", "Comoro franc");
            mapView.put("KPW", "North Korean won");
            mapView.put("KRW", "South Korean won");
            mapView.put("KWD", "Kuwaiti dinar");
            mapView.put("KYD", "Cayman Islands dollar");
            mapView.put("KZT", "Kazakhstani tenge");
            mapView.put("LAK", "Lao kip");
            mapView.put("LBP", "Lebanese pound");
            mapView.put("LKR", "Sri Lankan rupee");
            mapView.put("LRD", "Liberian dollar");
            mapView.put("LSL", "Lesotho loti");
            mapView.put("LYD", "Libyan dinar");
            mapView.put("MAD", "Moroccan dirham");
            mapView.put("MDL", "Moldovan leu");
            mapView.put("MGA", "Malagasy ariary");
            mapView.put("MKD", "Macedonian denar");
            mapView.put("MMK", "Myanmar kyat");
            mapView.put("MNT", "Mongolian tögrög");
            mapView.put("MOP", "Macanese pataca");
            mapView.put("MRO", "Mauritanian ouguiya");
            mapView.put("MUR", "Mauritian rupee");
            mapView.put("MVR", "Maldivian rufiyaa");
            mapView.put("MWK", "Malawian kwacha");
            mapView.put("MXN", "Mexican peso");
            mapView.put("MXV", "Mexican Unidad de Inversion (UDI) (funds code)");
            mapView.put("MYR", "Malaysian ringgit");
            mapView.put("MZN", "Mozambican metical");
            mapView.put("NAD", "Namibian dollar");
            mapView.put("NGN", "Nigerian naira");
            mapView.put("NIO", "Nicaraguan córdoba");
            mapView.put("NOK", "Norwegian krone");
            mapView.put("NPR", "Nepalese rupee");
            mapView.put("NZD", "New Zealand dollar");
            mapView.put("OMR", "Omani rial");
            mapView.put("PAB", "Panamanian balboa");
            mapView.put("PEN", "Peruvian Sol");
            mapView.put("PGK", "Papua New Guinean kina");
            mapView.put("PHP", "Philippine peso");
            mapView.put("PKR", "Pakistani rupee");
            mapView.put("PLN", "Polish złoty");
            mapView.put("PYG", "Paraguayan guaraní");
            mapView.put("QAR", "Qatari riyal");
            mapView.put("RON", "Romanian leu");
            mapView.put("RSD", "Serbian dinar");
            mapView.put("RUB", "Russian ruble");
            mapView.put("RWF", "Rwandan franc");
            mapView.put("SAR", "Saudi riyal");
            mapView.put("SBD", "Solomon Islands dollar");
            mapView.put("SCR", "Seychelles rupee");
            mapView.put("SDG", "Sudanese pound");
            mapView.put("SEK", "Swedish krona/kronor");
            mapView.put("SGD", "Singapore dollar");
            mapView.put("SHP", "Saint Helena pound");
            mapView.put("SLL", "Sierra Leonean leone");
            mapView.put("SOS", "Somali shilling");
            mapView.put("SRD", "Surinamese dollar");
            mapView.put("SSP", "South Sudanese pound");
            mapView.put("STD", "São Tomé and Príncipe dobra");
            mapView.put("SVC", "Salvadoran colón");
            mapView.put("SYP", "Syrian pound");
            mapView.put("SZL", "Swazi lilangeni");
            mapView.put("THB", "Thai baht");
            mapView.put("TJS", "Tajikistani somoni");
            mapView.put("TMT", "Turkmenistani manat");
            mapView.put("TND", "Tunisian dinar");
            mapView.put("TOP", "Tongan paʻanga");
            mapView.put("TRY", "Turkish lira");
            mapView.put("TTD", "Trinidad and Tobago dollar");
            mapView.put("TWD", "New Taiwan dollar");
            mapView.put("TZS", "Tanzanian shilling");
            mapView.put("UAH", "Ukrainian hryvnia");
            mapView.put("UGX", "Ugandan shilling");
            mapView.put("USD", "United States dollar");
            mapView.put("UYI", "Uruguay Peso en Unidades Indexadas (URUIURUI) (funds code)");
            mapView.put("UYU", "Uruguayan peso");
            mapView.put("UZS", "Uzbekistan som");
            mapView.put("VEF", "Venezuelan bolívar");
            mapView.put("VND", "Vietnamese dong");
            mapView.put("VUV", "Vanuatu vatu");
            mapView.put("WST", "Samoan tala");
            mapView.put("XAF", "CFA franc BEAC");
            mapView.put("XAG", "Silver (one troy ounce)");

        }
        return assetTree;
    }

}

