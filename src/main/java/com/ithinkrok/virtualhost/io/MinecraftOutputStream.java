package com.ithinkrok.virtualhost.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by paul on 20/01/15.
 */
public class MinecraftOutputStream extends DataOutputStream {

    public MinecraftOutputStream(OutputStream out) {
        super(out);
    }

    public void writeVarInt(int input) throws IOException {
        while ((input & -128) != 0)
        {
            writeByte(input & 127 | 128);
            input >>>= 7;
        }

        writeByte(input);
    }

    public void writeString(String input) throws IOException {
        byte[] bytes = input.getBytes(MinecraftInputStream.CHARSET);

        this.writeVarInt(bytes.length);
        this.write(bytes);

    }
}
