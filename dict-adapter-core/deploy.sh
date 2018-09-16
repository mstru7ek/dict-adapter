#!/bin/bash

echo ""
echo "Start deploy..."
echo ""

echo "---------------------------------------------------------------------"
echo " > Pre service status"
ssh root@vps512867.ovh.net systemctl status dict-adapter-core


echo " > Stop service"
ssh root@vps512867.ovh.net systemctl stop dict-adapter-core

sleep 2

echo " > Copy artifact"
scp build/libs/dict-adapter-core-1.0-SNAPSHOT.jar root@vps512867.ovh.net:/opt/dict-adapter-core


echo " > Start service"
ssh root@vps512867.ovh.net systemctl start dict-adapter-core

sleep 5

echo " > Post service status"
ssh root@vps512867.ovh.net systemctl status dict-adapter-core

echo "---------------------------------------------------------------------"
echo ""
echo "Done"
echo ""
