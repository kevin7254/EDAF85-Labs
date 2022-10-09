package lift;

import java.util.stream.IntStream;

public class Elevator {
    public static void main(String[] args) {
        final int NBR_FLOORS = 7, MAX_PASSENGERS = 4;

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        MonitorLift lift = new MonitorLift(NBR_FLOORS);

        Thread liftThread = new Thread(() -> {
            int currentFloor = 0;
            while (true) {
                int totalWaiting = IntStream.of(lift.getToEnter()).sum();
                if (lift.goingUp() && currentFloor < NBR_FLOORS - 1) {
                    if (((lift.getToEnter()[currentFloor] > 0 && lift.passengersInLift() < MAX_PASSENGERS) || lift.getToExit()[currentFloor] > 0) || totalWaiting == 0) {
                        view.openDoors(currentFloor);
                        lift.waitForPassengers(currentFloor);
                        view.closeDoors();
                    } else {
                        lift.moveLift(currentFloor, currentFloor + 1);
                        view.moveLift(currentFloor, currentFloor + 1);
                        currentFloor++;
                    }

                } else {
                    if (currentFloor > 0) {
                        if (((lift.getToEnter()[currentFloor] > 0 && lift.passengersInLift() < MAX_PASSENGERS) || lift.getToExit()[currentFloor] > 0) || totalWaiting == 0) {
                            view.openDoors(currentFloor);
                            lift.waitForPassengers(currentFloor);
                            view.closeDoors();
                        } else {
                            lift.moveLift(currentFloor, currentFloor - 1);
                            view.moveLift(currentFloor, currentFloor - 1);
                            currentFloor--;
                        }
                    }
                }
            }
        });
        liftThread.start();

        for (int i = 0; i < 20; i++) {
            Thread t = new Thread(() -> {
                while (true) {
                    Passenger pass = view.createPassenger();
                    int fromFloor = pass.getStartFloor();
                    int toFloor = pass.getDestinationFloor();
                    pass.begin();
                    lift.waitForLift(fromFloor);
                    pass.enterLift();
                    lift.enterLift(fromFloor, toFloor);
                    pass.exitLift();
                    lift.exitLift(toFloor);
                    pass.end();
                }
            });
            t.start();
        }
    }
}
