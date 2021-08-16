/*Stackoverflow: barrier fix*/

/* Generic */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <time.h>

/* Network */
#include <netdb.h>
#include <sys/socket.h>

#define BUF_SIZE 100

typedef struct thread {
    int id;
    char* filename;
    int clientfd;
    pthread_t t;
} thread;

char opt;                         //true (1) if second filename was passed in, false otherwise.
char* host;
char* portNum;
char* filename1;
char* filename2;
char policy;                      //0 for CONCUR, 1 for FIFO. argv[schedalg]
int threadNum;                    //N
pthread_barrier_t sync_barrier;   //tells threads to wait for each other
thread** threads;                 //collection of threads
pthread_mutex_t sync_mutex;       //allows threads to work concurently if need be

//declare functions
thread* makeThread(int c);
void* startThread();

// Get host information (used to establishConnection)
struct addrinfo *getHostInfo(char* host, char* port) {
  int r;
  struct addrinfo hints, *getaddrinfo_res;
  // Setup hints
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res))) {
    fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));
    return NULL;
  }

  return getaddrinfo_res;
}

// Establish connection with host
int establishConnection(struct addrinfo *info) {
  if (info == NULL) return -1;

  int clientfd;
  for (;info != NULL; info = info->ai_next) {
    if ((clientfd = socket(info->ai_family,
                           info->ai_socktype,
                           info->ai_protocol)) < 0) {
      perror("[establishConnection:35:socket]");
      continue;
    }

    if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0) {
      close(clientfd);
      perror("[establishConnection:42:connect]");
      continue;
    }

    freeaddrinfo(info);
    return clientfd;
  }

  freeaddrinfo(info);
  return -1;
}

// Send GET request
void GET(int clientfd, char *path) {
  char req[1000] = {0};
  sprintf(req, "GET %s HTTP/1.0\r\n\r\n", path);
  send(clientfd, req, strlen(req), 0);
}

int main(int argc, char **argv) {
  //argc must be 6 or 7 - optional
  if (argc == 6) opt = 0;
  else if (argc == 7) {
    filename2 = argv[6];
    opt = 1;
  }
  else {
    fprintf(stderr, "USAGE: ./httpclient [host] [portnum] [threads] [schedalg] [filename1] [filename2]\n");
    return 1;
  }
  //init filename1
  filename1 = argv[5];
  //init host
  host = argv[1];
  //init port number
  portNum = argv[2];
  //init amount of threads, N
  threadNum = atoi(argv[3]);
  //init type of scheduling policy
  if (!strcmp(argv[4], "CONCUR")) policy = 0;
  else if (!strcmp(argv[4], "FIFO")) policy = 1;
  else {
      fprintf(stderr, "[main:108] \"CONCUR/FIFO\" not specified in [schedalg]");
      return 3;
  }
  //init the barrier 
  pthread_barrier_init(&sync_barrier, NULL, threadNum);
  pthread_mutex_init(&sync_mutex, NULL);
  //init threads
  threads = calloc(threadNum, threadNum*sizeof(thread));
  int c;
  for (c = 0; c < threadNum; c++) {
    threads[c] = makeThread(c);
  }
  while(1){/*spin until destroyed*/}

  return 0;
}

thread* makeThread(int c) {
  thread* t = malloc(sizeof(thread));
  t->id = c;
  t->clientfd = 0;
  //distribute half threads to filename1 and half to filename2, but only if filename2 was given
  t->filename = opt ? c % 2 ? filename2:filename1 :filename1;
  pthread_create(&t->t, NULL, startThread, t);
  return t;
}

void* startThread(thread* t) {
  //int clientfd;
  char buf[BUF_SIZE];
  while(1) {
    //wait for other threads to complete
    pthread_barrier_wait(&sync_barrier);
    //if FIFO, Lock mutex and then wait for signal to relase mutex
    if(policy) pthread_mutex_lock(&sync_mutex);

    //now do what we did with the original basic main():
    //Establish connection with <hostname>:<port>
    t->clientfd = establishConnection(getHostInfo(host, portNum));
    if (t->clientfd == -1) {
      fprintf(stderr,
              "[main:154] Failed to connect to: %s:%s \n",
              host, portNum);
      return NULL;
    }

    // Send GET request > stdout
    GET(t->clientfd, t->filename);
    //unlock mutex if FIFO
    if (policy) pthread_mutex_unlock(&sync_mutex);
    while (recv(t->clientfd, buf, BUF_SIZE, 0) > 0) {
      fputs(buf, stdout);
      memset(buf, 0, BUF_SIZE);
    }
    close(t->clientfd);
  }
  return NULL;
}