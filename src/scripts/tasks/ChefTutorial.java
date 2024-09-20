package scripts.tasks;

import java.util.*;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Area;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.types.Player;
import scripts.Priority;
import scripts.Task;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

public class ChefTutorial extends UniqueTask implements Task {

    private HashMap<String, Runnable> actionsMap;

    public ChefTutorial(String uniqueID){
        super(uniqueID);
        this.actionsMap = new HashMap<>();
        initializeActions();
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public boolean validate() {
        // Task is validated when player is inside the Master Chef's area
        // and no other task is running according to the status.
        if (Arrays.stream(Constants.CHEF_AREAS).anyMatch(Area::containsMyPlayer) &&
             States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Chef Tutorial.\n Executing Chef Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Chef Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.CHEF_TUTORIAL);
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
                                    Helpers.waitNormalWithFatigue(500, 250);
                                    Log.debug("Text found (CHEF): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO CHEF ACTIONS): \n" + str);
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
        return;

    }

    private void initializeActions() {
        this.actionsMap.put("Talk to the chef indicated.", this::talkToMasterChef);
        this.actionsMap.put("This is the base for many meals.", this::makeDough);
        this.actionsMap.put("Dough can be baked into bread. To make dough you must mix flour with water.", this::makeDough);
        this.actionsMap.put("Now you have made the dough, you can bake it into some bread.", this::makeBreadWithDough);

    }

    public boolean talkToMasterChef(){
        Optional<Npc> masterChef = Query.npcs().nameEquals("Master Chef").findBestInteractable();

        if (masterChef.isPresent()) {
            masterChef.ifPresent(npc -> npc.interact("Talk-to"));
            Waiting.waitUntil(() -> ChatScreen.isOpen());
            Helpers.waitNormalWithFatigue(400, 200);
            ChatScreen.handle();
        } else {
            return false;
        }

        // Cancels the dialogue that may appear after talking when you are given tools.
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        return true;
    }
    //TODO
    public boolean makeDough(){
        // Check for Pot of Flour and Bucket of Water.
        // If we don't have Bucket of Flour and Bucket of Water, get them from Master Chef.
        if (!Inventory.contains(Constants.BUCKET_OF_WATER) &&
            !Inventory.contains(Constants.POT_OF_FLOUR) ){
            if (!talkToMasterChef()) {
                return false;
            }
        }
        // If we do, use Bucket of Water on Pot of Flour to make Bread Dough.
        Query.inventory()
             .idEquals(Constants.POT_OF_FLOUR)
             .findFirst()
             .ifPresent(
                        (f)->{
                            Query.inventory()
                                 .idEquals(Constants.BUCKET_OF_WATER)
                                 .findFirst()
                                 .ifPresent(
                                            (w) -> {
                                                w.useOn(f);
                                                Waiting.waitNormal(2500, 500);
                                            });
                        });

        Waiting.waitNormal(500, 250);
        // Dismiss dough made message.
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.CHEF_AREAS);
        }

        if (Inventory.contains(Constants.BREAD_DOUGH)) {
            return true;
        }
        return false;
    }

    public boolean makeBreadWithDough(){
        // Check if we have Bread Dough.
        // If we don't have Bread Dough, then we should make it.
        if (!Inventory.contains(Constants.BREAD_DOUGH)) {
            if (!talkToMasterChef()) {
                return false;
            }
        }
        // If we do, use Bread Dough on Range to make Bread.
        Query.inventory()
             .idEquals(Constants.BREAD_DOUGH)
             .findFirst()
             .ifPresent(
                        (d) -> {
                            Query.gameObjects()
                                 .idEquals(Constants.RANGE)
                                 .findBestInteractable()
                                 .ifPresent(
                                            (r)->{
                                                d.useOn(r);
                                                Waiting.waitUntil(15000, ()->MyPlayer.isAnimating());
                                                Waiting.waitUntil(15000, ()->!MyPlayer.isAnimating());
                                            });
                        });
        // Dismiss bread made message.
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.CHEF_AREAS);
        }
        return true;
    }
}
