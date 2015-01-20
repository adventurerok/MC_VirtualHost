package com.ithinkrok.virtualhost;

/**
 * Created by paul on 19/01/15.
 */
public class Address {

    public final String hostname;
    public final int port;

    public Address(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public Address(String parse){
        String[] parts = parse.split(":");

        hostname = parts[0].trim();

        if(parts.length > 1) port = Integer.parseInt(parts[1].trim());
        else port = 25565;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (port != address.port) return false;
        if (hostname != null ? !hostname.equals(address.hostname) : address.hostname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hostname != null ? hostname.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }
}
