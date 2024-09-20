package scripts.tasks;

import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.types.Player;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

import java.util.HashMap;
import java.util.Optional;


public class ChapelTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public ChapelTutorial(String uniqueId) {
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
        // Task is validated when player is inside the chapel area
        // and no other task is running according to the status.
        if (Constants.CHAPEL_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Chapel Tutorial.\n Executing Chapel Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Chapel Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.CHAPEL_TUTORIAL);
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
                                    Log.debug("Text found (CHAPELTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO CHAPELTUT ACTIONS): \n" + str);
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
        States.instance().removeTask(new ChapelTutorial(Constants.CHAPEL_TUTORIAL_ID));
        return;
    }

    private void initializeActions(){
        this.actionsMap.put("Follow the path to the chapel and enter it.", this::talkToBrotherBrace);
        this.actionsMap.put("Click on the flashing icon to open the Prayer menu.", this::openPrayerTab);
        this.actionsMap.put("Talk with Brother Brace and he'll tell you about prayers.", this::talkToBrotherBrace);
        this.actionsMap.put("You should now see another new icon. Click on the flashing face icon to open your friends and ignore lists.", this::openFriendsTab);
        this.actionsMap.put("These two lists can be very helpful for keeping track of your friends or for blocking people you don't like.", this::talkToBrotherBrace);
    }

    private void openFriendsTab() {
        if (GameTab.FRIENDS.isOpen()) {
           return;
        }
        GameTab.FRIENDS.open();
        Waiting.waitNormal(350, 100);
    }

    private void openPrayerTab() {
        if (GameTab.PRAYER.isOpen()) {
            return;
        }
        GameTab.PRAYER.open();
        Waiting.waitNormal(350, 100);
    }

    private boolean talkToBrotherBrace() {
        Optional<Npc> brotherBrace = Query.npcs().nameEquals("Brother Brace").findBestInteractable();
        if (brotherBrace.isEmpty()) {
            return false;
        }
        brotherBrace.ifPresent(npc -> npc.interact("Talk-to"));
        Waiting.waitUntil(()-> ChatScreen.isOpen());
        Waiting.waitNormal(350, 100);
        ChatScreen.handle();
        return true;
    }


}
