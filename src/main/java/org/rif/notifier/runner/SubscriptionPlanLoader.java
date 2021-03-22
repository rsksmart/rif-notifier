package org.rif.notifier.runner;

import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.SubscriptionPlan;
import org.rif.notifier.validation.SubscriptionPlanValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    String DISABLE_SUBSCRIPTION_PLAN = "disableSubscriptionPlan";
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanLoader.class);

    SubscriptionPlanValidator subscriptionPlanValidator;
    DbManagerFacade dbManagerFacade;

    public SubscriptionPlanLoader(SubscriptionPlanValidator subscriptionPlanValidator, DbManagerFacade dbManagerFacade) {
        this.subscriptionPlanValidator = subscriptionPlanValidator;
        this.dbManagerFacade = dbManagerFacade;
    }

    public void run(String... args){
        List<String> argsList = Arrays.asList(args);
        try {
            //use command line argument loadSubscriptionPlan to run the app
            if(argsList.stream().anyMatch(s->s.equalsIgnoreCase(LOAD_SUBSCRIPTION_PLAN))) {
                //validate the json structure  and the contents from subscription-plan.json
                List<SubscriptionPlan> plans = subscriptionPlanValidator.validate();
                //ensure only enabled plans are being added and that subscription price specified
                saveSubscriptionPlans(plans);
                System.exit(0);
            }
            else if (argsList.stream().anyMatch(s->s.equalsIgnoreCase(DISABLE_SUBSCRIPTION_PLAN)))  {
                if(argsList.size() > 1) {
                    int id = Integer.parseInt(argsList.get(1));
                    SubscriptionPlan plan = dbManagerFacade.getSubscriptionPlanById(id);
                    plan.setStatus(false);
                    dbManagerFacade.saveSubscriptionPlan(plan);
                }
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

    /**
     * Save or update all the valid subscription plans
     * @param plansToBeAdded
     */
    protected void saveSubscriptionPlans(List<SubscriptionPlan> plansToBeAdded)   {
        if (plansToBeAdded.isEmpty())   {
            logger.warn("No plans specified in subscription-plan.json or the plans already exist in the database");
            return;
        }
        dbManagerFacade.saveSubscriptionPlans(plansToBeAdded);
    }

}
