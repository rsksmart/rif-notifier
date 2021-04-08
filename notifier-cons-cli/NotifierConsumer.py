import json
import requests
from Config import Config


class NotifierConsumer:

    def __init__(self):
        self.config = Config()
        if self.config.hasProperty("notifierurl"):
            self.host = self.config.get("notifierurl")

    def checkConfig(self, props):
        ret = True
        missing = []
        for x in Config.PROPS if not props else props:
            if not self.config.hasProperty(x):
                ret = False
                missing.append(x)
        if not ret:
            print(",".join(missing) + " not configured.\n See 'notifier-cons-cli  configure --help'")
        return ret

    def listSubscriptionPlans(self):
        print(self.getSubscriptionPlans())

    def getSubscriptionPlans(self):
        if self.checkConfig(["notifierurl"]):
            try:
                response = requests.get(self.host + "/getSubscriptionPlans", verify=False)
                if response.status_code == 200:
                    return response.json()
                else:
                    print("Failed to get subscription plans. ", response)
            except requests.exceptions.RequestException as err:
                print ("Error getting subscription plans. Is the server running?",err)

