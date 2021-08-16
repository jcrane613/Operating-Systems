import java.rmi.*;
import java.rmi.registry.*;
public class Server {
    public static void main(String[] args) throws Exception
    {
        Impl obj = new Impl(args[0]);
        Registry registry = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));
        registry.rebind("reader", obj);
        System.out.println("Server Started");
    }
}