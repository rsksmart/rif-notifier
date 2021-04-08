import click
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
    """Configure RIF-Notifier"""
    setConfig(**kwargs)

@main.group(context_settings=CONTEXT_SETTINGS)
def list():
    """list subscription plans"""

@list.command('subscriptionplan', context_settings=CONTEXT_SETTINGS)
def listSubscriptionPlan():
    NotifierConsumer().listSubscriptionPlans()

def setConfig(**kwargs):
    c = Config()
    propSize = 0
    for x,y in kwargs.items():
        if y:
            c.set(x,y)
            propSize = propSize+1
    print_help() if propSize == 0 else c.configWrite()

if __name__ == "__main__":
    main()