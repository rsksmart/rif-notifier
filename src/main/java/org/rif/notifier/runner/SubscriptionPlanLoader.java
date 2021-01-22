package org.rif.notifier.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loader class used to create subscription plans based on subscription-plan.json specified in classpath
 * A sample subscription-plan.json is placed under src/main/resources
 */
@Component
public class SubscriptionPlanLoader implements CommandLineRunner {
    String LOAD_SUBSCRIPTION_PLAN = "loadSubscriptionPlan";
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanLoader.class);

    @Value("classpath:subscription-plan.json")
    private Resource subscriptionPlanJson;

    @Autowired
    NotifierConfig notifierConfig;

    @Autowired
    DbManagerFacade dbManagerFacade;

    public void run(String... args){
        List<String> argsList = Arrays.asList(args);
        try {
            if(argsList.stream().anyMatch(s->s.equalsIgnoreCase(LOAD_SUBSCRIPTION_PLAN))) {
                //validate the json structure from subscription-plan.json
                List<SubscriptionPlan> plans = validateJson();
                //ensure only enabled plans are being added and that subscription price specified
                validatePlans(plans);
                createSubscriptionPlans(plans);
                System.exit(0);
            }
        }
        catch(IOException e) {
            logger.error("Failed to read subscription plan from subscription-plan.json. Please ensure the json is in correct format. ",e);
            System.exit(1);
        }
        catch(Exception e)  {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private List<SubscriptionPlan> validateJson() throws IOException {
        if(!subscriptionPlanJson.exists())  {
            throw new ValidationException("subscription-plan.json is not found in the classpath");
        }
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(subscriptionPlanJson.getInputStream(), SubscriptionPlan[].class));
    }

    private void validatePlans(List<SubscriptionPlan> plans)    {
        //check if notification preferences specified in json are enabled
        boolean match = plans.stream().allMatch(
                p->notifierConfig.getEnabledServices().stream().map(NotificationServiceType::toString)
                        .collect(Collectors.toList()).containsAll(p.getNotificationPreferences()));
        if (!match) {
            throw new ValidationException("Invalid notification preferences specified or notification preferences not enabled. Please review the json.");
        }
        //ensure subscription price list is specified in the json
        match = plans.stream().allMatch(p->p.getSubscriptionPriceList() != null && p.getSubscriptionPriceList().size() > 0);
        if (!match) {
            throw new ValidationException("Subscription price(s) is required as part of the plan.");
        }
        //validate all fields for required
        plans.stream().forEach(p->{
            if(p.getValidity() == 0)  {
                throw new ValidationException("Validity must be greater than zero.");
            }
            else if(p.getNotificationPreferences() == null || p.getNotificationPreferences().isEmpty()) {
                throw new ValidationException("Atleast one notification preference must be specified.");
            }
            else if(p.getNotificationQuantity() == 0) {
                throw new ValidationException("Notification Quantity must be greater than zero.");
            }
            p.getSubscriptionPriceList().forEach(price->{
               if(price.getPrice() == null || price.getPrice().equals(BigInteger.ZERO))    {
                   throw new ValidationException("Subscription price cannot be zero");
               }
               else if(StringUtils.isBlank(price.getCurrency() ))    {
                    throw new ValidationException("Currency must be specified");
                }
               else if(!notifierConfig.getAcceptedCurrencies().contains(price.getCurrency() ))    {
                   throw new ValidationException("Currency is not in the list of accepted currencies. Please provide a currency from rif.notifier.subscription.currencies list.");
               }
            });

        });
    }

    protected void createSubscriptionPlans(List<SubscriptionPlan> plans)   {
        List<SubscriptionPlan> existingPlans = dbManagerFacade.getSubscriptionPlans();
        //check if plans already exist and return only those plans that are already in db to be saved
        List<SubscriptionPlan> plansToBeAdded = plans.stream().filter(plan->existingPlans.stream().noneMatch(
                p->{
                    return p.getValidity() == plan.getValidity() &&
                            p.getNotificationPreferences().containsAll(plan.getNotificationPreferences()) &&
                            p.getNotificationQuantity() == plan.getNotificationQuantity()
                            ;
                }
        )).collect(Collectors.toList());
        //associate price with plan
        plansToBeAdded.forEach(p->p.getSubscriptionPriceList().stream().forEach(price->price.setSubscriptionPlan(p)));
        if (plansToBeAdded.isEmpty())   {
            logger.warn("No plans specified in subscription-plan.json or the plans already exist in the database");
            return;
        }
        dbManagerFacade.saveSubscriptionPlans(plansToBeAdded);
    }

}