package scripts.tasks;

import org.tribot.api2007.types.RSCharacter;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.*;
import org.tribot.script.sdk.walking.GlobalWalking;
import org.tribot.script.sdk.walking.LocalWalking;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;
import org.tribot.api.input.Mouse;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import org.tribot.api2007.Combat;


public class CombatTutorial extends UniqueTask {

    private HashMap<String, Runnable> actionsMap;
    private int combatFailedCount;

    public CombatTutorial(String uniqueId) {
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
        // (Checks both areas in case player is on a non-overlapping tile)
        if ( (Constants.COMBAT_AREA_INCLUDING_PIT.containsMyPlayer() || Arrays.stream(Constants.COMBAT_AREA_EXCLUDING_PIT).anyMatch(Area::containsMyPlayer)) &&
            States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Combat Tutorial.\n Executing Combat Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Combat Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.COMBAT_TUTORIAL);
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
                                    Helpers.waitNormalWithFatigue(1000, 350);
                                    Log.debug("Text found (COMBATTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO COMBATTUT ACTIONS): \n" + str);
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
        return;

    }

    private void initializeActions(){
        this.actionsMap.put("In this area you will find out about melee and ranged combat.", this::talkToCombatInstructor);
        this.actionsMap.put("You now have access to a new interface. Click on the flashing icon of a man, the one to the right of your backpack icon.", this::openEquipmentTab);
        this.actionsMap.put("This is your worn inventory. Here you can see what items you have equipped.", this::viewEquipmentStats);
        this.actionsMap.put("You can see what items you are wearing in the worn inventory to the left of the screen, with their combined statistics on the right.", this::equipBronzeDagger); //equipBronzeDagger
        this.actionsMap.put("You're now holding your dagger. Clothes, armour, weapons and more are equipped like this.", this::talkToCombatInstructor);
        this.actionsMap.put("To unequip an item, go to your worn inventory and click on the item.", this::equipBronzeSwordAndWoodenShield); //equipBronzeSwordAndWoddenShield
        this.actionsMap.put("Click on the flashing crossed swords icon to open the combat interface.", this::openCombatTab);
        this.actionsMap.put("This is your combat interface. From here, you can select the attack style that you'll use in combat.", this::selectCombatStyleAndMeleeAttackGiantRat);
        this.actionsMap.put("Pass through the gate and talk to the combat instructor.", this::talkToCombatInstructor);
        this.actionsMap.put("Now you have a bow and some arrows.", this::equipShortBowAndArrowsAndRangeAttackGiantRat);
        this.actionsMap.put("It's time to slay some rats!", this::selectCombatStyleAndMeleeAttackGiantRat);
    }

    private boolean equipShortBowAndArrowsAndRangeAttackGiantRat() {
        return this.equipShortBowAndArrows() && this.RangeAttackGiantRat();
    }

    private boolean RangeAttackGiantRat() {
        Optional<Npc> giantRat = Query.npcs().filter(
                                                        // Filter for only non-reachable Giant Rats.
                                                        (npc) -> {
                                                            return !Query.npcs().isReachable().idEquals(npc.getId()).isAny();
                                                        })
                                                        .nameEquals("Giant Rat")
                                                        .findRandom();
        if (giantRat.isPresent()) {
            giantRat.map(npc -> npc.interact("Attack"));
            if (!Waiting.waitUntil(5000, ()->MyPlayer.isAnimating())) {
                Log.debug("Ranged combat failed to start");
                return false;
            }

            if ( !Waiting.waitUntil(60000, ()-> giantRat.map(npc -> npc.getHealthBarPercent() == 0).orElse(false))) {
                Log.debug("Ranged combat failed");
                return false;
            }
            Waiting.waitNormal(2000, 500);
        }
        return true;
    }


    private boolean equipShortBowAndArrows() {
        return equipShortBow() && equipBronzeArrow();
    }

    private boolean equipBronzeArrow() {
        if (!Inventory.contains(Constants.BRONZE_ARROW) &&
            !Query.equipment().idEquals(Constants.BRONZE_ARROW).isAny()) {
            talkToCombatInstructor();
        }

        Waiting.waitNormal(350, 100);
        Query.inventory().idEquals(Constants.BRONZE_ARROW).findRandom().ifPresent(obj -> obj.click());
        Waiting.waitNormal(350, 100);

        if (Query.equipment().idEquals(Constants.BRONZE_ARROW).isAny()) {
            return true;
        }

        return false;

    }

