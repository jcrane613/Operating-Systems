#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <time.h>
#define VERSION 23
#define BUFSIZE 8096
#define ERROR      42
#define LOG        44
#define FORBIDDEN 403
#define NOTFOUND  404
#define IMAGE 1
#define HTML 2

struct {
    char *ext;
    char *filetype;
} extensions [] = {
        {"gif", "image/gif" },
        {"jpg", "image/jpg" },
        {"jpeg","image/jpeg"},
        {"png", "image/png" },
        {"ico", "image/ico" },
        {"zip", "image/zip" },
        {"gz",  "image/gz"  },
        {"tar", "image/tar" },
        {"htm", "text/html" },
        {"html","text/html" },
        {0,0} };

typedef struct Task {
    int socketfd, hit, type, age;
    double arrival, dispatch, complete;
    char *buffer[BUFSIZE+1];
} Task;

int taskCount = 0;
int priorityCount = 0;
int queueNum = 0;
Task taskQ[256];
int priority;

//Usage Statistics
clock_t start;
int xStatReqArrivalCount = 0;
//double Xstatreqarrivaltime = 0.0;
int Xstatreqdispatchcount= 0;
//double Xstatreqdispatchtime=0.0;
int Xstatreqcompletecount = 0;
//double Xstatreqcompletetime = 0;
typedef struct data
{
    int id;
    int threadCount;
    int html;
    int image;
} data;

struct data statistics[256];
int currentThread;

pthread_mutex_t mutexQueue;
pthread_cond_t condQueueEmpty;
pthread_cond_t condQueueFull;

void logger(int type, char *s1, char *s2, int socket_fd)
{
    int fd ;
    char logbuffer[BUFSIZE*2];

    switch (type) {
        case ERROR: (void)sprintf(logbuffer,"ERROR: %s:%s Errno=%d exiting pid=%d",s1, s2, errno,getpid());
            break;
        case FORBIDDEN:
            if(write(socket_fd, "HTTP/1.1 403 Forbidden\nContent-Length: 185\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>403 Forbidden</title>\n</head><body>\n<h1>Forbidden</h1>\nThe requested URL, file type or operation is not allowed on this simple static file webserver.\n</body></html>\n",271)){}
            (void)sprintf(logbuffer,"FORBIDDEN: %s:%s",s1, s2);
            break;
        case NOTFOUND:
            if(write(socket_fd, "HTTP/1.1 404 Not Found\nContent-Length: 136\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>404 Not Found</title>\n</head><body>\n<h1>Not Found</h1>\nThe requested URL was not found on this server.\n</body></html>\n",224)){}
            (void)sprintf(logbuffer,"NOT FOUND: %s:%s",s1, s2);
            break;
        case LOG: (void)sprintf(logbuffer," INFO: %s:%s:%d",s1, s2,socket_fd); break;
    }
    /* No checks here, nothing can be done with a failure anyway */
    if((fd = open("nweb.log", O_CREAT| O_WRONLY | O_APPEND,0644)) >= 0) {
        if(write(fd,logbuffer,strlen(logbuffer))){}
        if(write(fd,"\n",1)){}
        (void)close(fd);
    }
    if(type == ERROR || type == NOTFOUND || type == FORBIDDEN) exit(3);
}

