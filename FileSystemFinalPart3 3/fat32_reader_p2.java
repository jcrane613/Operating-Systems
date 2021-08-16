import java.io.*;
import java.nio.file.*;
import java.util.*;


public class fat32_reader_p2 {

    public static byte[] data;
    public static int bytesPerSec;
    public static int secPerClus;
    public static int rsvdSecCnt;
    public static int numFATs;
    public static int fATSz32;
    public static int rootClus;
    public static int startFAT;
    public static int startClus;
    public static int rootDir;
    public static Node rootNode;
    public static Node currentNode;
    public static HashSet<String> hashSet = new HashSet<>();

    public static class Node {
        int shortName;
        String nameString;
        boolean isDir;
        public boolean isHidden;
        Node parent;
        List<Node> children;
        List<Byte> contentsFile;
        String stringContents;
        int clusNum;
        int size;

        public Node(int shortName) {

            this.shortName = shortName;
            this.children = new ArrayList<>();
            this.nameString = getFileName(shortName);
            this.isDir = isKthBitSet(data[shortName+11], 5);
            this.isHidden = isKthBitSet(data[shortName+11], 2);
            this.size = thirtytwoBits(shortName+28);
            if(this.size > 0 && !this.isHidden)
                this.contentsFile = buildArray(nameString, getClusNumFromShortName( this.shortName), this.size);
        }

        public void addChild(int child, int clusNum) {
            this.clusNum = clusNum;
            Node childNode = new Node(child);
            childNode.parent = this;
            this.children.add(childNode);
        }
        public Node getParent()
        {
            return parent;
        }
        public List<Node> getChildren(){
            return children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return shortName == node.shortName &&
                    isDir == node.isDir &&
                    isHidden == node.isHidden &&
                    Objects.equals(nameString, node.nameString) &&
                    Objects.equals(parent, node.parent) &&
                    Objects.equals(children, node.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shortName, nameString, isDir, isHidden, parent, children);
        }
    }

    public fat32_reader_p2(String pathToImg) throws IOException {
        Path path = Paths.get(pathToImg);
        //Path path = Paths.get("fat32.img");
        data = Files.readAllBytes(path);
        bytesPerSec = sixteenBits(11);
        secPerClus = data[13];
        rsvdSecCnt = sixteenBits(14);
        numFATs = data[16];
        fATSz32 = thirtytwoBits(36);
        rootClus = thirtytwoBits(44);
        startFAT = bytesPerSec*rsvdSecCnt;
        startClus = startFAT + (fATSz32*bytesPerSec*numFATs);
        rootDir = startClus + ((rootClus-2)*bytesPerSec*secPerClus);

        rootNode = new Node(rootDir);
        rootNode.nameString = "";
        constructSystemStructure(rootNode);
        currentNode = rootNode;
        //Path path = Paths.get("fat32.img");
        data = Files.readAllBytes(path);
        bytesPerSec = sixteenBits(11);
        secPerClus = data[13];
        rsvdSecCnt = sixteenBits(14);
        numFATs = data[16];
        fATSz32 = thirtytwoBits(36);
        rootClus = thirtytwoBits(44);
        startFAT = bytesPerSec*rsvdSecCnt;
        startClus = startFAT + (fATSz32*bytesPerSec*numFATs);
        rootDir = startClus + ((rootClus-2)*bytesPerSec*secPerClus);

        rootNode = new Node(rootDir);
        rootNode.nameString = "";
        constructSystemStructure(rootNode);
        currentNode = rootNode;
    }

//    public static void main(String[] args) throws IOException {
//
//        Path path = Paths.get(args[0]);
//        //Path path = Paths.get("fat32.img");
//        data = Files.readAllBytes(path);
//        bytesPerSec = sixteenBits(11);
//        secPerClus = data[13];
//        rsvdSecCnt = sixteenBits(14);
//        numFATs = data[16];
//        fATSz32 = thirtytwoBits(36);
//        rootClus = thirtytwoBits(44);
//        startFAT = bytesPerSec*rsvdSecCnt;
//        startClus = startFAT + (fATSz32*bytesPerSec*numFATs);
//        rootDir = startClus + ((rootClus-2)*bytesPerSec*secPerClus);
//
//        rootNode = new Node(rootDir);
//        rootNode.nameString = "";
//        constructSystemStructure(rootNode);
//        currentNode = rootNode;
//        //testFATIteration();
//
//        Scanner scanner = new Scanner(System.in);
//        String cont = "";
//        // testfileSystemTree();
//        while (!cont.equals("STOP"))
//        {
//            Node printNode = currentNode;
//            String dirPathName = "";
//            boolean help = false;
//            while(!currentNode.equals(rootNode))
//            {
//                dirPathName = "/" + currentNode.nameString + dirPathName;
//                currentNode = currentNode.getParent();
//                help = true;
//            }
//            if(help)
//                dirPathName = dirPathName.substring(1);
//            System.out.print("/"+dirPathName + "] ");
//            currentNode = printNode;
//            cont = scanner.nextLine();
//            String[] strings = cont.split(" ");
//            switch (strings[0])
//            {
//                case "info":
//                    info();
//                    break;
//                case "stat":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the stat command");
//                        break;
//                    }
//                    stat(strings[1].toUpperCase());
//                    break;
//                case "ls":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the ls command");
//                        break;
//                    }
//                    ls(strings[1].toUpperCase());
//                    break;
//                case "cd":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the cd command");
//                        break;
//                    }
//                    cd(strings[1].toUpperCase());
//                    break;
//                case "open":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the open command");
//                        break;
//                    }
//                    open(strings[1].toUpperCase());
//                    break;
//                case "close":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the close command");
//                        break;
//                    }
//                    close(strings[1].toUpperCase());
//                    break;
//                case "size":
//                    if(strings.length != 2) {
//                        System.out.println("You must enter two arguments when using the size command");
//                        break;
//                    }
//                    size(strings[1].toUpperCase());
//                    break;
//                case "read":
//                    if(strings.length != 4) {
//                        System.out.println("You must enter four arguments when using the read command");
//                        break;
//                    }
//                    int offset;
//                    int numBytes;
//                    try {
//                        offset = Integer.parseInt(strings[2]);
//                        numBytes = Integer.parseInt(strings[3]);
//                    }
//                    catch(Exception e) {
//                        System.out.println("You must enter integers for the offset and number of bytes");
//                        break;
//                    }
//                    read(strings[1].toUpperCase(), offset, numBytes);
//                    break;
//                case "STOP":
//                    break;
//                default:
//                    System.out.println("UNRECOGNIZED COMMAND, TRY AGAIN");
//                    break;
//            }
//        }
//    }

