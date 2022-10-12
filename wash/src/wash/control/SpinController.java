package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;
    private int i;
    private WashingMessage.Order order;

    public SpinController(WashingIO io) {
        this.io = io;
        this.i = WashingIO.SPIN_LEFT;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);
                if (m != null) {
                    System.out.println("washing spin got " + m);
                    order = m.getOrder();
                    switch (order) {
                        case SPIN_SLOW -> {
                            order = WashingMessage.Order.SPIN_SLOW;
                            m.getSender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case SPIN_FAST -> {
                            io.drain(true);
                            io.setSpinMode(WashingIO.SPIN_FAST);
                            m.getSender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case SPIN_OFF -> {
                            io.drain(false);
                            io.setSpinMode(WashingIO.SPIN_IDLE);
                            m.getSender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        default -> throw new Error("washing spin got unexpected message: " + m);
                    }
                }
                if (order == WashingMessage.Order.SPIN_SLOW) {
                    i = (WashingIO.SPIN_RIGHT == i) ? WashingIO.SPIN_LEFT : WashingIO.SPIN_RIGHT; //ternary operator
                    io.setSpinMode(i);
                }
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
