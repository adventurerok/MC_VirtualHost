package com.ithinkrok.virtualhost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by paul on 19/01/15.
 */
public class Hoster extends Thread{

    private ServerSocket socket;

    private ArrayList<User> users = new ArrayList<User>();


    public static void main(String[] args) {

    }

    public Hoster() throws IOException {
        socket = new ServerSocket(19285);

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
}
