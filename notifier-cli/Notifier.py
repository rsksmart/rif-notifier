import argparse
import os
import subprocess
import shutil
import json
from Config import Config
import requests


class Notifier:

    def __init__(self, docker=False):
        self.config = Config(docker)
        self.mvn = "mvn"
        if self.config.hasProperty("serverport"):
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

    def checkConfig(self, docker=False):
        ret = True
        missing = []
        for x in Config.PROPS if not docker else Config.DOCKERPROPS:
            if not self.config.hasProperty(x):
                ret = False
                missing.append(x)
        if not ret:
            print(",".join(missing) + " not configured.\n See 'notifier-cli " + ("dockerconfigure" if docker else "configure") + " --help'")
        return ret

    def checkDocker(self):
        if not shutil.which("docker"):
            print("Docker is not installed. Please download from https://www.oracle.com/java/technologies/javase-jre8-downloads.html")
            return False
        return self.checkConfig(True)

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
                return json.dumps(response.json(), indent=4)
            else:
                print("Failed to get subscription plans. ", response)
        except requests.exceptions.RequestException as err:
            print ("Error getting subscription plans. Is the server running?",err)

    def createDocker(self, plan):
        if self.checkDocker():
            try:
                os.system('docker cp ' + plan.planLocation + ' rif-notifier:/home/rif-user/rif-notifier/')
                os.system('docker exec rif-notifier /home/rif-user/sp-docker.sh loadSubscriptionPlan')
                plan.remove()
            except Exception as e:
                print("failed to create subscription plans.")

    def disableDocker(self, plan):
        if self.checkDocker():
            try:
                os.system('docker exec rif-notifier /home/rif-user/sp-docker.sh disableSubscriptionPlan')
            except Exception as e:
                print("failed to disable subscription plan.")

    def create(self, plan):
        if self.check():
            try:
                args = '-Dspring-boot.run.arguments=loadSubscriptionPlan -Dspring-boot.run.jvmArguments="-Dserverport=8180 '
                for x in self.config.configJson:
                    if x != "serverport":
                        args= args + "-D" + x + "=" + str(self.config.configJson[x]) + " "
                args = args + '"'
                os.system(self.mvn + " spring-boot:run " + args)
                plan.remove()
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
                print("failed to disable subscription plan.")

    def dockerStop(self):
        try:
            ret = os.system("docker ps | grep rif-notifier")
            if ret != 0:
                print('rif-notifier container is not running')
            else:
                os.system("docker-compose stop")
        except Exception as e:
            print("Error while stopping rif-notifier container docker instance")

    def dockerStart(self):
        if self.checkDocker():
            try:
                ret = os.system("docker ps | grep rif-notifier")
                if ret == 0:
                    print('rif-notifier is already running')
                    return
                ret = os.system("docker ps -a | grep rif-notifier")
                if ret != 0:
                    print('rif-notifier container does not exist. run notifier-cli dockerbuild')
                    return
                os.system("docker-compose start")
            except Exception as e:
                print(e)
                print("failed to start notifier docker instance")

    def dockerBuild(self):
        if self.checkDocker():
            try:
                ret = os.system("docker ps | grep rif-notifier")
                if ret == 0:
                    print('rif-notifier is already running')
                    return
                ret = os.system("docker ps -a | grep rif-notifier")
                if ret == 0:
                    print('rif-notifier container already exists . run notifier-cli dockerstart')
                    return
                os.system("docker-compose up --build")
            except Exception as e:
                print(e)
                print("failed to start notifier docker instance")