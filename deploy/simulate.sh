#!/bin/bash
./service.sh stop
./service.sh start 10000

./service.sh status
masterip=192.168.65.130
echo $masterip
read
echo "ls" | ./runClientHere $masterip
echo "put file1.c file1.c" | ./runClientHere $masterip
echo "put file2.c file2.c" | ./runClientHere $masterip
echo "ls" | ./runClientHere $masterip
read
ssh ubuntu@192.168.65.133 "nohup sudo bash -c 'ifconfig eth0 down; exit'"
./service.sh status
read
echo "ls" | ./runClientHere $masterip
read
echo "get file1.c file1.c" | ./runClientHere $masterip
echo "rm file2.c" | ./runClientHere $masterip
echo "get file2.c file2.c" | ./runClientHere $masterip
echo "ls" |  ./runClientHere $masterip
read
ssh ubuntu@$masterip "nohup sudo bash -c 'ifconfig eth0 down; exit'"
sleep 11
./service.sh status
echo "put file3.c file3.c" | ./runClientHere $masterip
echo "put file4.c file4.c" | ./runClientHere $masterip
echo "put file5.c file5.c" | ./runClientHere $masterip
echo "ls" | ./runClientHere $masterip
