/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mate.fetcher;

import java.rmi.Naming;

/**
 *
 * @author Máté
 */

public class RMIclient {
    public static void main(String[] args){
        int a = 5; 
        try{
            String url = "rmi://127.0.0.1:2001/Pinokio-PC";
            iRMI remote = (iRMI)Naming.lookup(url);
            System.out.println("5^2: " + remote.carrel(a));
            System.out.println("5^2: " + remote.racine(a));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
