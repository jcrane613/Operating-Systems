import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
public class Client {
    public static void main(String[] args) throws Exception
    {
        Registry registry = null;

        try
        {
             registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));
             InfoInterface infoObj = (InfoInterface)registry.lookup("reader");
        }
        catch (ConnectException e)
        {
            System.err.println("Server cannot be found: The port and ip address you are trying to reach were non reachable. Please try again");
            return;
        }
        System.out.print("/] ");
        Scanner scanner = new Scanner(System.in);
        String cont = "";
        while (!cont.equals("STOP")) {
            cont = scanner.nextLine();
            String[] strings = cont.split(" ");
            switch (strings[0])
            {
                case "info":
                    InfoInterface infoObj = (InfoInterface)registry.lookup("reader");
                    String infoString = infoObj.info();
                    System.out.print(infoString);
                    break;
                case "stat":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the stat command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    StatInterface statObj = (StatInterface)registry.lookup("reader");
                    String statString = statObj.stat(strings[1].toUpperCase());
                    System.out.print(statString);
                    break;
                case "ls":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the ls command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    LsInterface lsObj = (LsInterface)registry.lookup("reader");
                    String lsString = lsObj.ls(strings[1].toUpperCase());
                    System.out.print(lsString);
                    break;
                case "cd":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the cd command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    CdInterface cdObj = (CdInterface)registry.lookup("reader");
                    String cdString = cdObj.cd(strings[1].toUpperCase());
                    System.out.print(cdString);
                    break;
                case "open":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the open command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition());
                        break;
                    }
                    OpenInterface openObj = (OpenInterface)registry.lookup("reader");
                    String openString = openObj.open(strings[1].toUpperCase());
                    System.out.print(openString);
                    break;
                case "close":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the close command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    CloseInterface closeObj = (CloseInterface)registry.lookup("reader");
                    String closeString = closeObj.close(strings[1].toUpperCase());
                    System.out.print(closeString);
                    break;
                case "size":
                    if(strings.length != 2) {
                        System.out.println("You must enter two arguments when using the size command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    SizeInterface sizeObj = (SizeInterface)registry.lookup("reader");
                    String sizeString = sizeObj.size(strings[1].toUpperCase());
                    System.out.print(sizeString);
                    break;
                case "read":
                    if(strings.length != 4) {
                        System.out.println("You must enter four arguments when using the read command");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    int offset;
                    int numBytes;
                    try {
                        offset = Integer.parseInt(strings[2]);
                        numBytes = Integer.parseInt(strings[3]);
                    }
                    catch(Exception e) {
                        System.out.println("You must enter integers for the offset and number of bytes");
                        System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                        break;
                    }
                    ReadInterface readObj = (ReadInterface)registry.lookup("reader");
                    String readString = readObj.read(strings[1].toUpperCase(), offset, numBytes);
                    System.out.print(readString);
                    break;
                case "STOP":
                    break;
                default:
                    System.out.println("UNRECOGNIZED COMMAND, TRY AGAIN");
                    System.out.print("/" + fat32_reader_p2.getCurrentPosition() + "] ");
                    break;
            }
        }
    }
}