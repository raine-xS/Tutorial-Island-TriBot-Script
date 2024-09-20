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

public class QuestingTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public QuestingTutorial(String uniqueId) {
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
        // Task is validated when player is inside the Questing area
        // and no other task is running according to the status.
        if (Constants.QUEST_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Quest Tutorial.\n Executing Quest Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Quest Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.QUESTING_TUTORIAL);
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
                                    Log.debug("Text found (QUESTINGTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO QUESTINGTUT ACTIONS): \n" + str);
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
        States.instance().removeTask(new QuestingTutorial(Constants.SURVIVAL_TUTORIAL_ID));
        States.instance().removeTask(new ChefTutorial(Constants.CHEF_TUTORIAL_ID));
        States.instance().removeTask(new QuestingTutorial(Constants.QUEST_TUTORIAL_ID));
        return;

    }

    private void initializeActions(){
        this.actionsMap.put("It's time to learn about quests!", this::talkToQuestGuide);
        this.actionsMap.put("Click on the flashing icon to the left of your inventory.", this::openQuestJournal);
        this.actionsMap.put("This is your quest journal. It lists every quest in the game.", this::talkToQuestGuide);


    }

    public void openQuestJournal(){
        GameTab.QUESTS.open();

    }

    public boolean talkToQuestGuide(){
        Optional<Npc> questGuide = Query.npcs().nameEquals("Quest Guide").findBestInteractable();

        if (questGuide.isPresent()) {
            questGuide.map(npc -> npc.interact("Talk-to"));
            Waiting.waitUntil(5000, ()->ChatScreen.isOpen());
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            ChatScreen.handle();
            return true;
        }

        return false;
    }

}
