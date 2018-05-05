package org.firstinspires.ftc.teamcode.robot.test;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import org.firstinspires.ftc.teamcode.backgroundTask.Background;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Bryan Cook on 5/4/2018.
 */

public class Sound extends Background {

    private File SOUND_DIR;
    private File[] sounds;

    public MediaPlayer lift = new MediaPlayer();

    public int currentSound = 0;

    public void init() {
        SOUND_DIR = new File(Environment.DIRECTORY_MUSIC);
        sounds = SOUND_DIR.listFiles();
    }

    @Override
    protected void loop() {


    }

    public void next() {
        currentSound--;
        if (currentSound < 0) currentSound = sounds.length - 1;
        try {
            lift.setDataSource(Uri.fromFile(sounds[currentSound]).toString());
            lift.prepare();
            lift.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prev() {
        currentSound++;
        if (currentSound >= sounds.length) currentSound = 0;
        try {
            lift.setDataSource(sounds[currentSound].getPath());
            lift.prepare();
            lift.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
