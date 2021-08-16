import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InfoInterface extends Remote {
    // Declaring the method prototype
    public String info() throws RemoteException;
}