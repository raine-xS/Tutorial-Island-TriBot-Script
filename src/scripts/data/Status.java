package scripts.data;

public enum Status {
    BANKING_TUTORIAL("Doing banking tutorial."),
    CHAPEL_TUTORIAL("Doing chapel tutorial."),
    COMBAT_TUTORIAL("Doing combat tutorial."),
    CHEF_TUTORIAL("Doing chef tutorial."),
    FINANCIAL_TUTORIAL("Doing financial tutorial."),
    MINING_TUTORIAL("Doing mining tutorial"),
    QUESTING_TUTORIAL("Doing questing tutorial"),
    STARTING_TUTORIAL("Doing starting tutorial"),
    SURVIVAL_TUTORIAL("Doing survival tutorial."),
    WAITING_NEXT_TASK("Waiting for the next task."),
    WALKING_TO_TASK("Talking to the next task."),
    WIZARD_TUTORIAL("Doing wizard tutorial");

    private String statusMessage;

    private Status(String statusMessage){
        this.statusMessage = statusMessage;
    }

    public String getMessage(){
        return this.statusMessage;
    }

    @Override
    public String toString(){
        return this.getMessage();
    }
}
