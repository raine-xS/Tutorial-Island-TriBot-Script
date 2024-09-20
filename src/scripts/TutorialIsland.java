// TODO: clean up Waiting delays in functions
// TODO: clean up Log.debug messages

package scripts;

import org.tribot.script.sdk.Log;
import org.tribot.script.sdk.Waiting;
import org.tribot.script.sdk.input.Mouse;
import org.tribot.script.sdk.script.TribotScript;
import org.tribot.script.sdk.script.TribotScriptManifest;
import scripts.data.States;

import java.util.ArrayList;
import java.util.List;

@TribotScriptManifest(name = "Tutorial Island", author = "RAINEERAINEERAINEE", category = "Template", description = "Rainee's first script.")
public class TutorialIsland implements TribotScript {

	@Override
	public void execute(final String args) {
		States states = States.instance();

		// Main Loop:
		// Continuously loop over the task set,
		// while executing the tasks that meets the validation criteria.
		mainLoop:
		while (states.isRunning()) {

			int defaultMouseSpeed = 125;
			int mouseSpeed = (int) (defaultMouseSpeed * (2 - (States.instance().getFatigue())));
			Mouse.setSpeed(mouseSpeed);

			// Goes over a copy of the TaskSet on each iteration to
			// allow for modification of the TaskSet.
			// (prevents Concurrent Modification Exceptions)
			List<Task> tasksCopy = new ArrayList<>(states.getTasks());
			boolean taskExecuted = false;
			for (Task task : tasksCopy) {
				//Exits the main loop if there are no more tasks to do except WalkToNextTask
				if (states.getTasks().size() <= 1) {
					break mainLoop;
				}
				// Execute the tasks that meets validation
				if (task.validate()) {
					Log.debug("Validating next task...");
					task.execute();
					taskExecuted = true;
				}
			}

			//If we go through the entire list without a single valid task, then we might not be at tutorial island
			//so we should exit the program.
			if (taskExecuted == false) {
				Log.debug("No task executed so exiting");
				break mainLoop;
			}

			responsiveTermination();
		}
		Log.debug("FINISHED TUTORIAL");
		return;
	}

	private void responsiveTermination(){
		// Ensures TriBot's script termination doesn't become unresponsive inside an infinite loop.
		// Add into main loop.
		Waiting.wait(25);
	}

}
