#!/bin/bash

sudo mvn clean install -DskipTests
sudo docker build -t gcr.io/prj-ptf-svc-dmz-dev/ptf-mobile-authservice .
sudo docker push gcr.io/prj-ptf-svc-dmz-dev/ptf-mobile-authservice