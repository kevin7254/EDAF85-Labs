package clock;

import clock.io.ClockOutput;

public class ClockThread {
    static void mainLoop(ClockOutput out) throws InterruptedException {
        long t, diff;
        t = System.currentTimeMillis();
        int alarmBeeps = 0;
        boolean alarmOn = false;
        while (true) {
            t += 1000;
            TimerValues.incrementSeconds(out);
            if (TimerValues.isAlarm()) {
                alarmOn = true;
            }
            if (alarmOn && TimerValues.isAlarmOn()) {
                out.alarm();
                alarmBeeps++;
                if (alarmBeeps == 20) {
                    alarmOn = false;
                    alarmBeeps = 0;
                }
            }
            diff = t - System.currentTimeMillis();
            if (diff > 0) {
                Thread.sleep(diff);
            }
        }
    }
}
