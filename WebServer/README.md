
Contents of the Zip File: 

We have submitted the server called server_concur.c which has correctly implemented multithreaded processing. This can be seen since the dispatch, arrival, and complete times all overlap with one another thereby showing that the threads are running concurrently. This version of server was our first iteration and it does not have the ability to label the contents of the http for specific processing depending if its an image or an html as well as priority scheduling logic. This version has multithreaded working based on the set number of threads declared upon the creation of the server. 
Based on the previous implementation we began to implement the ability to determine if a file is an html or image, as well as policy for scheduling tasks in the server called, server_stats.c. This server correctly processes a request based on if it’s an image or html. This server will correctly determine the type so it could implement the correct policy, update the correct statistics for threads, such as image and html count, and age of the request. This server, although based on server_concur.c is not processing threads concurrently. This is observed since the times for dispatch and complete do not overlap with one another. it is unclear if this is an issue with the timing and printing statements or with the multithreaded implementation. Both seem implausible since each was throughly tested independently as will be highlighted below.  
Overall, Soclof and Crane implemented the first three parts, but they did not combine well together. 
Also, we submitted client.c which is the regular client that we used for testing for our two servers highlighted above since we had greater control of the sending of http requests.  
The multithreaded server is called client_multi.c and is the multithread client with FIFO and CONCUR policies. 
Also included in the zip is everything needed to run the code, such as the html and jpg files. 


Design overview: A few simple paragraphs describing the overall structure of your code and any important structures:
In order for the server to be multi threaded, we create a fixed size pool of worker threads when the web server is first started, and this number is passed in as an argument. Each of these threads will wait to handle a http request as the server receives them. The number of requests allowed in the buffer is also determined based on a command line argument. This means if the buffer is full, then the master thread blocks and waits for another thread to be freed up before accepting another task. The threads are created and stored in a threads array. Each thread has a parallel index in a statistics array which keeps track of the data regarding that thread and what types of data it has implemented. The id number of each thread, which is assigned upon creation of the thread, corresponds to a parallel index of an array of statistics structs that will be used to keep track of each individual thread’s statistics such as the amount of html’s processed, images processed, and total amount of usage. In order to determine if a given http request was an html or image, to ensure it was processed correctly, we read the contents of the requests into a buffer to determine the file type. This meant we had already read the contents of the request, therefore we passed the already made buffer to the web method, using the task struct. The task struct also stored the time of its different uses such as dispatch and arrival, as well the age and type of the specific task being parsed. The policy of prioritization is set in the main method and can be ANY, FIFO, HTML preference, or IMAGE preference as well as the amount of threads to be created and used. The way that scheduling policies was implemented is a global variable was set to store the state for the entire program. If ANY or FIFO were chosen, this means we put all the tasks into a task array once there is room in the queue. Based on the order they are submitted will be the order that they are processed in. The way a task is processed, is when a thread is ready, we take the task at the zero index of the task array, and move all other tasks down and execute the current task. If the HPIC policy is chosen, then there is a global index pointer to the last inserted image, so all other html are moved to the right one spot, and then the image is inserted in the newly freed slot, thereby maintaing FIFO ordering among the images inserted. A similar method is employed when HPHC is selected and the Html are put at the front of the array by moving all the images over one space to the right. When a task is submitted, the internal task statistics are incremented, when it arrives the time is given to its variable arrival time, when it is dispatched, time is taken again using the clock_t library. After a thread picks up a task it is executed using the web method. We added descriptors to be sent to the client by getting the data on the specific thread being using from the statistics array and the data that was stored inside of the task struct with the time and counts of html requests, and image requests, thread id, amount of times used, age etc.     


	•	Complete specification: Describe how you handled any ambiguities in the specification. For example, how do you implement the ANY policy?
We implemented the FIFO policy for ANY. It was unclear how we should process the HTML or IMAGE priority policy. Do we need to implement a fifo policy from within the context of choosing only html’s first? This was later clarified on piazza but we had done another implementation with disregarded fifo in terms of processing. 


	•	Known bugs or problems: A list of any features that you did not implement or that you know are not working correctly 

