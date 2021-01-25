# Rif-Notifier 

## Indexes

1. [Quick start](#quick-start) 
2. [Installation](#installation) 
3. [First steps](#first-steps)


## Installation
### Requirements
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

---

### Installation steps
1. Pick a directory for all the RIF Notifier code to reside in. From now on this will be called `notifier-dir`, but replace it with your own.
2. Clone the RIF Notifier git project into its directory doing:

```shell
git clone https://github.com/rsksmart/rif-notifier notifier-dir
```

Stay on the `master` branch.

3. Create the RIF Notifier database and configure its access. This is done by first opening the MySQL prompt by executing:

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

4. Populate the RIF Notifier database by first moving to the `notifier-dir/src/main/resources/db_dumps` directory and then executing:
   
```shell
mysql -u notifier_db_user -p notifier_db < Dump20200714.sql 
```

and then entering the `notifier_db_password` when prompted immediately after.

5. Configure the RIF Notifier parameters by navigating to `notifier-dir/src/main/resources/` and opening the file `application.properties` for edting.

Then, replace the values in the first section (`##Dev`) as follow, using your own MySQL parameters:

```
spring.datasource.url=jdbc:mysql://localhost:3306/notifier_db
spring.datasource.username=notifier_db_user
rsk.blockchain.endpoint=http://localhost:4444
spring.datasource.password=notifier_db_password
rsk.blockchain.tokennetworkregistry=0x060B81E90894E1F38A625C186CB1F4f9dD86A2B5
rsk.blockchain.multichaincontract=0x99a12be4C89CbF6CFD11d1F2c029904a7B644368
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

To run the RIF Notifier start a terminal in `notifier-dir` and run:


```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8080"
```

---

## Update
To update an already installed RIF Notifier follow these steps:
1. Stop the RIF Notifier process.
2. Navigate to the `notifier-dir` and pull the latest code by executing `git pull`. The `master` branch should still be used.
3. Re-initialize the RIF Notifier database by following **step 4** in the [Installation steps section](#installation-steps).
4. Start the RIF Notifier as indicated in the [Execution section](#execution).

## Firsts steps


### Generate a user

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

### Now you need to generate a subscription to the service


```
POST Request: http://localhost:8080/subscribe?type=SUBSCRIPTION_TYPE
Header param: 
	key: apiKey #Remember that you get this apiKey from previous step
	value: API_KEY 
```

*Not implemented yet*

*SUBSCRIPTION_TYPE will indicate how many notifications you can recieve. So when selecting one, will be giving you a notification balance.*

*When consuming this endpoint, the notifier will be creating a subscription and giving a lumino-invoice, that the user will need to pay. For development purposes, right now it's creating a subscription with MAX_INT*


### Now just rest to send the topics with params to be listened


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
    "type": "NEW_BLOCKS", 
}
```
Or
```json
{
    "type": "NEW_TRANSACTIONS", 
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

### Getting notifications

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


### Unsubscribing from a Topic

```
POST Request: http://localhost:8080/unsubscribeFromTopic?idTopic=ID_TOPIC
Header param: 
	key: apiKey
	value: API_KEY 
```

### Other available endpoints

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
Short description: This endpoint subscribes you to all tokens, and returns an array of topic id, each topic will represent a event for each token. Also this endpoint accepts params for participantone and participanttwo, if sended, will filter all the topics.
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

