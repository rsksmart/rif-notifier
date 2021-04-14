# Notifier Provider CLI

Notifier Provider CLI is a command line tool to configure and run RIF notifier.

## Prerequisites

* Python3.6 or later

    If you don't have python 3 installed, install python by running the following commands
  
    ```sudo apt-get update```

    ```sudo apt-get install python3```


* Pip

    If you don't have pip installed, install pip by running the following command
  
    ```sudo apt-install python3-pip```

## Install

Install the notifier-prov-cli by running the below script 

```sudo notifier-prov-cli/install.sh``` 


## Available Commands

### `notifier-prov-cli configure`
Configure the RIF notifier local instance. Available options are
* `--serverport` - server port to start the RIF notifier on
* `--dbhost` - database ip address or hostname where the RIF notifier mysql database is installed
* `--dbuser` -  username to use while connecting to RIF notifier mysql database 
* `--dbpassword` -  password to use while connect to RIF notifier mysql database
* `--rskendpoint` -  the url of rsk blockchain (ex. http://localhost:4444)
* `--blockconfirmationcount` -  the number of blocks to wait for confirmation before RIF notifier can read the block.
* `--notificationmanagercontract` -  deployed contract address of Notificationmanager contract in rsk blockchain
* `--tokennetworkregistry` -  the address of the token network registry
* `--multichaincontract` -  the address of the token network registry where tokens can be read in rsk blockchain
* `--provideraddress` -  provider address of the provider
* `--providerprivatekey` -  private key that corresponds to the provider address
* `--notificationpreferences` -  notification preferences supported by the provider (API/SMS/EMAIL) (accepts multiple values)
* `--acceptedcurrencies` -  currencies accepted by the provider (accepts multiple values)

   To set multiple values for same option (for ex. notificationpreferences) use

```notifier-prov-cli configure --notificationpreferences API --notificationpreferences SMS```
**Note:** when using ```notifier-prov-cli configure``` the configure option will always overwrite the previous configuration. So always use the above command example when setting multiple values for the same option.

### `notifier-prov-cli create`
`create` provides subcommand `subscriptionplan`
* `notifier-prov-cli create subscriptionplan`(aliased as sp)  - This command is used to create a new subscription plan. The interactive prompts will guide you through creating a new subscription plan. Command options are:
  
  `--local` or `--docker` - specifes whether the subscription plan should be created in the local machine or docker container.


### `notifier-prov-cli disable`
`disable` provides subcommand `subscriptionplan`
* `notifier-prov-cli disable subscriptionplan`(aliased as sp)  - This command is used to disable an existing subscription plan. The interactive prompts will guide you through creating a new subscription plan. Command options are:
  
    `--local` or `--docker` - specifes whether the subscription plan should be created in the local machine or docker container.

### `notifier-prov-cli dockerbuild`
This command is used to build RIF notifier docker instance, can be used if you choose to run RIF notifier as a docker instance as opposed to running locally. Before running this command, ensure that you have set the dbpassword property in the .env file

### `notifier-prov-cli dockerconfigure`
This command is used to configure RIF notifier docker instance. Available options are:

* `--dbpassword` -  password to use while connecting to rif-mysql docker container. This password should be same as the one in .env file that was used when building the docker instance
* `--rskendpoint` -  the url of rsk blockchain, default is http://regtest:4444
* `--blockconfirmationcount` -  the number of blocks to wait for confirmation before RIF notifier can read the block.
* `--notificationmanagercontract` -  deployed contract address of Notificationmanager contract in rsk blockchain
* `--tokennetworkregistry` -  the address of the token network registry
* `--multichaincontract` -  the address of the token network registry where tokens can be read in rsk blockchain
* `--provideraddress` -  provider address of the provider
* `--providerprivatekey` -  private key that corresponds to the provider address
* `--notificationpreferences` -  notification preferences supported by the provider (API/SMS/EMAIL) (accepts multiple values)
* `--acceptedcurrencies` -  currencies accepted by the provider (accepts multiple values)


### `notifier-prov-cli dockerstart`
This command is used to start the RIF notifier docker instance. 

### `notifier-prov-cli dockerstop`
This command is used to stop the RIF notifier docker instance.


### `notifier-prov-cli edit`
`edit` provides subcommand `subscriptionplan`
* `notifier-prov-cli edit subscriptionplan` - This command is used to edit an existing subscription plan. The interactive prompts will guide you through editing the subscription plan. Command options are:

  `--local` or `--docker` - specifes whether the subscription plan should be created in the local machine or docker container.


### `notifier-prov-cli healthcheck`
This command is used to monitor the status of the running RIF notifier running locally or in docker instance. 

### `notifier-prov-cli list`
List provides a subcommand`subscriptionplan`
* `notifier-prov-cli list subscriptionplan`(aliased as sp)  - Lists the subscription plans offered by the provider

### `notifier-prov-cli restart`
This command is used to restart RIF notifier instance running locally

### `notifier-prov-cli start`
This command is used to start RIF notifier instance locally

### `notifier-prov-cli stop`
This command is used to stop RIF notifier instance running locally

## Help
Run `notifier-prov-cli <command> --help` to get help on each command or simply `notifier-prov-cli --help` to see available commands.
