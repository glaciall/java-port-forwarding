package cn.org.hentai.server.util;

import java.io.FileOutputStream;

/**
 * Created by matrixy on 2017/8/22.
 */
public final class ByteUtils
{
    public static byte[] parse(String hexString)
    {
        String[] hexes = hexString.split(" ");
        byte[] data = new byte[hexes.length];
        for (int i = 0; i < hexes.length; i++) data[i] = (byte)(Integer.parseInt(hexes[i], 16) & 0xff);
        return data;
    }

    public static String toString(byte[] data)
    {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++)
        {
            if ((data[i] & 0xff) < 0x10) sb.append('0');
            sb.append(Integer.toHexString(data[i] & 0xff).toUpperCase());
            sb.append(' ');
        }
        return sb.toString();
    }

    public static boolean getBit(int val, int pos)
    {
        return getBit(new byte[] {
                (byte)((val >> 0) & 0xff),
                (byte)((val >> 8) & 0xff),
                (byte)((val >> 16) & 0xff),
                (byte)((val >> 24) & 0xff)
        }, pos);
    }

    public static int reverse(int val)
    {
        byte[] bytes = toBytes(val);
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) ret[i] = bytes[3 - i];
        return toInt(ret);
    }

    public static int toInt(byte[] bytes)
    {
        int val = 0;
        for (int i = 0; i < 4; i++) val |= (bytes[i] & 0xff) << ((3 - i) * 8);
        return val;
    }

    public static byte[] toBytes(int val)
    {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            bytes[i] = (byte)(val >> ((3 - i) * 8) & 0xff);
        }
        return bytes;
    }

    public static int getInt(byte[] data, int offset, int length)
    {
        int val = 0;
        for (int i = 0; i < length; i++) val |= (data[offset + i] & 0xff) << ((length - i - 1) * 8);
        return val;
    }

    public static long getLong(byte[] data, int offset, int length)
    {
        long val = 0;
        for (int i = 0; i < length; i++) val |= ((long)data[offset + i] & 0xff) << ((length - i - 1) * 8);
        return val;
    }

    public static boolean getBit(byte[] data, int pos)
    {
        return ((data[pos / 8] >> (pos % 8)) & 0x01) == 0x01;
    }

    public static byte[] concat(byte[] data1, byte[] data2)
    {
        byte[] result = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        return result;
    }
}
