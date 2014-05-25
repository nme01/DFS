#!/bin/bash
#http://notes.asd.me.uk/2011/01/04/iterating-over-a-range-of-ip-addresses-in-bash/


function ip_to_int()
{
  local IP="$1"
  local A=`echo $IP | cut -d. -f1`
  local B=`echo $IP | cut -d. -f2`
  local C=`echo $IP | cut -d. -f3`
  local D=`echo $IP | cut -d. -f4`
  local INT

  INT=`expr 256 "*" 256 "*" 256 "*" $A`
  INT=`expr 256 "*" 256 "*" $B + $INT`
  INT=`expr 256 "*" $C + $INT`
  INT=`expr $D + $INT`

  echo $INT
}


function int_to_ip()
{
  local INT="$1"

  local D=`expr $INT % 256`
  local C=`expr '(' $INT - $D ')' / 256 % 256`
  local B=`expr '(' $INT - $C - $D ')' / 65536 % 256`
  local A=`expr '(' $INT - $B - $C - $D ')' / 16777216 % 256`

  echo "$A.$B.$C.$D"
}


S=`ip_to_int $1`
E=`ip_to_int $2`

for INT in `seq $S 1 $E`
do
  IP=`int_to_ip $INT`
  echo $IP
done


