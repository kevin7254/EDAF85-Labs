package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);
        
        WashingIO io = sim.startSimulation();

        TemperatureController temp = new TemperatureController(io);
        WaterController water = new WaterController(io);
        SpinController spin = new SpinController(io);

        temp.start();
        water.start();
        spin.start();

        ActorThread<WashingMessage> program = null;

        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);

            switch (n) {
                case 0:
                    assert program != null;
                    program.interrupt();
                    break;

                case 1:
                    program = new WashingProgram1(io, temp, water, spin);
                    program.start();
                    program.join();
                    break;
                case 3: {
                    program = new WashingProgram3(io, temp, water, spin);
                    program.start();
                    program.join();
                    break;
                }
            }
            /*case 0:
                temp.send(new WashingMessage(temp, WashingMessage.Order.TEMP_IDLE));
                water.send(new WashingMessage(water, WashingMessage.Order.WATER_IDLE));
                spin.send(new WashingMessage(spin, WashingMessage.Order.SPIN_IDLE));
                break;
            case 1:
                temp.send(new WashingMessage(temp, WashingMessage.Order.TEMP_SET_40));
                water.send(new WashingMessage(water, WashingMessage.Order.WATER_FILL));
                spin.send(new WashingMessage(spin, WashingMessage.Order.SPIN_SLOW));
                break;
            case 2:
                temp.send(new WashingMessage(temp, WashingMessage.Order.TEMP_SET_60));
                water.send(new WashingMessage(water, WashingMessage.WATER_FILL, 20));
                spin.send(new WashingMessage(spin, WashingMessage.SPIN_FAST));
                break;
            }*/

            // TODO:
            // if the user presses buttons 1-3, start a washing program
            // if the user presses button 0, and a program has been started, stop it
        }
    }
};
