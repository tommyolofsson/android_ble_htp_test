package se.tommyolofsson.ble_htp_test;

public class Util {
    public static String byteArrayToString(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++)
            sb.append(String.format("0x%02x ", a[i]));
        return sb.toString().trim();
    }
}
