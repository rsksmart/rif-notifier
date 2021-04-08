## Notifier Provider CLI

### Prerequisites

* Python3.6 or later

    If you don't have python 3 installed, install python by running the following commands
  
    ```sudo apt-get update```

    ```sudo apt-get install python3```


* Pip

    If you don't have pip installed, install pip by running the following command
  
    ```sudo apt-install python3-pip```

### Configure

Install the notifier-prov-cli by running the below script 

```sudo notifier-prov-cli/install.sh``` 


### Run

 ```notifier-prov-cli``` or ```notifier-prov-cli --help```

### Usage

   To set multiple values for same option (for ex. notificationpreferences) use

```notifier-prov-cli configure --notificationpreferences API --notificationpreferences SMS```

**Note:** when using ```notifier-prov-cli configure``` the configure option will always overwrite the previous configuration. So always use the above command example when setting multiple values for the same option.
