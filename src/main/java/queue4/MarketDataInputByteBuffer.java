package queue4;

import queue4.externalizableObjects.FieldDataType;
import queue4.externalizableObjects.MarketDataField;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility methods for dealing with byte buffer containing market data updates.
 */
public class MarketDataInputByteBuffer
{
    private static final String UTF8 = "UTF-8";


    /**
     * Parses the bytes passed in a market data update and calls a specified callback to deal with them.
     *
     * @param byteArray The byte array containing the fields updated.
     * @param callback  The callback to inform.
     */
    public static void parseMarketDataUpdatesByteArray(byte[] byteArray, MarketDataUpdateCallback callback) throws Exception
    {
        ByteBuffer marketDataValuesByteBuffer = ByteBuffer.wrap(byteArray);

        while (marketDataValuesByteBuffer.remaining() != 0)
        {
            int marketDataFieldId = marketDataValuesByteBuffer.getInt();
            MarketDataField marketDataField = MarketDataField.fromId(marketDataFieldId);
            byte fieldDataTypeId = marketDataValuesByteBuffer.get();
            FieldDataType fieldDataType = FieldDataType.fromId(fieldDataTypeId);

            if( fieldDataType == null )
            {
                throw new IllegalArgumentException("Does not yet support updates with fields whose type has an ID of \"" + fieldDataTypeId + "\"");
            }
            else
            {
                switch (fieldDataType)
                {
                    case BOOLEAN:
                        boolean boolValue = marketDataValuesByteBuffer.get() == 1;
                        callback.onBooleanUpdate(marketDataField, boolValue);
                        break;

                    case CHARACTER:
                        char charValue = marketDataValuesByteBuffer.getChar();
                        callback.onCharacterUpdate(marketDataField, charValue);
                        break;

                    case STRING:
                        int length = marketDataValuesByteBuffer.getInt();
                        byte[] bytes = new byte[length];

                        for (int i = 0; i < length; i++)
                        {
                            bytes[i] = marketDataValuesByteBuffer.get();
                        }

                        String value = new String(bytes, MarketDataInputByteBuffer.UTF8);
                        callback.onStringUpdate(marketDataField, value);
                        break;

                    case INTEGER:
                        int integerValue = marketDataValuesByteBuffer.getInt();
                        callback.onIntegerUpdate(marketDataField, integerValue);
                        break;

                    case LONG:
                        long longValue = marketDataValuesByteBuffer.getLong();
                        callback.onLongUpdate(marketDataField, longValue);
                        break;

                    case FLOAT:
                        float floatValue = marketDataValuesByteBuffer.getFloat();
                        callback.onFloatUpdate(marketDataField, floatValue);
                        break;

                    case DOUBLE:
                        double doubleValue = marketDataValuesByteBuffer.getDouble();
                        callback.onDoubleUpdate(marketDataField, doubleValue);
                        break;

                    case BYTES:
                        length = marketDataValuesByteBuffer.getInt();
                        bytes = new byte[length];

                        for (int i = 0; i < length; i++)
                        {
                            bytes[i] = marketDataValuesByteBuffer.get();
                        }

                        callback.onByteArrayUpdate(marketDataField, bytes);
                        break;

                    default:
                        throw new IllegalArgumentException("Does not yet support updates with fields of type \"" + fieldDataType + "\"");
                }
            }
        }
    }


    /**
     * Parses the bytes passed in a market data update and calls a specified callback to deal with them.
     *
     * @param byteArray The byte array containing the fields updated.
     */
    public static Map<MarketDataField, Object> parseMarketDataUpdatesByteArray(byte[] byteArray) throws Exception
    {
        ByteBuffer marketDataValuesByteBuffer = ByteBuffer.wrap(byteArray);
        Map<MarketDataField, Object> toReturn = new HashMap<>();

        while (marketDataValuesByteBuffer.remaining() != 0)
        {
            int marketDataFieldId = marketDataValuesByteBuffer.getInt();
            MarketDataField marketDataField = MarketDataField.fromId(marketDataFieldId);
            byte fieldDataTypeId = marketDataValuesByteBuffer.get();
            FieldDataType fieldDataType = FieldDataType.fromId(fieldDataTypeId);

            switch (fieldDataType)
            {
                case BOOLEAN:
                    boolean boolValue = marketDataValuesByteBuffer.get() == 1;
                    toReturn.put(marketDataField, boolValue);
                    break;

                case CHARACTER:
                    char charValue = marketDataValuesByteBuffer.getChar();
                    toReturn.put(marketDataField, charValue);
                    break;

                case STRING:

                    int length = marketDataValuesByteBuffer.getInt();
                    byte[] bytes = new byte[length];

                    for (int i = 0; i < length; i++)
                    {
                        bytes[i] = marketDataValuesByteBuffer.get();
                    }

                    String value = new String(bytes, MarketDataInputByteBuffer.UTF8);
                    toReturn.put(marketDataField, value);
                    break;

                case INTEGER:
                    int integerValue = marketDataValuesByteBuffer.getInt();
                    toReturn.put(marketDataField, integerValue);
                    break;

                case LONG:
                    long longValue = marketDataValuesByteBuffer.getLong();
                    toReturn.put(marketDataField, longValue);
                    break;

                case FLOAT:
                    float floatValue = marketDataValuesByteBuffer.getFloat();
                    toReturn.put(marketDataField, floatValue);
                    break;

                case DOUBLE:
                    double doubleValue = marketDataValuesByteBuffer.getDouble();
                    toReturn.put(marketDataField, doubleValue);
                    break;

                case BYTES:
                    length = marketDataValuesByteBuffer.getInt();
                    bytes = new byte[length];

                    for (int i = 0; i < length; i++)
                    {
                        bytes[i] = marketDataValuesByteBuffer.get();
                    }

                    toReturn.put(marketDataField, bytes);
                    break;

                default:
                    throw new IllegalArgumentException("Does not yet support updates with fields of type \"" + fieldDataType + "\"");
            }
        }

        return toReturn;
    }
}
