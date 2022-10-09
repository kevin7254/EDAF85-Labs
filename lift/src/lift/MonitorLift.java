package lift;


import java.util.stream.IntStream;

public class MonitorLift {
    private int[] toEnter;
    private int[] toExit;
    private int currentFloor;
    private int passengersInLift;

    private boolean doorsOpen = false;
    private boolean isMoving = false;
    private boolean goingUp = true;

    private int peopleMoving;

    private final int NBR_FLOORS;


    public MonitorLift(int nbrFloors) {
        toEnter = new int[nbrFloors];
        toExit = new int[nbrFloors];
        NBR_FLOORS = nbrFloors;
    }

    public synchronized void moveLift(int fromFloor, int toFloor) {
        while (!isMoving) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        goingUp = (toFloor > fromFloor) && (toFloor < NBR_FLOORS);
        currentFloor = fromFloor;
        while (currentFloor != toFloor && (toFloor < NBR_FLOORS && toFloor >= 0)) {
            if (goingUp) {
                currentFloor++;
            } else {
                if (--currentFloor == 0) {
                    goingUp = true;
                }
            }
        }
    }

    public synchronized void waitForPassengers(int currentFloor) {
        doorsOpen = true;
        notifyAll();
        while ((toEnter[currentFloor] > 0 && passengersInLift < 4) || toExit[currentFloor] > 0 || peopleMoving > 0 || IntStream.of(toEnter).sum() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isMoving = true;
        doorsOpen = false;
        notifyAll();
    }


    public synchronized void enterLift(int fromFloor, int toFloor) {
        toEnter[fromFloor]--;
        passengersInLift++;
        toExit[toFloor]++;
        peopleMoving--;
        notifyAll();
        moving(toFloor);
    }

    private synchronized void moving(int floor) {
        while (!isMoving || floor != currentFloor) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        peopleMoving++;
        notifyAll();
    }

    public synchronized void exitLift(int floor) {
        toExit[floor]--;
        isMoving = true;
        passengersInLift--;
        peopleMoving--;
        notifyAll();
    }

    public synchronized void waitForLift(int floor) {
        if(!isMoving) notifyAll();
        toEnter[floor]++;
        while (!doorsOpen || passengersInLift >= 4 || currentFloor != floor || peopleMoving + passengersInLift >= 4) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        peopleMoving++;
        notifyAll();
    }

    public boolean goingUp() {
        return goingUp;
    }

    public int passengersInLift() {
        return passengersInLift;
    }

    public int[] getToEnter() {
        return toEnter;
    }

    public int[] getToExit() {
        return toExit;
    }

    public boolean doorsOpen() {
        return doorsOpen;
    }
}
