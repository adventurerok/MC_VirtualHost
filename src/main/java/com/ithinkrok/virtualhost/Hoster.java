package com.ithinkrok.virtualhost;

import com.ithinkrok.virtualhost.io.MinecraftOutputStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by paul on 19/01/15.
 */
public class Hoster extends Thread {

    private ServerSocket socket;

    //private ArrayList<User> users = new ArrayList<>();

    private ConcurrentHashMap<Address, Address> virtualServers = new ConcurrentHashMap<>();

    private byte[] defaultStatus;

    private static Hoster instance;

    public static final Logger LOGGER = Logger.getLogger("MCVH");

    static{

    }

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";


    private String defaultVersion = "1.8";
    private String defaultProtocol = "5";
    private String defaultDescription = "{\"text\":\"Unknown virtual host\"}";

    public static Hoster getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException {

        Hoster host = instance = new Hoster();

        host.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            String line = reader.readLine().trim().toLowerCase();

            switch(line){
                case "reload":
                    LOGGER.info("Reloading all files. No users will be disconnected");
                    host.reload();
                    break;
                case "exit":
                    LOGGER.warning("exit command issued. Stopping. All users will be disconnected");
                    System.exit(0);
                    break;
                default:
                    LOGGER.info("Unknown command.");
            }
        }
    }

    public Hoster() throws IOException {
        int port = 25565;

        File propertiesFile = new File("config.properties");
        if (!propertiesFile.exists()) {
            writeResource("config.properties", propertiesFile);
        } else {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(propertiesFile);
            props.load(in);
            in.close();

            try {
                port = Integer.parseInt(props.getProperty("port", "25565"));
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid port. Exiting");
                return;
            }

            defaultVersion = props.getProperty("default_version", "1.8");
            defaultProtocol = props.getProperty("default_protocol", "5");
            defaultDescription = props.getProperty("default_description", "{\"text\":\"Unknown virtual host\"}");

        }

        File serversFile = new File("virtualservers.servers");
        if (!serversFile.exists()) {
            writeResource("virtualservers.servers", serversFile);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(serversFile)));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ");

            Address incoming = new Address(parts[0]);
            Address virtual = new Address(parts[1]);

            virtualServers.put(incoming, virtual);

            LOGGER.info("Converting " + incoming.toString() + " to " + virtual.toString());
        }

        reader.close();

        getDefaultStatus();

        socket = new ServerSocket(port);
    }

    public Address getVirtualAddress(Address external) {
        return virtualServers.get(external);
    }


    public void reload() throws IOException {
        int port;

        File propertiesFile = new File("config.properties");
        if (!propertiesFile.exists()) {
            writeResource("config.properties", propertiesFile);
        } else {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(propertiesFile);
            props.load(in);
            in.close();

            try {
                port = Integer.parseInt(props.getProperty("port", "25565"));
            } catch (NumberFormatException e) {
                LOGGER.warning("Failed to read port in reload.");
                return;
            }

            if(port != socket.getLocalPort()){
                LOGGER.warning("Port changed. Please restart for it to take effect");
            }

            defaultVersion = props.getProperty("default_version", "1.8");
            defaultProtocol = props.getProperty("default_protocol", "5");
            defaultDescription = props.getProperty("default_description", "{\"text\":\"Unknown virtual host\"}");

        }

        File serversFile = new File("virtualservers.servers");
        if (!serversFile.exists()) {
            writeResource("virtualservers.servers", serversFile);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(serversFile)));

        ConcurrentHashMap<Address, Address> newVirtualServers = new ConcurrentHashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ");

            Address incoming = new Address(parts[0]);
            Address virtual = new Address(parts[1]);

            newVirtualServers.put(incoming, virtual);

            LOGGER.info("Converting " + incoming.toString() + " to " + virtual.toString());
        }

        virtualServers = newVirtualServers;

        reader.close();

        generateDefaultStatus();
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Socket user = socket.accept();

                //System.out.println("Used connected");

                User u = new User(user);

                u.start();

                //users.add(u);
            } catch (IOException e) {
                LOGGER.severe("Exception when accepting user");
                e.printStackTrace();
                return;
            }
        }
    }


    private void writeResource(String resource, File file) throws IOException {
        try (InputStream in = Hoster.class.getClassLoader().getResourceAsStream(resource);
             FileOutputStream out = new FileOutputStream(file)) {
            int read;

            while ((read = in.read()) != -1) {
                out.write(read);
            }

        }


    }

    public byte[] getDefaultStatus() throws IOException {
        if (defaultStatus != null) return defaultStatus;

        return generateDefaultStatus();
    }

    private byte[] generateDefaultStatus() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        MinecraftOutputStream out = new MinecraftOutputStream(byteOut);

        out.writeVarInt(0);

        out.writeString("{" + "\"version\": {" + "\"name\": \"" + defaultVersion + "\"," + "\"protocol\": " + defaultProtocol + "}," + "\"players\": {" + "\"max\": 0," + "\"online\": 0" + "}," + "\"description\": " + defaultDescription + "}");

        byte[] bytes = byteOut.toByteArray();

        return defaultStatus = bytes;
    }
}
