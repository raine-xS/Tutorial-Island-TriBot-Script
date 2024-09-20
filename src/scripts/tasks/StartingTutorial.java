package scripts.tasks;

import java.awt.*;
import java.util.*;

import org.tribot.api.input.Mouse;
import org.tribot.script.sdk.*;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Npc;
import org.tribot.script.sdk.types.Player;
import org.tribot.script.sdk.types.Widget;
import scripts.Priority;
import scripts.Task;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.Helpers;
import scripts.data.States;
import scripts.data.Status;

public class StartingTutorial extends UniqueTask implements Task {

    private HashMap<String, Runnable> actionsMap;

    public StartingTutorial(String uniqueID){
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
        if (Constants.STARTING_AREA.containsMyPlayer() &&
                States.instance().getStatus() == Status.WAITING_NEXT_TASK) {
            Log.debug("Validation success for: Starting tutorial.\n Executing Starting Tutorial.");
            return true;
        }
        Log.debug("Validation failed for: Starting Tutorial.");
        return false;
    }

    @Override
    public void execute() {
        // Starting
        States.instance().setStatus(Status.STARTING_TUTORIAL);
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
                                    Log.debug("Text found (STARTINGTUT): " + str);
                                    this.actionsMap.get(str).run();
                                    foundAnyText[0] = true;
                                } else {
                                    Log.debug("Text not found (NO STARTINGTUT ACTIONS): \n" + str);
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
        return;

    }

    private void initializeActions() {
        this.actionsMap.put("Before you get started, you'll need to set the appearance of your character.", this::createCharacter);
        this.actionsMap.put("Before you begin, have a read through the controls guide in the top left of the screen.", this::talkToGielinorGuide);
        this.actionsMap.put("Please click on the flashing spanner icon found at the bottom right of your screen.", this::openSettingsAndTurnOffRoofs);
        this.actionsMap.put("On the side panel, you can now see a variety of game settings.", this::talkToGielinorGuide);
    }

    private void createCharacter() {
        ArrayList<Customizable> customizables = new ArrayList<>();

        Customizable genderOption =  new Customizable(CustomizableOption.GENDER_OPTION, 0, 1);
        genderOption.randomize();

        if (GameState.getVarbit(14021) == 0) {
            // If varbit 14021 = 0, then gender is male.
            Collections.addAll(customizables, new Customizable(CustomizableOption.HEAD_DESIGN_OPTION, 0, 56),
                                              new Customizable(CustomizableOption.JAW_DESIGN_OPTION, 0, 14),
                                              new Customizable(CustomizableOption.TORSO_DESIGN_OPTION, 0, 13),
                                              new Customizable(CustomizableOption.ARMS_DESIGN_OPTION, 0, 11),
                                              new Customizable(CustomizableOption.HANDS_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.LEGS_DESIGN_OPTION, 0, 10),
                                              new Customizable(CustomizableOption.HANDS_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.FEET_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.HAIR_COLOUR_OPTION, 0, 29),
                                              new Customizable(CustomizableOption.TORSO_COLOUR_OPTION, 0, 28),
                                              new Customizable(CustomizableOption.LEGS_COLOUR_OPTION, 0, 28),
                                              new Customizable(CustomizableOption.SKIN_COLOUR_OPTION, 0, 7));
        } else if (GameState.getVarbit(14021) == 1) {
            Collections.addAll(customizables, new Customizable(CustomizableOption.HEAD_DESIGN_OPTION, 0, 56),
                                              new Customizable(CustomizableOption.JAW_DESIGN_OPTION, 0, 0),
                                              new Customizable(CustomizableOption.TORSO_DESIGN_OPTION, 0, 10),
                                              new Customizable(CustomizableOption.ARMS_DESIGN_OPTION, 0, 10),
                                              new Customizable(CustomizableOption.HANDS_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.LEGS_DESIGN_OPTION, 0, 10),
                                              new Customizable(CustomizableOption.HANDS_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.FEET_DESIGN_OPTION, 0, 1),
                                              new Customizable(CustomizableOption.HAIR_COLOUR_OPTION, 0, 29),
                                              new Customizable(CustomizableOption.TORSO_COLOUR_OPTION, 0, 28),
                                              new Customizable(CustomizableOption.LEGS_COLOUR_OPTION, 0, 28),
                                              new Customizable(CustomizableOption.SKIN_COLOUR_OPTION, 0, 7));
        }

        int maxAttempts = 30;
        int i = 0;
        Iterator<Customizable> iterator = customizables.iterator();
        RandomCameraPan randomPan = new RandomCameraPan();
        RandomCameraTilt randomTilt = new RandomCameraTilt();
        randomPan.start();
        randomTilt.start();
        while (iterator.hasNext()) {
            Customizable customizable = iterator.next();
            double rng = Math.random();

            if (rng <= 0.15) {
                //Chance to skip and come back later. (15%)
                Log.debug("Let's skip this option and come back later.");
                continue;
            } else if (rng <= 0.25) {
                //Chance to skip entirely. (0.25 - 0.15 = 10%)
                Log.debug("Let's skip this option entirely.");
                iterator.remove();
            } else {
                //Random selection from next option. (75%)
                Log.debug("Getting next option and randomizing..");
                customizable.randomize();
                iterator.remove();
            }

            if (!(iterator.hasNext()) && !(customizables.isEmpty()) ){
                iterator = customizables.iterator();
            }
            if (!(iterator.hasNext())) {
                break;
            }
            // Case for not being able to find character creation screen.
            i += 1;
            if (i > maxAttempts) {
                break;
            }
        }
        randomPan.stopRunning();
        randomTilt.stopRunning();

        Optional<Widget> confirmCreation = Widgets.get(679, 68);
        Waiting.waitNormal(4000, 2000);
        confirmCreation.map(c -> c.click());
        Log.debug("character made");

        // Let's wait a little for the text to update afterwards so that it doesn't reloop Character Creation.
        Waiting.waitNormal(2500, 500);
        return;
    }

