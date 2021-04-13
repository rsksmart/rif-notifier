# Notifier Consumer CLI

Notifier Consumer CLI is a command line tool for RIF-Notifier consumers to perform operations like subscribe to a plan, renew a subscription, and list user subscriptions.

## Prerequisites

* Python3.6 or later

    If you don't have python 3 installed, install python by running the following commands
  
    ```sudo apt-get update```

    ```sudo apt-get install python3```


* Pip

    If you don't have pip installed, install pip by running the following command
  
    ```sudo apt-install python3-pip```

## Install

Install the notifier-cons-cli by running the below script 

```sudo notifier-cons-cli/install.sh``` 


## Available Commands

### `notifier-cons-cli configure` 
Configure the notifier-cons-cli, available options are 
* `--notifierurl` - url of the rif-notifier provider 
* `--useraddress` - user address
* `--apikey` -  user api key (This will be set automatically when the user subscribes using `subscribe` command. This option can be used to change an apikey if required.)

### `notifier-cons-cli list` 
List provides two sub commands `subscription` and `subscriptionplan`
* `notifier-cons-cli list subscription`(aliased as sub)  - Lists the user subscriptions
* `notifier-cons-cli list subscriptionplan`(aliased as sp)  - Lists the subscription plans offered by the provider


### `notifier-cons-cli subscribe`
This command is used to subscribe to a provider offered subscription plan with topics and notification preferences. The interactive prompts will guide you through adding topics to listen to and the notification preference for each topic.

### `notifier-cons-cli renew`
This command is used to renew an existing subscription which is about to expire or alread expired. The interactive prompts will guide you through adding topics to listen to and the notification preferences for each topic.

## Help
Run `notifier-cons-cli <command> --help` to get help on each command or simply `notifier-cons-cli --help` to see available commands.



