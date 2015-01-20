package com.ithinkrok.virtualhost;

import java.io.*;
import java.net.Socket;

/**
 * Created by paul on 19/01/15.
 */
public class User extends Thread{

    private class UserServer extends Thread {
        public Socket socket;
        public DataInputStream in;
        public DataOutputStream out;

        public UserServer(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            while(!socket.isClosed()){
                try {
                    User.this.out.write(in.read());
                } catch (IOException e) {
                    System.out.println("User server error:");
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    UserServer server;

    Socket client;
    DataInputStream in;
    DataOutputStream out;

    public User(Socket client) throws IOException {
        this.client = client;

        in = new DataInputStream(this.client.getInputStream());
        out = new DataOutputStream(this.client.getOutputStream());

    }

    @Override
    public void run() {
        while(!client.isClosed()){
            try {
                if(server == null){
                    int packetLength = readVarInt(in);

                    byte[] packet = readBytes(packetLength);
                    DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(packet));

                    int packetId = readVarInt(packetIn);
                    if(packetId != 0){
                        System.out.println("PacketId " + packetId + " instead of 0, continuing");
                        continue;
                    }

                    readVarInt(packetIn);
                    String address = readUTF8(packetIn);
                    int port = packetIn.readUnsignedShort();
                    int nextState = readVarInt(packetIn);

                    connectToServer(address, port, nextState, packet);
                } else {
                    server.out.write(in.read());
                }
            } catch(IOException e){
                System.out.println("User client error:");
                e.printStackTrace();
                return;
            }
        }
    }


    private void connectToServer(String address, int port, int nextState, byte[] packet) throws IOException {
        address = address.trim();
        if(address.endsWith("FML")) address = address.substring(0, address.length() - 3);

        //address = address.replaceAll("(?i)[(\\[{]?null[)\\]}]?", "");
        Address us = new Address(address, port);

        Address out = Hoster.getInstance().getVirtualAddress(us);
        if(out == null){
            System.out.println("Unknown incoming address: " + us.toString());
        } else {
            System.out.println("Forwarding " + us + " to " + out);
        }

        Socket socket = new Socket(out.hostname, out.port);

        server = new UserServer(socket);

        writeVarInt(server.out, packet.length);
        server.out.write(packet);

        server.start();


    }

    private byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];

        in.read(bytes);

        return bytes;
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int out = 0;
        byte b;
        for(int d = 0; ; ++d){
            b = in.readByte();

            out ^= (b & 127) << (d * 7);

            if((b & 128) == 0) return out;
        }
    }

    private void writeVarInt(DataOutputStream out, int var) throws IOException {
        while(true){
            byte b = 0;
            if(var > 127) b |= 128;

            b |= (var & 127);
            out.writeByte(b);

            var >>= 7;

            if(var == 0) break;
        }
    }

    private String readUTF8(DataInputStream in) throws IOException {
        int length = readVarInt(in);

        StringBuilder str = new StringBuilder();

        for(int d = 0; d < length; ++d){
            byte b0 = in.readByte();

            if((b0 & 128) == 0){
                if(b0 == 0) continue;
                str.append((char)b0);
                continue;
            }

            byte b1 = in.readByte();

            //if((b1 & 0b11000000) == 0b11000000){
                //str.append()
            //}
        }

        return str.toString();
    }



}
