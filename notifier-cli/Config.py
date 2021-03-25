import json
import os

from pathlib import Path

class Config:
    PROPS = ["serverport", "dbhost", "dbname", "dbuser", "dbpassword", "rskendpoint", "blockconfirmationcount", "notificationmanagercontract",
             "tokennetworkregistry", "multichaincontract", "provideraddress", "notificationpreferences", "acceptedcurrencies"]
    def __init__(self):
        self.configJson = {}
        self.configLocation = str(Path.home()) + "/" + self.configFile()
        self.configRead()

    def set(self, key, value):
        self.configJson[key] = value

    def get(self, key):
        return self.configJson[key]

    def configFile(self):
        return "rif-notifier/config.json"

    def configRead(self):
        try:
            with open(self.configLocation, "r", encoding='utf-8') as f:
                self.configJson = json.load(f)
        except:
            pass

    def hasProperty(self, key):
        return key in self.configJson

    def configWrite(self):
        try:
            os.makedirs(os.path.dirname(self.configLocation), exist_ok=True)
            with open(self.configLocation, "w", encoding='utf-8') as f:
                json.dump(self.configJson, f)
        except Exception as e:
            print(str(e))
            try:
                self.configLocation = self.configFile()
                os.makedirs(os.path.dirname(self.configLocation), exist_ok=True)
                with open(self.configLocation, "w") as f:
                    json.dump(self.configJson, f)
            except Exception as e1:
                print(str(e1))