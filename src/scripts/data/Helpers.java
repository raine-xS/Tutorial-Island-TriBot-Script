package scripts.data;

import org.tribot.script.sdk.ChatScreen;
import org.tribot.script.sdk.Log;
import org.tribot.script.sdk.MyPlayer;
import org.tribot.script.sdk.Waiting;
import org.tribot.script.sdk.interfaces.Positionable;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.Area;
import org.tribot.script.sdk.types.LocalTile;
import org.tribot.script.sdk.types.WorldTile;
import org.tribot.script.sdk.walking.GlobalWalking;
import org.tribot.script.sdk.walking.LocalWalking;
import org.tribot.script.sdk.walking.WalkState;
import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Helpers {

    //Utility Class:
    //Private constructor to prevent initialization.
    //Static methods only.
    private Helpers(){}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Attempts to humanly dismiss a single dialogue by either moving a short distance or clicking
    // continue on the dialogue.
    public static boolean dismissSingleDialogue(Area areaBound){
        double r = Math.random();
        // (Anti-ban)
        // 66% chance to dismiss chat dialogue with short movement.
        // 34% chance to dismiss chat dialogue by clicking continue.
        Helpers.waitNormalWithFatigue(500, 100);
        if (ChatScreen.isOpen()) {
            if (r <= .66) {
                Helpers.moveRandomTilesAway(2);
            } else {
                ChatScreen.clickContinue();
            }
        }
        // Checks again if dialogue is actually dismissed and if not, attempts to dismiss it by movement.
        // (Maybe sometimes there's a mistake it was a multiple message dialogue)
        if (ChatScreen.isOpen()) {
            Helpers.moveRandomTilesAway(2);
        }
        // Finally checks one last time if dismissing dialogue actually succeeded:
        if (ChatScreen.isOpen()) {
            // if it didn't return false
            return false;
        } else {
            // else we can return true, but first check the main's player position and return the player
            // back to the area if moving somehow brought the player outside the valid area.
            if (!areaBound.containsMyPlayer()) {
                LocalWalking.walkTo(areaBound.getRandomTile());
            }
            return true;
        }
    }
    public static boolean dismissSingleDialogue(Area[] areaBound){
        double r = Math.random();
        // (Anti-ban)
        // 66% chance to dismiss chat dialogue with short movement.
        // 34% chance to dismiss chat dialogue by clicking continue.
        Helpers.waitNormalWithFatigue(500, 100);
        if (ChatScreen.isOpen()) {
            if (r <= .66) {
                Helpers.moveRandomTilesAway(2);
            } else {
                ChatScreen.clickContinue();
            }
        }
        // Checks again if dialogue is actually dismissed and if not, attempts to dismiss it by movement.
        // (Maybe sometimes there's a mistake it was a multiple message dialogue)
        if (ChatScreen.isOpen()) {
            Helpers.moveRandomTilesAway(2);
        }
        // Finally checks one last time if dismissing dialogue actually succeeded:
        if (ChatScreen.isOpen()) {
            // if it didn't return false
            return false;
        } else {
            // else we can return true, but first check the main's player position and return the player
            // back to the area if moving somehow brought the player outside the valid area.
            if (Arrays.stream(areaBound).noneMatch(Area::containsMyPlayer)) {
                Random random = new Random();
                int s = random.nextInt(areaBound.length - 1);
                LocalWalking.walkTo(areaBound[s].getRandomTile());
            }
            return true;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Move randomly a short distance away up to the given tiles passed in.
    public static void moveRandomTilesAway(int radius){

        List<LocalTile> reachableTiles = Query.tiles()
                                              .inArea(Area.fromRadius(MyPlayer.getTile(), radius)) // Gets a query of tiles within X radius
                                              .filter( (t) -> {
                                                                // For each queried tile, check if there is any Game Object tiles
                                                                // that matches the queried tile and if there are (true), we want
                                                                // to filter it out (false).
                                                                boolean cond1 = !Query.gameObjects().tileEquals(t.getTile())
                                                                                          .isAny();
                                                                return cond1;
                                                              })
                                             .isReachable() // Filter again for reachable only tiles (We wouldn't want to walk somewhere unreachable).
                                             .stream()
                                             .collect(Collectors.toList());

        Random random = new Random();
        int r = random.nextInt(reachableTiles.size());

        Log.debug("Walking to random tile within: " + reachableTiles);
        LocalWalking.walkTo(reachableTiles.get(r));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Wrapper function for org.tribot.script.sdk.Waiting.waitNormal()
    //Like the original waitNormal(), it waits a random amount of time which clusters over time according
    //to the mean and standard deviation, but the mean will shift according to a fatigue multiplier.
    public static void waitNormalWithFatigue(int mean, int std){
        int adjustedMean = (int) (mean * States.instance().getFatigue());
        Waiting.waitNormal(adjustedMean, std);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Wrapper function for org.tribot.script.sdk.walking.GlobalWalking.walkTo()
    //Attempts to provides a more human-like walking method that walks to the position
    //with less continuous clicking.
    public static boolean walkToWithAFKs(@Nonnull Positionable position, int mean, int std){
        //Supplier function to be passed into walkTo()
        Supplier<WalkState> randomStop = () -> {
            Random random = new Random();
            // Chance of cancelling walkTo() represented as in fractional form as:
            // [chance] = [numerator] / [denominator]
            final int numerator = 2;
            final int denominator = 3;
            // Returns random WalkState in accordance with the chance.
            int r = random.nextInt(denominator);
            if (r < numerator) {
                return WalkState.FAILURE;
            } else {
                return WalkState.CONTINUE;
            }
        };

        //Keeps track of the last 5 tiles that the main player has been on within a sliding window.
        final int windowSize = 5;
        ArrayDeque<WorldTile> window = new ArrayDeque<>(windowSize);
        Supplier<WorldTile> averageTileFunc = () -> {
                                                    int xSum = 0;
                                                    int ySum = 0;
                                                    for (WorldTile w : window){
                                                        xSum += w.getX();
                                                        ySum += w.getY();
                                                    }
                                                    int xAverage = (int) Math.round(1.0 * xSum / window.size());
                                                    int yAverage = (int) Math.round(1.0 * ySum / window.size());
                                                    WorldTile averageTile = new WorldTile(xAverage, yAverage);
                                                    return averageTile;
                                                };

        final double e = 5; // some small number of tiles
        int iterationCount = 0;  // counts the number of iterations
        while (true) {
            iterationCount += 1;
            Log.debug("Walking...");
            // Walks to the destination until the main player is there, then return true.
            boolean arrived = GlobalWalking.walkTo(position, randomStop); //Walking has a random chance to cancel.
            if (arrived) {
                Log.debug("Arrived.");
                return true;
            }
            // Periodically pushes the main player's location into the sliding window.
            window.add(MyPlayer.getTile());
            Log.debug(averageTileFunc.get());
            // If the main player is stuck around the same spot for too long then walking to the destination
            // is a failure and we should return false.
            //      1. Main player is determined stuck if the player's position is too close to the average of the last 5 locations
            //      (Which would mean the player is stuck around some general area and isn't moving towards a destination).
            //      2. "Too close" is defined by the absolute value distance between the current position and the average
            //      // of the last 5 positions being within some epsilon, e
            //      (Sometimes the player could be stuck walking back and forth around a general area and not
            //      necessarily stuck on one tile).
            //      3. Since tiles have X, Y: the player is considered making progress towards the destination if the algorithm determined
            //      with either X OR Y meets the criteria
            //      (It doesn't matter if the player walks in a straight line along the X or Y coords,
            //      as long as ONE of the values is making significant progress towards a destination).
            //      5. The check is only started after a some attempts has been made at walking since walking may sometimes
            //      initially stall.
            int changeOfXFromAverage = Math.abs(MyPlayer.getTile().getX() - averageTileFunc.get().getX());
            int changeOfYFromAverage = Math.abs(MyPlayer.getTile().getY() - averageTileFunc.get().getY());
            Log.debug("changeOfXFromAverage: " + changeOfXFromAverage);
            Log.debug("changeOfYFromAverage: " + changeOfYFromAverage);
            if ( (changeOfYFromAverage < e || changeOfYFromAverage < e) && (iterationCount > 50) ) {
                if (GlobalWalking.walkTo(position)) {
                    // Let's just try to walk normally..
                    return true;
                } else {
                    Log.debug("WALK FAILED.");
                    return false;
                }

            }

            // If walking is canceled, then the main player AFKs for a while before attempting to walk again.
            waitNormalWithFatigue(mean, std);
            Log.debug("WALK " + iterationCount);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

}
