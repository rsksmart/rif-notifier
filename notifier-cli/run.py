from Notifier import Notifier
from Config import Config
from SubscriptionPlan import SubscriptionPlan
import click

CONTEXT_SETTINGS = dict(help_option_names=['-h', '--help'])

def print_help():
    ctx = click.get_current_context()
    click.echo(ctx.get_help())


@click.group()
def main():
    pass


@main.command(context_settings=CONTEXT_SETTINGS)
@click.option("--serverport", type=int, required=False, help="Server Listen port")
@click.option("--dbhost", required=False, help="Database Hostname name")
@click.option("--dbname", required=False, help="Database name")
@click.option("--dbuser", required=False, help="Database username")
@click.option("--dbpassword", required=False, help="Database password")
@click.option("--rskendpoint", required=False, help="Rsk Endpoint")
@click.option("--blockconfirmationcount", required=False, help="Number of confirmations to wait")
@click.option("--notificationmanagercontract", required=False, help="Contract address of notification manager")
@click.option("--tokennetworkregistry", required=False, help="Address of Token network")
@click.option("--multichaincontract", required=False, help="Address of multi chain contract")
@click.option("--provideraddress", required=False, help="Provider address")
@click.option("--providerprivatekey", required=False, help="Provider private key")
@click.option("--notificationpreferences", type=click.Choice(['API', 'SMS', 'EMAIL']), required=False, multiple=True, help="Notification Preferences")
@click.option("--acceptedcurrencies", required=False, multiple=True, help="Accepted Currency Names")
def configure(**kwargs):
    """Configure RIF-Notifier"""
    setConfig(**kwargs)

@main.command(context_settings=CONTEXT_SETTINGS)
@click.option("--dbpassword", required=False, help="Database password")
@click.option("--rskendpoint", required=False, help="Set Rsk Endpoint (default http://regtest:4444)")
@click.option("--blockconfirmationcount", required=False, help="Number of confirmations to wait")
@click.option("--notificationmanagercontract", required=False, help="Contract address of notification manager")
@click.option("--tokennetworkregistry", required=False, help="Address of Token network")
@click.option("--multichaincontract", required=False, help="Address of multi chain contract")
@click.option("--provideraddress", required=False, help="Provider address")
@click.option("--providerprivatekey", required=False, help="Provider private key")
@click.option("--notificationpreferences", type=click.Choice(['API', 'SMS', 'EMAIL']), required=False, multiple=True, help="Notification Preferences")
@click.option("--acceptedcurrencies", required=False, multiple=True, help="Accepted Currency Names")
def dockerconfigure(**kwargs):
    """Configure RIF-Notifier Docker instance"""
    setConfig(True, **kwargs)

@main.command(context_settings=CONTEXT_SETTINGS)
def start():
    """Start RIF-Notifier"""
    Notifier().start()

@main.command(context_settings=CONTEXT_SETTINGS)
def stop():
    """Stop RIF-Notifier"""
    Notifier().stop()

@main.command(context_settings=CONTEXT_SETTINGS)
def restart():
    """Restart RIF-Notifier"""
    notifier = Notifier()
    notifier.stop()
    notifier.start()

@main.command(context_settings=CONTEXT_SETTINGS)
def healthcheck():
    """RIF-Notifier health check"""
    Notifier().healthCheck()

@main.group(context_settings=CONTEXT_SETTINGS)
def create():
    """create subscription plan"""

@main.group(context_settings=CONTEXT_SETTINGS)
def list():
    """list subscription plans"""

@list.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
def listSubscriptionPlan():
    Notifier().listSubscriptionPlans()

def validatePreferences(ctx, param, value):
    vals = value.split(',')
    for x in vals:
        if x  == 'API' or x == 'SMS' or x == 'EMAIL':
            pass
        else:
            raise click.BadParameter('notification preference must be API, SMS or EMAIL')
    return value

def validatePlanId(ctx, param, value):
    plan = Notifier().getSubscriptionPlan(value)
    if plan:
        return value
    else:
        raise click.BadParameter('Invalid plan id or the plan cannot be read from the server.')

