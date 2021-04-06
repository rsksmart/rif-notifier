## Notifier-CLI

### Prerequisites

* Python3.6 or later

* Pip

### Configure

Install the notifier-cli by running the below script 

```sudo notifier-cli/install.sh``` 


### Run

 ```notifier-cli``` or ```notifier-cli --help```

### Usage

   To set multiple values for same option (for ex. notificationpreferences) use

```notifier-cli configure --notificationpreferences API --notificationpreferences SMS```

**Note:** when using ```notifier-cli configure``` the configure option will always overwrite the previous configuration. So always use the above command example when setting multiple values for the same option.