    private void openSettings(){
        GameTab.OPTIONS.open();
        Waiting.waitNormal(350, 100);
        return;
    }

    private boolean openSettingsAndTurnOffRoofs(){
        // If it's already off, then we don't have to do anything.
        if (GameState.getSetting(3074) == 167772160) {
            Log.debug("Roofs already off: " + GameState.getSetting(3074));
            return true;
        }

        if (!GameTab.OPTIONS.isOpen()) {
            this.openSettings();
        }

        if (GameTab.OPTIONS.isOpen()) {
            // Click All Settings
            Optional<Widget> allSettingsButton = Widgets.get(116, 32);
            if (allSettingsButton.isEmpty()) {
                return false;
            }

            allSettingsButton.ifPresent((i) -> {
                i.click();
                Waiting.waitNormal(500, 200);
            });

            // Click Display Tab
            Optional<Widget> displayTab = Widgets.get(134, 24);
            if (displayTab.isEmpty()) {
                return false;
            }

            displayTab.ifPresent((i) -> {
                Random random = new Random();
                int pX = i.getBounds().x + random.nextInt(73) + 8;
                int pY = i.getBounds().y + random.nextInt(21) + 113;
                Mouse.click(new Point(pX, pY), 1);
                Waiting.waitNormal(500, 200);
            });

            // Click Hide Roofs
            Optional<Widget> hideRoofsButton = Widgets.get(134, 18, 52);
            if (hideRoofsButton.isEmpty()) {
                return false;
            }

            hideRoofsButton.ifPresent((i) -> {
                                                i.click();
                                                Waiting.waitNormal(500, 200);
                                             });

            Optional<Widget> allSettingsInterface = Widgets.get(134, 3);
            allSettingsInterface.ifPresent((i) -> {
                                                    Random random = new Random();
                                                    int pX = i.getBounds().x + random.nextInt(21) + 486;
                                                    int pY = i.getBounds().y + random.nextInt(18) + 11;
                                                    Mouse.click(new Point(pX, pY), 1);
                                                    Waiting.waitNormal(600, 250);
            });

            if (GameState.getSetting(3074) == 167772160) {
                Log.debug("Roofs turned off: " + GameState.getSetting(3074));
                return true;
            }
        }

        return false;

    }

    private boolean talkToGielinorGuide(){
        Optional<Npc> gielinorGuide = Query.npcs().nameEquals("Gielinor Guide").findBestInteractable();
        if (gielinorGuide.isPresent()) {
            gielinorGuide.ifPresent(npc -> npc.interact("Talk-to"));
            Waiting.waitNormal(1000, 300);
            Waiting.waitUntil(4000, ()->ChatScreen.isOpen());
        } else {
            return false;
        }
        String[] chatOptions = {"I am brand new! This is my first time here.",
                                "I've played in the past, but not recently",
                                "I am an experienced player"};
        Random random = new Random();
        int r = random.nextInt(3);
        if (ChatScreen.isOpen()) {
            ChatScreen.handle(chatOptions[r]);
            return true;
        } else {
            return false;
        }
    }

    private class RandomCameraTilt extends Thread{
        private volatile boolean running = true;
        private RandomCameraTilt(){}

        @Override
        public void run(){
            while (running){
                Random random = new Random();
                int randomDegree = random.nextInt(360 + 1);
                int randomWait = random.nextInt(45000) + 2000;
                Camera.setAngle(randomDegree);
                Waiting.wait(randomWait);
            }
        }

        public void stopRunning(){
            this.running = false;
        }
    }

    private class RandomCameraPan extends Thread{
        private volatile boolean running = true;
        private RandomCameraPan(){}

        @Override
        public void run(){
            while (running){
                Random random = new Random();
                int randomAngle = random.nextInt(100 + 1);
                int randomWait = random.nextInt(45000) + 2000;
                Camera.setRotation(randomAngle);
                Waiting.wait(randomWait);
            }
        }

        public void stopRunning(){
            this.running = false;
        }
    }

    private class Customizable {

        private String uniqueID;
        private Optional<Widget> nextOption;
        private Optional<Widget> previousOption;
        private int lowerBound;
        private int upperBound;

