package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> sender;
    private double waterLevel;

    public WaterController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(500 / Settings.SPEEDUP);
                if (m != null) {
                    sender = m.getSender();
                    switch (m.getOrder()) {
                        case WATER_IDLE -> {
                            io.drain(false);
                            io.fill(false);
                        }
                        case WATER_FILL -> {
                            io.fill(true);
                            while (io.getWaterLevel() < 10) {
                                Thread.sleep(1000 / Settings.SPEEDUP);
                            }
                            io.fill(false);
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case WATER_DRAIN -> {
                            io.drain(true);
                            while (io.getWaterLevel() > 0) {
                                Thread.sleep(1000 / Settings.SPEEDUP);
                            }
                            io.drain(false);
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        default -> throw new Error("Invalid command: " + m.getOrder());
                    }
                } else {

                }
            }
        } catch (InterruptedException e) {
            System.out.println("WaterController terminated");
        }
    }
}
