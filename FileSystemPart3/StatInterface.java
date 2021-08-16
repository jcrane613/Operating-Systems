import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StatInterface extends Remote {
    // Declaring the method prototype
    public String stat(String dirName) throws RemoteException;
}