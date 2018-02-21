/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mate.fetcher;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author Máté
 */
public interface iRMI extends Remote {
    int carrel(int n) throws RemoteException;
    double racine(int n ) throws RemoteException;
}
