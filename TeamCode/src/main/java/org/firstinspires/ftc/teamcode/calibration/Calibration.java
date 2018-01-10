package org.firstinspires.ftc.teamcode.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.config.calibration.Claws;
import org.firstinspires.ftc.teamcode.robot.config.calibration.Wheels;

import java.util.Vector;

@TeleOp(name = "Calibration", group = "Calibration")
public class Calibration extends OpMode {
    private static final String NEXT_SUBSYSTEM = "NEXT_SUBSYSTEM";

    // Devices & Buttons
    private Robot robot = null;
    private ButtonHandler buttons = null;

    // Subsystems
    private Subsystem current = null;
    private Vector<Subsystem> subsystems = new Vector<>();

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry);
        buttons = new ButtonHandler(robot);

        // Our master switch
        buttons.register(NEXT_SUBSYSTEM, gamepad1, PAD_BUTTON.guide);

        // Manual registration of subsystems
        // These subsystems will change year-to-year but the framework is bot-agnostic
        subsystems.add(new Claws(this, robot, buttons));
        subsystems.add(new Wheels(this, robot, buttons));
    }

    @Override
    public void start() {
        // Select and activate the first subsystem
        current = next(current);
        current.activate();
    }

    @Override
    public void loop() {
        // Print the header
        telemetry.clear();
        telemetry.addData("Subsystem", current.name());

        // User input
        buttons.update();

        // Cycle through the registered subsystems
        if (buttons.get(NEXT_SUBSYSTEM)) {
            current.deactivate();
            telemetry.clearAll();
            current = next(current);
            current.activate();
        }

        // Do whatever the subsystem wants
        current.loop();

        // Publish
        telemetry.update();
    }

    private Subsystem next(Subsystem last) {
        Subsystem next;
        if (last == null) {
            next = subsystems.firstElement();
        } else {
            int i = subsystems.indexOf(last);
            i++;
            if (i >= subsystems.size()) {
                i = 0;
            }
            next = subsystems.elementAt(i);
        }
        return next;
    }
}
