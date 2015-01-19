package com.ithinkrok.virtualhost;

import java.io.*;
import java.net.Socket;

/**
 * Created by paul on 19/01/15.
 */
public class User extends Thread{

    private static class UserServer extends Thread {
        public Socket socket;
        public InputStream in;
        public OutputStream out;

        public UserServer(Socket socket) throws IOException {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        }

        @Override
        public void run() {
            while(!socket.isClosed()){
                try {
                    in.read();
                } catch (IOException e) {
                    System.out.println("com.ithinkrok.virtualhost.User server error:");
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
                    int packetLength = in.readInt();
                    int packetId = in.readInt();
                    if(packetId != 0){
                        skip(in, packetLength - 1);
                        continue;
                    }

                    int protocolVersion = in.readInt();
                    String address = in.readUTF();
                    int port = in.readUnsignedShort();
                    int nextState = in.readInt();
                } else {
                    server.out.write(in.read());
                }
            } catch(IOException e){
                System.out.println("com.ithinkrok.virtualhost.User client error:");
                e.printStackTrace();
                return;
            }
        }
    }

    private void skip(InputStream in, int bytes) throws IOException {
        for(; bytes > 0; --bytes)in.read();
    }

    private void connectToServer(int packetLength, int protocolVersion, String address, int port, int nextState){

    }



}