    private boolean equipShortBow(){
        if (!Inventory.contains(Constants.SHORTBOW) &&
            !Query.equipment().idEquals(Constants.SHORTBOW).isAny()) {
            talkToCombatInstructor();
        }

        Waiting.waitNormal(350, 100);
        Query.inventory().idEquals(Constants.SHORTBOW).findRandom().ifPresent(obj -> obj.click());
        Waiting.waitNormal(350, 100);

        if (Query.equipment().idEquals(Constants.SHORTBOW).isAny()) {
            return true;
        }

        return false;
    }

    private boolean selectCombatStyleAndMeleeAttackGiantRat() {
        Log.debug("this.selectCombatStyle() && this.MeleeAttackGiantRat() " + (this.selectCombatStyle() && this.MeleeAttackGiantRat()));
        return this.selectCombatStyle() && this.MeleeAttackGiantRat();
    }

    private boolean MeleeAttackGiantRat() {
        if (!walkInPit()) {
            Log.debug("not walkinpit");
            return false;
        }

        Optional<Npc> giantRat = Query.npcs().nameEquals("Giant Rat").findBestInteractable();
        if (giantRat.isPresent()) {
            giantRat.map(npc -> npc.interact("Attack"));
            if (!Waiting.waitUntil(5000, ()->Combat.isUnderAttack())) {
                Log.debug("not underattack in 5s");
                return false;
            }
            if (!Waiting.waitUntil(60000, ()->!Combat.isUnderAttack())) {
                Log.debug("not out of combat in 60s");
                return false;
            }
            return true;
        }
        Waiting.waitNormal(3000, 1000);
        Log.debug("rat not found");
        return false;
    }