Overall there were times when we got the server to run along smoothly processing threads and http requests perfectly fine, printing out the correct statistics, for time and counts of all the different variables and working concurrently. There were other times when  without changing the code or anything else, just rerunning the server and client, there was a failure of some kind or an error. Simply running and testing the code proved to be extremely tedious and difficult to do. Sometime our server would crash mid processing or the school’s server. Other times everything would be working correctly, we would come back and rerun it again to see where we left off and it simply wouldn’t run at all. Furthermore, when Eitan and I were working together sometimes the code worked only on my computer and we were both using the COM org server. this meant we were shorthanded and could only test and improve using one computer which was a big inconvience. The overall nature of the assignment meant that we would devote large chucks of time to debugging, testing and so on, only to never getting it working making the whole task extremely frustrating and difficult to improve on.   
Also, after adding the statistics for the threads and tasks, sometimes the numbers for arrival time, dispatch and complete overlapped with one another, while other times they only processed iteratively, all without changing the processing logic  one another. It was difficult to determine the exact bug here. Was it the time functions, printing, or were the threads not really running concurrently? 
The scheduling policy for HTML and IMAGE’s would sometimes work, but also, it was hard to get consistent test results since even though an image finished before an html request, this didn’t mean the policy wasn’t working. Perhaps it was the only remaining request in the queue. 



	•	Testing: This requirement an aspect that I am very interested in. I will assign points for answering these questions. Describe how you tested the functionality of your web server. Describe how can you use the various versions of your extended client to see if the server is handing requests concurrently and implementing the FIFO, HPSC, or HPDC policies. 

We have submitted this assignment in two stages. The first stage is an implementation of part 1 - a multi-threaded web server that only displays the times of the requests being handled. The second stage is built on top of the first stage, and is our attempt to implement the prioritization of part 2 and 3. Because the client was not implemented in time, we tested the server using a shell script (named: test.sh) that ran the out-of-the-box client.c many times as many backgrounds processes. In order to ensure that the server is processing threads concurrently, we used a sleep(1) method before a http request was executed using the web method. When we sent 10 http requests to five threads in concurrently using the test.sh script, this resulted in all 5 threads executing and printing the descriptors of the requests to console, and then sleeping for a second, and saw another 5 requests printed. This indicated that the requests were being handled properly. In addition, the times displayed overlap with each other, indicating concurrency.
We then set out to implement the prioritization which required us to determine the file type in the main thread. This is where it all went downhill. For some reason, when we tried to include this functionality, this made it that the server would sometimes crash and sometimes work. When it did work, the times printed out did not overlap. We used the same testing strategy outlined above. We were not able to try to test concurrency with the sleep(1) function because that too caused the server to crash. 
Also, after adding the statistics for the threads and tasks, sometimes the numbers for arrival time, dispatch and complete overlapped with one another, while other times they only processed after one another. It was difficult to determine the exact bug here. Was it the time functions, printing, or were the threads not really running concurrently.

 Specifically, what are the exact parameters one should pass to the client and the server to demonstrate that the server is handling requests concurrently? To demonstrate that the server is correctly running the FIFO policy? the HPSC policy? the HPDC policy? In each case, if your client and server are behaving correctly, what output and statistics should you see?


To run server_concur.c:
./server_concur 4009 4 10 ANY &

We tested the code on port 4009 (This is the port that test.sh is running)
Also, in this implementation the policy for HTML nor image preference is not working, therefore we used ANY, which just uses the FIFO policy to test it out 

Run the test.sh which sends requests:
chmod +x test.sh 
./test.sh 


______________________

To run server_stats.c:
./server_stats.c 4009 4 10 ANY &

We tested the code on port 4009 (This is the port that test.sh is running)
In this implemntion, the times will be off but the rest of the statistics will run correctly, including preference and count of types of submissions 

Run the test.sh which sends requests:
chmod +x test.sh 
./test.sh 

In order to properly test this code we used the log file that was generated after each run to clearly see the statistics of the threads and how they implemented the http requests. In the example below it is clear which thread we are working with and the 
type of http request it had processed. We can use this information to analyze this thread at different points of the log to see how it changed based on new http requests to ensure everything is functioning properly. In terms of this example copied below, we 
can see that the content type matches up with the image count, and based on other thread times it is clear that they were processed concurrently since their times for dispatch, arrival and complete overlap with one another. Also, this thread says it has complete count 4, and it was the 4th thread printed. Overall the log file provided to be very useful in terms of properly determine which parts of the code needed to be fixed and allows for a global view for how the threads are behaving. 

 INFO: request:GET /nigel.jpg HTTP/1.0****:4
 INFO: SEND:nigel.jpg:4
 INFO: Header:HTTP/1.1 200 OK
Server: nweb/23.0
Content-Length: 10184
Connection: close
Content-Type: image/jpg
 Thread Stat:
 Thread ID: 3
 Thread Count: 1
 Thread HTML Count: 0
 Thread IMAGE count: 1 
 Arrival Count: 5
 Arrival Time: 221.000000 
 Dispatch Count: 6 
 Dispatch Time: 246.000000 
 Complete Count: 4 
 Complete Time: 816.000000 
 Age: 0 


Client Implementation Write Up- Yoni Lazarus 

Multi-Threaded client
Design: main() parses CL arguments and creates a collection of N threads with run either concurrently or in FIFO policy, as specified by the input.

Specification: used thread barrier and mutex un/locking. If FIFO policy is selected, then mutex locking is used so that threads can wait for each other. If user chooses to run concurrently, mutex locking is not used by definition. 

Testing: to test the client, I put print statements specifying the time and ID of the thread being run in the start_thread function.
