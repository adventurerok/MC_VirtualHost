package com.ithinkrok.virtualhost.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by paul on 20/01/15.
 *
 * Reads variables like VarInts and Strings used in Minecraft packets
 */
public class MinecraftInputStream extends DataInputStream {

    public static Charset CHARSET = Charset.forName("UTF-8");


    public MinecraftInputStream(InputStream in) {
        super(in);
    }

    public int readVarInt() throws IOException {
        int var = 0;
        int counter = 0;
        byte b;

        do
        {
            b = this.readByte();
            var |= (b & 127) << counter++ * 7;

            if (counter > 5)
            {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((b & 128) == 128);

        return var;

    }

    public String readString() throws IOException {
        int length = readVarInt();

        byte[] bytes = new byte[length];
        read(bytes);

        return new String(bytes, CHARSET);


    }
}
