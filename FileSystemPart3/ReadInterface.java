import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReadInterface extends Remote {
    // Declaring the method prototype
    public String read(String dirName, int offset, int numBytes) throws RemoteException;
}