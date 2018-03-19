package cn.org.hentai.server.core;

import cn.org.hentai.server.util.AES;
import cn.org.hentai.util.ByteUtils;

/**
 * Created by matrixy on 2017/12/30.
 * FA FA FA 协议头
 * 00 00 00 00 加密后的数据体长度
 * 00 00 00 00 主机ID
 * 00 00 指令，最高2位用于描述加密类型，01表示AES加密
 * ...... AES加密后的数据体
 */
public class Packet
{
    public static final int ENCRYPT_NONE = 0x00;
    public static final int ENCRYPT_AES = 0x01;
    public static final int ENCRYPT_DES = 0x02;

    public static Packet parse(byte[] data, String key)
    {
        int length = 0;
        Packet packet = new Packet();
        packet.setLength(length = ByteUtils.getInt(data, 3, 4));
        packet.setClientId(ByteUtils.getInt(data, 7, 4));
        int command = ByteUtils.getInt(data, 11, 2) & 0xff;
        int encrypt = (command >> 14) & 0x03;
        command = command & 0x3f;
        packet.setEncrypt(encrypt);
        byte[] body = new byte[length];
        if (encrypt == ENCRYPT_NONE) packet.setData(body);
        if (encrypt == ENCRYPT_AES) packet.setData(AES.decode(body, key));
        // packet.setData();
        return packet;
    }

    private int length;
    private int clientId;
    private int encrypt;
    private short command;
    private byte[] data;

    public byte[] toBytes()
    {
        return null;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public short getCommand() {
        return command;
    }

    public void setCommand(short command) {
        this.command = command;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "length=" + length +
                ", clientId=" + clientId +
                ", encrypt=" + encrypt +
                ", command=" + command +
                ", data=" + ByteUtils.toString(data) +
                '}';
    }
}