/* this is a child web server process, so we can exit on errors */
void web(Task *task)
{
    int fd = task->socketfd;
    int hit = task->hit;
    int statIndex = task->type;
    int age = task->age;
    double dispatch = task->dispatch;
    int j, file_fd, buflen;
    long i, ret, len;
    char * fstr;
    char *buffer = *task->buffer; /* static so zero filled */

    logger(LOG,"request",buffer,hit);
    if( strncmp(buffer,"GET ",4) && strncmp(buffer,"get ",4) ) {
        logger(FORBIDDEN,"Only simple GET operation supported",buffer,fd);
    }
    for(i=4;i<BUFSIZE;i++) { /* null terminate after the second space to ignore extra stuff */
        if(buffer[i] == ' ') { /* string is "GET URL " +lots of other stuff */
            buffer[i] = 0;
            break;
        }
    }
    for(j=0;j<i-1;j++)  /* check for illegal parent directory use .. */
        if(buffer[j] == '.' && buffer[j+1] == '.') {
            logger(FORBIDDEN,"Parent directory (..) path names not supported",buffer,fd);
        }
    if( !strncmp(&buffer[0],"GET /\0",6) || !strncmp(&buffer[0],"get /\0",6) ) /* convert no filename to index file */
        (void)strcpy(buffer,"GET /index.html");

    /* work out the file type and check we support it */
    buflen=strlen(buffer);
    fstr = (char *)0;
    for(i=0;extensions[i].ext != 0;i++) {
        len = strlen(extensions[i].ext);
        //printf("Extension %s",extensions[i].ext );
        if( !strncmp(&buffer[buflen-len], extensions[i].ext, len)) {
            fstr =extensions[i].filetype;
            //printf("FileType %s",extensions[i].filetype );
            break;
        }
    }
    if(fstr == 0) logger(FORBIDDEN,"file extension type not supported",buffer,fd);

    if(( file_fd = open(&buffer[5],O_RDONLY)) == -1) {  /* open the file for reading */
        logger(NOTFOUND, "failed to open file",&buffer[5],fd);
    }
    logger(LOG,"SEND",&buffer[5],hit);
    len = (long)lseek(file_fd, (off_t)0, SEEK_END); /* lseek to the file end to find the length */
    (void)lseek(file_fd, (off_t)0, SEEK_SET); /* lseek back to the file start ready for reading */
    Xstatreqcompletecount++;
    clock_t end = clock();
    double Xstatreqcompletetime = (double) (end - start);
    (void)sprintf(buffer,"HTTP/1.1 200 OK\nServer: nweb/%d.0\nContent-Length: %ld\nConnection: close\nContent-Type: %s\n Thread Stat:\n Thread ID: %d\n Thread Count: %d\n Thread HTML Count: %d\n Thread IMAGE count: %d \n Arrival Count: %d\n Arrival Time: %f \n Dispatch Count: %d \n Dispatch Time: %f \n Complete Count: %d \n Complete Time: %f \n Age: %d \n\n\n", VERSION, len, fstr, statistics[statIndex].id, statistics[statIndex].threadCount, statistics[statIndex].html, statistics[statIndex].image, xStatReqArrivalCount-1, task->arrival, Xstatreqdispatchcount, dispatch, Xstatreqcompletecount, Xstatreqcompletetime, age ); /* Header + a blank line */
    logger(LOG,"Header",buffer,hit);
    if(write(fd,buffer,strlen(buffer))){}

    // Send the statistical headers described in the paper, example below
    /*
    (void)sprintf(buffer,"X-stat-req-arrival-count: %d\r\n", xStatReqArrivalCount);
    write(fd,buffer,strlen(buffer));
    (void)sprintf(buffer,"X-stat-req-arrival-time: %f\r\n", Xstatreqarrivaltime);
    (void)write(fd,buffer,strlen(buffer));
    (void)sprintf(buffer,"X-stat-req-dispatch-count: %d\r\n", Xstatreqdispatchcount);
    (void)write(fd,buffer,strlen(buffer));
    (void)sprintf(buffer,"X-stat-req-dispatch-time: %f\r\n", Xstatreqdispatchtime);
    (void)write(fd,buffer,strlen(buffer));
    (void)sprintf(buffer,"X-stat-req-arrival-time: %f\r\n", Xstatreqarrivaltime);
    (void)write(fd,buffer,strlen(buffer));
    (void)sprintf(buffer,"X-stat-req-arrival-time: %f\r\n", Xstatreqarrivaltime);
    (void)write(fd,buffer,strlen(buffer));
    */



    /* send file in 8KB block - last block may be smaller */
    while ( (ret = read(file_fd, buffer, BUFSIZE)) > 0 ) {
        if(write(fd,buffer,ret)){}
    }
    sleep(1); /* allow socket to drain before signalling the socket is closed */

}

void executeTask(Task* task) {
    web(task);
}

void submitTask(Task task, int i) {
    pthread_mutex_lock(&mutexQueue);
    while (taskCount >= queueNum){
        pthread_cond_wait(&condQueueFull, &mutexQueue);
    }
    if(i == 1 && priority == 1)
    {
        for (int i = priorityCount; i < taskCount - 1; i++) {
            taskQ[i+1] = taskQ[i];
        }
        taskQ[priorityCount] = task;
        taskCount++;
        priorityCount++;
    }
    else if(i == 2 && priority == 2)
    {
        for (int i = priorityCount; i < taskCount - 1; i++) {
            taskQ[i+1] = taskQ[i];
        }
        taskQ[priorityCount] = task;
        taskCount++;
        priorityCount++;
    }
    else
    {
        taskQ[taskCount] = task;
        taskCount++;
    }
    xStatReqArrivalCount++;
    pthread_mutex_unlock(&mutexQueue);
    pthread_cond_signal(&condQueueEmpty);
}

