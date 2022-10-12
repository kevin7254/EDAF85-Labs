package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * <p>
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * <p>
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram2 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram2(WashingIO io,
                           ActorThread<WashingMessage> temp,
                           ActorThread<WashingMessage> water,
                           ActorThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    @Override
    public void run() {
        try {
            io.lock(true);

            water.send(new WashingMessage(this, WATER_FILL));
            WashingMessage ack1 = receive();
            System.out.println("got " + ack1);
            water.send(new WashingMessage(this, WATER_IDLE));
            temp.send(new WashingMessage(this, TEMP_SET_40));
            WashingMessage m = receive();
            System.out.println("goht " + m);


            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, SPIN_SLOW));
            WashingMessage ack2 = receive();
            System.out.println("washing program 1 got " + ack2);

            // Spin for 20 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(20 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, TEMP_IDLE));
            receive();
            // Instruct SpinController to stop spin barrel spin.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, SPIN_OFF));
            receive();

            // Instruct WaterController to drain water from barrel.
            // Expect an acknowledgment in response.
            System.out.println("setting WATER_DRAIN...");
            water.send(new WashingMessage(this, WATER_DRAIN));
            WashingMessage ack4 = receive();
            System.out.println("washing program 1 got " + ack4);

            water.send(new WashingMessage(this, WATER_FILL));
            receive();
            water.send(new WashingMessage(this, WATER_IDLE));
            temp.send(new WashingMessage(this, TEMP_SET_60));
            receive();
            System.out.println("goht " + m);


            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, SPIN_SLOW));
            receive();

            // Spin for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(30 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, TEMP_IDLE));
            receive();
            // Instruct SpinController to stop spin barrel spin.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, SPIN_OFF));
            receive();

            // Instruct WaterController to drain water from barrel.
            // Expect an acknowledgment in response.
            System.out.println("setting WATER_DRAIN...");
            water.send(new WashingMessage(this, WATER_DRAIN));
            receive();

            for (int i = 0; i < 5; i++) {
                water.send(new WashingMessage(this, WATER_FILL));
                WashingMessage ack5 = receive();
                System.out.println("got " + ack5);
                water.send(new WashingMessage(this, WATER_IDLE));
                Thread.sleep(5 * 60000 / Settings.SPEEDUP);
                water.send(new WashingMessage(this, WATER_DRAIN));
                receive();
            }
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_FAST));
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);
            WashingMessage ack6 = receive();
            System.out.println("got " + ack6);
            spin.send(new WashingMessage(this, SPIN_OFF));

            receive();
            io.lock(false);
        } catch (InterruptedException e) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle

            temp.send(new WashingMessage(this, TEMP_IDLE));
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }
}
