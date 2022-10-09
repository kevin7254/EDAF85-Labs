package clock;

import clock.io.ClockOutput;

import java.util.concurrent.Semaphore;

public class TimerValues {
    private static int hours = 0;
    private static int minutes = 0;
    private static int seconds = 0;

    private static int alarmHours = 0;

    private static int alarmMinutes = 0;

    private static int alarmSeconds = 0;

    private static boolean alarmOn = false;
    private static final Semaphore semaphore = new Semaphore(1);

    public static void setTime(int hours, int minutes, int seconds) throws InterruptedException {
        semaphore.acquire();
        TimerValues.hours = hours;
        TimerValues.minutes = minutes;
        TimerValues.seconds = seconds;
        semaphore.release();
    }

    public static void setAlarmTime(int hours, int minutes, int seconds) throws InterruptedException {
        semaphore.acquire();
        TimerValues.alarmHours = hours;
        TimerValues.alarmMinutes = minutes;
        TimerValues.alarmSeconds = seconds;
        semaphore.release();
    }

    public static boolean isAlarm() throws InterruptedException {
        semaphore.acquire();
        if (alarmOn && hours == alarmHours && minutes == alarmMinutes && seconds == alarmSeconds) {
            semaphore.release();
            return true;
        }
        semaphore.release();
        return false;
    }

    public static boolean isAlarmOn() {
        return alarmOn;
    }

    public static void setAlarmOn(boolean on) {
        alarmOn = on;
    }

    public static void incrementSeconds(ClockOutput out) throws InterruptedException {
        semaphore.acquire();
        seconds++;
        if (seconds == 60) {
            seconds = 0;
            minutes++;
            if (minutes == 60) {
                minutes = 0;
                hours++;
                if (hours == 24) {
                    hours = 0;
                }
            }
        }

        out.displayTime(hours, minutes, seconds);
        semaphore.release();
    }
}
