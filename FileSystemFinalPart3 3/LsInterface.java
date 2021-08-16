import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LsInterface extends Remote {
    // Declaring the method prototype
    public String ls(String dirName) throws RemoteException;
}