    public static void constructSystemStructure(Node rootNode){
        int off = rootNode.shortName;
        int clusNum = 2;
        boolean isDir;
        int attribute;
        if (off != rootDir){
            clusNum = getClusNumFromShortName(off);
        }
        while(clusNum != 0x0FFFFFFF) {
            off = startClus + ((clusNum - 2) * bytesPerSec * secPerClus);
            int start = off;

            while (off - start < bytesPerSec * secPerClus) {
                attribute = data[off + 11];
                if (!isKthBitSet(attribute, 5)) {
                    isDir = false;
                }
                //If location is Long Name or file
                //Can I get rid of hte stuff after the or sttament
                if ((data[off] == 0x41 && data[off + 11] == 0x0F) || data[off] == 0xE5) {
                    off += 32;
                    continue;
                }
                //Reached end of directory
                if (data[off] == 0x00) {
                    break;
                }
                rootNode.addChild(off, clusNum);
                off += 32;
            }
            clusNum = getNextClusterFromFAT(clusNum);
        }
        List<Node> children = rootNode.getChildren();
        for (Node node : children) {
            isDir = true;
            attribute = data[node.shortName + 11];
            String fileName = getFileName(node.shortName);
            //System.out.println(fileName);
            if (!isKthBitSet(attribute, 5) || fileName.equals(".") || fileName.equals("..")) {
                isDir = false;
            }
            if (isDir) {
                constructSystemStructure(node);
            }
        }
    }

    public static String info(){
        return "BPB_BytesPerSec is 0x"+bytesPerSec +", "+ bytesPerSec+ '\n' +
                "BPB_SecPerClus is 0x" + secPerClus+ ", "+secPerClus + '\n'
                + "BPB_RsvdSecCnt is 0x" + rsvdSecCnt+ ", " +rsvdSecCnt+ '\n'+
                "BPB_NumFATs is 0x" + numFATs + ", " + numFATs +'\n' +
                "BPB_FATSz32 is 0x" +fATSz32+ ", "+ fATSz32+ '\n';
    }

