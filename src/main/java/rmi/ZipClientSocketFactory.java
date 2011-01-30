package main.java.rmi;

import java.io.IOException; 
import java.io.Serializable; 
import java.net.Socket; 
import java.rmi.server.RMIClientSocketFactory;

public class ZipClientSocketFactory 
    implements RMIClientSocketFactory, Serializable { 
    public Socket createSocket(String host, int port) 
        throws IOException { 
            ZipSocket socket = new ZipSocket(host, port); 
            return socket; 
    } 
}
