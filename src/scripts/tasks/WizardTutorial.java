package scripts.tasks;

import org.tribot.api2007.Game;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class WizardTutorial extends UniqueTask implements Task {

    private HashMap<String, Runnable> actionsMap;

    public WizardTutorial(String uniqueID){
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
        // Task is validated when player is inside the Wizard's area
        // and no other task is running according to the status.
        if (Arrays.stream(Constants.WIZARD_AREAS).anyMatch(Area::containsMyPlayer) &&
             States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Wizard Tutorial.\n Executing Wizard Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Wizard Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.WIZARD_TUTORIAL);
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
                                    Log.debug("Text found (Wizard): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO WIZARD ACTIONS): \n" + str);
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
        States.instance().removeTask(new WizardTutorial(Constants.WIZARD_TUTORIAL_ID));
        return;

    }

    private void initializeActions() {
        this.actionsMap.put("Follow the path to the wizard's house, where you will be shown how to cast spells.", this::talkToMagicInstructor);
        this.actionsMap.put("Open up the magic interface by clicking on the flashing icon.", this::openMagicTab);
        this.actionsMap.put("This is your magic interface. All of your spells can be found here.", this::talkToMagicInstructor);
        this.actionsMap.put("You now have some runes. All spells require runes to cast them.", this::windStrikeChicken);
        this.actionsMap.put("You're nearly finished with the tutorial. All you need to do now is move on to the mainland.", this::talkToMagicInstructor);
    }

    private boolean windStrikeChicken() {
        if (!(Inventory.contains(Constants.AIR_RUNE) && Inventory.contains(Constants.MIND_RUNE))) {
            if (!this.talkToMagicInstructor()) {
                return false;
            }
        }

        Optional<Npc> chicken = Query.npcs().nameEquals("Chicken").findBestInteractable();
        if (chicken.isEmpty()) {
            return false;
        }
        Magic.castOn("Wind Strike", chicken.get());
        Waiting.waitNormal(350, 100);
        return true;
    }

    private boolean openMagicTab() {
        if (GameTab.MAGIC.isOpen()) {
            return true;
        }
        GameTab.MAGIC.open();
        Waiting.waitNormal(350, 100);
        return true;
    }

    private boolean talkToMagicInstructor() {
        Optional<Npc> magicInstructor = Query.npcs().nameEquals("Magic Instructor").findBestInteractable();
        if (magicInstructor.isEmpty()) {
            return false;
        }
        magicInstructor.ifPresent(npc -> npc.interact("Talk-to"));
        Waiting.waitUntil(()->ChatScreen.isOpen());
        Waiting.waitNormal(350, 100);
        ChatScreen.handle("Yes.", "No, I'm not planning to do that.");
        Waiting.waitNormal(350, 100);
        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.WIZARD_AREAS);
        }
        return true;
    }

    

}
