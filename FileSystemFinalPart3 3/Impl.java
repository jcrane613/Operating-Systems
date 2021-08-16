import java.rmi.*;
import java.rmi.server.*;

public class Impl extends UnicastRemoteObject implements LsInterface, StatInterface, InfoInterface, CdInterface,
        OpenInterface, CloseInterface, SizeInterface, ReadInterface {

    // Default constructor to throw RemoteException
    // from its parent constructor
    public Impl(String path) throws Exception {
        super();
        this.reader = new fat32_reader_p2(path);
    }

    private fat32_reader_p2 reader;

    // Implementation of all the interfaces
    @Override
    public String ls(String dirName) {
        return reader.ls(dirName) + reader.getCurrentPosition();
    }
    @Override
    public String stat(String dirName) {
        return reader.stat(dirName) + "\n" + reader.getCurrentPosition();
    }

    @Override
    public String info() {
        return reader.info() + reader.getCurrentPosition();
    }

    @Override
    public String cd(String dirName) {
        String returnString = reader.cd(dirName);
        if(!returnString.equals("")) {
            return returnString + "\n" + reader.getCurrentPosition();
        }
        return reader.getCurrentPosition();
    }

    @Override
    public String open(String dirName) {
        return reader.open(dirName) + "\n" + reader.getCurrentPosition();
    }

    @Override
    public String close(String dirName) {
        return reader.close(dirName) + "\n" + reader.getCurrentPosition();
    }

    @Override
    public String size(String dirName) {
        return reader.size(dirName) + "\n" + reader.getCurrentPosition();
    }

    @Override
    public String read(String dirName, int offset, int numBytes) {
        return reader.read(dirName, offset, numBytes) + "\n" + reader.getCurrentPosition();
    }
}