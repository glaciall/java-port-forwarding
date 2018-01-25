package cn.org.hentai.server.proxy;

/**
 * Created by Expect on 2018/1/25.
 */
public class Constants
{
    // 数据包加密类型
    public static final int ENCRYPT_TYPE_NONE = 0x00;                       // 不加密
    public static final int ENCRYPT_TYPE_DES = 0x01;                        // DES加密

    // 服务器控制指令
    public static final int COMMAND_TEST_CONNECTION = 0x01;                 // 连接测试
    public static final int COMMAND_AUTHENTICATION = 0x02;                  // 身份鉴权
    public static final int COMMAND_REQUEST_FORWARD = 0x03;                 // 要求转发
    public static final int COMMAND_REQUEST_DISCONNECT = 0x04;              // 要求断开连接
}
