# Rif-Notifier 
RIF Notifier is a service that listens to events on the blockchain and notifies its users of the blockchain events using one of the supported notification methods (API/SMS/EMAIL)

## Indexes

1. [Quick start](#quick-start) 
2. [Docker Installation](#installation-guide)
   1. [Prerequisites for docker installation](#prerequisites-for-docker-installation)
   2. [Docker Installation](#docker-installation-steps)
3. [Local Installation](#installation-guide)
   1. [Prerequisites for local installation](#prerequisites-for-local-installation)
   2. [Local Installation](#local-installation-steps)
4. [Update](#update)
5. [Usage Guide](#usage-guide)   
    1. [Preconditions](#preconditions)
    2. [Notifier Provider CLI](#notifier-provider-cli)
    3. [Notifier Consumer CLI](#notifier-consumer-cli)
    4. [Start the application](#start-the-application)
    5. [Create Subscription Plan](#create-subscription-plan)
    6. [Update Subscription Plan](#update-subscription-plan)
    7. [Disable Subscription Plan](#disable-subscription-plan)
    8. [Subscribe to Plan](#subscribe-to-plan)
    9. [Renew Subscription](#renew-subscription)
    10. [Subscription and Renewal Response](#subscription-and-renewal-response)
6. [Other available endpoints](#other-available-endpoints)   
	1. [Retrieve notifications](#getting-notifications)
	2. [Get subscriptions](#get-subscriptions)
	3. [Get Lumino tokens](#get-lumino-tokens)
	4. [Subscribe to specific open channel](#subscribe-to-specific-open-channel)
	5. [Subscribe to close channel](#subscribe-to-close-channel)
	6. [Subscribe to all open channels](#subscribe-to-all-lumino-open-channels)
	7. [Get chain addresses events](#get-rns-events)
7. [Health Check](#health-check)
8. [Advanced Usage Guide](#advanced-usage-guide)


## Quick Start

(This steps you can follow if you're already familiar with the notifier, otherwise jump to the [Installation](#installation-guide) guide first)

-Configure Notifier Provider CLI, if not already configured, by following the steps in https://github.com/rsksmart/rif-notifier/tree/master/notifier-prov-cli

-Run the command `notifier-prov-cli start` to start the rif-notifier local instance or `notifier-prov-cli dockerstart` if you are running rif-notifier as docker container.

-Ensure that the blockchain endpoint property ```rskendpoint``` is correctly set (for ex. http://localhost:4444 for local instance or http://regtest:4444 for docker instance)

-To view all endpoints use http://localhost:8080/swagger-ui.html

-To subscribe or renew a plan use `notifier-cons-cli subscribe` or `notifier-cons-cli renew`

-We use mysql for DB, setup notifier db connectivity using `notifier-prov-cli configure` for local instance, or `notifier-prov-cli dockerconfigure` for docker instance.

-DB Schema and database rif_notifier will be generated if it does not exist, provided the mysql user has enough permissions.

-Get started with the following steps for a fresh install

## Installation guide
RIF Notifier can be installed to run locally or in a docker container. Local installation can be performed by following the steps in [Local Installation](#local-installation-steps). To run rif-notifier in a docker container, follow the instructions in [Docker Installation Steps](#docker-installation-steps).
## Setup

### Prerequisites for docker installation
#### 1. Docker
Make sure you have docker installed in your machine. If not, download from https://www.docker.com/products/docker-desktop
#### 2. git
The latest version of the `git` client can be installed through:
```shell
sudo apt update
sudo apt install git
```

### Docker Installation Steps

* Ensure the [prerequisites for docker installation](#prerequisites-for-docker-installation) are met.
  
* Open a terminal.

* Clone this repo using `git clone https://github.com/rsksmart/rif-notifier rif-notifier` and switch to the rif-notifier directory by using command ```cd rif-notifier```
  
* Install notifier provider cli by following steps in [notifier provider cli](#notifier-provider-cli)

* Copy `config-docker-template.json` to `config-docker.json`
  
* Set the `dbpassword` environment variable in your machine. This password is used by `notifier_user` user in mysql docker container when it is created. 
  
* Set the same database password to rif-notifier docker configuration by running command `notifier-prov-cli dockerconfigure --dbpassword`.
  
* Run the command `notifier-prov-cli dockerconfigure` to configure other required properties to run RIF notifier docker instance.

* Run the command ```notifier-prov-cli dockerbuild```  This command will build the mysql, and rif-notifier docker images and run it. Wait for the server to start until you see message started application in x(seconds).

* Run the command ```notifier-prov-cli create subscriptionplan --docker``` to create subscription plans.

* Once the containers are fully running, test it by using following command ```curl -k http://localhost:8080/getSubscriptionPlans```. And that's it, congrats! you should see a response with the json of subscription plans.

* Subsequently to stop and start the docker containers use ```notifier-prov-cli dockerstop``` and ```notifier-prov-cli dockerstart```


### Prerequisites for local installation
#### 1. git
The latest version of the `git` client can be installed through:

```shell
sudo apt update
sudo apt install git
```
#### 2. RSK Blockchain (Mainnet)
The next requirement is an RSK node which can be run using the **JAR file** method. Use the latest RSKj version avaiable and have it sync with mainnet.

This node should be accessible through `localhost:4444`. For more information on how to achieve this, please consult the [_Setup node on Java_ section on the Developer Portal](https://developers.rsk.co/rsk/node/install/java/).


#### 3. MySQL
The latest version of the `mysql-server` database can be installed through:

```shell
sudo apt update
sudo apt install mysql-server
```

Then, run the installer by executing:

```shell
sudo mysql_secure_installation
```

This will take you through a series of prompts where you can configure your MySQL installation.

##### 3.1 Verification
You can verify the MySQL service is running by executing:

```shell
sudo service mysql status
```

You can verify the port used by the MySQL is of the expected value `3306` by executing:

```shell
sudo netstat -tlnp | grep mysql
```

`netstat` can be installed through `sudo apt install net-tools`.

#### 4. Maven
The latest version of maven can be installed through:

```shell
sudo apt install maven
```



### Local Installation Steps

The steps in this section will help you configure mysql database in local environment.

1. Ensure [prerequisites for local installation](#prerequisites-for-local-installation) are met.
   
2. Run the script bin/install.sh with 3 parameters as below, this will create the mysql user notifier_user with required permissions
```
bin/install.sh <database root user> <database root password> <notifier_user password>
```
The third parameter is the password for user ```notifier_user``` which will be created by this script



---

## Update
To update an already installed RIF Notifier follow these steps:
1. Stop the RIF Notifier process.
2. Navigate to the `rif-notifier` directory and pull the latest code by executing `git pull`. The `master` branch should still be used.
3. Start the RIF Notifier by running the command `notifier-prov-cli start`.


## Usage guide
### Preconditions

RIF Notifier uses `eth_getLogs` rpc to get the information about events, therefore the RSK node must respond in a reasonable
timeframe (< 30s)

Since the `eth_getLogs` result is cached, it will take a long time for this call to finish the first time it is executed after the RSK node is started. This will happen each time the RSK node boots.
After this, each call should be finished in a reasonable time.

Use this curl to test the `eth_getLogs` response:
```
curl -X POST http://localhost:4444 -H 'Content-Type: application/json' -d '{"jsonrpc":"2.0","method":"eth_getLogs","params":[{"address":"0xde2D53e8d0E673A4b1D9054cE83091777F2fd8Ce","fromBlock":"0x0","toBlock":"latest"}],"id":74}'
```

## Notifier Provider CLI

Notifier Provider CLI is a command line tool to configure and run the rif-notifier. 

To install and use notifier provider cli see https://github.com/rsksmart/rif-notifier/tree/master/notifier-prov-cli

## Notifier Consumer CLI

Notifier Consumer CLI is a command line tool for RIF-Notifier consumers to perform operations like subscribe to a plan, renew a subscription, and list user subscriptions.

To install and use notifier consumer cli see https://github.com/rsksmart/rif-notifier/tree/master/notifier-cons-cli 

### Start the application

Use ```notifier-prov-cli start``` to start the rif notifier locally, or ```notifier-prov-cli dockerstart``` to start rif notifier  docker instance


#### **Create Subscription Plan**
Run the command ```notifier-prov-cli create subscriptionplan``` or ```notifier-prov-cli create subscriptionplan --docker``` to create subscription plan in local machine or docker container. 
#### **Update Subscription Plan**
Run the command ```notifier-prov-cli edit subscriptionplan``` or ```notifier-prov-cli edit subscriptionplan --docker``` to update a subscription plan in local machine or docker container.
#### Disable subscription plan
Run the command ```notifier-prov-cli disable subscriptionplan``` or ```notifier-prov-cli disable subscriptionplan --docker``` to disable a subscription plan in local machine or docker container. 


### **Subscribe to Plan**
Run the command ```notifier-cons-cli subscribe``` to subscribe to a plan

### **Renew Subscription**
Run the command ```notifier-cons-cli renew``` to renew existing subscription

### **Subscription and Renewal response**
As part of the subscription and renewal response a ```hash``` of the subscription along with the ```signature``` is returned. The ```hash``` can be used to identify a subscription. 

**Api key**  
An api key is also generated as part of the response, which can be used to perform [get subscription info](#get-subscription-info) and [get notifications](#getting-notifications) operations

-------------------

###### Getting notifications

When you're subscribed to topics, and a event is triggered the notifier will be processing the data, and saving it so you can access to that.

```
GET Request: http://localhost:8080/getNotifications
Header param: 
	key: apiKey
	value: API_KEY 
Query params: 
	idTopic [Optional]: The notifications will be filtered with this param, so it brings only the idTopics associated with each, you can send lots of ids, separating each with commas: 12,15,21
	fromId [Optional]: Each notification has an id, you can make a greater than by providing this param
	lastRows [Optional]: With this param you can set how many notifications will the notifier return. MAX is setted in applications.properties at 1000, so this number need to less than that
```



###### Other available endpoints

----------------
###### Get Subscriptions
```
GET Request: http://localhost:8080/getSubscriptions
 or
GET Request: http://localhost:8080/getSubscriptions/hash1,hash2...
Header param: 
    Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
	Path param:
	value: SUBSCRIPTION_HASH(s) separated by comma
Short description: Gets all the subscriptions or subscriptions for provided hashes. In case the `apikey` header parameter is not provided, public data will be returned. More detailed subscription info will be returned for users with valid api key. 
```
Return example:
```json
{
    "message": "OK",
    "content": "SEE JSON BELOW",
    "status": "OK"
}
```
```json
 {
  "id": 4,
  "activeSince": null,
  "status": "PENDING",
  "subscriptionPlan": {
    "id": 1,
    "name": "rif-10k",
    "validity": 3,
    "planStatus": "ACTIVE",
    "notificationPreferences": [
      "API"
    ],
    "notificationQuantity": 100,
    "subscriptionPriceList": [
      {
        "price": "10",
        "currency": {
          "name": "RIF",
          "address": {
            "value": "0x0000000000000000000000000000000000000000",
            "typeAsString": "address"
          }
        }
      }
    ]
  },
  "price": 10,
  "currency": {
    "name": "RIF",
    "address": {
      "value": "0x0000000000000000000000000000000000000000",
      "typeAsString": "address"
    }
  },
  "previousSubscription": null,
  "hash": "0x2cf5eed6b54477b51a6468357e4efbf102002cdc1b25077553a502b617e62aab",
  "expirationDate": "2021-04-15T18:30:00.000+00:00",
  "lastUpdated": "2021-04-13T10:11:04.000+00:00",
  "userAddress": "0x0",
  "topics": [
    {
      "id": 10,
      "type": "NEW_BLOCK",
      "hash": "-1799345841"
    }
  ],
  "notificationPreferences": [
    {
      "id": 1,
      "notificationService": "API",
      "destination": "http://localhost:9000",
      "destinationParams": {
        "username": "",
        "password": "",
        "apiKey": null
      },
      "idTopic": 10
    }
  ],
  "notificationBalance": 100,
  "subscriptionPayments": [],
  "active": false,
  "pending": true,
  "paid": false
}

```
----------------
###### Get lumino tokens
```
GET Request: http://localhost:8080/getLuminoTokens
Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
Short description: Brings an array of Token Network Address for the tokens registered in the blockchain, it can be used in other endpoints to subscribe to OpenChannels for the token or Close Channel events.
```
Return example:
```json
{
    "message": "OK",
    "data": ["0x386d436aAaDB7a14904B754695A9a79d3E1D521E"],
    "status": "OK"
}
```
----------------
###### Subscribe to specific open channel
```
GET Request: http://localhost:8080/subscribeToOpenChannel
Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
Query params: 
	token [Required]: Token network id for the token that you want to listen to open channel events
	participantone [Optional]: Address participant 1 of the channel
	participanttwo [Optional]: Address participant 2 of the channel
Short description: The notifier will listen to the events for the specified token.
```
Return example:
```json
{
    "message": "OK",
    "data": "{\"topicId\": 0}",
    "status": "OK"
}
```
----------------
###### Subscribe to close channel
```
GET Request: http://localhost:8080/subscribeToCloseChannel
Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
Query params: 
	token [Required]: Token network id for the token that you want to listen to open channel events
	closingParticipant [Optional]: Address of the participant who closes the channel
	channelidentifier [Optional]: Id of the channel
Short description: Similar to subscribeToOpenChannel, but for close channel event.
```
Return example:
```json
{
    "message": "OK",
    "data": "{\"topicId\": 0}",
    "status": "OK"
}
```
----------------
###### Subscribe to all lumino open channels
```
GET Request: http://localhost:8080/subscribeToLuminoOpenChannels
Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
Query params: 
	closingParticipant [Optional]: Address of the participant who closes the channel
	channelidentifier [Optional]: Id of the channel
Short description: This endpoint subscribes you to all tokens, and returns an array of topic id, each topic will represent a event for each token. Also this endpoint accepts params for participantone and participanttwo, if sent, will filter all the topics.
```
Return example:
```json
{
    "message": "OK",
    "data": "{\"topicId\": 0}",
    "status": "OK"
}
```
----------------
###### Get RNS events
```
GET Request: http://localhost:8080/getRnsEvents
Header param: 
	key: userAddress
	value: USER_ADDRESS
	key: apiKey
	value: API_KEY 
Query params: 
	nodehash [Optional]: Hashed name of the owner of the chain address
	eventName [Optional]: "ChainAddrChanged" for all types of chain address or "AddrChanged" to RSK Chain addresses events
Short description: From this endpoint you can bring the events emmited by the chain addresses set.
```
Return example:
```json
{
    "message": "OK",
    "data": [
        {
            "id": 772,
            "nodehash": "0x5f0169581c8d8a20ced1dc6f11c6e72485191e6a6a8fcc833692b4b966c63ab5",
            "eventName": "ChainAddrChanged",
            "chain": "0x80000132",
            "address": "0xC9dB73F54D43479b1a67DB2284bCFed17b0A13c2",
            "rowhashcode": 762164261,
            "block": 9976
        },
	{
            "id": 921,
            "nodehash": "0x55343d1b4bf30f6cde61752100e8d7d4061b87424e1697697e9e8ac23970ef04",
            "eventName": "AddrChanged",
            "chain": "0x80000089",
            "address": "0xad12408d680504719756cc4969eec9f302335c44",
            "rowhashcode": 210602843,
            "block": 1087198
        }
    ],
    "status": "OK"
}
```

###### Verify blockchain events
All rsk events are stored under raw_data and notification tables, there should be data for any of the subscribed events.
Check notifications are being sent by verifying sent=1 in notification table, for the notification preference set. In case not sent check the notification_log table.

## Health Check
Health check provides a way to ensure that the rif-notifier service is
fully functional. The following url is used for health check
``` 
http://localhost:8080/actuator/health
```
A sample response is given below. status property in json is either UP or DOWN. The response json 
also provides more details on individual services. The status for each individual
service is UP, DOWN or disabled. When a notification service is not provided by the provider 
thru ```notifier.services.enabled``` configuration property, the status of the 
service is shown as disabled.
```
{
    "status": "UP",
    "details": {
        "mail": {
            "status": "disabled",
            "details": {
                "service": "service is not enabled in configuration"
            }
        },
        "RSK": {
            "status": "UP"
        },
        "SMS": {
            "status": "disabled",
            "details": {
                "service": "service is not enabled in configuration"
            }
        },
        "db": {
            "status": "UP",
            "details": {
                "database": "MySQL",
                "hello": 1
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 119824367616,
                "free": 21785624576,
                "threshold": 10485760
            }
        }
    }
}
```


## Advanced Usage Guide

The steps below are an alternative way to configure and run rif-notifier without the notifier-prov-cli or notifier-cons-cli. Follow these steps only when advanced setup is required or for troubleshooting prupose.

-For local instance, as an alternative to ```notifier-prov-cli```, the configuration file ```config.json``` can be edited to set correct values. See [config.json](#configjson) section to view details of each property.

-To subscribe or renew a plan use http://localhost:8080/subscribeToPlan endpoint and http://localhost:8080/renewSubscription

### Advanced Docker Configuration
1. Modify ```subscription-plan.json``` under src/main/resources to provide the subscription plan details. See [create subscription plans](#create-subscription-plans) to change or add new subscription plans. To use the example provided, leave the file unchanged.

2. Modify config-docker.json file to set the ```dbpasword, notificationmanagercontract, tokennetworkregistry, multichaincontract, provideraddress, providerprivatekey``` application properties.

### Advanced Installation Steps for Local Machine

**Note:** the steps in this section are an alternative way to configure mysql database in local environment, and can be skipped if you already followed the steps in [local Installation](#local-installation-steps) above. The

1. Ensure [prerequisites for local installation](#prerequisites-for-local-installation) are met.
2. Pick a directory for all the RIF Notifier code to reside in. From now on this will be called `rif-notifier`, but replace it with your own.
3. Clone the RIF Notifier git project into its directory doing:

```shell
git clone https://github.com/rsksmart/rif-notifier rif-notifier
```
Stay on the `master` branch.

. Create the RIF Notifier database and configure its access. This is done by first opening the MySQL prompt by executing:

```shell
sudo mysql
```

Then, pick a name for the RIF Notifier database to be used. From now on this will be called `rif_notifier`, but replace it with your own.

Create the schema by entering:

```mysql
CREATE DATABASE rif_notifier;
```

in the `mysql` prompt.

Now pick a username and password for the database to be accessed with. From now on these will be called `notifier_user` and `notifier_db_password`, but replace them with your own.

To have these set up in the MySQL database first do:

```mysql
CREATE USER 'notifier_user'@'localhost' IDENTIFIED BY 'notifier_db_password';
```

in the `mysql` prompt. Then grant this user all permissions on the schema by doing:

```mysql
GRANT ALL PRIVILEGES ON rif_notifier.* TO 'notifier_user'@'localhost';
```

and then exit the `mysql` terminal by entering `exit`.

Restart the MySQL service by executing:

```shell
sudo /etc/init.d/mysql restart
```
#### Setup notifier database password

Run the command ```notifier-prov-cli configure --dbpassword ```  to set the dbpassword property the password you just entered in previous steps for ```notifier_user_password```.

Navigate to [usage guide](#usage-guide) to learn more about how to run and use the application.

#### config.json

First modify the ```config.json``` file to setup the rsk blockchain and database properties. Note: the comments should be removed in the actual json. The below are example values for each json property.
```
{
	"serverport":"8080",  // server port to start the server on

	"dbhost":"localhost",  //database host name
	"dbname":"rif_notifier",  //database name
	"dbuser":"notifier_user",  //database user with privileges
	"dbpassword":"***********",  //database password

	"rskendpoint":"http://localhost:7545",  //rsk blockchain endpoint
	"blockconfirmationcount":"20",  // number of blocks to wait for confirmation
	"smartcontractaddress":"0xC2Cd3835d36510dc065d0f8991785DAA70a601a4",  // smart contract address for payments
	"tokennetworkregistry":"0xFEC354973ca22697BC5Cd1E7F372609574e2AfcA", 
	"multichaincontract":"0xFEC354973ca22697BC5Cd1E7F372609574e2AfcA",

	"provideraddress":"0x882bf23c4a7E73cA96AF14CACfA2CC006F6781A9",  // provider address
	"providerprivatekey":"***************************************", //provider privatekey without hex prefix

	"notificationpreferences":"API,EMAIL",  // supported notifications comma separated

	"acceptedcurrencies":"RIF,RBTC"  // supported currencies comma separated
}

To run the RIF Notifier start a terminal in `rif-notifier` directory and run:

```

### Run the application
Run the script ```bin/run.sh``` 

#### To create subscription plan manually
1. One or more subscription plans can be created by modifying subscription-plan.json under resources folder with your own plan details.
2. All the json attributes are required. The notificationPreferences should only contain preferences that are enabled in application.yml
3. ```currency``` field in subscriptionPrice should be one of those currencies specified in rif.notifier.subscription.currencies property of application.yml
4. Run bin/subscriptionplans.sh from the home directory of this project
5. If the json is correct, the plans will be created in the database.
6. A sample json structure is given below
```
[
  {
    "name": "RIF-10k",
    "notificationQuantity": 10000,
    "validity": 100,
    "notificationPreferences": "API,EMAIL",
    "status": true,
    "subscriptionPriceList": [
      {
        "price": "10",
        "currency": {
          "name":"RBTC",
          "address": "0xD9F3C552704B716EB2b825F20178181aB28F9eD8"
        }
      },
      {
        "price": "20",
        "currency": {
          "name":"RIF",
          "address": "0x2C51B7bed742689D13F8DFb74487410cFa0ccAF4"
        }
      }
    ]
  },
  {
    "name": "RIF-20k",
    "notificationQuantity": 20000,
    "validity": 100,
    "notificationPreferences": "API,EMAIL",
    "status": true,
    "subscriptionPriceList": [
      {
        "price": "20",
        "currency": {
          "name":"RBTC",
          "address": "0xD9F3C552704B716EB2b825F20178181aB28F9eD8"
        }
      },
      {
        "price": "40",
        "currency": {
          "name":"RIF",
          "address": "0x2C51B7bed742689D13F8DFb74487410cFa0ccAF4"
        }
      }
    ]
  }
]
```

### Subscribe to plan

Users can create a subscription to a given plan by providing their user address and plan id along with topic details and the notification preferences.

The endpoint http://localhost:8080/subscribeToPlan can be used to create a new subscription. A sample json that can be sent in the request body is provided under ```src/main/resources/subscription-batch-example.json```. Modify the json to provide your own topics and preferences. More information on how to specify the topic subsection can be found at [topic json](#topic-json)

```currency``` in json should be one of the currencies accepted by the provider, and allowed in the plan.

```price``` should be same price as in subscription plan for the given currency

One or more ```notificationPreferences``` can be specified
```notificationService``` currently supported are ```API, EMAIL and SMS```

### Renew a plan

Users can renew a subscription to a given plan by providing their user address and plan id along with topic details and the notification preferences. The renewal json is same as [subscribe to plan](#subscribe-to-plan) json. However, previousSubscriptionHash has to be sent along with the request. The previous subscription cannot be in ```PENDING``` state.

The endpoint http://localhost:8080/renewSubscription?previousSubscriptionHash can be used to create a new subscription. ```previousSubscriptionHash``` parameter must be sent as a request parameter. 

###### **Topic Json**

```json
{
    "type": "CONTRACT_EVENT", 
    "topicParams": [{
            "type": "CONTRACT_ADDRESS",
            "value": "0xf4af6e52b1bcbbe31d1332eb32d463fb10bded27"
        },
        {
            "type": "EVENT_NAME",
            "value": "LogSellArticle"
        },
        {
            "type": "EVENT_PARAM",
            "value": "seller",
            "order": 0,
            "valueType": "Address",
            "indexed": 1
        },
        {
            "type": "EVENT_PARAM",
            "value": "article",
            "order": 1,
            "valueType": "Utf8String",
            "indexed": 0,
            "filter": "iphone x"
        },
        {
            "type": "EVENT_PARAM",
            "value": "price",
            "order": 2,
            "valueType": "Uint256",
            "indexed": 0,
            "filter": "1000"
        }
    ]
}
```

#### **Notes of Topic Json structure**

Event type (First param of the json structure), this can be type of: `CONTRACT_EVENT, NEW_TRANSACTIONS, NEW_BLOCK`. It'll indicate the notifier what type of event you want to listen in the blockchain

When you want to listen to a certain contract event in the blockchain, you need to indicate type: `CONTRACT_EVENT` type, some needed params are required for this type of event:

-CONTRACT_ADDRESS param like the described before, this will indicate to the notifier that this param is the address to be listened. *It is required.

-EVENT_NAME param, this will be used to check that the name of the contract event is the same as the blockchain when it's called. *It is required.

-EVENT_PARAM, here you will need to indicate an order as described by the contract signature, please indicate when a param is indexed also. The valueType need to be a web3 accepted type. Not required, in case the event doest have one, dont send this.

--EVENT_PARAM is composed of some attributes, "value" indicates the name of the event parameter, "order" is for the order of the param that appears in the event, this will be used to filter the data. "valueType" is used to create the event listener in rif-notification, so it needs to be a valid web3 type, "indexed" is used to indicate if the param is indexed, default value is 0, and we add a "filter" param, so you can use it to filtering the data you want to retrieve from the event.

For others types, you only need to send the Type without Params.

As an example for `NEW_BLOCKS` or `NEW_TRANSACTIONS`

```json
{
    "type": "NEW_BLOCK" 
}
```
Or
```json
{
    "type": "NEW_TRANSACTION" 
}
```
