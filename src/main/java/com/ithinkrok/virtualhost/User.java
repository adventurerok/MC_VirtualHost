package com.ithinkrok.virtualhost;

import com.ithinkrok.virtualhost.io.MinecraftInputStream;
import com.ithinkrok.virtualhost.io.MinecraftOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by paul on 19/01/15.
 * <p/>
 * Handles a connection between a user and a server
 */
public class User extends Thread {

    UserServer server;
    Socket client;
    MinecraftInputStream in;
    MinecraftOutputStream out;

    public User(Socket client) throws IOException {
        this.client = client;

        client.setTcpNoDelay(true);

        in = new MinecraftInputStream(this.client.getInputStream());
        out = new MinecraftOutputStream(this.client.getOutputStream());

    }

    @Override
    public void run() {
        while (true) {
            try {

                int packetLength = in.readVarInt();

                byte[] packet = readBytes(packetLength);
                MinecraftInputStream packetIn = new MinecraftInputStream(new ByteArrayInputStream(packet));

                int packetId = packetIn.readVarInt();
                if (packetId > 1) {
                    Hoster.LOGGER.warning("PacketId " + packetId + " instead of 0, continuing");
                    continue;
                } else if (packetId == 1) {
                    out.writeVarInt(packetLength);
                    out.write(packet);
                    continue;
                }

                packetIn.readVarInt();
                String address = packetIn.readString();
                int port = packetIn.readUnsignedShort();
                int nextState = packetIn.readVarInt();

                connectToServer(address, port, nextState, packet);
                break;

            } catch (IOException e) {
                Hoster.LOGGER.info("User " + client.getRemoteSocketAddress().toString() + " disconnected");

                if (!server.socket.isClosed()) {
                    try {
                        server.socket.close();
                    } catch (IOException ignored) {

                    }
                }

                return;
            }
        }


        MinecraftOutputStream out = server.out;
        MinecraftInputStream in = this.in;
        while (true) {
            try {
                out.write(in.read());
            } catch (IOException e) {
                Hoster.LOGGER.info("User " + client.getRemoteSocketAddress().toString() + " disconnected");

                if (!server.socket.isClosed()) {
                    try {
                        server.socket.close();
                    } catch (IOException ignored) {

                    }
                }
                return;
            }
        }
    }

    private byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];

        in.read(bytes);

        return bytes;
    }

    private void connectToServer(String address, int port, int nextState, byte[] packet) throws IOException {
        Hoster.LOGGER.info("User " + client.getRemoteSocketAddress().toString() + " is connecting");

        address = address.trim();
        int nullIndex = address.indexOf("\0");
        if (nullIndex > -1) address = address.substring(0, nullIndex); //so FML can connect

        //address = address.replaceAll("(?i)[(\\[{]?null[)\\]}]?", "");
        Address us = new Address(address, port);

        Address out = Hoster.getInstance().getVirtualAddress(us);
        if (out == null) {
            Hoster.LOGGER.info("- Unknown incoming address: " + us.toString());

            if (nextState == 1) {
                Hoster.LOGGER.info("- Sending default ping to client");

                packet = Hoster.getInstance().getDefaultStatus();

                this.out.writeVarInt(packet.length);
                this.out.write(packet);
            }
            return;
        } else {
            Hoster.LOGGER.info("- Forwarding " + us + " to " + out);
        }

        if (nextState == 1) {
            Hoster.LOGGER.info("- User is just pinging the server");
        } else if (nextState == 2) {
            Hoster.LOGGER.info("- " + Hoster.ANSI_GREEN + "User is connecting to the server" + Hoster.ANSI_RESET);
        }

        Socket socket = new Socket(out.hostname, out.port);

        server = new UserServer(socket);

        server.out.writeVarInt(packet.length);
        server.out.write(packet);

        server.start();


    }

    private class UserServer extends Thread {
        public Socket socket;
        public MinecraftInputStream in;
        public MinecraftOutputStream out;

        public UserServer(Socket socket) throws IOException {
            this.socket = socket;
            socket.setTcpNoDelay(true);
            this.in = new MinecraftInputStream(socket.getInputStream());
            this.out = new MinecraftOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            MinecraftInputStream in = this.in;
            MinecraftOutputStream out = User.this.out;
            while (true) {
                try {
                    out.write(in.read());
                } catch (IOException e) {
                    if (!client.isClosed()) {
                        try {
                            client.close();
                        } catch (IOException ignored) {
                        }
                    }
                    return;
                }
            }
        }
    }


}
