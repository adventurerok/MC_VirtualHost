package com.ithinkrok.virtualhost;

import com.ithinkrok.virtualhost.io.MinecraftOutputStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by paul on 19/01/15.
 */
public class Hoster extends Thread{

    private ServerSocket socket;

    //private ArrayList<User> users = new ArrayList<>();

    private HashMap<Address, Address> virtualServers = new HashMap<>();

    private byte[] defaultStatus;

    private static Hoster instance;

    public static final Logger LOGGER = Logger.getLogger("MCVH");


    private String defaultVersion = "1.8";
    private String defaultProtocol = "5";
    private String defaultDescription = "{\"text\":\"Unknown virtual host\"}";

    public static Hoster getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException {
        Hoster host = instance = new Hoster();

        host.start();
    }

    public Hoster() throws IOException {
        int port = 25565;

        File propertiesFile = new File("config.properties");
        if(!propertiesFile.exists()){
            writeResource("config.properties", propertiesFile);
        } else {
            Properties props = new Properties();
            props.load(new FileInputStream(propertiesFile));

            try{
                port = Integer.parseInt(props.getProperty("port", "25565"));
            } catch (NumberFormatException e){
                LOGGER.severe("Invalid port. Exiting");
                return;
            }

            defaultVersion = props.getProperty("default_version", "1.8");
            defaultProtocol = props.getProperty("default_protocol", "5");
            defaultDescription = props.getProperty("default_description", "{\"text\":\"Unknown virtual host\"}");

        }

        File serversFile = new File("virtualservers.servers");
        if(!serversFile.exists()){
            writeResource("virtualservers.servers", serversFile);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(serversFile)));

        String line;
        while((line = reader.readLine()) != null){
            line = line.trim();
            if(line.isEmpty()) continue;
            String[] parts = line.split(" ");

            Address incoming = new Address(parts[0]);
            Address virtual = new Address(parts[1]);

            virtualServers.put(incoming, virtual);

            LOGGER.info("Converting " + incoming.toString() + " to " + virtual.toString());
        }

        getDefaultStatus();

        socket = new ServerSocket(port);
    }

    public Address getVirtualAddress(Address external){
        return virtualServers.get(external);
    }


    @Override
    public void run() {
        while(!socket.isClosed()){
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
        try(InputStream in = Hoster.class.getClassLoader().getResourceAsStream(resource);
            FileOutputStream out = new FileOutputStream(file)) {
            int read;

            while ((read = in.read()) != -1) {
                out.write(read);
            }

        }


    }

    public byte[] getDefaultStatus() throws IOException {
        if(defaultStatus != null) return  defaultStatus;

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        MinecraftOutputStream out = new MinecraftOutputStream(byteOut);

        out.writeVarInt(0);

        StringBuilder json = new StringBuilder();

        json.append("{");

        json.append("\"version\": {");
        json.append("\"name\": \"");
        json.append(defaultVersion);
        json.append("\",");

        json.append("\"protocol\": ");
        json.append(defaultProtocol);
        json.append("},");

        json.append("\"players\": {");
        json.append("\"max\": 0,");
        json.append("\"online\": 0");
        json.append("},");

        json.append("\"description\": ");
        json.append(defaultDescription);

        json.append("}");

        out.writeString(json.toString());

        byte[] bytes = byteOut.toByteArray();

        return defaultStatus = bytes;

    }
}
