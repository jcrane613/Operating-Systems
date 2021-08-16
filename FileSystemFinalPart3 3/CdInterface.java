import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CdInterface extends Remote {
    // Declaring the method prototype
    public String cd(String dirName) throws RemoteException;
}