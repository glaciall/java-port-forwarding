package cn.org.hentai.host;

import cn.org.hentai.util.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2017-12-10.
 */
public class Forwarding
{
    public static void main(String[] args) throws Exception
    {
        ServerSocket server = new ServerSocket(1212);
        while (true)
        {
            final Socket xxoo = server.accept();
            new Thread()
            {
                public void run()
                {
                    try
                    {
                        Socket client = new Socket("localhost", 3306);
                        byte[] buff = new byte[4096000];

                        InputStream is = client.getInputStream();
                        OutputStream os = client.getOutputStream();

                        InputStream reader = xxoo.getInputStream();
                        OutputStream writer = xxoo.getOutputStream();

                        while (true)
                        {
                            // 转发得到的内容
                            int bufLength = is.available();
                            if (bufLength > 0)
                            {
                                bufLength = is.read(buff);
                                System.out.println("Length: " + bufLength);
                                System.out.println("Receive from server: " + ByteUtils.toString(buff, bufLength));
                                writer.write(buff, 0, bufLength);
                            }

                            // 转发回应的
                            bufLength = reader.available();
                            if (bufLength > 0)
                            {
                                bufLength = reader.read(buff);
                                System.out.println("Length: " + bufLength);
                                System.out.println("Response from host: " + ByteUtils.toString(buff, bufLength));
                                os.write(buff, 0, bufLength);
                            }
                            Thread.sleep(100);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}