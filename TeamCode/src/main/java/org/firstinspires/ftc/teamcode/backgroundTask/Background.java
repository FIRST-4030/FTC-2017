package org.firstinspires.ftc.teamcode.backgroundTask;

/**
 * Created by Bryan on 4/20/2018.
 */

public abstract class Background implements Runnable {

    private boolean running = false;
    private Thread thread;

    public void start() {
        if (thread != null) return;
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (thread == null) return;
        running = false;
        thread.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            loop();
        }
    }

    protected abstract void loop();

}
