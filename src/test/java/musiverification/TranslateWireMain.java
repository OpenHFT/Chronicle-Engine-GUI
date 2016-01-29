package musiverification;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.BinaryWire;
import net.openhft.chronicle.wire.TextWire;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by peter.lawrey on 04/12/2015.
 */
public class TranslateWireMain {
    @Test
    public void translate() {
        Bytes bytes = Bytes.fromHexString("00000060 79 82 06 02 00 00 8A 80  04 F4 FF 01 53 4B 33 50 y······· ····SK3P\n" +
                "00000070 4D 50 4D 46 44 53 54 4D  34 54 37 52 43 31 45 4F MPMFDSTM 4T7RC1EO\n" +
                "00000080 4D 44 33 4E 51 37 46 45  39 55 55 34 42 4E 53 31 MD3NQ7FE 9UU4BNS1\n" +
                "00000090 46 47 4E 4C 47 36 43 4A  47 5A 34 50 49 52 48 39 FGNLG6CJ GZ4PIRH9\n" +
                "000000a0 41 56 48 57 32 55 38 56  48 55 4A 38 54 31 36 57 AVHW2U8V HUJ8T16W\n" +
                "000000b0 38 38 33 45 4B 58 54 35  41 38 58 38 4C 56 53 4F 883EKXT5 A8X8LVSO\n" +
                "000000c0 46 32 36 37 51 46 5A 42  39 45 30 44 42 42 46 39 F267QFZB 9E0DBBF9\n" +
                "000000d0 56 4A 4A 56 52 35 37 35  4F 4B 55 37 45 5A 34 55 VJJVR575 OKU7EZ4U\n" +
                "000000e0 37 50 55 41 4C 44 49 41  59 55 4D 54 52 32 4B 54 7PUALDIA YUMTR2KT\n" +
                "000000f0 33 4B 47 51 4A 55 33 54  4E 47 5A 4B 4A 4B 34 52 3KGQJU3T NGZKJK4R\n" +
                "00000100 52 31 56 33 55 32 50 41  45 45 35 5A 51 44 4B 45 R1V3U2PA EE5ZQDKE\n" +
                "00000110 58 4D 5A 32 42 31 4D 4F  55 54 42 5A 48 44 38 4D XMZ2B1MO UTBZHD8M\n" +
                "00000120 44 55 50 47 44 51 4C 54  4C 37 47 52 44 39 36 39 DUPGDQLT L7GRD969\n" +
                "00000130 56 54 4C 52 4D 55 4B 35  55 53 43 4D 48 4D 38 47 VTLRMUK5 USCMHM8G\n" +
                "00000140 39 42 43 52 42 44 52 4B  4E 55 59 58 4D 48 54 46 9BCRBDRK NUYXMHTF\n" +
                "00000150 32 44 41 47 37 37 4D 55  51 30 43 32 4C 35 57 31 2DAG77MU Q0C2L5W1\n" +
                "00000160 58 37 35 46 45 32 55 51  46 36 49 49 33 47 38 4E X75FE2UQ F6II3G8N\n" +
                "00000170 49 44 45 57 55 46 31 51  4E 57 43 4F 41 33 53 43 IDEWUF1Q NWCOA3SC\n" +
                "00000180 57 48 54 58 5A 41 4E 35  45 30 4E 53 45 51 57 5A WHTXZAN5 E0NSEQWZ\n" +
                "00000190 52 52 4F 34 59 51 4B 35  39 4C 31 4E 58 53 30 55 RRO4YQK5 9L1NXS0U\n" +
                "000001a0 31 55 4D 42 33 4C 44 33  38 53 45 44 53 38 51 46 1UMB3LD3 8SEDS8QF\n" +
                "000001b0 56 49 51 37 45 54 53 52  37 4B 34 49 57 38 5A 59 VIQ7ETSR 7K4IW8ZY\n" +
                "000001c0 48 42 53 56 4F 36 31 4B  59 56 38 4B 49 54 34 48 HBSVO61K YV8KIT4H\n" +
                "000001d0 56 4C 37 35 41 53 46 5A  4F 30 41 59 48 5A 4F 49 VL75ASFZ O0AYHZOI\n" +
                "000001e0 58 37 4E 5A 52 38 4F 36  4F 4E 55 37 55 55 49 34 X7NZR8O6 ONU7UUI4\n" +
                "000001f0 4A 54 57 43 58 4D 45 51  34 46 55 59 45 34 4D 31 JTWCXMEQ 4FUYE4M1\n" +
                "00000200 36 53 46 4C 44 4D 58 58  58 41 45 50 33 45 57 49 6SFLDMXX XAEP3EWI\n" +
                "00000210 30 51 37 35 58 52 34 55  38 47 4B 4D 36 34 39 39 0Q75XR4U 8GKM6499\n" +
                "00000220 43 55 56 39 57 59 35 55  31 58 50 41 47 35 35 48 CUV9WY5U 1XPAG55H\n" +
                "00000230 31 30 59 39 32 34 4F 38  34 4D 34 59 50 38 5A 56 10Y924O8 4M4YP8ZV\n" +
                "00000240 34 59 31 4C 33 57 4F 41  49 35 32 41 45 57 51 44 4Y1L3WOA I52AEWQD\n" +
                "00000250 37 57 48 49 48 4A 48 38  42 4A 55 41 30 4F 54 36 7WHIHJH8 BJUA0OT6\n" +
                "00000260 44 58 33 4D 54 4E 59 30  46 45 34 43 C9 74 69 6D DX3MTNY0 FE4C·tim\n" +
                "00000270 65 73 74 61 6D 70 A7 4C  81 82 6D 51 01 00 00 CA estamp·L ··mQ····\n" +
                "00000280 69 64 65 6E 74 69 66 69  65 72 04 C9 69 73 44 65 identifi er··isDe\n" +
                "00000290 6C 65 74 65 64 B0 D2 62  6F 6F 74 53 74 72 61 70 leted··b ootStrap\n" +
                "000002a0 54 69 6D 65 53 74 61 6D  70 A7 51 56 91 6D 51 01 TimeStam p·QV·mQ·\n" +
                "000002b0 00 00 BE 12 72 65 6D 6F  74 65 49 64 65 6E 74 69 ····remo teIdenti\n" +
                "000002c0 66 69 65 72 3D 33                                fier=3");
        Bytes<ByteBuffer> text = Bytes.elasticByteBuffer();
        new BinaryWire(bytes)
                .copyTo(new TextWire(text));
        System.out.println(text);
    }
}
