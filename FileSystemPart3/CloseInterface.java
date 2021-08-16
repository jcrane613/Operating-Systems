import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CloseInterface extends Remote {
    // Declaring the method prototype
    public String close(String dirName) throws RemoteException;
}