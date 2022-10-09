package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;
    private int i;
    public SpinController(WashingIO io) {
        this.io = io;
        this.i = 0;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(1000 / Settings.SPEEDUP);
                if (m != null) {
                    System.out.println("washing spin got " + m);
                    switch (m.getOrder()) {
                        case SPIN_SLOW -> {
                            io.drain(false);
                            if(i == 0) {
                                io.setSpinMode(WashingIO.SPIN_RIGHT);
                                i = 1;
                            }
                            else if(i == 1) {
                                io.setSpinMode(WashingIO.SPIN_LEFT);
                                i = 0;
                            }
                            m.getSender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        }
                        case SPIN_FAST -> {
                            io.drain(false);
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
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