void* startThread(void* args) {
    while (1) {
        Task task;
        int pointer = *(int *)args;
        pthread_mutex_lock(&mutexQueue);
        while (taskCount == 0 ) {
            pthread_cond_wait(&condQueueEmpty, &mutexQueue);
        }
        clock_t end = clock();
        double Xstatreqdispatchtime = (double) (end - start);
        if(priorityCount != 0)
        {
            priorityCount--;
        }
        task = taskQ[0];
        statistics[pointer].threadCount++;
        if(task.type == IMAGE)
        {
            statistics[pointer].image++;
        }
        else
        {
            statistics[pointer].html++;
        }
        int i;
        for (i = 0; i < taskCount-1; i++) {
            taskQ[i] = taskQ[i + 1];
            taskQ[i].age++;
        }
        taskCount--;
        Xstatreqdispatchcount++;
        task.type = pointer;
        task.dispatch = Xstatreqdispatchtime;
        pthread_mutex_unlock(&mutexQueue);
        pthread_cond_signal(&condQueueFull);
        executeTask(&task);
    }
}

int main(int argc, char **argv)
{
    int i, port, listenfd, socketfd, hit, threadNum;
    //int pid;
    socklen_t length;
    static struct sockaddr_in cli_addr; /* static = initialised to zeros */
    static struct sockaddr_in serv_addr; /* static = initialised to zeros */

/*
    if( argc < 3  || argc > 3 || !strcmp(argv[1], "-?") ) {
        (void)printf("hint: nweb Port-Number Top-Directory\t\tversion %d\n\n"
    "\tnweb is a small and very safe mini web server\n"
    "\tnweb only servers out file/web pages with extensions named below\n"
    "\t and only from the named directory or its sub-directories.\n"
    "\tThere is no fancy features = safe and secure.\n\n"
    "\tExample: nweb 8181 /home/nwebdir &\n\n"
    "\tOnly Supports:", VERSION);
        for(i=0;extensions[i].ext != 0;i++)
            (void)printf(" %s",extensions[i].ext);
        (void)printf("\n\tNot Supported: URLs including \"..\", Java, Javascript, CGI\n"
    "\tNot Supported: directories / /etc /bin /lib /tmp /usr /dev /sbin \n"
    "\tNo warranty given or implied\n\tNigel Griffiths nag@uk.ibm.com\n"  );
        exit(0);
    }
 */
    if( !strncmp(argv[2],"/"   ,2 ) || !strncmp(argv[2],"/etc", 5 ) ||
        !strncmp(argv[2],"/bin",5 ) || !strncmp(argv[2],"/lib", 5 ) ||
        !strncmp(argv[2],"/tmp",5 ) || !strncmp(argv[2],"/usr", 5 ) ||
        !strncmp(argv[2],"/dev",5 ) || !strncmp(argv[2],"/sbin",6) ){
        (void)printf("ERROR: Bad top directory %s, see nweb -?\n",argv[2]);
        exit(3);
    }
    /*
    if(chdir(argv[2]) == -1){
        (void)printf("ERROR: Can't Change to directory %s\n",argv[2]);
        exit(4);
    }
     */
    /* Become deamon + unstopable and no zombies children (= no wait()) */
    if(fork() != 0)
        return 0; /* parent returns OK to shell */
    (void)signal(SIGCHLD, SIG_IGN); /* ignore child death */
    (void)signal(SIGHUP, SIG_IGN); /* ignore terminal hangups */
    for(i=0;i<32;i++)
        (void)close(i);     /* close open files */
    (void)setpgrp();        /* break away from process group */
    logger(LOG,"nweb starting",argv[1],getpid());
    /* setup the network socket */
    if((listenfd = socket(AF_INET, SOCK_STREAM,0)) <0)
        logger(ERROR, "system call","socket",0);

    port = atoi(argv[1]);
    //Initialize number of treads
    threadNum = atoi(argv[2]);
    //Initialize size of queue
    queueNum = atoi(argv[3]);
    //The type of scheduling policy
    char *schedalg = argv[4];

    if(port < 0 || port >60000)
        logger(ERROR,"Invalid port number (try 1->60000)",argv[1],0);

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(port);
    if(bind(listenfd, (struct sockaddr *)&serv_addr,sizeof(serv_addr)) <0)
        logger(ERROR,"system call","bind",0);
    if( listen(listenfd,64) <0)
        logger(ERROR,"system call","listen",0);


    int j;
    int id[threadNum];
    pthread_t th[threadNum];
    for (j = 0; j < threadNum; j++) {
        statistics[j].html = 0;
        statistics[j].id = j;
        /*
                Note, currently this j valude for the id is not being set??

        */
        statistics[j].image = 0;
        statistics[j].threadCount = 0;
        id[j] = j;
        if (pthread_create(&th[j], NULL, &startThread, &id[j]) != 0) {
            perror("Failed to create the thread");
        }
        pthread_detach(th[j]);
    }


    pthread_mutex_init(&mutexQueue, NULL);
    pthread_cond_init(&condQueueEmpty, NULL);
    pthread_cond_init(&condQueueFull, NULL);
    char* formatAny= "ANY";
    char* formatFifo= "FIFO";
    char* formatHpic= "HPIC";
    char* formatHphc= "HPHC";
    start = clock();
    for(hit=1; ;hit++) {
        //(void) printf("Num of threads: %d", threadNum);
        length = sizeof(cli_addr);
        if((socketfd = accept(listenfd, (struct sockaddr *)&cli_addr, &length)) < 0)
            logger(ERROR,"system call","accept",0);
        clock_t end = clock();
        double Xstatreqarrivaltime = (double) (end - start);

        int   buflen;
        long i, len;
        static char buffer[BUFSIZE+1]; /* static so zero filled */

        long ret = read(socketfd,buffer,BUFSIZE);    /* read Web request in one go */
        if(ret == 0 || ret == -1) {	/* read failure stop now */
		logger(FORBIDDEN,"failed to read browser request","",socketfd);
	    }

	    if(ret > 0 && ret < BUFSIZE)	/* return code is valid chars */
		    buffer[ret]=0;		/* terminate the buffer */
	    else buffer[0]=0;
	    for(i=0;i<ret;i++)	/* remove CF and LF characters */
		if(buffer[i] == '\r' || buffer[i] == '\n')
			buffer[i]='*';
       for(i=4;i<BUFSIZE;i++) { /* null terminate after the second space to ignore extra stuff */
		if(buffer[i] == ' ') { /* string is "GET URL " +lots of other stuff */
			buffer[i] = 0;
			break;
		    }
    	}
        /*
        if( !strncmp(&buffer[0],"GET /\0",6) || !strncmp(&buffer[0],"get /\0",6) ) // convert no filename to index file
            (void)strcpy(buffer,"GET /index.html");
*/
        // work out the file type and check we support it
        buflen=strlen(buffer);
        int pointer =0 ;
        for(i=0;extensions[i].ext != 0;i++) {
            len = strlen(extensions[i].ext);
            if( !strncmp(&buffer[buflen-len], extensions[i].ext, len)) {
                pointer = i;
                break;
            }
        }
        //Normal FIFO policy or ANY policy
        if((!strcmp(schedalg,formatAny)) || (!strcmp(schedalg,formatFifo))){
            int current;
            if(pointer < 8) {
                current = IMAGE;
            }
            else {
                current = HTML;
            }

            Task t = {
                    .socketfd = socketfd,
                    .hit = hit,
                    .type = current,
                    .age = 0,
                    .arrival = Xstatreqarrivaltime,
                    .dispatch = 0,
                    .complete = 0,
                    .buffer = {buffer}
            };
            submitTask(t, 0);
        }
        else {
            //Logic to get the file type
            if((!strcmp(schedalg,formatHpic)))
            {
                priority = 1;
            }
            if((!strcmp(schedalg,formatHphc)))
            {
                priority = 2;
            }
            //This is an image file
            if(pointer <8){
                Task t = {
                        .socketfd = socketfd,
                        .hit = hit,
                        .type = IMAGE,
                        .age = 0,
                        .arrival = Xstatreqarrivaltime,
                        .dispatch = 0,
                        .complete = 0,
                        .buffer = {buffer}
                };
                submitTask(t, 1);
            }
                //This is an html file
            else if(pointer <10){
                Task t = {
                        .socketfd = socketfd,
                        .hit = hit,
                        .type = HTML,
                        .age = 0,
                        .arrival = Xstatreqarrivaltime,
                        .dispatch = 0,
                        .complete = 0,
                        .buffer = {buffer}
                };
                submitTask(t, 2);
            }

        }

       }
}
