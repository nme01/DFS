#!/bin/bash
#
# Script running the server.
# 

source ../config.properties

uphosts=0
masterip=0

function startService()
{
    ip=$1
    echo "Trying $ip ..."
    if [[ $uphosts > 0 ]]; then
        ./runSlave $ip $masterip &
        pid=$!
        sleep 1
        ./runCheckServer $ip
        result=$?
        echo "slave server result is $result"
        if [[ $result == 0 ]]; then
	   #we've got a new host
           uphosts=$(($uphosts+1))
           echo "Slave $ip is ready to serve $uphosts"
        else
           echo "Server $ip failed to init"
           disown $pid
           kill -9 $pid &>/dev/null
        fi
        
    else
        ./runMaster $ip &
        pid=$!
        sleep 1
        ./runCheckServer $ip
        result=$?
        echo "result is $result"
        if [[ $result == 1 ]]; then
            #it's an error, kill ssh
            echo "Server $ip failed to init"
            disown $pid
            kill -9 $pid &>/dev/null
        else
	    #it's a new master!
            masterip=$ip
            uphosts=$(($uphosts+1))
	    echo "Master $ip is ready to serve $uphosts"
        fi
    fi
}

if [[ $1 == 'start' ]]
then

uphosts=0

for i in {1..9}; do
   varName=server0$i
   value=${!varName}
   if [[ -n $value ]]; then
      startr=`echo $value | awk -F"-" '{print $1}'`
      endr=`echo $value | awk -F"-" '{print $2}'`
      if [[ ! '' == $endr ]]; then
          #it's ip range
          for ip in `./iprange.sh $startr $endr`; do
             startService $ip
          done
      else
          #it's plain ip
          ip=$startr
          startService $ip
      fi
   fi
done

fi
