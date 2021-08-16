#Designed and implemented a user-space shell utility capable of processing a FAT32 file system image using RMI capability

Files Included:
The various interfaces that all extend Remote and are individual commands that the client can call
CdInterface - The cd command interface. Returns a string which either highlights the information requested or with an error message
CloseInterface - The close command interface. Returns a string which either highlights the information requested or with an error message
InfoInterface - The info command interface. Returns a string which either highlights the information requested or with an error message
LsInterface - The ls command interface. Returns a string which either highlights the information requested or with an error message
OpenInterface - The open command interface. Returns a string which either highlights the information requested or returns an error message
ReadInterface - The read command interface. Returns a string which either highlights the information requested or returns an error message
SizeInterface - The size command interface. Returns a string which either highlights the information requested or returns an error message
StatInterface - The stat command interface. Returns a string which either highlights the information requested or returns an error message

Impl - implementation for all the interfaces above

fat32.img - Img file we tested on

fat32_reader_p2 - same fat32 reader as previously submitted which handles all the processing of the img file by constructing a structure for all the different files

The java files that the user directly interacts with:
Server - The server that calls the fat32 reader upon specific requests
Client - allows the client to call specific information from the server

Compiling/Running Program:
Steps to run the Server
1. javac *.java - command to compile all the java files in the folder
2. rmic Impl - The rmic tool is used to invoke the rmi compiler that creates the Stub and Skeleton objects.
3. For windows - start rmiregistry [portName] - start the rmi registry service by using rmiregistry tool and a specific portName the user
specifies. We used 1234 for testing purposes
    For mac we ran it by using: rmiregistry [portname] & 
	[In terms of our testing, we tested using two macs and connected using the mac rmiregistry [portName] command and successfully connected the two computers using the VPN provided]
4. java Server [img file] [hostname] [portName] - Runs the server's main class, which contains three arguments to run correctly. 1- A path to the img file 2 - the ip address that is being used. When testing we used 192.168.130.XX and in this case whatever the specific ip that you are currently connected. This ip will be the exact same as the one that will be used by the client. Also, use the same port that was specified in step 3.

Steps to run the Client
5. java Client [hostname] [portName] - Runs the client's main class which requires two arguments to run correctly. Use the same port and host that were used for the server above.
If the client cannot find a server with the given inputs it will throw an error asking for a new hostname/port to be entered. 
	In our testing when connecting two computers, the computer running the server would need to follow steps 1-4 to run correctly. The client on the other computer also needs to do java *.java, to compile all the files and then rmic Impl as well. Step 5 specified above is if the files are already compiled and the rmic is already made previously locally. When testing across two computers, then the extra step of compiling and rmic Impl need to be done as well. 


Commands: All commands from the previous assignments are available. 

Terminate the Client: STOP
Terminate the Server: Control C

Challenges:
1. It was a challenge to coordinate all the different versions of the code that were flying around from
all the various steps and stages in the project
2. To connect two of our computers together, which we ultimately figured out, took some troubleshooting
3. Understanding the breakdown of RMI and where the processing was being done for the Client and the Server
side took some sketching out but we came to a working theory
4. We were never together so we had to meet and code over Zoom which provided it's own challenges.

Source we used to check whether k-th bit is set:
https://www.geeksforgeeks.org/check-whether-k-th-bit-set-not/