    public static String stat(String name){
        Node currentWorkingDirectory = currentNode;
        if(name.charAt(0)== '/')
        {
            currentNode = rootNode;
            name = name.substring(1);
        }
        Node node = getNodeFromDirPath(name);
        String output = "";
        if (node != null){
            int size = thirtytwoBits(node.shortName+28);
            int clusNum = getClusNumFromShortName(node.shortName);
            int attribute = data[node.shortName+11];
            String attr = attribute(attribute);
            output = "Size is " + size + "\nAttributes " + attr + "\nNext cluster number is 0x" + Integer.toHexString(clusNum);
            /*System.out.printf("Size is %d\n", size);
            System.out.println("Attributes "+ attr);
            System.out.printf("Next cluster number is 0x%x\n", clusNum);*/
        } else{
            output = "Error: file/directory does not exist";
        }
        currentNode = currentWorkingDirectory;
        return output;
    }

    public static String ls(String dirName) {
        String output = "";
        Node currentWorkingDirectory = currentNode;

        if(dirName.equals("/")) {
            return "Error: / is not a directory\n";
        }
        boolean slashAtBeginning = false;
        if(dirName.charAt(0)== '/')
        {
            currentNode = rootNode;
            dirName = dirName.substring(1);
            slashAtBeginning = true;
        }
        Node node = getNodeFromDirPath(dirName);
        if (node == null || (!node.isDir && node.shortName != startClus)) {
            //System.out.println("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a directory");
            return "Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a directory\n";
        }
        List<String> list = new ArrayList<>();
        for(Node s: node.children) {
            if ((!isKthBitSet(data[s.shortName+11], 2)) && (!isKthBitSet(data[s.shortName+11], 3)) && (!isKthBitSet(data[s.shortName+11], 4))){
                list.add(s.nameString);
            }
        }
        Collections.sort(list);
        for(String s: list) {
            //System.out.print(s + " ");
            output += s + " ";
        }
        //System.out.println();
        output += "\n";
        currentNode = currentWorkingDirectory;
        return output;
    }
    public static String open(String dirName)
    {
        Node currentWorkingDirectory = currentNode;
        if(dirName.equals("/")) {
            return "Error: / is not a file\n";
        }
        boolean slashAtBeginning = false;
        if(dirName.charAt(0)== '/')
        {
            currentNode = rootNode;
            dirName = dirName.substring(1);
            slashAtBeginning = true;
        }
        Node node = getNodeFromDirPath(dirName);
        if (node == null || node.isDir){
            return ("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a file");
        }
        String dirPathName = "/" + node.nameString;
        while(!node.equals(rootNode))
        {
            node = node.getParent();
            dirPathName = "/" + node.nameString + dirPathName;
        }
        currentNode = currentWorkingDirectory;
        if(hashSet.contains(dirPathName.substring(1)))
        {
            return ("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is already open");
        }
        else{
            hashSet.add(dirPathName.substring(1));
            return ((slashAtBeginning ? "/" : "") + dirName + " is open");
        }
    }
    public static String close(String dirName){
        Node currentWorkingDirectory = currentNode;
        if(dirName.equals("/")) {
            return ("Error: / is not a file");
        }
        boolean slashAtBeginning = false;
        if(dirName.charAt(0)== '/')
        {
            currentNode = rootNode;
            dirName = dirName.substring(1);
            slashAtBeginning = true;
        }
        Node node = getNodeFromDirPath(dirName);
        if (node == null || node.isDir){
            return("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a file");

        }
        String dirPathName = "/"+node.nameString;
        while(!node.equals(rootNode))
        {
            node = node.getParent();
            dirPathName = "/" + node.nameString + dirPathName;
        }
        currentNode = currentWorkingDirectory;
        if(!hashSet.contains(dirPathName.substring(1)))
        {
            return ("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not open");
        }
        else{
            hashSet.remove(dirPathName.substring(1));
            return ((slashAtBeginning ? "/" : "") + dirName + " is closed");
        }

    }
    public static String size(String dirName){
        Node currentWorkingDirectory = currentNode;
        if(dirName.equals("/")) {
            return ("Error: / is not a file");

        }
        boolean slashAtBeginning = false;
        if(dirName.charAt(0)== '/')
        {
            currentNode = rootNode;
            slashAtBeginning = true;
            dirName = dirName.substring(1);
        }
        Node node = getNodeFromDirPath(dirName);
        String returnString = "";
        if(node == null || node.isDir) {
            returnString = ("Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a file");
        }
        else {
            returnString = ("Size of " + (slashAtBeginning ? "/" : "") + dirName + " is " + node.size + " bytes");
        }
        currentNode = currentWorkingDirectory;
        return returnString;
    }
    public static String read(String dirName, int offset, int numBytes) {
        Node workingDirectoryNode = currentNode;
        if(dirName.equals("/")) {
            return ("Error: / is not a file");
        }
        boolean slashAtBeginning = false;
        if(dirName.charAt(0)== '/')
        {
            currentNode = rootNode;
            slashAtBeginning = true;
            dirName = dirName.substring(1);
        }
        String returnString = "";
        Node node = getNodeFromDirPath(dirName);
        if(node == null || node.isDir) {
            returnString =  "Error: " + (slashAtBeginning ? "/" : "") + dirName + " is not a file";
        }
        else if(offset < 0) {
            returnString =  "Error: OFFSET must be a positive value";
        }
        else if(numBytes <= 0) {
            returnString = "Error: NUM_BYTES must be a greater than zero";
        }
        else
        {
            Node readNode = node;
            String dirPathName = "/" + node.nameString;
            while (!node.equals(rootNode))
            {
                node = node.getParent();
                dirPathName = "/" + node.nameString + dirPathName;
            }
            if (!hashSet.contains(dirPathName.substring(1)))
            {
                returnString = ("Error: file is not open");
            }
            else if (readNode.contentsFile == null)
            {
                returnString = "Error: The file you are trying to read has no contents";
            } else if (numBytes + offset > readNode.contentsFile.size())
            {
                returnString = "Error: attempt to read data outside of file bounds";
            } else
            {
                String printString = "";
                byte[] bytes = new byte[offset + numBytes + 1];
                for (int i = offset; i <= offset + numBytes; i++)
                {
                    bytes[i] = readNode.contentsFile.get(i);
                }
                returnString = new String(bytes);
            }
        }
        currentNode = workingDirectoryNode;
        return returnString;
    }
    public static Node getNodeFromDirPath(String name){
        String[] arrOfStr = name.split("/");
        Node node = currentNode;
        for (String s : arrOfStr) {
            if (node == null) {
                return null;
            } else if (s.equals("..") && node.shortName == rootDir) {
                return null;
            } else if (s.equals("..")) {
                node = node.parent;
            } else if (s.equals(".")) {
                continue;
            } else {
                node = getShortNameTree(node, s);
            }
        }
        return node;
    }

