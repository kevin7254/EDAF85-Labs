package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> sender;
    private final double LOWER_BOUND = 0.000952;
    private final double UPPER_BOUND = 0.0476;

    public TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(1000 / Settings.SPEEDUP);
                if (m != null) {
                    sender = m.getSender();
                    switch (m.getOrder()) {
                        case TEMP_IDLE -> io.heat(false);
                        case TEMP_SET_40 -> {
                            io.heat(true);
                            while (io.getTemperature() < 40 - LOWER_BOUND) {
                                Thread.sleep(1000 / Settings.SPEEDUP);
                            }
                            io.heat(false);
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case TEMP_SET_60 -> {
                            io.heat(true);
                            while (io.getTemperature() < 60 - LOWER_BOUND) {
                                Thread.sleep(1000 / Settings.SPEEDUP);
                            }
                            io.heat(false);
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        default -> throw new Error("Invalid command: " + m.getOrder());
                    }
                } else {
                    if (io.getTemperature() > UPPER_BOUND) {
                        io.heat(false);
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("TemperatureController terminated");
        }
    }
}
