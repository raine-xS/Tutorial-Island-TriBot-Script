package scripts.tasks;

import java.util.HashMap;
import java.util.Optional;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.types.GameObject;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Player;
import scripts.UniqueTask;
import scripts.Priority;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;


public class SurvivalTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public SurvivalTutorial(String uniqueId) {
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
        if (Constants.SURVIVAL_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Survival Tutorial.\n Executing Survival Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Survival Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.SURVIVAL_TUTORIAL);
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
                                    Log.debug("Text found (SURVIVAL): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO SURVIVAL ACTIONS): \n" + str);
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
        return;

    }

    private boolean chopTree(){
        // Firstly attempts to get a Bronze Axe from Survival Expert if we don't have one.
        while (!(Inventory.contains(Constants.BRONZE_AXE))) {
            if (talkToSurvivalExpert() == false) {
                return false;
            }
        }

        // Tree Object
        Optional<GameObject> tree = Query.gameObjects()
                                    .nameEquals("Tree")
                                    .findBestInteractable();

        // Attempts to chop tree.
        if (tree.isPresent()) {
            tree.ifPresent(gameObject -> gameObject.interact("Chop down"));
            Waiting.waitUntil(15000, () -> MyPlayer.isAnimating());
            Waiting.waitUntil(30000, () -> !MyPlayer.isAnimating());
        } else {
            return false;
        }

        // Dismiss chopped tree dialogue.
        Log.debug("ChatScreen.isOpen(): " + ChatScreen.isOpen());
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        return true;
    }

    private boolean talkToSurvivalExpert(){

        Optional<Npc> survivalExpert = Query.npcs().nameEquals("Survival Expert")
                                                   .findBestInteractable();

        if (survivalExpert.isPresent()) {
            survivalExpert.ifPresent(npc -> npc.interact("Talk-to"));
            Waiting.waitUntil(() -> ChatScreen.isOpen());
            Helpers.waitNormalWithFatigue(400, 200);
            ChatScreen.handle();
        } else {
            return false;
        }

        // Cancels the dialogue that may appear after talking when you are given tools.
        Log.debug("ChatScreen.isOpen(): " + ChatScreen.isOpen());
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        return true;
    }

    private void openSkills(){
        GameTab.SKILLS.open();
        return;
    }

    private void openInventory(){
        GameTab.INVENTORY.open();
        return;
    }

    // TODO: Check this for bugs
    private boolean makeFire(){
        //Firstly check if we Logs and Tinderbox.
        //Open inventory if it's not open.
        if (!GameTab.INVENTORY.isOpen()) {
            openInventory();
        }
        //If we don't have logs, we should chop a tree.
        if (!Inventory.contains(Constants.LOGS)) {
            if (!chopTree()) {
                return false;
            }
        }
        //If we don't have tinderbox, we should get one from Survival Expert.
        if (!Inventory.contains(Constants.TINDERBOX)) {
            if (!talkToSurvivalExpert()) {
                return false;
            }
        }
        //Checks if we are standing on an unsuitable place to make a fire and
        //if we are, move to a suitable place.
        if (Query.gameObjects().tileEquals(MyPlayer.getTile()).isAny()) {
            Helpers.moveRandomTilesAway(1);
        }

        //Use Logs on Tinderbox.
        // If Logs is in inventory,
        Query.inventory()
             .idEquals(Constants.LOGS)
             .findFirst()
             .ifPresent(
                         // and Tinderbox is in inventory:
                         (logs) -> {
                            Query.inventory()
                                 .idEquals(Constants.TINDERBOX)
                                 .findFirst()
                                 .ifPresent(
                                            // use Tinderbox on Logs.
                                            (obj) -> {
                                                obj.useOn(logs);
                                                Waiting.waitUntil(()->MyPlayer.isAnimating());
                                                Waiting.waitUntil(()->!MyPlayer.isAnimating());
                                                Waiting.waitNormal(1000, 200); // Wait for main player to step off fire after animation.
                                            });
                         });



        //Dismiss fire made dialogue.
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        //Checks if we've made the fire and return true, otherwise false.
        if (Query.gameObjects().idEquals(Constants.FIRE).maxDistance(1).isAny()) {
            Log.debug("Made a fire.");
            return true;
        } else {
            Log.debug("Failed to make a fire.");
            return false;
        }
    }

    //TODO: COOK SHRIMP
    private boolean cookShrimp(){
        // Firstly check if we have Raw Shrimps and if there's a Fire nearby.
        // If we don't have Raw Shrimps, we should catch some.
        if (!Inventory.contains(Constants.RAW_SHRIMPS)) {
            if (!catchShrimp()) {
                return false;
            }
        }
        // If there's no Fire nearby, we should make one.
        if (!Query.gameObjects().idEquals(Constants.FIRE).isAny()) {
            if (!makeFire()) {
                return false;
            }
        }
        // Use Raw Shrimps on Fire.
        Query.inventory()
             .idEquals(Constants.RAW_SHRIMPS)
             .findRandom()
             .ifPresent(
                        (s) -> {
                            Query.gameObjects()
                                 .idEquals(Constants.FIRE)
                                 .findBestInteractable()
                                 .ifPresent(
                                            (f)->{
                                                s.useOn(f);
                                                Waiting.waitUntil(()->MyPlayer.isAnimating());
                                                Waiting.waitUntil(()->!MyPlayer.isAnimating());
                                            });
                        });
        
        //Dismiss shrimp cooked dialogue
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        //Check if we have Cooked Shrimp and return true if we do, otherwise false
        //(Sometime the shrimp is burnt).
        if (Inventory.contains(Constants.SHRIMPS)) {
            return true;
        } else {
            Inventory.drop(Constants.BURNT_SHRIMP);
            return false;
        }
    }

    private boolean catchShrimp(){
        //Firstly checks if we have a fishing net and attempts to get one from Survival Expert if we don't.
        while (!(Inventory.contains(Constants.SMALL_FISHING_NET))) {
            if (talkToSurvivalExpert() == false) {
                return false;
            }
        }

        // Fishing Spot Object
        Optional<Npc> fishingSpot = Query.npcs().nameEquals("Fishing spot")
                                                .findBestInteractable();

        // Attempts to catch shrimp.
        if (fishingSpot.isPresent()) {
            fishingSpot.ifPresent(spot -> spot.interact("Net"));
            Waiting.waitUntil(15000, () -> MyPlayer.isAnimating());
            Waiting.waitUntil(30000, () -> !MyPlayer.isAnimating());
        } else {
            return false;
        }

        // Dismiss caught fish dialogue.
        Log.debug("ChatScreen.isOpen(): " + ChatScreen.isOpen());
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.SURVIVAL_AREA);
        }

        return true;
    }



    private void initializeActions(){
        this.actionsMap.put("Follow the path to find the next instructor. Clicking on the ground will walk you to that point.", this::talkToSurvivalExpert);
        this.actionsMap.put("To view the item you've been given, you'll need to open your inventory.", this::openInventory);
        this.actionsMap.put("This is your inventory. You can view all of your items here, including the net you've just been given.", this::catchShrimp);
        this.actionsMap.put("Click on the flashing bar graph icon near the inventory button to see your skills menu.", this::openSkills);
        this.actionsMap.put("On this menu you can view your skills.", this::talkToSurvivalExpert);
        this.actionsMap.put("It's time to cook your shrimp. However, you require a fire to do that which means you need some logs.", this::chopTree);
        this.actionsMap.put("Now that you have some logs, it's time to light a fire.", this::makeFire);
        this.actionsMap.put("Now it's time to get cooking.", this::cookShrimp);
        this.actionsMap.put("Oh no, you've just burnt your meal!", this::cookShrimp);
    }
}
