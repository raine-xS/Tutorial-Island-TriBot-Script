package scripts.tasks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Area;
import scripts.UniqueTask;
import scripts.Priority;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

public class WalkToNextTask extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;

    public WalkToNextTask(String uniqueId) {
        super(uniqueId);
        this.actionsMap = new HashMap<>();
        initializeActions();
    }

    @Override
    public Priority priority() {
        //Priority is set to low so that walking is only activated when
        //none of the other tasks can be done.
        return Priority.LOW;
    }

    @Override
    public boolean validate() {
        //Task is validated when player is not currently doing a task.
        if (States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Walking To Next Task.\n Executing Walking To Next Task.");
            return true;
        } else {
            Log.debug("Validation failed for: Walking To Next Task.");
            return false;
        }
    }

    @Override
    public void execute() {
        States.instance().setStatus(Status.WALKING_TO_TASK);
        //Iterate through each actionsMap's String keys and runs the corresponding walk function if
        //it matches the tutorial step determined by the given text in-game.
        this.actionsMap.keySet()
                    .stream()
                    .forEach(
                                (str) -> {
                                    if (Query.widgets().textContains(str).isAny()) {
                                        Log.debug("Text found: " + str);
                                        this.actionsMap.get(str).run();
                                        Waiting.waitNormal(850, 85);
                                    } else {
                                        Log.debug("Text not found: \n" + str);
                                        States.instance().setStatus(Status.WAITING_NEXT_TASK);
                                        return;
                                    }
                                });

        // Finished Walking
        States.instance().setStatus(Status.WAITING_NEXT_TASK);
        return;
    }

    private void initializeActions(){
        this.actionsMap.put("It's time to meet your first instructor. To continue, all you need to do is click on the door.", this::walkToSurvivalTutorial);
        this.actionsMap.put("Follow the path to find the next instructor. Clicking on the ground will walk you to that point.", this::walkToSurvivalTutorial);
        this.actionsMap.put("To view the item you've been given, you'll need to open your inventory.", this::walkToSurvivalTutorial);
        this.actionsMap.put("This is your inventory. You can view all of your items here, including the net you've just been given.", this::walkToSurvivalTutorial);
        this.actionsMap.put("Click on the flashing bar graph icon near the inventory button to see your skills menu.", this::walkToSurvivalTutorial);
        this.actionsMap.put("On this menu you can view your skills.", this::walkToSurvivalTutorial);
        this.actionsMap.put("It's time to cook your shrimp. However, you require a fire to do that which means you need some logs.", this::walkToSurvivalTutorial);
        this.actionsMap.put("Now that you have some logs, it's time to light a fire.", this::walkToSurvivalTutorial);
        this.actionsMap.put("Now it's time to get cooking.", this::walkToSurvivalTutorial);
        this.actionsMap.put("Well done, you've just cooked your first meal!", this::walkToChefTutorial);
        this.actionsMap.put("Well done, you've just cooked another meal!", this::walkToChefTutorial);
        this.actionsMap.put("Oh no, you've just burnt your meal!", this::walkToChefTutorial);
        this.actionsMap.put("Follow the path until you get to the door with the yellow arrow above it.", this::walkToChefTutorial);
        this.actionsMap.put("Talk to the chef indicated.", this::walkToChefTutorial);
        this.actionsMap.put("This is the base for many meals.", this::walkToChefTutorial);
        this.actionsMap.put("Dough can be baked into bread. To make dough you must mix flour with water.", this::walkToChefTutorial);
        this.actionsMap.put("Now you have made the dough, you can bake it into some bread.", this::walkToChefTutorial);
        this.actionsMap.put("Well done! You've baked your first loaf of bread.", this::walkToQuestingTutorial);
        this.actionsMap.put("When navigating the world, you can either run or walk.", this::walkToQuestingTutorial);
        this.actionsMap.put("Follow the path to the next guide. When you get there, click on the door to pass through it.", this::walkToQuestingTutorial);
        this.actionsMap.put("It's time to learn about quests!", this::walkToQuestingTutorial);
        this.actionsMap.put("This is your quest journal. It lists every quest in the game.", this::walkToQuestingTutorial);
        this.actionsMap.put("Click on the flashing icon to the left of your inventory.", this::walkToQuestingTutorial);
        this.actionsMap.put("It's time to enter some caves.", this::walkToMiningArea);
        this.actionsMap.put("Next let's get you a weapon, or more to the point, you can make your first weapon yourself.", this::walkToMiningArea);
        this.actionsMap.put("It's quite simple really. To mine a rock, all you need to do is click on it.", this::walkToMiningArea);
        this.actionsMap.put("Now that you hve some tin ore, you just need some copper.", this::walkToMiningArea);
        this.actionsMap.put("You now have some tin ore and some copper ore. You can smelt these into a bronze bar.", this::walkToMiningArea);
        this.actionsMap.put("Now it's time to make a bronze bar. You'll need some tin and copper ore though.", this::walkToMiningArea);
        this.actionsMap.put("You've made a bronze bar! Speak to the mining instructor and he'll show you how to make it into a weapon.", this::walkToMiningArea);
        this.actionsMap.put("To smith you'll need a hammer and enough metal bars to make the desired item, as well as a handy anvil.", this::walkToMiningArea);
        this.actionsMap.put("Now you have the smithing menu open, you will see a list of all the things you can make.", this::walkToMiningArea);
        this.actionsMap.put("Congratulations, you've made your first weapon. Now it's time to move on.", this::walkToCombatArea);
        this.actionsMap.put("In this area you will find out about melee and ranged combat.", this::walkToCombatArea);
        this.actionsMap.put("You now have access to a new interface. Click on the flashing icon of a man, the one to the right of your backpack icon.",this::walkToCombatArea);
        this.actionsMap.put("This is your worn inventory. Here you can see what items you have equipped.", this::walkToCombatArea);
        this.actionsMap.put("You can see what items you are wearing in the worn inventory to the left of the screen, with their combined statistics on the right.", this::walkToCombatArea);
        this.actionsMap.put("You're now holding your dagger. Clothes, armour, weapons, and more are equipped like this.", this::walkToCombatArea);
        this.actionsMap.put("To unequip an item, go to your worn inventory and click on the item.", this::walkToCombatArea);
        this.actionsMap.put("Click on the flashing crossed swords icon to open the combat interface.", this::walkToCombatArea);
        this.actionsMap.put("This is your combat interface. From here, you can select the attack style that you'll use in combat.", this::walkToCombatArea);
        this.actionsMap.put("Pass through the gate and talk to the combat instructor.", this::walkToCombatArea);
        this.actionsMap.put("Now you have a bow and some arrows.", this::walkToCombatArea);
        this.actionsMap.put("You're now holding your dagger. Clothes, armour, weapons and more are equipped like this.", this::walkToCombatArea);
        this.actionsMap.put("It's time to slay some rats!", this::walkToCombatArea);
        this.actionsMap.put("You have completed the tasks here. To move on, click on the indicated ladder.", this::walkToBankingArea);
        this.actionsMap.put("Follow the path and you will come to the front of the building.", this::walkToBankingArea);
        this.actionsMap.put("This is your bank. You can store things here for safekeeping.", this::walkToBankingArea);
        this.actionsMap.put("Now it's time for a quick look at polls.", this::walkToBankingArea);
        this.actionsMap.put("Polls are run periodically to let the Old School RuneScape community vote on how the game should", this::walkToFinancialArea);
        this.actionsMap.put("Continue through the next door.", this::walkToChapelArea);
        this.actionsMap.put("Follow the path to the chapel and enter it.", this::walkToChapelArea);
        this.actionsMap.put("Click on the flashing icon to open the Prayer menu.", this::walkToChapelArea);
        this.actionsMap.put("Talk with Brother Brace and he'll tell you about prayers.", this::walkToChapelArea);
        this.actionsMap.put("You should now see another new icon. Click on the flashing face icon to open your friends and ignore lists.", this::walkToChapelArea);
        this.actionsMap.put("These two lists can be very helpful for keeping track of your friends or for blocking people you don't like.", this::walkToChapelArea);
        this.actionsMap.put("You're almost finished on tutorial island. Pass through the door to find the path leading to your final instructor.", this::walkToWizardArea);
        this.actionsMap.put("Follow the path to the wizard's house, where you will be shown how to cast spells.", this::walkToWizardArea);
        this.actionsMap.put("Open up the magic interface by clicking on the flashing icon.", this::walkToWizardArea);
        this.actionsMap.put("This is your magic interface. All of your spells can be found here.", this::walkToWizardArea);
        this.actionsMap.put("You now have some runes. All spells require runes to cast them.", this::walkToWizardArea);
        this.actionsMap.put("You're nearly finished with the tutorial. All you need to do now is move on to the mainland.", this::walkToWizardArea);
    }

    private void walkToWizardArea() {
        Log.debug("Walking to wizard area.");
        if (Arrays.stream(Constants.WIZARD_AREAS).noneMatch(Area::containsMyPlayer)) {
            Random random = new Random();
            int r = random.nextInt(Constants.WIZARD_AREAS.length);
            Helpers.walkToWithAFKs(Constants.WIZARD_AREAS[r].getRandomTile(), 2500, 1500);
        }
    }

    private void walkToChapelArea() {
        Log.debug("Walking to chapel area.");
        if (!(Constants.CHAPEL_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.CHAPEL_AREA.getRandomTile(), 1000, 200);
            return;
        }
    }

    private void walkToFinancialArea() {
        Log.debug("Walking to financial area.");
        if (!(Constants.FINANCIAL_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.FINANCIAL_AREA.getRandomTile(), 1000, 200);
            return;
        }
    }

    private void walkToBankingArea() {
        Log.debug("Walking to banking area.");
        if (!(Constants.BANKING_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.BANKING_AREA.getRandomTile(), 1000, 200);
            return;
        }
    }

    private void walkToCombatArea() {
        Log.debug("Walking to combat area.");
        if (Arrays.stream(Constants.COMBAT_AREA_EXCLUDING_PIT).noneMatch(Area::containsMyPlayer)) {
            Random random = new Random();
            int r = random.nextInt(Constants.COMBAT_AREA_EXCLUDING_PIT.length);
            Helpers.walkToWithAFKs(Constants.COMBAT_AREA_EXCLUDING_PIT[r].getRandomTile(), 2500, 1500);
        }
    }

    private void walkToMiningArea() {
        Log.debug("Walking to mining area.");
        if (!(Constants.MINING_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.MINING_AREA.getRandomTile(), 1000, 200);
            return;
        }
    }

    //todo: walk to starting area

    private void walkToQuestingTutorial() {
        //Make sure run is enabled
        if (GameState.getSetting(173) == 0) {
            Options.setRunEnabled(true);
        }

        Log.debug("Walking to questing area.");
        if (!(Constants.QUEST_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.QUEST_AREA.getRandomTile(), 3500, 1500);
            return;
        }
    }


    private void walkToChefTutorial() {
        Log.debug("Walking to chef area.");
        if (Arrays.stream(Constants.CHEF_AREAS).noneMatch(Area::containsMyPlayer)) {
            Random random = new Random();
            int r = random.nextInt(Constants.CHEF_AREAS.length);
            Helpers.walkToWithAFKs(Constants.CHEF_AREAS[r].getRandomTile(), 2500, 1500);
        }
    }

    public void walkToSurvivalTutorial(){
        Log.debug("Walking to survival area.");
        if (!(Constants.SURVIVAL_AREA.containsMyPlayer())) {
            Helpers.walkToWithAFKs(Constants.SURVIVAL_AREA.getRandomTile(), 2500, 1500);
            return;
        }
    }

}
