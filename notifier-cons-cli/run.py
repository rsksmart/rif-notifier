import click
from NotifierConsumer import NotifierConsumer
from Config import Config

CONTEXT_SETTINGS = dict(help_option_names=['-h', '--help'])

class AliasedGroup(click.Group):
    def get_command(self, ctx, cmd_name):
        if cmd_name == 'sp':
            cmd_name = 'subscriptionplan'
        elif cmd_name == 'sub':
            cmd_name = 'subscription'
        return super().get_command(ctx, cmd_name)


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

@main.group(context_settings=CONTEXT_SETTINGS, cls=AliasedGroup)
def list():
    """list subscription and subscription plans"""

@list.command('subscriptionplan', context_settings=CONTEXT_SETTINGS, help="sp - list subscription plans")
def listSubscriptionPlan():
    NotifierConsumer().getSubscriptionPlans()

@list.command('subscription', context_settings=CONTEXT_SETTINGS, help="sub - list user subscriptions")
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

def subscribe_options(function):
    function = click.option("--price", required=False, type=int, prompt="Enter subscription price", help="subscription plan price:")(function)
    function = click.option("--currency", required=False, prompt="Enter subscription plan currency", help="subscription plan currency:")(function)
    function = click.option("--planid", required=False, prompt="Enter subscription plan id", help="subscription plan id:")(function)
    return function


@main.command(context_settings=CONTEXT_SETTINGS)
@subscribe_options
def subscribe(planid, currency, price):
    """Subscribe to a RIF Notifier plan"""
    NotifierConsumer().subscribeOrRenew(planid, currency, price)


@main.command(context_settings=CONTEXT_SETTINGS)
@subscribe_options
@click.option("--previoussubscriptionhash", required=False, prompt="Enter previous subscription hash", help="previous subscription hash:")
def renew(previoussubscriptionhash, planid, price, currency):
    """Renew a RIF Notifier plan"""
    NotifierConsumer().subscribeOrRenew(planid, currency, price, previoussubscriptionhash)


if __name__ == "__main__":
    main()