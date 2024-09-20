package scripts.tasks;

import org.tribot.api.input.Mouse;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.GameObject;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.types.Player;
import org.tribot.script.sdk.types.Widget;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;


public class BankingTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public BankingTutorial(String uniqueId) {
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
        // Task is validated when player is inside the banking area
        // and no other task is running according to the status.
        if (Constants.BANKING_AREA.containsMyPlayer() &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Banking Tutorial.\n Executing Banking Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Banking Tutorial.");
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
                                    Log.debug("Text found (BANKINGTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO BANKINGTUT ACTIONS): \n" + str);
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
        return;
    }

    private void initializeActions(){
        this.actionsMap.put("Follow the path and you will come to the front of the building.", this::useBankBooth);
        this.actionsMap.put("This is your bank. You can store things here for safekeeping.", this::usePollBooth);
        this.actionsMap.put("Now it's time for a quick look at polls.", this::usePollBooth);
    }

    private boolean usePollBooth() {
        // Check for poll booth.
        if (Query.gameObjects().idEquals(Constants.POLL_BOOTH).findBestInteractable().isEmpty()) {
            return false;
        }

        // Use poll booth.
        Optional<GameObject> pollBooth = Query.gameObjects().idEquals(Constants.POLL_BOOTH).findBestInteractable();
        pollBooth.ifPresent((p) -> {
                                        double r = Math.random();
                                        // 50% chance to use poll booth by left-clicking, 50% chance to use by right-clicking.
                                        if (r < 50) {
                                            p.interact("Use");
                                        } else {
                                            p.click();
                                        }
                                        Waiting.waitNormal(3500, 500);
                                   });

        // Handle chat (that appears upon the first time using it)
        if (ChatScreen.isOpen()) {
            ChatScreen.handle();
            Waiting.waitNormal(3500, 500);
        }

        // Check if poll booth interface is opened.
        if (Widgets.get(310, 2).isEmpty()) {
            return false;
        }

        // Closes the poll booth interface.
        Optional<Widget> pollBoothInterface = Widgets.get(310, 2);
        pollBoothInterface.ifPresent((i) -> {
                                                Random random = new Random();
                                                int pX = i.getBounds().x + random.nextInt(19) + 473;
                                                int pY = i.getBounds().y + random.nextInt(18) + 10;
                                                Point p = new Point(pX, pY);
                                                Mouse.click(p, 1);
                                                Waiting.waitNormal(350, 100);
                                            });

        return true;
    }

    private boolean useBankBoothThenUsePollBooth(){
        return useBankBooth() && usePollBooth();
    }
    private boolean useBankBooth() {
        if (Query.gameObjects().idEquals(Constants.BANKING_BOOTH).findBestInteractable().isEmpty()) {
            return false;
        }

        Optional<GameObject> bankBooth = Query.gameObjects().idEquals(Constants.BANKING_BOOTH).findBestInteractable();
        bankBooth.ifPresent(b -> b.interact("Use"));
        
        Waiting.waitNormal(3500, 500);
        if (Widgets.get(12, 3).isEmpty()) {
            return false;
        }
        // X bank interface
        Widgets.get(12, 3).ifPresent((i) -> {
                                                Random random = new Random();
                                                int pX = i.getBounds().x + random.nextInt(16) + 456;
                                                int pY = i.getBounds().y + random.nextInt(17) + 5;
                                                Point p = new Point(pX, pY);
                                                Mouse.click(p, 1);
                                                Waiting.waitNormal(500, 200);
        });

        return true;
    }


}
