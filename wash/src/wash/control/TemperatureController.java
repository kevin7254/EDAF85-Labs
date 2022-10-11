package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> sender;
    private final double LOWER_BOUND = (0.000952 * 10) + 0.2;
    private final double UPPER_BOUND = (0.0478 * 10) + 0.2;
    private WashingMessage.Order order = WashingMessage.Order.TEMP_IDLE;
    private int temperature = 0;

    private int count = 0;

    public TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(10000 / Settings.SPEEDUP);
                if (m != null) {
                    sender = m.getSender();
                    order = m.getOrder();
                    switch (order) {
                        case TEMP_IDLE -> {
                            io.heat(false);
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case TEMP_SET_40 -> {
                            temperature = 40;
                            count = 0;
                        }
                        case TEMP_SET_60 -> {
                            temperature = 60;
                            count = 0;
                        }
                        default -> throw new Error("Invalid command: " + m.getOrder());
                    }
                }
                if(order != WashingMessage.Order.TEMP_IDLE) {
                    if(io.getTemperature() < temperature - 2 + LOWER_BOUND) {
                        io.heat(true);
                    } else if(io.getTemperature() > temperature - UPPER_BOUND) {
                        io.heat(false);
                        count++;
                    }
                    if(count == 1) {
                        sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        count++;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("TemperatureController terminated");
        }
    }
}
