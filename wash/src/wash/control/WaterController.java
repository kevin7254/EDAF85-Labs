package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> sender;
    private double waterLevel;
    private boolean drained, filled;
    private WashingMessage.Order order;

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
                    order = m.getOrder();
                    switch (m.getOrder()) {
                        case WATER_IDLE -> {
                            order = WashingMessage.Order.WATER_IDLE;
                            io.fill(false);
                            io.drain(false);
                            drained = false;
                            filled = false;
                        }
                        case WATER_FILL -> {
                            order = WashingMessage.Order.WATER_FILL;
                            io.drain(false);
                            io.fill(true);
                        }
                        case WATER_DRAIN -> {
                            order = WashingMessage.Order.WATER_DRAIN;
                            io.drain(true);
                            io.fill(false);
                        }
                        default -> throw new Error("Invalid command: " + m.getOrder());
                    }
                }
                if(order == WashingMessage.Order.WATER_FILL) {
                    filled = !(io.getWaterLevel() < 10);
                } else if(order == WashingMessage.Order.WATER_DRAIN) {
                    drained = !(io.getWaterLevel() > 0);
                }
                if(drained) {
                    sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                }
                if(filled) {
                    sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                }
            }
        } catch (InterruptedException e) {
            System.out.println("WaterController terminated");
        }
    }
}
