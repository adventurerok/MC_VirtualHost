package com.ithinkrok.virtualhost;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by paul on 19/01/15.
 */
public class Hoster extends Thread{

    private ServerSocket socket;

    private ArrayList<User> users = new ArrayList<>();

    private HashMap<Address, Address> virtualServers = new HashMap<>();


    public static void main(String[] args) throws IOException {
        Hoster host = new Hoster();

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
                System.out.println("Invalid port. Exiting");
                return;
            }

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


        }

        socket = new ServerSocket(port);
    }



    @Override
    public void run() {
        while(!socket.isClosed()){
            try {
                Socket user = socket.accept();

                User u = new User(user);

                users.add(u);
            } catch (IOException e) {
                System.out.println("Exception when accepting: ");
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
}
