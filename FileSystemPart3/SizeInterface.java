import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SizeInterface extends Remote {
    // Declaring the method prototype
    public String size(String dirName) throws RemoteException;
}