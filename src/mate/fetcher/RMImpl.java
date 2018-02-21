/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mate.fetcher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Máté
 */
public class RMImpl extends UnicastRemoteObject implements iRMI{

    public RMImpl() throws RemoteException {
        super(); //calls the UnicastRemoteObject default constructor
    }
    @Override
    public int carrel(int n) throws RemoteException {
        return n * n;
    }

    @Override
    public double racine(int n) throws RemoteException {
        return Math.sqrt(n);
    }
    
}
