package queue4.atTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import queue4.PriceConsumerMarketDataUpdateCallback;
import queue4.externalizableObjects.MarketDataField;
import queue4.externalizableObjects.MarketDataType;


/**
 * Market data update callback for At Time functionality.
 * <p>
 * Only interested in long and specifically if it is TIMESTAMP_SEC and Byte array if it is VALUATION_ENVIRONMENT.
 */
public class AtTimeMarketDataUpdateCallback extends PriceConsumerMarketDataUpdateCallback {
    private final static Logger LOGGER = LogManager.getLogger(AtTimeMarketDataUpdateCallback.class);
    private long _atMillis;
    private byte[] _valuationEnvironment;
    private ValuationEnvironmentSaver _valuationEnvironmentSaver;
    // Set to true once one entry processed
    private boolean _isProcessed = false;


    /**
     * @param atMillis The number of seconds since the EPOCH that marks the period of time of interest.
     */
    public AtTimeMarketDataUpdateCallback(ValuationEnvironmentSaver valuationEnvironmentSaver, long atMillis) {
        _atMillis = atMillis;
        _valuationEnvironmentSaver = valuationEnvironmentSaver;
    }



    @Override
    public void onBooleanUpdate(MarketDataField marketDataField, boolean value) {
    }



    @Override
    public void onCharacterUpdate(MarketDataField marketDataField, char value) {
    }




    @Override
    public void onIntegerUpdate(MarketDataField marketDataField, int value) {
    }



    @Override
    public void onLongUpdate(MarketDataField marketDataField, long value) {
        // The logic here waits until atMillis time is reached, and then saves the last value of valuation environment encountered.

        if (!_isProcessed && marketDataField.TIMESTAMP_SEC == marketDataField) {
            // Value in secs, convert to millis

            long timeInMillis = value * 1000L;

            if (timeInMillis <= _atMillis) {
                _isProcessed = true;
                if (_valuationEnvironment != null) {
                    boolean fileSaved = _valuationEnvironmentSaver.saveValuationEnvironment(_valuationEnvironment);
                    if (fileSaved) {
                        LOGGER.info("Valuation environment saved successfully.");
                    } else {
                        LOGGER.error("Valuation environment could not be saved.");
                    }
                } else {
                    LOGGER.info("Valuation environment not found until {}", _atMillis);
                }
            }
        }
    }



    @Override
    public void onFloatUpdate(MarketDataField marketDataField, float value) {
    }



    @Override
    public void onDoubleUpdate(MarketDataField marketDataField, double value) {
    }



    @Override
    public void onStringUpdate(MarketDataField marketDataField, String value) {
    }


    @Override
    public void onObjectUpdate(MarketDataField marketDataField, Object value) {
    }



    @Override
    public void onByteArrayUpdate(MarketDataField marketDataField, byte[] value) {
        //TODO: Remove the storage after isProcessed is true;
        if (MarketDataField.BINARY_SERIALIZATION.equals(marketDataField) && MarketDataType.VALUATION_ENVIRONMENT.equals(_marketDataType)) {
            _valuationEnvironment = value;
        }
    }
}
