import argparse
import os
import subprocess
import shutil
import json
from Config import Config
import requests


class Notifier:

    def __init__(self):
        self.config = Config()
        self.mvn = "mvn"
        self.host = "http://127.0.0.1:" + str(self.config.get("serverport"))

    def check(self):
        if not shutil.which("java"):
            print("Java is not installed. Please download from https://www.oracle.com/java/technologies/javase-jre8-downloads.html")
            return False
        if not shutil.which("mvn"):
            self.mvn = os.path.expandvars("$M2_HOME") + "/bin/mvn"
            if not os.path.exists(self.mvn):
                print ("Maven is not installed or M2_HOME environment variable not set. Please download from link http://maven.apache.org/download.cgi and set the M2_HOME env variable")
                return False
        return self.checkConfig()

    def checkConfig(self):
        ret = True
        missing = []
        for x in Config.PROPS:
            if not self.config.hasProperty(x):
                ret = False
                missing.append(x)
        if not ret:
            print(",".join(missing) + " not configured. please use 'configure' option to set the property. see configure --help")
        return ret


    def start(self):
        if os.path.isfile("application.pid"):
            print('server is already running')
        else:
            self.startStop('run')

    def stop(self):
        try:
            pid = None
            if os.path.isfile("application.pid"):
                with open("application.pid", "r") as f:
                    pid = f.read()
                resp = subprocess.run(['kill', pid])
                print("server stopped successfully " if resp.returncode==0 else "could not gracefully stop the server")
            else:
                print("server is not running or run notifier-cli from home directory of rif-notifier")
        except Exception as e:
            print("Error stopping application ", e)

    def startStop(self, cmd):
        if self.check():
            try:
                args = '-Dspring-boot.run.jvmArguments="'
                for x in self.config.configJson:
                    args= args + "-D" + x + "=" + str(self.config.configJson[x]) + " "
                args = args + '"'
                os.system(self.mvn + " spring-boot:" +cmd + " " + args)
            except Exception as e:
                print(e)
                print("failed to start notifier")

    def healthCheck(self):
        try:
            response = requests.get(self.host + "/actuator/health", verify=False)
            if response.status_code == 200:
                print("Server is running ", response)
            else:
                print("Server health check failed. ", response.json())
        except requests.exceptions.RequestException as err:
            print ("Health check error. ",err)

    def listSubscriptionPlans(self):
        print(self.getSubscriptionPlans())

    def getSubscriptionPlan(self, id):
        try:
            response = requests.get(self.host + "/getSubscriptionPlan/"+id, verify=False)
            if response.status_code == 200:
                return response.json()
            else:
                print("Failed to get subscription plan. ", response)
        except requests.exceptions.RequestException as err:
            print ("Error getting subscription plan. Is the server running?",err)

    def getSubscriptionPlans(self):
        try:
            response = requests.get(self.host +"/getSubscriptionPlans", verify=False)
            if response.status_code == 200:
                return response.json()
            else:
                print("Failed to get subscription plans. ", response)
        except requests.exceptions.RequestException as err:
            print ("Error getting subscription plans. Is the server running?",err)

    def create(self):
        if self.check():
            try:
                args = '-Dspring-boot.run.arguments=loadSubscriptionPlan -Dspring-boot.run.jvmArguments="-Dserverport=8180 '
                for x in self.config.configJson:
                    if x != "serverport":
                        args= args + "-D" + x + "=" + str(self.config.configJson[x]) + " "
                args = args + '"'
                os.system(self.mvn + " spring-boot:run " + args)
            except Exception as e:
                print("failed to create subscription plans.")

    def disable(self, id):
        if self.check():
            try:
                args = '-Dspring-boot.run.arguments="disableSubscriptionPlan,' + id + '" -Dspring-boot.run.jvmArguments="-Dserverport=8180 '
                for x in self.config.configJson:
                    if x != "serverport":
                        args= args + "-D" + x + "=" + str(self.config.configJson[x]) + " "
                args = args + '"'
                os.system(self.mvn + " spring-boot:run " + args)
            except Exception as e:
                print("failed to create subscription plans.")