@main.group(context_settings=CONTEXT_SETTINGS)
def disable():
    """disable subscription plan"""

@disable.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
@click.option("--docker/--local", '-d/-l', is_flag=True, help="Specifies whether the plan should be disabled in a docker container or in the local machine", default=False, show_default=True)
@click.option("--id", callback=validatePlanId, prompt="Enter subscription plan id", help="Id of existing subscription plan")
def disableSubscriptionPlan(docker, id,**kwargs):
    notifier = Notifier(docker)
    if docker:
        notifier.disableDocker(id)
    else:
        notifier.disable(id)

@main.group()
def edit():
    """edit subscription plan"""

@edit.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
@click.option("--docker/--local", '-d/-l', is_flag=True, help="Specifies whether the plan should be edited in a docker container or in the local machine", default=False, show_default=True)
@click.option("--id", callback=validatePlanId, prompt="Enter subscription plan id", help="Id of existing subscription plan")
@click.option("--name", prompt="Enter subscription plan name", help="name of subscription plan")
@click.option("--notificationQuantity", type=int, prompt="Enter notification quantity", help="Total quantity of notifications offered")
@click.option("--validity", type=int, prompt="Enter validity (days)", help="plan validity in days")
@click.option("--notificationPreferences", prompt="Enter Notification Preferences(API,SMS,EMAIL)", callback=validatePreferences, help="Comma separated Notification Preferences - API,SMS,EMAIL")
def editSubscriptionPlan(id, **kwargs):
    subscriptionplan(id, **kwargs)

def subscriptionplan(id, **kwargs):
    plan = SubscriptionPlan()
    if id is not None: plan.set("id", id)
    docker = False
    for x,y in kwargs.items():
        x = "notificationPreferences" if x == "notificationpreferences" else x
        x = "notificationQuantity" if x == "notificationquantity" else x
        if x == 'docker':
            docker = y
        else :
            plan.set(x,y)
    while True:
        price = click.prompt("Enter subscription price")
        currency = click.prompt("Enter subscription currency")
        address = click.prompt("Enter currency address")
        plan.addPrice(price, currency, address)
        confirm = click.confirm("Do you want to add another price and currency combination for this subscription plan?")
        if not confirm: break
    plan.add()
    plan.planWrite()
    Notifier().create(plan) if not docker else Notifier(True).createDocker(plan)



@create.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
@click.option("--docker/--local", '-d/-l', is_flag=True, help="Specifies whether the plan should be created in a docker container or in the local machine", default=False, show_default=True)
@click.option("--name", prompt="Enter subscription plan name", help="name of subscription plan")
@click.option("--notificationQuantity", type=int, prompt="Enter notification quantity", help="Total quantity of notifications offered")
@click.option("--validity", type=int, prompt="Enter validity (days)", help="plan validity in days")
@click.option("--notificationPreferences", prompt="Enter Notification Preferences(API,SMS,EMAIL)", callback=validatePreferences, help="Comma separated Notification Preferences - API,SMS,EMAIL")
def createsubscriptionplan(**kwargs):
    subscriptionplan(None, **kwargs)

@main.command(context_settings=CONTEXT_SETTINGS)
def dockerstart():
    """Start RIF-Notifier Docker Instance"""
    Notifier(True).dockerStart()

@main.command(context_settings=CONTEXT_SETTINGS)
def dockerstop():
    """Stop RIF-Notifier Docker Instance"""
    Notifier(True).dockerStop()

@main.command(context_settings=CONTEXT_SETTINGS)
def dockerbuild():
    """Build RIF-Notifier Docker Instance"""
    Notifier(True).dockerBuild()


def setConfig(docker=False, **kwargs):
    c = Config(docker)
    propSize = 0
    for x,y in kwargs.items():
        if y:
            if x == 'notificationpreferences' or x == 'acceptedcurrencies':
                v = ','.join(y)
                c.set(x, v)
            else:
                c.set(x,y)
            propSize = propSize+1
    if docker and not c.hasProperty("rskendpoint"):
        c.set("rskendpoint", "http://regtest:4444")
    print_help() if propSize == 0 else c.configWrite()

if __name__ == "__main__":
    main()