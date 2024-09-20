package scripts.tasks;

import java.util.HashMap;
import java.util.Optional;

import org.tribot.api.input.Mouse;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.GameObject;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.types.Player;
import org.tribot.script.sdk.types.Widget;
import org.tribot.script.sdk.walking.LocalWalking;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

import javax.swing.text.html.Option;

public class MiningTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public MiningTutorial(String uniqueId) {
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
        // Task is validated when player is inside the mining area
        // and no other task is running according to the status.
        if (Constants.MINING_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Mining Tutorial.\n Executing Mining Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Mining Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.MINING_TUTORIAL);
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
                                    Log.debug("Text found (MININGTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO MININGTUT ACTIONS): \n" + str);
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
        States.instance().removeTask(new MiningTutorial(Constants.MINING_TUTORIAL_ID));
        return;

    }

    private void initializeActions(){
        actionsMap.put("Next let's get you a weapon, or more to the point, you can make your first weapon yourself.", this::talkToMiningInstructor);
        actionsMap.put("It's quite simple really. To mine a rock, all you need to do is click on it.", this::mineTinRocks);
        actionsMap.put("Now that you have some tin ore, you just need some copper.", this::mineCopperRocks);
        actionsMap.put("You now have some tin ore and some copper ore. You can smelt these into a bronze bar.", this::makeBronzeBar);
        actionsMap.put("Now it's time to make a bronze bar. You'll need some tin and copper ore though.", this::makeBronzeBar);
        actionsMap.put("You've made a bronze bar! Speak to the mining instructor and he'll show you how to make it into a weapon.", this::talkToMiningInstructor);
        actionsMap.put("To smith you'll need a hammer and enough metal bars to make the desired item, as well as a handy anvil.", this::smithBronzeDagger);
        actionsMap.put("Now you have the smithing menu open, you will see a list of all the things you can make.", this::smithBronzeDagger);


    }

    private boolean talkToMiningInstructor() {
        Optional<Npc> miningInstructor= Query.npcs().nameEquals("Mining Instructor").findBestInteractable();

        if (miningInstructor.isPresent()) {
            miningInstructor.map(npc -> npc.interact("Talk-to"));
            Waiting.waitUntil(5000, ()->ChatScreen.isOpen());
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            ChatScreen.handle();
            Waiting.waitNormal(350, 100);
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.MINING_AREA);
        }

        return true;

    }

    private boolean mineTinRocks(){
        boolean b = true;
        if (!Inventory.contains(Constants.BRONZE_PICKAXE)) {
            b = talkToMiningInstructor();
        } else if (b == false) {
            return false;
        }

        Optional<GameObject> tinRocks = Query.gameObjects().idEquals(Constants.TIN_ROCKS).findBestInteractable();

        if (tinRocks.isPresent()) {
            tinRocks.map(obj -> obj.interact("Mine"));
            Waiting.waitUntil(()->MyPlayer.isAnimating());
            Waiting.waitUntil(()->!MyPlayer.isAnimating());
            Waiting.waitNormal(350, 100);
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.MINING_AREA);
        }

        return true;
    }

    private boolean mineCopperRocks(){
        boolean b = true;
        if (!(Inventory.contains(Constants.BRONZE_PICKAXE))) {
            b = talkToMiningInstructor();
        } else if (b == false) {
            return false;
        }

        Optional<GameObject> copperRocks = Query.gameObjects().idEquals(Constants.COPPER_ROCKS).findBestInteractable();

        if (copperRocks.isPresent()) {
            copperRocks.map(obj -> obj.interact("Mine"));
            Waiting.waitUntil(()->MyPlayer.isAnimating());
            Waiting.waitUntil(()->!MyPlayer.isAnimating());
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.MINING_AREA);
        }

        return true;
    }

    private boolean makeBronzeBar(){
        boolean b = true;
        if (!(Inventory.contains(Constants.TIN_ORE)) ) {
            b = mineTinRocks();
        } else if (b == false) {
            return false;
        }

        if (!(Inventory.contains(Constants.COPPER_ORE)) ) {
            b = mineCopperRocks();
        } else if (b == false) {
            return false;
        }

        Optional<GameObject> furnace = Query.gameObjects().idEquals(Constants.FURNACE).findBestInteractable();

        if (furnace.isPresent()) {
            furnace.map(o -> o.interact("Use"));
            Waiting.waitUntil(()->MyPlayer.isAnimating());
            Waiting.waitUntil(()->!MyPlayer.isAnimating());
            Waiting.waitNormal(3000, 1000);
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.MINING_AREA);
        }

        Waiting.waitNormal(3500, 500);
        return true;
    }

    protected boolean smithBronzeDagger(){
        if (!(Inventory.contains(Constants.BRONZE_BAR))) {
            makeBronzeBar();
        }

        Optional<Integer> smithingMenuOpened = GameState.getVarcInt(989);
        if (smithingMenuOpened.orElse(0) == 1) {
            Helpers.moveRandomTilesAway(4);
            Waiting.waitNormal(5000, 200);
        }

        Optional<GameObject> anvil = Query.gameObjects().idEquals(Constants.ANVIL).findBestInteractable();

        if (anvil.isPresent()) {
            anvil.map(obj -> obj.interact("Smith"));
            Waiting.waitUntil(() -> GameState.getVarcInt(989).orElse(0) == 1); // Wait until smithing interface is opened.
            Waiting.waitNormal(350, 100);
        } else {
            return false;
        }

        Optional<Widget> daggerOption = Widgets.get(312, 9);

        if (daggerOption.isPresent()) {
            daggerOption.map(o -> o.click());
            Waiting.waitUntil(() -> MyPlayer.isAnimating());
            Waiting.waitUntil(() -> !MyPlayer.isAnimating());
            Waiting.waitNormal(350, 100);
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.MINING_AREA);
        }

        if (Inventory.contains(Constants.BRONZE_DAGGER)) {
            return true;
        }
        return false;
    }


}
