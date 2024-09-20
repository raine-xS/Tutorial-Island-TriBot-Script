package scripts.data;

import org.tribot.script.sdk.types.Area;
import org.tribot.script.sdk.types.WorldTile;

public class Constants {

    //Unique Task IDs
    public static final String COMBAT_TUTORIAL_ID = "COMBAT+TUTORIAL";
    public static final String CHEF_TUTORIAL_ID = "CHEF_TUTORIAL";
    public static final String MINING_TUTORIAL_ID = "MINING_TUTORIAL";
    public static final String QUEST_TUTORIAL_ID = "QUEST_TUTORIAL";
    public static final String STARTING_TUTORIAL_ID = "STARTING_TUTORIAL_ID";
    public static final String SURVIVAL_TUTORIAL_ID = "SURVIVAL_TUTORIAL";
    public static final String WALK_TO_NEXT_TASK_ID = "WALK_TO_NEXT_TASK";
    public static final String BANKING_TUTORIAL_ID = "BANKING_TUTORIAL_ID";
    public static final String FINANCIAL_TUTORIAL_ID = "FINANCIAL_TUTORIAL_ID";
    public static final String CHAPEL_TUTORIAL_ID = "CHAPEL_TUTORIAL_ID";
    public static final String WIZARD_TUTORIAL_ID = "WIZARD_TUTORIAL_ID";



    //Item IDs
    public static final int AIR_RUNE = 556;
    public static final int BREAD_DOUGH = 2307;
    public static final int BRONZE_ARROW = 882;
    public static final int BRONZE_AXE = 1351;
    public static final int BRONZE_BAR = 2349;
    public static final int BRONZE_DAGGER = 1205;
    public static final int BRONZE_PICKAXE = 1265;
    public static final int BRONZE_SWORD = 1277;
    public static final int BUCKET_OF_WATER = 1929;
    public static final int BURNT_SHRIMP = 7954;
    public static final int COPPER_ORE = 436;
    public static final int FIRE = 26185;
    public static final int LOGS = 2511;
    public static final int MIND_RUNE = 558;
    public static final int POT_OF_FLOUR = 2516;
    public static final int SMALL_FISHING_NET = 303;
    public static final int RAW_SHRIMPS = 2514;
    public static final int SHORTBOW = 841;
    public static final int SHRIMPS = 315;
    public static final int TIN_ORE = 438;
    public static final int TINDERBOX = 590;
    public static final int WOODEN_SHIELD = 1171;


    //Object IDs
    public static final int ANVIL = 2097;
    public static final int BANKING_BOOTH = 10083;
    public static final int COPPER_ROCKS = 10079;
    public static final int FURNACE = 10082;
    public static final int OAK_TREE = 9734;
    public static final int POLL_BOOTH = 26815;
    public static final int RANGE = 9736;
    public static final int RAT_PIT_GATE_IN = 9719;
    public static final int RAT_PIT_GATE_OUT = 9720;
    public static final int TIN_ROCKS = 10080;
    public static final int TREE = 9730;

    //Area Definitions
    public static final Area[] CHEF_AREAS = {Area.fromRectangle(new WorldTile(3073, 3091, 0), new WorldTile(3077, 3089, 0)),
                                             Area.fromRectangle(new WorldTile(3074, 3089, 0), new WorldTile(3075, 3086, 0)),
                                             Area.fromRectangle(new WorldTile(3073, 3086, 0), new WorldTile(3078, 3083, 0)),
                                             Area.fromRectangle(new WorldTile(3075, 3083, 0), new WorldTile(3076, 3081, 0))};

    public static final Area STARTING_AREA = Area.fromRectangle(new WorldTile(3096, 3110, 0), new WorldTile(3091, 3105, 0));

    public static final Area SURVIVAL_AREA = Area.fromRectangle(new WorldTile(3090, 3099, 0), new WorldTile(3105, 3091, 0));

    public static final Area QUEST_AREA = Area.fromRectangle(new WorldTile(3083,3119, 0), new WorldTile(3089,3125, 0));

    public static final Area MINING_AREA = Area.fromRectangle(new WorldTile(3088, 9508, 0), new WorldTile(3075, 9495, 0));

