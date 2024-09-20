package scripts.tasks;

import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Player;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

import java.util.HashMap;
import java.util.Optional;


public class FinancialTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public FinancialTutorial(String uniqueId) {
        super(uniqueId);
        this.actionsMap = new HashMap<>();
        initializeActions();
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public boolean validate() {
        // Task is validated when player is inside the Survival Expert's area
        // and no other task is running according to the status.
        if (Constants.FINANCIAL_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Financial Tutorial.\n Executing Financial Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Financial Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.FINANCIAL_TUTORIAL);
        Log.debug(States.instance().getStatus());

        Optional<Player> myPlayer = MyPlayer.get();

        boolean done = false;
        while (!done) {
            // Variables captured by lambda must be effectively final.
            boolean[] foundAnyText = {false}; // Declared as array reference to be a final.

            this.actionsMap.keySet()
                    .stream()
                    .forEach(
                            (str) -> {
                                if (Query.widgets().textContains(str).isAny()) {
                                    Helpers.waitNormalWithFatigue(600, 300);
                                    Log.debug("Text found (FINANCIALTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO FINANCIALTUT ACTIONS): \n" + str);
                                }
                            });

            if (foundAnyText[0] == false) {
                done = true;
            }
        }

        // Finished
        States.instance().setStatus(Status.WAITING_NEXT_TASK);
        Log.debug(States.instance().getStatus().getMessage());

        States.instance().addFatigue(0.01);

        States.instance().removeTask(new StartingTutorial(Constants.STARTING_TUTORIAL_ID));
        States.instance().removeTask(new SurvivalTutorial(Constants.SURVIVAL_TUTORIAL_ID));
        States.instance().removeTask(new ChefTutorial(Constants.CHEF_TUTORIAL_ID));
        States.instance().removeTask(new QuestingTutorial(Constants.QUEST_TUTORIAL_ID));
        States.instance().removeTask(new MiningTutorial(Constants.MINING_TUTORIAL_ID));
        States.instance().removeTask(new CombatTutorial(Constants.COMBAT_TUTORIAL_ID));
        States.instance().removeTask(new BankingTutorial(Constants.BANKING_TUTORIAL_ID));
        States.instance().removeTask(new FinancialTutorial(Constants.FINANCIAL_TUTORIAL_ID));
        return;
    }

    private void initializeActions(){
        this.actionsMap.put("The guide here will tell you all about your account.", this::talkToAccountGuide);
        this.actionsMap.put("Click on the flashing icon to open your Account Management menu.", this::openAccountManagementTab);
        this.actionsMap.put("This is your Account Management menu where you can control various aspects of your account.", this::talkToAccountGuide);
    }

    private void openAccountManagementTab() {
        if (GameTab.ACCOUNT.isOpen()) {
            return;
        }
        GameTab.ACCOUNT.open();
        Waiting.waitNormal(350, 100);
        return;
    }

    private boolean talkToAccountGuide() {
        if (!Query.npcs().nameEquals("Account Guide").findBestInteractable().isPresent()) {
            return false;
        }
        Query.npcs().nameEquals("Account Guide").findBestInteractable().ifPresent(npc -> npc.interact("Talk-to"));
        Waiting.waitUntil(()-> ChatScreen.isOpen());
        Waiting.waitNormal(350, 100);
        ChatScreen.handle();
        return true;
    }
}
