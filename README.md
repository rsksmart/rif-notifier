# Rif-Notifier 

## Indexes

1. [Quick start](#quick-start) 
2. [Installation](#installation-guide) 
3. [Usage Guide](#usage-guide)
4. [Retrieve notifications](#getting-notifications)
5. [Unsubscribe from topic](#unsubscribing-from-a-topic)
6. [Save notification Preference](#save-notification-preference)
7. [Remove notification Preference](#remove-notification-preference)
8. [Other available endpoints](#other-available-endpoints)
	1. [Get subscription info](#get-subscription-info)
	2. [Get Lumino tokens](#get-lumino-tokens)
	3. [Subscribe to specific open channel](#subscribe-to-specific-open-channel)
	4. [Subscribe to close channel](#subscribe-to-close-channel)
	5. [Subscribe to all open channels](#subscribe-to-all-lumino-open-channels)
	6. [Get chain addresses events](#get-rns-events)
9. [Verify blockchain events are processed](#verify-blockchain-events)
10. [Create Subscription Plans](#create-subscription-plans)
11. [Update Subscription Plans](#update-subscription-plans)
12. [Enable or Disable Subscription Plans](#enable-or-disable-subscription-plans)
13. [Health Check](#health-check)


## Quick Start

(This steps you can follow if you're already familiar with the notifier, otherwise jump to the [Installation](#installation-guide) guide first)

-First of all you need to set the blockchain endpoint property  ```rskendpoint:``` for ex. ```http://localhost:4444``` in the config.json found in home directory of this project

-To subscribe to a plan use the endpoint http://localhost:8080/subscribeToPlan

-To view all endpoints use http://localhost:8080/swagger-ui.html

-To subscribe or renew a plan use http://localhost:8080/subscribeToPlan endpoint http://localhost:8080/renewSubscription

-To subscribe to Events like Open Channel or Close Channel, use the property  that needs the Token Network Registry Address to be setted in the application.yml

-We use mysql for DB, please put your DB settings in the application.yml too

-You have the DB schema in src/main/resources/db_dumps/, look for the latest Dump.sql, create a DB with this schema, and in application.yml set the connection to your DB

-Get started with the following steps

## Installation guide
## Setup

### Prerequisites
#### 1. RSK Blockchain (Mainnet)
The first requirement is an RSK node which can be run using the **JAR file** method. Use the latest RSKj version avaiable and have it sync with mainnet.

This node should be accessible through `localhost:4444`. For more information on how to achieve this, please consult the [_Setup node on Java_ section on the Developer Portal](https://developers.rsk.co/rsk/node/install/java/).

#### 2. git
The latest version of the `git` client can be installed through:

```shell
sudo apt update
sudo apt install git
```

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
###Installation steps
1. Pick a directory for all the RIF Notifier code to reside in. From now on this will be called `notifier-dir`, but replace it with your own.
2. Clone the RIF Notifier git project into its directory doing:

```shell
git clone https://github.com/rsksmart/rif-notifier notifier-dir
```
Stay on the `master` branch.

Run bin/install.sh with 3 parameters, this will clone the rif-notifier repo from github and create the mysql user notifier_user with
 required permissions
```
bin/install.sh <database root user> <database root password> <notifier_user password>
```
The third parameter is the password for preconfigured user notifier_user which will be created by this script

####Manual steps
Alternatively the steps can be performed manually as below

. Create the RIF Notifier database and configure its access. This is done by first opening the MySQL prompt by executing:

```shell
sudo mysql
```

Then, pick a name for the RIF Notifier database to be used. From now on this will be called `notifier_db`, but replace it with your own.

Create the schema by entering:

```mysql
CREATE DATABASE notifier_db;
```

in the `mysql` prompt.

Now pick a username and password for the database to be accessed with. From now on these will be called `notifier_db_user` and `notifier_db_password`, but replace them with your own.

To have these set up in the MySQL database first do:

```mysql
CREATE USER 'notifier_db_user'@'localhost' IDENTIFIED BY 'notifier_db_password';
```

in the `mysql` prompt. Then grant this user all permissions on the schema by doing:

```mysql
GRANT ALL PRIVILEGES ON notifier_db.* TO 'notifier_db_user'@'localhost';
```

and then exit the `mysql` terminal by entering `exit`.

Restart the MySQL service by executing:

```shell
sudo /etc/init.d/mysql restart
```


## Docker Installation guide
1. Download docker image <rifnotifier.tar> from gdrive link provided
2. Run command below - 
```
docker load -i rifnotifier.tar
docker run -it --name rifn2  -p 8080:8080 rifn2 bash
cd ~/rif-notifier
git pull
mvn spring-boot:run
```
---

## Execution

### Preconditions

RIF Notifier uses `eth_getLogs` rpc to get the information about events, therefore the RSK node must respond in a reasonable
timeframe (< 30s)

Since the `eth_getLogs` result is cached, it will take a long time for this call to finish the first time it is executed after the RSK node is started. This will happen each time the RSK node boots.
After this, each call should be finished in a reasonable time.

Use this curl to test the `eth_getLogs` response:
```
curl -X POST http://localhost:4444 -H 'Content-Type: application/json' -d '{"jsonrpc":"2.0","method":"eth_getLogs","params":[{"address":"0xde2D53e8d0E673A4b1D9054cE83091777F2fd8Ce","fromBlock":"0x0","toBlock":"latest"}],"id":74}'
```

### Start the application

To run the RIF Notifier start a terminal in `rif-notifier` and run:


First modify the ```config.json``` file to setup the rsk blockchain and database properties
```
{
	"serverport":"8080",  // server port to start the server on

	"dbhost":"localhost",  //database host name
	"dbname":"rif_notifier",  //database name
	"dbuser":"notifier_user",  //database user with privileges
	"dbpassword":"##password##",  //database password

	"rskendpoint":"http://localhost:7545",  //rsk blockchain endpoint
	"blockconfirmationcount":"20",  // number of blocks to wait for confirmation
	"smartcontractaddress":"0xC2Cd3835d36510dc065d0f8991785DAA70a601a4",  // smart contract address for payments
	"tokennetworkregistry":"0xFEC354973ca22697BC5Cd1E7F372609574e2AfcA", 
	"multichaincontract":"0xFEC354973ca22697BC5Cd1E7F372609574e2AfcA",

	"provideraddress":"0x882bf23c4a7E73cA96AF14CACfA2CC006F6781A9",  // provider address
	"providerprivatekey":"b1ed36c7f7e02edeaacfd7b485cc857e3051e94a73195a6c96c88dd74d22744a", //provider privatekey without hex prefix

	"notificationpreferences":"API,EMAIL",  // supported notifications comma separated

	"acceptedcurrencies":"RIF,RBTC"  // supported currencies comma separated
}

```
Then run the command ```bin/subscriptionplans.sh```  to create the subscription plans. Refer to [create subscription plans](#create-subscription-plans) and [update subscription plans](#update-subscription-plans)

###Start the application
 Run
the command ```bin/run.sh``` to start the application.
---

## Update
To update an already installed RIF Notifier follow these steps:
1. Stop the RIF Notifier process.
2. Navigate to the `notifier-dir` and pull the latest code by executing `git pull`. The `master` branch should still be used.
3. Re-initialize the RIF Notifier database by following **step 4** in the [Installation steps section](#installation-steps).
4. Start the RIF Notifier as indicated in the [Execution section](#execution).

###### Get signed address and privatekey
1. Get a wallet address and private key from a wallet for ex. nifty wallet
2. Sign the address using the wallet private key with the below javascript 
```
var ethers = require('ethers');
const Web3 = require('web3');
const SigningHandler = () => {
  let web3;
  let wallet;
  let decryptedAccount;
  const init = (_web3, _privateKey) => {
    web3 = _web3;
    wallet = new ethers.Wallet(_privateKey);
console.log(wallet);
    decryptedAccount = web3.eth.accounts.privateKeyToAccount(_privateKey);
  };
  const offChainSign = data => {
	console.log(wallet);
    const signature = wallet.signMessage(data);
    return signature;
  };
  const sign = async tx => {
    const signed_tx = await decryptedAccount.signTransaction(tx);
    return signed_tx.rawTransaction;
  };
  return { init, offChainSign, sign };
};
let privatekey = <your wallet private key>;
let addressstoSign = <your wallet address>;
let web3 = new Web3('ws://localhost:4444');
var s = SigningHandler();
s.init(web3, privatekey);
console.log(s.offChainSign(addressToSign));
```
Note down the signed address to use as body to register user.

###### First you need to register a user
```
POST Request: http://localhost:8080/users?address=YOUR_ADDRESS
Body (Text/plain): Here you need to put your address signed with your private key
```

The notifier will validate the body of that request, and making sure you own this address.

This endpoint will give you an ApiKey, please keep track of this Api Key cause you will need it for future calls

Newest implementations, if you re-send this, it will return the ApiKey, same as you register for the first time.

Return example:

```json
{
    "message": "OK",
    "data": {
        "address": "0x7bDB21b2d21EE4b30FB4Bb791781F7D17f465309",
        "apiKey": "t9lkxFcjIsJL5rnwfPsAayPYBFdjxB74"
    },
    "status": "OK"
}
```


###### Now you need to generate a subscription to the service


```
POST Request: http://localhost:8080/subscribe?type=SUBSCRIPTION_TYPE
Header param: 
	key: apiKey #Remember that you get this apiKey from previous step
	value: API_KEY 
```

*Not implemented yet*

*SUBSCRIPTION_TYPE will indicate how many notifications you can recieve. So when selecting one, will be giving you a notification balance.*

*When consuming this endpoint, the notifier will be creating a subscription and giving a lumino-invoice, that the user will need to pay. For development purposes, right now it's creating a subscription with MAX_INT*


###### Now just rest to send the topics with params to be listened


```
POST Request: http://localhost:8080/subscribeToTopic
Header param: 
	key: apiKey
	value: API_KEY 
```

Sending the next json structure on the body of the request (In this example we're using example values from a contract, so feel free to change them): 

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

**Notes of Json structure**

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
    "type": "NEW_BLOCK", 
}
```
Or
```json
{
    "type": "NEW_TRANSACTION", 
}
```

Return example: 

```json
{
    "message": "OK",
    "data": "{\"topicId\": 1}",
    "status": "OK"
}
```
You can store that topicId for later get the notifications for that particular event

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


###### Unsubscribing from a Topic

```
POST Request: http://localhost:8080/unsubscribeFromTopic?idTopic=ID_TOPIC
Header param: 
	key: apiKey
	value: API_KEY 
```

###### Save Notification Preference
Notification Preference allows to save a type of notification to send for all blockchain notifications. The different types of notifiction preference available are SMS, EMAIL and API. 
A notification preference is usually associated to a user and topic id. When no topic id is provided a default topic id 0 is used, and set as default notification preference when no
preference found for given user and topic id.
```
POST Request: http://localhost:8080/saveNotificationPreference
Header param:
    key: apiKey
    value: API_KEY
```
Api Json Body:
```
        {
          "notificationService":"API",
          "destination":"http://host/notify",
          "idTopic":"0",
          "destinationParams":{
              "apiKey":"test",
              "username":"test",
              "password":"test"
            }
        }
```
Email Json Body:
```
        {
                "notificationService":"EMAIL",
                "destination":"123456@abc.com;123@abc.com", /*(multiple email addresses separated by semi-colon)*/ 
                "idTopic":"11",
        }
```
Sms Json Body:
```
        {
               "notificationService":"SMS",
                "destination":"+191725245555", /* in exact format, +(country code)(phone number)*/
                "idTopic":"10",
        }     
```

###### RemoveNotification Preference
Removes a given notification preference
```
POST Request: http://localhost:8080/removeNotificationPreference
Header param:
    key: apiKey
    value: API_KEY
```
API Json Body
```
    {
          "notificationService":"API",
          "idTopic":"10",
    }
```
SMS Json Body
```
    {
          "notificationService":"SMS",
          "idTopic":"10",
    }
```
Email Json Body
```
    {
          "notificationService":"EMAIL",
          "idTopic":"10",
    }

```

###### Other available endpoints

----------------
###### Get Subscription info
```
GET Request: http://localhost:8080/getSubscriptionInfo
Header param: 
	key: apiKey
	value: API_KEY 
Short description: Brings the data associated with your subscription (Notification_Balance, Topics subscribed with params, etc)
```
Return example:
```json
{
    "message": "OK",
    "data": "SEE JSON BELOW",
    "status": "OK"
}
```
```json
{
   "id":0,
   "activeSince":"2020-00-00 00:00:00.0",
   "active":true,
   "userAddress":"MY_ADDRESS",
   "type":{
      "id":0,
      "notifications":2147483647
   },
   "state":"PAYED",
   "topics":[
   ],
   "notificationPreferences":[
   ],
   "notificationBalance":2147483647
}
```
----------------
###### Get lumino tokens
```
GET Request: http://localhost:8080/getLuminoTokens
Header param: 
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
1. Download ganache from tufflesuite.com/ganache
```
2. npm install -g truffle
3. truffle unbox metacoin
```
4. uncomment the entire networks structure in truffle-config.js
5. Open ganache and make sure you are pointing the truffle-config.js from the metacoin project
6. Run commands below from truffle
```
 truffle console --network development
 migrate --reset
 let contract = await MetaCoin.deployed()
 contract.sendCoin('###2nd address in ganache ###', 100)
```
7. Check results: Now under raw_data and notification tables there should be data.
8. Check notifications are being sent by verifying sent=1 in notification table, for the notification preference set. In case not sent check the notification_log table.

##### Create Subscription Plans
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
##### Update Subscription Plans
1. A subscription plan that already exists can be updated. In order to update, the "id" attribute must be specified as part of the subscription-plan.json for the plan to be updated. 
2. All json attributes are required as given in subscription-plan.json. The notificationPreferences should only contain enabled preferences in application.yml
2. Modify subscription-plan.json under resources folder with your own plan details.
5. ```currency``` field in subscriptionPrice should be one of those currencies specified in rif.notifier.subscription.currencies property of application.yml 
6. Run bin/subscriptionplans.sh from the home directory of this project
7. If the json is correct, the plans will be created in the database.
8. A sample json structure for update operation is given below
```
[
  {
    "id":1,
    "name": "RIF-10k",
    "notificationQuantity": 10000,
    "validity": 150,
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
    "id":"2"
    "name": "RIF-20k",
    "notificationQuantity": 20000,
    "validity": 200,
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

##### Enable or disable subscription plans
A subscription plan can be enabled or disabled by setting the "status" property to true or false in subscription-plan.json

##### Health Check
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
