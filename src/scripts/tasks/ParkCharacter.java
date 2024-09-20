package scripts.tasks;

import org.tribot.script.sdk.ChatScreen;
import org.tribot.script.sdk.MyPlayer;
import org.tribot.script.sdk.Waiting;
import org.tribot.script.sdk.types.WorldTile;
import org.tribot.script.sdk.walking.GlobalWalking;
import scripts.Priority;
import scripts.UniqueTask;
import scripts.data.Constants;
import scripts.data.States;

import java.lang.module.Configuration;

public class ParkCharacter extends UniqueTask {
    public ParkCharacter(String uniqueId) {
        super(uniqueId);
    }

    @Override
    public Priority priority() {
        return Priority.MEDIUM;
    }

    @Override
    public boolean validate() {
        // Validated when the player is in Lumbridge
        if (Constants.LUMBRIDGE_AREA.containsMyPlayer()) {
            return true;
        }
        return false;
    }

    @Override
    public void execute() {
        Waiting.waitNormal(5000, 1000);
        if (ChatScreen.isOpen()) {
            Waiting.waitNormal(1500, 300);
            ChatScreen.handle();
        }
        Waiting.waitNormal(3000, 1000);
        GlobalWalking.walkTo(new WorldTile(3095, 3497));
        Waiting.waitUntil(()-> MyPlayer.getTile().getX() == 3097 && MyPlayer.getTile().getY() == 3497);
        States.instance().removeTask(new ParkCharacter("PARK"));
    }
}