    public static Node getShortNameTree(Node node, String name){
        for (Node x : node.getChildren()){
            if (compareFileNames(name, x.nameString)){
                return x;
            }
        }
        return null;
    }

    public static String cd(String path){
        Node workingDirectoryNode = currentNode;
        if(path.equals("/")) {
            return "Error: / is not a file\n";
        }
        boolean slashAtBeginning = false;
        if(path.charAt(0) == '/')
        {
            currentNode = rootNode;
            slashAtBeginning = true;
            path = path.substring(1);
        }
        Node node = getNodeFromDirPath(path);
        if(node == null) {
            currentNode = workingDirectoryNode;
            return "Error: " + (slashAtBeginning ? "/" : "") + path + " is not a directory\n";
        }
        else if (!node.isDir && node.shortName != rootDir){ // Does this include the slash at beginning?
            currentNode = workingDirectoryNode;
            return "Error: " + node.nameString + " is not a directory\n";
        }
        currentNode = node;
        return "";
    }

    public static String getCurrentPosition(){
        Node printNode = currentNode;
        String dirPathName = "";
        boolean help = false;
        if(currentNode == null) {
            return "";
        }
        while(!currentNode.equals(rootNode))
        {
            dirPathName = "/" + currentNode.nameString + dirPathName;
            currentNode = currentNode.getParent();
            help = true;
        }
        if(help)
            dirPathName = dirPathName.substring(1);
        currentNode = printNode;
        return "/" + dirPathName + "] ";
    }

    //https://www.geeksforgeeks.org/check-whether-k-th-bit-set-not/
    public static boolean isKthBitSet(int n, int k)
    {
        return (n & (1 << (k - 1))) > 0;
    }
    private static String attribute(int attribute)
    {
        String returnString = "";
        if(isKthBitSet(attribute, 7))
        {
            returnString += "ATTR_LONG_NAME" +" ";
        }
        if(isKthBitSet(attribute, 6))
        {
            returnString += "ATTR_ARCHIVE" +" ";
        }
        if(isKthBitSet(attribute, 5))
        {
            returnString +="ATTR_DIRECTORY" + " ";
        }
        if(isKthBitSet(attribute, 4))
        {
            returnString +="ATTR_VOLUME_ID" + " ";
        }
        if(isKthBitSet(attribute, 3))
        {
            returnString +="ATTR_SYSTEM" + " ";
        }
        if(isKthBitSet(attribute, 2))
        {
            returnString +="ATTR_HIDDEN" + " ";
        }
        if(isKthBitSet(attribute, 1))
        {
            returnString +="ATTR_READ_ONLY" + " ";
        }
        return returnString ;
    }