    public static final Area COMBAT_AREA_INCLUDING_PIT = Area.fromPolygon(new WorldTile(3110, 9527, 0),
                                                                          new WorldTile(3110, 9526, 0),
                                                                          new WorldTile(3110, 9525, 0),
                                                                          new WorldTile(3110, 9524, 0),
                                                                          new WorldTile(3110, 9523, 0),
                                                                          new WorldTile(3110, 9522, 0),
                                                                          new WorldTile(3109, 9522, 0),
                                                                          new WorldTile(3108, 9522, 0),
                                                                          new WorldTile(3107, 9522, 0),
                                                                          new WorldTile(3106, 9522, 0),
                                                                          new WorldTile(3105, 9522, 0),
                                                                          new WorldTile(3104, 9522, 0),
                                                                          new WorldTile(3103, 9522, 0),
                                                                          new WorldTile(3102, 9522, 0),
                                                                          new WorldTile(3101, 9522, 0),
                                                                          new WorldTile(3100, 9522, 0),
                                                                          new WorldTile(3100, 9521, 0),
                                                                          new WorldTile(3100, 9520, 0),
                                                                          new WorldTile(3100, 9519, 0),
                                                                          new WorldTile(3100, 9518, 0),
                                                                          new WorldTile(3100, 9517, 0),
                                                                          new WorldTile(3100, 9516, 0),
                                                                          new WorldTile(3100, 9515, 0),
                                                                          new WorldTile(3100, 9514, 0),
                                                                          new WorldTile(3100, 9513, 0),
                                                                          new WorldTile(3100, 9512, 0),
                                                                          new WorldTile(3101, 9512, 0),
                                                                          new WorldTile(3102, 9512, 0),
                                                                          new WorldTile(3103, 9512, 0),
                                                                          new WorldTile(3103, 9511, 0),
                                                                          new WorldTile(3103, 9510, 0),
                                                                          new WorldTile(3103, 9509, 0),
                                                                          new WorldTile(3103, 9508, 0),
                                                                          new WorldTile(3104, 9508, 0),
                                                                          new WorldTile(3104, 9507, 0),
                                                                          new WorldTile(3105, 9507, 0),
                                                                          new WorldTile(3105, 9508, 0),
                                                                          new WorldTile(3104, 9508, 0),
                                                                          new WorldTile(3105, 9508, 0),
                                                                          new WorldTile(3106, 9508, 0),
                                                                          new WorldTile(3107, 9508, 0),
                                                                          new WorldTile(3108, 9508, 0),
                                                                          new WorldTile(3109, 9508, 0),
                                                                          new WorldTile(3110, 9508, 0),
                                                                          new WorldTile(3111, 9508, 0),
                                                                          new WorldTile(3111, 9509, 0),
                                                                          new WorldTile(3111, 9510, 0),
                                                                          new WorldTile(3111, 9511, 0),
                                                                          new WorldTile(3111, 9512, 0),
                                                                          new WorldTile(3111, 9513, 0),
                                                                          new WorldTile(3111, 9514, 0),
                                                                          new WorldTile(3111, 9515, 0),
                                                                          new WorldTile(3111, 9516, 0),
                                                                          new WorldTile(3111, 9517, 0),
                                                                          new WorldTile(3111, 9518, 0),
                                                                          new WorldTile(3113, 9518, 0),
                                                                          new WorldTile(3113, 9519, 0),
                                                                          new WorldTile(3113, 9520, 0),
                                                                          new WorldTile(3113, 9521, 0),
                                                                          new WorldTile(3112, 9521, 0),
                                                                          new WorldTile(3112, 9522, 0),
                                                                          new WorldTile(3112, 9523, 0),
                                                                          new WorldTile(3112, 9524, 0),
                                                                          new WorldTile(3112, 9525, 0),
                                                                          new WorldTile(3112, 9526, 0),
                                                                          new WorldTile(3112, 9527, 0),
                                                                          new WorldTile(3111, 9527, 0));

    public static final Area[] COMBAT_AREA_EXCLUDING_PIT = {Area.fromRectangle(new WorldTile(3103, 9507, 0), new WorldTile(3018, 9509, 0)),
                                                            Area.fromRectangle(new WorldTile(3106, 9510, 0), new WorldTile(3106, 9510, 0)),
                                                            Area.fromRectangle(new WorldTile(3107, 9510, 0), new WorldTile(3112, 9512, 0)),
                                                            Area.fromRectangle(new WorldTile(3109, 9513, 0), new WorldTile(3109, 9513, 0)),
                                                            Area.fromRectangle(new WorldTile(3110, 9513, 0), new WorldTile(3112, 9516, 0)),
                                                            Area.fromRectangle(new WorldTile(3111, 9517, 0), new WorldTile(3113, 9521, 0))};

    public static final Area COMBAT_AREA_PIT = Area.fromRadius(new WorldTile(3102, 9518, 0), 3);

    public static final Area AROUND_COMBAT_AREA_PIT_GATE = Area.fromRadius(new WorldTile(3110, 9518, 0), 2);

    public static final Area BANKING_AREA = Area.fromRectangle(new WorldTile(3118, 3119, 0), new WorldTile(3124, 3125, 0));

    public static final Area FINANCIAL_AREA = Area.fromRectangle(new WorldTile(3125, 3123, 0), new WorldTile(3129, 3125, 0));


    public static final Area CHAPEL_AREA = Area.fromRectangle(new WorldTile(3127, 3110, 0), new WorldTile(3121, 3103, 0));

    public static final Area[] WIZARD_AREAS = {Area.fromRectangle(new WorldTile(3140, 3083, 0), new WorldTile(3142, 3090, 0)),
                                              Area.fromRectangle(new WorldTile(3141, 3090, 0), new WorldTile(3139, 3091, 0))};

    public static final Area LUMBRIDGE_AREA = Area.fromRectangle(new WorldTile(3188, 3302, 0), new WorldTile(3266,3187, 0 ));

}
