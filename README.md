# Rif-Notifier

## Quick Start

-First of all you need to set the blockchain endpoint in the application.properties of this project

-To subscribe to Events like Open Channel or Close Channel, there's a property that needs the Token Network Registry Address to be setted in the application.properties

-We use mysql for DB, please put your DB settings in the application.properties too

-You have the DB schema in src/main/resources/db_dumps/, look for the latest Dump.sql, create a DB with this schema, and in application.properties set the connection to your DB

-Get started with the following steps

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

###### Other available endpoints

----------------
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