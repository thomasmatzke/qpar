package main.java.rmi;

import java.io.IOException; 
import java.io.Serializable; 
import java.net.ServerSocket; 
import java.rmi.server.RMIServerSocketFactory; 
  
public class ZipServerSocketFactory 
    implements RMIServerSocketFactory, Serializable { 
    public ServerSocket createServerSocket(int port) 
        throws IOException { 
            ZipServerSocket server = new ZipServerSocket(port); 
            return server; 
    } 
}