        public Customizable(CustomizableOption customizableOption, int lowerBound, int upperBound) {
            this.uniqueID = customizableOption.uniqueID;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            if (this.upperBound < lowerBound) {
                throw new IllegalArgumentException("Upper bound must be greater than or equal to lower bound");
            }

            switch (customizableOption) {
                case HEAD_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 12);
                    this.nextOption = Widgets.get(679, 13);
                    break;
                case JAW_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 16);
                    this.nextOption = Widgets.get(679, 17);
                    break;
                case TORSO_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 20);
                    this.nextOption = Widgets.get(679, 21);
                    break;
                case ARMS_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 24);
                    this.nextOption = Widgets.get(679, 25);
                    break;
                case HANDS_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 28);
                    this.nextOption = Widgets.get(679, 29);
                    break;
                case LEGS_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 32);
                    this.nextOption = Widgets.get(679, 33);
                    break;
                case FEET_DESIGN_OPTION:
                    this.previousOption = Widgets.get(679, 36);
                    this.nextOption = Widgets.get(679, 37);
                    break;
                case HAIR_COLOUR_OPTION:
                    this.previousOption = Widgets.get(679, 43);
                    this.nextOption = Widgets.get(679, 44);
                    break;
                case TORSO_COLOUR_OPTION:
                    this.previousOption = Widgets.get(679, 47);
                    this.nextOption = Widgets.get(679, 48);
                    break;
                case LEGS_COLOUR_OPTION:
                    this.previousOption = Widgets.get(679, 51);
                    this.nextOption = Widgets.get(679, 52);
                    break;
                case FEET_COLOUR_OPTION:
                    this.previousOption = Widgets.get(679, 55);
                    this.nextOption = Widgets.get(679, 56);
                    break;
                case SKIN_COLOUR_OPTION:
                    this.previousOption = Widgets.get(679, 59);
                    this.nextOption = Widgets.get(679, 60);
                    break;
                case GENDER_OPTION:
                    this.previousOption = Widgets.get(679, 66);
                    this.nextOption = Widgets.get(679, 65);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value for customizableOption: " + customizableOption);
            }

        }

        public Customizable(CustomizableOption customizableOption, int choiceIndex){
            this(customizableOption, choiceIndex, choiceIndex);
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Customizable)) {
                return false;
            }
            Customizable comparedObj = (Customizable) obj;
            if ( (comparedObj.uniqueID.equals(this.uniqueID)) &&
                    (this.lowerBound == comparedObj.lowerBound) &&
                    (this.upperBound == comparedObj.upperBound) ) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode(){
            int nonzeroAccumulator = 1;
            int prime = 31;
            nonzeroAccumulator = prime * nonzeroAccumulator + this.uniqueID == null ? this.uniqueID.hashCode() : 0;
            nonzeroAccumulator = prime * nonzeroAccumulator + this.lowerBound;
            nonzeroAccumulator =  prime * nonzeroAccumulator + this.upperBound;
            return nonzeroAccumulator;
        }

        public void randomize(){
            Random random = new Random();
            int r = random.nextInt(this.upperBound+1 - this.lowerBound) + this.lowerBound;
            for (int i = 1; i <= r; i++) {
                // 8% chance to back-select.
                if (Math.random() < 0.08) {
                    int s = random.nextInt(3);
                    for (int j = 0; j < s; j++) {
                        this.previousOption.map(o -> o.click());
                        Waiting.waitNormal(1500, 500);
                    }
                    for (int j = 0; j < s; j++) {
                        this.nextOption.map(o -> o.click());
                        Waiting.waitNormal(1000, 250);
                    }
                }
                this.nextOption.map(o -> o.click());
                Waiting.waitNormal(1400, 500);
            }
        }
    }
    public enum CustomizableOption {
        HEAD_DESIGN_OPTION("HEAD_DESIGN_OPTION"),
        JAW_DESIGN_OPTION("JAW_DESIGN_OPTION"),
        TORSO_DESIGN_OPTION("TORSO_DESIGN_OPTION"),
        ARMS_DESIGN_OPTION("TORSO_DESIGN_OPTION"),
        HANDS_DESIGN_OPTION("HANDS_DESIGN_OPTION"),
        LEGS_DESIGN_OPTION("LEGS_DESIGN_OPTION"),
        FEET_DESIGN_OPTION("FEET_DESIGN_OPTION"),
        HAIR_COLOUR_OPTION("HAIR_COLOUR_OPTION"),
        TORSO_COLOUR_OPTION("TORSO_COLOUR_OPTION"),
        LEGS_COLOUR_OPTION("LEGS_COLOUR_OPTION"),
        FEET_COLOUR_OPTION("FEET_COLOUR_OPTION"),
        SKIN_COLOUR_OPTION("SKIN_COLOUR_OPTION"),
        GENDER_OPTION("GENDER_OPTION");

        private String uniqueID;

        CustomizableOption (String uniqueID){
            this.uniqueID = uniqueID;
        }
    }

}

