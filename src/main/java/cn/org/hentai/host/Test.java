package cn.org.hentai.host;

import cn.org.hentai.server.proxy.Packet;
import cn.org.hentai.server.util.NonceStr;
import cn.org.hentai.util.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Expect on 2018/1/26.
 */
public class Test
{
    public static void main(String[] args) throws Exception
    {
        // testParse();
        testHost();
    }

    public static void testHost() throws Exception
    {
        Socket host = new Socket("localhost", 9999);
        InputStream inputStream = host.getInputStream();
        OutputStream outputStream = host.getOutputStream();

        byte[] data = NonceStr.generate(32).getBytes();
        System.out.println("Data Length: " + data.length);
        byte[] packet = Packet.create(2, 1, 1, data, "yLbu7FXLqJxdmzXlF0zVa6EpX731tZgltZ0o4cDIW92C6XLer2GOHgdtQWujMXYL");
        outputStream.write(packet);
        outputStream.flush();
        System.out.println("Send: " + ByteUtils.toString(packet));
        while (true)
        {
            if (inputStream.available() > 0) break;
        }
        byte[] recv = new byte[inputStream.available()];
        inputStream.read(recv);
        System.out.println("Recv: " + ByteUtils.toString(recv));
        host.close();
    }

    public static void testParse() throws Exception
    {
        byte[] data = ByteUtils.parse("FA FA FA 00 00 00 28 00 00 00 02 40 01 67 03 C4 FC DB 74 6C CE 34 B5 4D 20 21 E1 90 4B AF DA 79 CC AD 23 6A 30 7C B4 7E 6C AB 0C 80 44 52 C9 93 FB AE 5D 22 0B");
        byte[] recv = Packet.read(new ByteArrayInputStream(data));
        System.out.println(ByteUtils.toString(recv));
    }
}
