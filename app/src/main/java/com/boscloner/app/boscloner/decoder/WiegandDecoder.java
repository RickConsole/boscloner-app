package com.boscloner.app.boscloner.decoder;

import android.util.Log;

public class WiegandDecoder {
    private static final String LOG_TAG = "WiegandDecoder";

    public enum CardFormat {
        BIT_26,
        BIT_33,
        BIT_34,
        BIT_35
    }

    public static class DecodedCard {
        public String format;
        public long cardNumber;
        public int facilityCode;

        public DecodedCard(String format, long cardNumber, int facilityCode) {
            this.format = format;
            this.cardNumber = cardNumber;
            this.facilityCode = facilityCode;
        }

        @Override
        public String toString() {
            return "Format: " + format + ", Facility Code: " + facilityCode + ", Card Number: " + cardNumber;
        }
    }

    public static DecodedCard decode(byte[] data, CardFormat format) {
        if (data.length != 5) {
            throw new IllegalArgumentException("Input must be 5 bytes long");
        }

        long code = bytesToLong(data);
        String binaryString = String.format("%40s", Long.toBinaryString(code)).replace(' ', '0');
        Log.d(LOG_TAG, "Full binary (40 bits): " + binaryString);

        switch (format) {
            case BIT_26:
                return decode26Bit(code);
            case BIT_33:
                return decode33Bit(code);
            case BIT_34:
                return decode34Bit(code);
            case BIT_35:
                return decode35Bit(code);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private static DecodedCard decode26Bit(long code) {
        long bits26 = code & 0x3FFFFFF;
        int facilityCode = (int)((bits26 >> 17) & 0xFF);
        int cardNumber = (int)((bits26 >> 1) & 0xFFFF);
        Log.d(LOG_TAG, "26-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("26-bit H10301", cardNumber, facilityCode);
    }

    private static DecodedCard decode33Bit(long code) {
        // Extract the last 33 bits
        long bits33 = code & 0x1FFFFFFF;
        int facilityCode = (int)((bits33 >> 26) & 0x7F);
        long cardNumber = (bits33 >> 1) & 0x3FFFFFF;
        Log.d(LOG_TAG, "33-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("33-bit HID Generic", cardNumber, facilityCode);
    }

    private static DecodedCard decode34Bit(long code) {
        // Extract the last 34 bits
        long bits34 = code & 0x3FFFFFFF;
        int facilityCode = (int)((bits34 >> 17) & 0xFFFF);
        long cardNumber = (bits34 >> 1) & 0xFFFF;
        Log.d(LOG_TAG, "34-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("34-bit HID Generic", cardNumber, facilityCode);
    }

    private static DecodedCard decode35Bit(long code) {
        // Extract the last 35 bits (ignore the leading 5 bits)
        long bits35 = code & 0x7FFFFFFF;

        // Facility code = bits 2 to 14 (13 bits)
        int facilityCode = (int)((bits35 >> 21) & 0x1FFF);

        // Card code = bits 15 to 34 (20 bits)
        long cardNumber = (bits35 >> 1) & 0xFFFFF;

        Log.d(LOG_TAG, "35-bit decode result: Facility Code: " + facilityCode + ", Card Number: " + cardNumber);
        return new DecodedCard("35-bit HID Corporate 1000", cardNumber, facilityCode);
    }

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}
