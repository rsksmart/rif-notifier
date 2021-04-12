import click
import json
from NotifierConsumer import NotifierConsumer
from Config import Config

CONTEXT_SETTINGS = dict(help_option_names=['-h', '--help'])

def print_help():
    ctx = click.get_current_context()
    click.echo(ctx.get_help())


@click.group()
def main():
    pass

@main.command(context_settings=CONTEXT_SETTINGS)
@click.option("--notifierurl", required=False, help="Notifier Provider url")
@click.option("--useraddress", required=False, help="Consumer user address")
@click.option("--apikey", required=False, help="Consumer api key")
def configure(**kwargs):
    """Configure RIF-Notifier access"""
    setConfig(**kwargs)

@main.group(context_settings=CONTEXT_SETTINGS)
def list():
    """list subscription and subscription plans"""

@list.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
def listSubscriptionPlan():
    NotifierConsumer().listSubscriptionPlans()

@list.command('subscription', context_settings=CONTEXT_SETTINGS)
def listSubscriptions():
    NotifierConsumer().getSubscriptions()

def setConfig(**kwargs):
    c = Config()
    propSize = 0
    for x,y in kwargs.items():
        if y:
            c.set(x,y)
            propSize = propSize+1
    print_help() if propSize == 0 else c.configWrite()



@main.command(context_settings=CONTEXT_SETTINGS)
@click.option("--planid", required=False, prompt="Enter subscription plan id", help="subscription plan id:")
@click.option("--currency", required=False, prompt="Enter subscription plan currency", help="subscription plan currency:")
@click.option("--price", required=False, type=int, prompt="Enter subscription price", help="subscription plan price:")
@click.option("--apikey", required=False, help="Consumer api key")
def subscribe(planid, price, currency, apikey):
    """Subscribe to a RIF Notifier plan"""
    NotifierConsumer().subscribe(planid, currency, price, apikey)


if __name__ == "__main__":
    main()