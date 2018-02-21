/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mate.fetcher;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.security.Policy;
/**
 *
 * @author Máté
 */
public class RMIserver {
    public static void main(String[] args){
        System.out.println("Launching Server ...");
        try{
            //System.setProperty("java.security.policy", "file:/d:\\Doksi\\Varsó\\security\\server.policy");
            System.setSecurityManager(new RMISecurityManager()); 
           
            RMImpl srv = new RMImpl();
            String url = "rmi://127.0.0.1:2001/carrel"; //protocol:ip address:port/arbitary  identifier
            Naming.rebind(url, srv);
            System.out.println("Server listening...");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