    //Takes a cluster number and file name and returns the location of the short name for the given file
    public static int getShortName(int clusNum, String name){
        boolean exists = false;
        int off = 0;

        while(clusNum != 0x0FFFFFFF) {
            off = startClus + ((clusNum - 2) * bytesPerSec * secPerClus);
            int start = off;

            while (off - start < bytesPerSec * secPerClus) {
                //If location is Long Name or Deleted file
                if ((data[off] == 0x41 && data[off + 11] == 0x0F) || data[off] == 0xE5) {
                    off += 32;
                    continue;
                }
                //Reached end of directory
                if (data[off] == 0x00) {
                    break;
                }
                //compareFileNames() needs to be fine tuned!!!!!
                if (compareFileNames(name, getFileName(off))) {
                    exists = true;
                    break;
                }
                off += 32;
            }
            if(exists){
                break;
            }
            clusNum = getNextClusterFromFAT(clusNum);
        }

        if (exists){
            return off;
        } else {
            return -1;
        }
    }

    //takes the location of a Short Name as a parameter and returns the name of file
    public static String getFileName(int off){
        byte[] nameArray = new byte[11];
        System.arraycopy(data, off, nameArray, 0, 8);
        String name = new String(nameArray);
        name = name.trim();
        byte[] extArray = new byte[3];
        System.arraycopy(data, off + 8, extArray, 0, 3);
        String ext = new String(extArray);
        if (ext.equals("   ")){
            return name;
        }
        return name + "." + ext;
    }

    public static int getClusNumFromShortName(int off){
        int hi = sixteenBits(off+20);
        int lo = sixteenBits(off+26);
        return hi<<16&0xFFFF0000 | lo&0xFFFF;
    }

    public static int getNextClusterFromFAT(int currClusNum){
        int nextClusNum = startFAT+(currClusNum*4);
        return thirtytwoBits(nextClusNum);
    }

    public static boolean compareFileNames(String userInput, String fromFileSystem){
        boolean check = true;
        fromFileSystem = fromFileSystem.replaceAll(" ", "");
        if(userInput.length() != fromFileSystem.length())
        {
            return false;
        }
        for (int i = 0; i < userInput.length(); i++){
            if (userInput.charAt(i) == '.'){

                for(int j = fromFileSystem.length() - 3; j < fromFileSystem.length() && j >= 0; j++)
                {
                    if (userInput.charAt(j) != fromFileSystem.charAt(j)){
                        check = false;
                    }

                }
                break;
            }

            if (userInput.charAt(i) != fromFileSystem.charAt(i)){
                check = false;
            }
        }
        //Above only checks if names are equal but still need to check if extensions are equal!!!!!
        return check;
    }

    public static int sixteenBits(int off) {
        return  data[off+1]<<8&0xFF00 | data[off] &0xFF;
    }

    public static int thirtytwoBits(int off) {
        return data[off+3]<<24&0xFF000000 | data[off+2]<<16&0xFF0000 | data[off+1]<<8&0xFF00 | data[off] &0xFF;
    }

    public static List<Byte> buildArray(String name, int clusNum, int size){

        List<Byte> arrayList = new ArrayList<>();
        int counter = 0;
        byte[] bytes = new byte[bytesPerSec*secPerClus];
        while (clusNum != 0x0FFFFFFF){
            int start = startClus + ((clusNum - 2) * bytesPerSec * secPerClus);
            if (bytes.length >= 0) System.arraycopy(data, start, bytes, 0, bytes.length);
            for (byte b : bytes) {
                if (counter >= size)
                    break;
                arrayList.add(b);
                counter++;
            }
            clusNum = getNextClusterFromFAT(clusNum);
        }
        return arrayList;
    }

    public static void testfileSystemTree(){
        List<Node> children = rootNode.getChildren();
        for (Node child : children) {
            if (compareFileNames("DIR", getFileName(child.shortName))) {
                List<Node> list = child.getChildren();
                for (Node value : list) {
                    System.out.println(getFileName(value.shortName));
                    if (compareFileNames("A", getFileName(value.shortName))) {
                        List<Node> list2 = value.getChildren();
                        for (Node node : list2) {
                            System.out.println(getFileName(node.shortName));
                        }
                    }
                }
            }
        }
    }

}