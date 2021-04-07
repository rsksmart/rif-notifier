import json
import os

from pathlib import Path

class Config:
    PROPS = ["serverport", "dbhost", "dbname", "dbuser", "dbpassword", "rskendpoint", "blockconfirmationcount", "notificationmanagercontract",
             "tokennetworkregistry", "multichaincontract", "provideraddress", "providerprivatekey", "notificationpreferences", "acceptedcurrencies"]

    DOCKERPROPS = ["dbpassword", "rskendpoint", "blockconfirmationcount", "notificationmanagercontract", "tokennetworkregistry",
                   "multichaincontract", "provideraddress", "providerprivatekey", "notificationpreferences", "acceptedcurrencies"]

    def __init__(self, dockerConfig=False):
        self.configJson = {}
        self.dockerConfig = dockerConfig
        self.configLocation = os.path.join(os.getcwd() , self.configFile()) if dockerConfig else str(Path.home()) + "/" + self.configFile()
        self.configRead()

    def set(self, key, value):
        self.configJson[key] = value

    def get(self, key):
        return self.configJson[key]

    def configFile(self):
        return "rif-notifier/config.json" if not self.dockerConfig else "config-docker.json"

    def configRead(self):
        try:
            with open(self.configLocation, "r", encoding='utf-8') as f:
                self.configJson = json.load(f)
        except:
            pass

    def hasProperty(self, key):
        hasProp = key in self.configJson
        return hasProp and str(self.get(key)).strip() != ""

    def configWrite(self):
        try:
            os.makedirs(os.path.dirname(self.configLocation), exist_ok=True)
            with open(self.configLocation, "w", encoding='utf-8') as f:
                json.dump(self.configJson, f, indent=4)
        except Exception as e:
            print(str(e))
            try:
                self.configLocation = self.configFile()
                os.makedirs(os.path.dirname(self.configLocation), exist_ok=True)
                with open(self.configLocation, "w") as f:
                    json.dump(self.configJson, f, indent=4)
            except Exception as e1:
                print(str(e1))