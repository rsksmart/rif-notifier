package org.rif.notifier.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.models.entities.NotificationServiceType;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.models.entities.SubscriptionPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionPlanValidator extends BaseValidator    {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanValidator.class);

    private Resource subscriptionPlanJson;

    private NotifierConfig notifierConfig;

    private DbManagerFacade dbManagerFacade;

    SubscriptionPlanValidator(@Value("classpath:subscription-plan.json") Resource subscriptionPlanJson,
                              NotifierConfig nottifierConfig, DbManagerFacade dbManagerFacade) {
        this.subscriptionPlanJson = subscriptionPlanJson;
        this.notifierConfig = nottifierConfig;
        this.dbManagerFacade = dbManagerFacade;
    }

    public List<SubscriptionPlan> validate()    throws IOException {
        List<SubscriptionPlan> plans = validateJson();
        return validatePlans(plans);
    }

    /**
     * Validates the subscription-plan.json and loads the plans and prices into a List of
     * SubscriptionPlan objects
     * @return
     * @throws IOException
     */
    public List<SubscriptionPlan> validateJson() throws IOException {
        if(!subscriptionPlanJson.exists())  {
            throw new ValidationException("subscription-plan.json is not found in the classpath");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(subscriptionPlanJson.getInputStream(), SubscriptionPlan[].class));
        } catch(Exception e)    {
            throw new ValidationException("subscription-plan.json contains invalid data. " + e.getMessage(), e);
        }
    }

    /**
     * validate subscription plan preference, price list, validity, quantity and plan name
     * @param p
     */
    private void validatePlan(SubscriptionPlan p) {
        validatePlanNotificationPreference(p);
        validatePlanSubscriptionPriceList(p);
        if (StringUtils.isBlank(p.getName()))    {
            throw new ValidationException("Plan name is required");
        }
        else if(p.getValidity() == 0)  {
            throw new ValidationException("Validity must be greater than zero.");
        }

        else if(p.getNotificationQuantity() == 0) {
            throw new ValidationException("Notification Quantity must be greater than zero.");
        }
    }

    /*
     * check if notification preferences specified in json are enabled
     */
    private void validatePlanNotificationPreference(SubscriptionPlan plan)  {
        if(plan.getNotificationPreferences() == null || plan.getNotificationPreferences().isEmpty()) {
            throw new ValidationException("Atleast one notification preference must be specified for a subscription plan.");
        }
        boolean match = notifierConfig.getEnabledServices().stream().map(NotificationServiceType::toString)
                        .collect(Collectors.toList()).containsAll(plan.getNotificationPreferences());
        if (!match) {
            throw new ValidationException("Invalid notification preferences specified or notification preferences not enabled. Please review the json.");
        }
    }

    /*
     * ensure subscription price list is specified in the json
     */
    private void validatePlanSubscriptionPriceList(SubscriptionPlan plan) {
        if(plan.getSubscriptionPriceList() == null || plan.getSubscriptionPriceList().size() == 0)  {
            throw new ValidationException("Subscription price(s) is required as part of the plan.");
        }
        plan.getSubscriptionPriceList().forEach(price->{
            if(price.getPrice() == null || price.getPrice().equals(BigInteger.ZERO))    {
                throw new ValidationException("Subscription price cannot be zero");
            }
            else if(price.getCurrency()  == null)    {
                throw new ValidationException("Currency must be specified");
            }
            else if (price.getCurrency().getAddress() == null || price.getCurrency().getName() == null) {
                throw new ValidationException("Currency address and name must be specified");
            }
            else if(!notifierConfig.getAcceptedCurrencies().contains(price.getCurrency().getName() ))    {
                throw new ValidationException("Currency is not in the list of accepted currencies. Please provide a currency from rif.notifier.subscription.currencies list.");
            }
            saveCurrency(price);
            //associate the subscription plan with the price
            price.setSubscriptionPlan(plan);
        });
    }

    /**
     * Saves the currency in db if one doesn't exist already.
     * @param price
     */
    protected void saveCurrency(SubscriptionPrice price) {
            String name = price.getCurrency().getName();
            //if the currency specified doesnt exist create it, or else just return the existing one
            price.setCurrency(dbManagerFacade.getCurrencyByName(name).orElseGet(
                    ()->dbManagerFacade.saveCurrency(price.getCurrency())));
    }


    public List<SubscriptionPlan> validatePlans(List<SubscriptionPlan> plans)    {
        List<SubscriptionPlan> existingPlans = dbManagerFacade.getSubscriptionPlans();
        validateCurrencies(plans);
        //match = plans.stream().allMatch(p->p.getSubscriptionPriceList() != null && p.getSubscriptionPriceList().size() > 0);
        //validate existing plan ids and all fields for required. Also if similar plans exist then throw validationexception
        //a plan is considered similar if name is same, or if it has same notificationpreferences, quantity, and validity
        return plans.stream().filter(plan->existingPlans.stream().noneMatch(
                p->{
                    if(plan.getId() == 0 && (p.getName().equalsIgnoreCase(plan.getName()) ||  (p.getValidity() == plan.getValidity() &&
                            p.getNotificationPreferences().containsAll(plan.getNotificationPreferences()) &&
                            p.getNotificationQuantity() == plan.getNotificationQuantity() &&
                            p.getSubscriptionPriceList().containsAll(plan.getSubscriptionPriceList())
                    ))) {
                        throw new ValidationException("Subscription plan " + plan.getName() + " cannot be created. Similar plan already exists in the database");
                    }
                    return false;
                }
        )).map(planJson->{
            int planId = planJson.getId();
            if (planId != 0) {
                existingPlans.stream().filter(plandb ->plandb.getId() == planId).findFirst()
                        .orElseThrow (()->new ValidationException("Invalid plan id specified for update in subscription-plan.json. Please provide the correct id"));
            }
            validatePlan(planJson);
            return planJson;
        }).collect(Collectors.toList());
    }


    /*
     * Validates that same currency name with different address not specified and
     * same currency address with different names not specified
     */
    private void validateCurrencies(List<SubscriptionPlan> plans)   {
        List<Currency> currencies = new ArrayList<>(5);
        plans.forEach(plan->{
            currencies.addAll(plan.getSubscriptionPriceList().stream().map(SubscriptionPrice::getCurrency).collect(Collectors.toList()));
        });
        long distinctCurrencies = currencies.stream().distinct().count();
        long distinctCurrencyNames = currencies.stream().map(c->c.getName()).distinct().count();
        long distinctCurrencyAddresses = currencies.stream().map(c->c.getAddress()).distinct().count();
        if(distinctCurrencies != distinctCurrencyNames || distinctCurrencies != distinctCurrencyAddresses)  {
            throw new ValidationException("Same currency name with different addresses found or same currency address with different names found Please correct the json");
        }
    }
}