    private boolean walkInPit(){
        boolean nearGate = Constants.AROUND_COMBAT_AREA_PIT_GATE.containsMyPlayer();
        // If we can't reach any rats.
        if (!Query.npcs().nameEquals("Giant Rat").isReachable().isAny()) {
            if (!nearGate) {
                LocalWalking.walkTo(Constants.AROUND_COMBAT_AREA_PIT_GATE.getRandomTile());
            }
            Query.gameObjects().idEquals(Constants.RAT_PIT_GATE_IN).findBestInteractable().ifPresent(obj -> obj.click());
            Waiting.waitNormal(4000, 500);
        }

        LocalWalking.walkTo(Constants.COMBAT_AREA_PIT.getRandomTile());
        Waiting.waitUntil(()->!MyPlayer.isMoving());
        Waiting.waitNormal(1000, 250);

        if (Constants.COMBAT_AREA_PIT.containsMyPlayer()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean selectCombatStyle() {
        Random random = new Random();
        int randomZeroOneTwo = random.nextInt(3);
        switch (randomZeroOneTwo) {
            case 0:
                Combat.selectIndex(0);
                break;
            case 1:
                Combat.selectIndex(1);
                break;
            case 2:
                Combat.selectIndex(2);
                break;
            default:
                return false;
        }

        return true;
    }

    private void openCombatTab() {
        GameTab.COMBAT.open();
        Waiting.waitNormal(350, 100);
    }

    private void openEquipmentTab() {
        GameTab.EQUIPMENT.open();
        Waiting.waitNormal(350, 100);
    }

    private boolean equipBronzeSwordAndWoodenShield() {
        return this.equipBronzeSword() && this.equipWoodenShield();
    }

    private boolean equipBronzeSword(){
        // If it's already equipped, let's not equip it again.
        if (Query.equipment().idEquals(Constants.BRONZE_SWORD).isAny()) {
            return false;
        }

        // If we don't have a bronze sword, let's get one from the instructor, otherwise return false if we can't.
        if (!Inventory.contains(Constants.BRONZE_SWORD)) {
            if (!this.talkToCombatInstructor()) {
                return false;
            }
        }

        // Equips bronze sword.
        if (Query.inventory().idEquals(Constants.BRONZE_SWORD).findRandom().isPresent()) {
            Query.inventory().idEquals(Constants.BRONZE_SWORD).findRandom().map(obj -> obj.click());
            Waiting.waitUntil(5000, ()->Query.equipment().idEquals(Constants.BRONZE_SWORD).findFirst().isPresent());
            Waiting.waitNormal(600, 200);
        } else {
            return false;
        }

        // Checks if it's equipped and returns true if so, otherwise return false.
        if (Query.equipment().idEquals(Constants.BRONZE_SWORD).findFirst().isPresent()) {
            return true;
        }
        return false;
    }

    private boolean equipWoodenShield(){
        // If it's already equipped, let's not equip it again.
        if (Query.equipment().idEquals(Constants.WOODEN_SHIELD).isAny()) {
            return false;
        }

        // If we don't have a wooden shield, let's get one from the instructor, otherwise return false if we can't.
        if (!Inventory.contains(Constants.WOODEN_SHIELD)) {
            if (!this.talkToCombatInstructor()) {
                return false;
            }
        }

        // Equips wooden shield.
        if (Query.inventory().idEquals(Constants.WOODEN_SHIELD).findRandom().isPresent()) {
            Query.inventory().idEquals(Constants.WOODEN_SHIELD).findRandom().map(obj -> obj.click());
            Waiting.waitUntil(5000, ()->Query.equipment().idEquals(Constants.WOODEN_SHIELD).findFirst().isPresent());
            Waiting.waitNormal(600, 200);
        } else {
            return false;
        }

        // Checks if it's equipped and returns true if so, otherwise return false.
        if (Query.equipment().idEquals(Constants.WOODEN_SHIELD).findFirst().isPresent()) {
            return true;
        }
        return false;

    }

    private MiningTutorial revisitMiningTutorial(){
        GlobalWalking.walkTo(Constants.MINING_AREA.getRandomTile());
        return new MiningTutorial("MINING_TUTORIAL_REDO");
    }

    private boolean equipBronzeDagger() {
        // If it's already equipped, let's not reequip it.
        if (Query.equipment().idEquals(Constants.BRONZE_DAGGER).isAny()) {
            return false;
        }

        if (!Inventory.contains(Constants.BRONZE_DAGGER)) {
            revisitMiningTutorial().smithBronzeDagger();
        }

        Query.inventory().idEquals(Constants.BRONZE_DAGGER).findRandom().ifPresent( (obj) -> {
                                                                                                obj.click();
                                                                                                Waiting.waitUntil(5000, ()->Query.equipment().idEquals(Constants.BRONZE_DAGGER).isAny());
                                                                                             });

        this.closeEquipmentTab();

        if (Query.equipment().idEquals(Constants.BRONZE_DAGGER).isAny()) {
            return true;
        }

        return false;
    }

    private boolean viewEquipmentStats() {
        Optional<Widget> equipmentStats = Widgets.get(387, 2);

        this.openEquipmentTab();

        if (equipmentStats.isPresent() && GameTab.EQUIPMENT.isOpen()) {
            equipmentStats.map(w -> w.click());
            Waiting.waitNormal(350, 100);
            return true;
        }

        return false;
    }

    private boolean closeEquipmentTab(){
        Optional<Widget> equipmentInterface = Widgets.get(84, 3);

        if (equipmentInterface.isPresent()) {
            equipmentInterface.ifPresent( (i) -> {
                                                    Random random = new Random();
                                                    // Random point on equipment interface's X button
                                                    Point p = new Point(i.getBounds().x + random.nextInt(19) + 473,
                                                                        i.getBounds().y + random.nextInt(19) + 10);
                                                    Mouse.click(p, 1);
                                                    Waiting.waitNormal(350, 100);
                                                 });
            return true;
        }

        return false;
    }

    private boolean talkToCombatInstructor() {

        ChatScreen.clickContinue();

        Optional<Npc> combatInstructor = Query.npcs().nameEquals("Combat Instructor").findBestInteractable();

        if (combatInstructor.isPresent()) {
            if (! (Query.npcs().isReachable().nameEquals("Combat Instructor").isAny()) ) {
                // If Combat instructor is present but not reachable, let's walk through the gate.
                Log.debug("CANT REACH INSTRUCTOR");
                Waiting.waitNormal(1000, 200);
                LocalWalking.walkTo(Constants.AROUND_COMBAT_AREA_PIT_GATE.getRandomTile());
                Waiting.waitUntil(()->!MyPlayer.isMoving());
                Waiting.waitNormal(2500, 500);
                Query.gameObjects().idEquals(Constants.RAT_PIT_GATE_OUT).findBestInteractable().ifPresent(obj -> obj.click());
                Waiting.waitUntil(()->!MyPlayer.isMoving());
                Waiting.waitNormal(2500, 500);
            } else {
                Log.debug("CAN REACH INSTRUCTOR");
            }
            combatInstructor.map(npc -> npc.interact("Talk-to"));
            Waiting.waitUntil(()->ChatScreen.isOpen());
            Waiting.waitNormal(350, 100);
            ChatScreen.handle();
            Waiting.waitNormal(350, 100);
        } else {
            return false;
        }

        if (ChatScreen.isOpen()) {
            Helpers.dismissSingleDialogue(Constants.COMBAT_AREA_EXCLUDING_PIT);
            Waiting.waitNormal(350, 100);
        }

        return true;
    }

}
