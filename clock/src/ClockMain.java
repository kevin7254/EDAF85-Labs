package clock;

import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;

import java.util.concurrent.Semaphore;

public class ClockMain {

    public static void main(String[] args) {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput in = emulator.getInput();
        ClockOutput out = emulator.getOutput();

        Thread clockThread = new Thread(() -> {
            try {
                ClockThread.mainLoop(out);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread choicesThread = new Thread(() -> {
            try {
                while (true) {
                    Semaphore semaphore = in.getSemaphore();
                    semaphore.acquire();
                    UserInput userInput = in.getUserInput();
                    int choice = userInput.getChoice();
                    if (choice == ClockInput.CHOICE_SET_TIME) {
                        TimerValues.setTime(userInput.getHours(), userInput.getMinutes(), userInput.getSeconds());
                    } else if (choice == ClockInput.CHOICE_SET_ALARM) {
                        TimerValues.setAlarmTime(userInput.getHours(), userInput.getMinutes(), userInput.getSeconds());
                    } else if (choice == ClockInput.CHOICE_TOGGLE_ALARM) {
                        TimerValues.setAlarmOn(!TimerValues.isAlarmOn());
                        out.setAlarmIndicator(TimerValues.isAlarmOn());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        clockThread.start();
        choicesThread.start();
    }
}
