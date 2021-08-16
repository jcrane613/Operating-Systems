COUNT=0
while [ $COUNT -lt 3 ]
  do
    ECHO $COUNT
    ./client localhost 4009 /index.html &
    ./client localhost 4009 /nigel.jpg &
    ((COUNT+=1))
  done