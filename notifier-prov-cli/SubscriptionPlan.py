import json
import os
from pathlib import Path


class SubscriptionPlan:

    def __init__(self, delete=True):
        self.subscriptionPlans = []
        self.subscriptionPlan = {}
        self.planLocation = str(Path.home()) + "/" + self.planFile()
        self.delete = delete
        #self.planRead()

    def addPrice(self, price, currency, address):
        priceList = self.subscriptionPlan["subscriptionPriceList"] if self.hasProperty("subscriptionPriceList") else []
        subscriptionPrice = {}
        subscriptionPrice["price"] = price
        subscriptionPrice["currency"]= {}
        subscriptionPrice["currency"]["name"] = currency
        subscriptionPrice["currency"]["address"] = address
        priceList.append(subscriptionPrice)
        self.subscriptionPlan["subscriptionPriceList"] = priceList

    def set(self, key, value):
        self.subscriptionPlan[key] = value

    def get(self, key):
        return self.subscriptionPlan[key]

    def planFile(self):
        return "rif-notifier/subscription-plan.json"

    def planRead(self):
        try:
            with open(self.planLocation, "r", encoding='utf-8') as f:
                self.subscriptionPlans = json.load(f)
        except:
            pass

    def hasProperty(self, key):
        return key in self.subscriptionPlan

    def add(self):
        self.subscriptionPlan["status"] = True
        self.subscriptionPlans.append(self.subscriptionPlan)

    def planWrite(self):
        try:
            os.makedirs(os.path.dirname(self.planLocation), exist_ok=True)
            with open(self.planLocation, "w", encoding='utf-8') as f:
                json.dump(self.subscriptionPlans, f, indent=4)
        except Exception as e:
            try:
                self.planLocation = self.planFile()
                os.makedirs(os.path.dirname(self.planLocation), exist_ok=True)
                with open(self.planLocation, "w") as f:
                    json.dump(self.subscriptionPlans, f, indent=4)
            except Exception as e1:
                print(str(e1))

    def remove(self):
        if self.delete:
            os.remove(self.planLocation)

