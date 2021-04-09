import json
import requests
import click
from urllib.parse import urljoin
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


    def subscribe(self, planid, currency, price, apikey):
        if not self.checkConfig(["notifierurl", "useraddress"]):
            return
        subscription = {}
        subscription["subscriptionPlanId"] = planid
        subscription["price"] = price
        subscription["currency"] = currency
        subscription["userAddress"] = self.config.get("useraddress")
        topics = self.addTopics()
        if len(topics) == 0:
            print("atleast one topic must be added for the subscription")
            return
        subscription["topics"] = topics
        subscriptionJson = json.dumps(subscription, indent=4)
        print(subscriptionJson)
        confirm = click.confirm("Do you want to subscribe with the above json?")
        if confirm:
            self.subscribeToPlan(subscriptionJson)

    def subscribeToPlan(self, subscriptionJson):
        headers = {'Content-Type': 'application/json'}
        subscribeurl = urljoin(self.config.get("notifierurl"), "/subscribeToPlan")
        response = requests.post(subscribeurl, headers=headers, data=subscriptionJson)
        subscriptionResponse = response.json()
        if response.status_code == 200:
            print(json.dumps(subscriptionResponse, indent=4))
            if "content" in subscriptionResponse and "subscription" in subscriptionResponse["content"] and "apiKey" in subscriptionResponse["content"]["subscription"]:
                apiKey = subscriptionResponse["content"]["subscription"]["apiKey"]
                self.config.set("apikey", apiKey)
                self.config.configWrite()
        else:
            print("Error while subscribing " + json.dumps(subscriptionResponse, indent=4))


    def addNotificationPreferences(self):
        notificationPreferences = []
        while(True):
            notificationPreference = {}
            notificationService = click.prompt("Enter Notification Service", type=click.Choice(['API', 'EMAIL', 'SMS']))
            notificationPreference["notificationService"] = notificationService
            notificationPreference["destination"] = click.prompt("Enter " + notificationService +" destination")
            if(notificationService == 'API'):
                destinationParam = {}
                destinationParam["username"] = click.prompt("Enter username", default="")
                destinationParam["password"] = click.prompt("Enter password", default="")
                destinationParam["apikey"] = click.prompt("Enter apikey", default="")
                notificationPreference["destinationParams"]=destinationParam
            notificationPreferences.append(notificationPreference)
            confirm = click.confirm("Do you want to add another Notification preference for this topic?")
            if not confirm: break
        return notificationPreferences


    def addTopics(self):
        topics = []
        while True:
            topic = {}
            topic["type"] = click.prompt("Enter Topic Type", type=click.Choice(['NEW_BLOCK', 'NEW_TRANSACTIONS', 'CONTRACT_EVENT']))
            if(topic["type"] == 'CONTRACT_EVENT'):
               topic["topicParams"] = self.addTopicParams()
            topic["notificationPreferences"] = self.addNotificationPreferences()
            topics.append(topic)
            confirm = click.confirm("Do you want to add another topic for this subscription?")
            if not confirm: break
        return topics

    def addTopicParams(self):
        topicParams = []
        topicParam = {}
        topicParam["type"] = "CONTRACT_ADDRESS"
        topicParam["value"] = click.prompt("Enter contract address")
        topicParams.append(topicParam)
        topicParam = {}
        topicParam["type"] = "EVENT_NAME"
        topicParam["value"] = click.prompt("Enter event name")
        topicParams.append(topicParam)
        confirm = click.confirm("Do you want to add event parameters?")
        if confirm: self.addAdditionalTopicParams(topicParams)
        return topicParams

    def addAdditionalTopicParams(self, topicParams):
        while True:
            topicParam = {}
            topicParam["type"] = "EVENT_PARAM"
            topicParam["value"] = click.prompt("Enter Parameter Name")
            topicParam["order"] = click.prompt("Enter Order", type=int)
            topicParam["valueType"] = click.prompt("Enter Parameter Value Type (ex. Utf8String)")
            topicParam["indexed"] = 1 if click.prompt("Parameter is indexed?", type=click.BOOL, default=False) else 0
            filter = click.prompt("Enter Filter", default="")
            if len(filter.strip()) > 0:
                topicParam["filter"] = filter
            topicParams.append(topicParam)
            confirm = click.confirm("Do you want to add another parameter for this event?")
            if not confirm: break

