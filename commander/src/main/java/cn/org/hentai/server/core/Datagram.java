package cn.org.hentai.server.core;

import cn.org.hentai.server.util.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by matrixy on 2018/1/5.
 */
public final class Datagram
{
    public static byte[] read(InputStream reader) throws IOException
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream(512);
        int buffByteCount = reader.available();
        if (buffByteCount == 0) return null;
        byte[] data = new byte[Math.max(buffByteCount, 13)];

        // 读取包头
        reader.read(data);

        // 协议头检测
        if (data[0] != 0xfa || data[1] != 0xfa || data[2] != 0xfa) throw new RuntimeException("错误的协议头");

        // 数据体长度
        int bodyLength = ByteUtils.getInt(data, 3, 4);
        buff.write(data);

        // 读取数据体
        int blockCount = (int)Math.ceil(bodyLength / 512f);
        data = new byte[512];
        for (int i = 0; i < blockCount; i++)
        {
            int len = reader.read(data);
            buff.write(data, 0, len);
        }

        return buff.toByteArray();
    }
}
