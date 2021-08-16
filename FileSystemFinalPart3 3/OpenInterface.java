import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OpenInterface extends Remote {
    // Declaring the method prototype
    public String open(String dirName) throws RemoteException;
}