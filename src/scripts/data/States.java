package scripts.data;

import org.tribot.script.sdk.Log;
import scripts.Task;
import scripts.TaskSet;
import scripts.tasks.*;

import java.util.Iterator;

public class States {

    private double fatigue = 1.00;
    private final double maxFatigue = 2.00;
    private boolean isRunning = true;
    private Status status = Status.WAITING_NEXT_TASK;
    private TaskSet tasks = new TaskSet(new StartingTutorial(Constants.STARTING_TUTORIAL_ID),
                                        new SurvivalTutorial(Constants.SURVIVAL_TUTORIAL_ID),
                                        new WalkToNextTask(Constants.WALK_TO_NEXT_TASK_ID),
                                        new ChefTutorial(Constants.CHEF_TUTORIAL_ID),
                                        new QuestingTutorial(Constants.QUEST_TUTORIAL_ID),
                                        new MiningTutorial(Constants.MINING_TUTORIAL_ID),
                                        new CombatTutorial(Constants.COMBAT_TUTORIAL_ID),
                                        new BankingTutorial(Constants.BANKING_TUTORIAL_ID),
                                        new FinancialTutorial(Constants.FINANCIAL_TUTORIAL_ID),
                                        new ChapelTutorial(Constants.CHAPEL_TUTORIAL_ID),
                                        new WizardTutorial(Constants.WIZARD_TUTORIAL_ID),
                                        new ParkCharacter("PARK"));

    //Singleton Class:
    //Private constructor to prevent initialization.
    //Instantiation done using instance(). Only one instance of the class is allowed.
    private static final States INSTANCE = new States();
    private States(){}
    public static States instance(){return INSTANCE;}

    public void addFatigue(double percentageAsDecimal){
        if (percentageAsDecimal < 0) {
            return;
        }
        if (this.fatigue + percentageAsDecimal >= this.maxFatigue) {
            this.fatigue = this.maxFatigue;
        } else {
            this.fatigue += percentageAsDecimal;
        }
    }

    public void addTask(Task task){
        this.tasks.add(task);
    }

    public boolean isRunning(){
        return this.isRunning;
    }

    public double getFatigue(){
        return this.fatigue;
    }

    public Status getStatus(){
        return this.status;
    }

    public TaskSet getTasks(){
        return this.tasks;
    }

    public void removeTask(Task task){
        // Iterator to safely ensure removal during iteration
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task currentTask = iterator.next();
            Log.debug("Attempting to remove task: " + task + " vs " + currentTask);
            if (currentTask.equals(task)) {
                iterator.remove();
                Log.debug("Task removed. Tasks: " + getTasks());
                break; //
            }
        }
        Log.debug(tasks);
    }

    public void resetFatigue(){
        this.fatigue = 1.0;
    }

    public void setFatigue(double fatigue){
        if (fatigue >= this.maxFatigue) {
            this.fatigue = this.maxFatigue;
        } else {
            this.fatigue = fatigue;
        }
        if (this.fatigue < 0) {
            this.fatigue = 0;
        }
    }

    public void setIsRunning(Boolean bool){
        this.isRunning = bool;
    }

    public void setStatus(Status status){
        this.status = status;
    }




}
