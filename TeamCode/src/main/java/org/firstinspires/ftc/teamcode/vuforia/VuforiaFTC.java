/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode.vuforia;

import android.graphics.Color;

import com.qualcomm.ftcrobotcontroller.R;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.HINT;
import com.vuforia.Image;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class VuforiaFTC {
    /**
     * TODO: If you downloaded this file from another team you need to get your own Vuforia key
     * See https://library.vuforia.com/articles/Solution/How-To-Create-an-App-License for instructions
     */
    // Team-specific Vuforia key
    private static final String VUFORIA_KEY = "AbgpAh3/////AAAAGTwS0imaZU6wjVVHhw7cr1iHxcyPegw1+zPNzs+oNjtZlwpyvuwb2hdTLeEEj0gPTWUgVfLbnn6BrV6pafSnN8oCEEZrbVicTGw02BT+V0IzD43++kcsLVuumaM9yAUlAaDPiuEvEx6AZxYnM05KMzlAtMtfgW8tOIvjlicxep9tPhr1Z1Z3JrDt8s8mPo3GsSRSvpoSXZfxRLi0CwGEJlTuVrP59wLhsvr3CZ5Nr7gCNznhAaiGp4LhtCPoXsIUjsQHwO2hmskW670gZGIZl7BvqVbN5mIwqOYF3ZsCUkR83pM7jSIsOMdiaLK5ZlVLG+z5AfgoPNDZo8iYiqTncIiSUL5oJuh2NIeiG+nwcPJV";

    // Short names for external constants
    private static final VuforiaLocalizer.CameraDirection CAMERA_DIRECTION = VuforiaLocalizer.CameraDirection.BACK;
    private static final AxesReference AXES_REFERENCE = AxesReference.EXTRINSIC;
    private static final AngleUnit ANGLE_UNIT = AngleUnit.DEGREES;

    // Cartesian heading constants
    private static final int FULL_CIRCLE = 360;
    private static final int HEADING_OFFSET = -FULL_CIRCLE / 4;

    // Frame capture constants
    private static final int CAPTURE_QUEUE_DISABLE = 0;
    private static final int CAPTURE_QUEUE_LEN = 2;
    private static final int CAPTURE_POLL_TIMEOUT = 100;
    private static final int RED = 0;
    private static final int GREEN = RED + 1;
    private static final int BLUE = GREEN + 1;

    // Tracking config
    private final String CONFIG_ASSET;
    private final int CONFIG_TARGETS_NUM;
    private final VuforiaTarget[] CONFIG_TARGETS;
    private final VuforiaTarget CONFIG_PHONE;

    // Dynamic things we need to remember
    private VuforiaLocalizer vuforia = null;
    private int trackingTimeout = 100;
    private VuforiaTrackables targetsRaw = null;
    private final List<VuforiaTrackable> targets = new ArrayList<>();

    // The actual data we care about
    private long timestamp = 0;
    private final int[] location = new int[3];
    private final int[] orientation = new int[3];
    private final HashMap<String, Boolean> targetVisible = new HashMap<>();
    private final HashMap<String, Integer> targetAngle = new HashMap<>();
    private final HashMap<String, Integer> targetIndex = new HashMap<>();
    private ImageFTC image = null;

    public VuforiaFTC(String targetAsset, int numTargets, VuforiaTarget[] targetConfig, VuforiaTarget phoneConfig) {
        CONFIG_TARGETS = targetConfig;
        CONFIG_PHONE = phoneConfig;
        CONFIG_ASSET = targetAsset;
        CONFIG_TARGETS_NUM = numTargets;
    }

    public void init() {
        // Init Vuforia
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CAMERA_DIRECTION;
        vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        /*
         * Pre-processed target images from the Vuforia target manager:
         * https://developer.vuforia.com/target-manager.
         */
        targetsRaw = vuforia.loadTrackablesFromAsset(CONFIG_ASSET);
        com.vuforia.Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, CONFIG_TARGETS_NUM);
        targets.addAll(targetsRaw);

        // Configure target names, locations, rotations and hashmaps
        for (int i = 0; i < CONFIG_TARGETS_NUM; i++) {
            initTrackable(targetsRaw, i);
        }

        // Location and rotation of the image sensor plane relative to the robot
        OpenGLMatrix phoneLocation = positionRotationMatrix(CONFIG_PHONE.raw, CONFIG_PHONE.rotation, CONFIG_PHONE.axesOrder);
        for (VuforiaTrackable trackable : targets) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(phoneLocation, parameters.cameraDirection);
        }
    }

    public void start() {
        targetsRaw.activate();
    }

    public void track() {
        for (VuforiaTrackable trackable : targets) {
            // Per-target visibility (somewhat imaginary but still useful)
            targetVisible.put(trackable.getName(), ((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible());

            // Angle to target, if available
            OpenGLMatrix newPose = ((VuforiaTrackableDefaultListener) trackable.getListener()).getPose();
            if (newPose != null) {
                Orientation poseOrientation = Orientation.getOrientation(newPose, AXES_REFERENCE, AxesOrder.XYZ, ANGLE_UNIT);
                targetAngle.put(trackable.getName(), (int) poseOrientation.secondAngle);
            }

            /*
             * Update the location and orientation track
             *
             * We poll for each trackable so this happens in the loop, but the overall tracking
             * is aggregated among all targets with a defined pose and location. The current
             * field of view will dictate the quality of the track and if one or more targets
             * are present they will be the primary basis for tracking but tracking persists
             * even when the view does not include a target, and is self-consistent when the
             * view includes multiple targets
             */
            OpenGLMatrix newLocation = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
            if (newLocation != null) {
                // Extract our location from the matrix
                for (int i = 0; i < location.length; i++) {
                    location[i] = (int) newLocation.get(i, 3);
                }

                // Calculate the orientation of our view
                Orientation newOrientation = Orientation.getOrientation(newLocation, AXES_REFERENCE, AxesOrder.XYZ, ANGLE_UNIT);
                orientation[0] = (int) newOrientation.firstAngle;
                orientation[1] = (int) newOrientation.secondAngle;
                orientation[2] = (int) newOrientation.thirdAngle;

                // Timestamp the update
                timestamp = System.currentTimeMillis();
            }
        }
    }

    public void display(Telemetry telemetry) {

        // Is the location track valid?
        telemetry.addData("Valid", isStale() ? "No" : "Yes");

        // List of visible targets (if any)
        String visibleStr = "";
        for (String target : targetVisible.keySet()) {
            if (getVisible(target)) {
                if (!visibleStr.isEmpty()) {
                    visibleStr += ", ";
                }
                visibleStr += target;
            }
        }
        if (visibleStr.isEmpty()) {
            visibleStr = "<None>";
        }
        telemetry.addData("Visible", visibleStr);

        // Angle to each visible target (if any)
        for (String target : targetVisible.keySet()) {
            if (getVisible(target)) {
                telemetry.addData(target + " ∠", getTargetAngle(target) + "°");
            }
        }

        // Raw data from the last location and orientation fix
        telemetry.addData("X/Y Heading", getX() + "/" + getY() + " " + getHeading() + "°");
    }

    /**
     * @return True if frame capture is enabled
     */
    @SuppressWarnings("WeakerAccess")
    public boolean capturing() {
        return vuforia.getFrameQueueCapacity() > CAPTURE_QUEUE_DISABLE;
    }

    /**
     * @param enable Enable or disable frame capture
     */
    public void enableCapture(boolean enable) {
        vuforia.setFrameQueueCapacity(enable ? CAPTURE_QUEUE_LEN : CAPTURE_QUEUE_DISABLE);
        Vuforia.setFrameFormat(ImageFTC.FORMAT_DEFAULT_VUFORIA, enable);
    }

    /**
     * Grab the currently available frame, if any
     */
    public void capture() {
        if (!capturing()) {
            return;
        }

        BlockingQueue<VuforiaLocalizer.CloseableFrame> queue = vuforia.getFrameQueue();
        //noinspection EmptyCatchBlock
        try {
            VuforiaLocalizer.CloseableFrame frame = queue.poll(CAPTURE_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
            if (frame != null && frame.getNumImages() > 0) {
                for (int i = 0; i < frame.getNumImages(); i++) {
                    Image img = frame.getImage(i);
                    if (img != null && img.getFormat() == ImageFTC.FORMAT_DEFAULT_VUFORIA) {
                        image = new ImageFTC(img, ImageFTC.FORMAT_DEFAULT_VUFORIA);
                        break;
                    }
                }
            }
            if (frame != null) {
                frame.close();
            }
        } catch (InterruptedException e) {
        }
    }

    /**
     * @return The most recent available frame, if any
     */
    public ImageFTC getImage() {
        return image;
    }

    /**
     * @param x x coordinate of the pixel to be analyzed
     * @param y y coordinate of the pixel to be analyzed
     * @return Individual R, G, and B values from the pixel
     */
    public int rgb(int x, int y) {
        int[] pixel = {x, y};
        return rgb(pixel, pixel);
    }

    /**
     * @param c1 x,y coordinates of the upper-left corner of the region to be analyzed
     * @param c2 x,y coordinates of he lower-right corner of the region to be analyzed
     * @return Individual sums of the R, G, and B values from the region specified
     */
    @SuppressWarnings("WeakerAccess")
    public int rgb(int[] c1, int[] c2) {
        if (image == null) {
            throw new IllegalStateException("No image captured");
        }

        // Ensure the rectangle we define exists
        if (c1[0] < c2[0] || c1[1] < c2[1] ||
                c2[0] >= image.getHeight() ||
                c2[1] >= image.getWidth()) {
            throw new IllegalArgumentException("Invalid corners: " +
                    "i(" + image.getHeight() + "," + image.getWidth() + ")" +
                    ", c1(" + c1[0] + "," + c1[1] + ")" +
                    ", c2(" + c2[0] + "," + c2[1] + ")");
        }

        // Sum all of the RGB values in the defined region
        int numPixels = 0;
        double[] rgb = {0, 0, 0};
        for (int y = c1[1]; y <= c2[1]; y++) {
            for (int x = c1[0]; x <= c2[0]; x++) {
                int pixel = image.getPixel(x, y);
                rgb[RED] += Color.red(pixel);
                rgb[GREEN] += Color.green(pixel);
                rgb[BLUE] += Color.blue(pixel);
                numPixels++;
            }
        }

        // Return the average color of the region
        return Color.rgb(
                (int) (rgb[RED] / numPixels),
                (int) (rgb[GREEN] / numPixels),
                (int) (rgb[BLUE] / numPixels)
        );
    }

    /**
     * Getters
     */
    public HashMap<String, Boolean> getVisible() {
        return targetVisible;
    }

    /**
     * @param target Name of the target of interest.
     * @return True if the target was actively tracked in the last round of VuforiaFTC processing
     */
    public boolean getVisible(String target) {
        return targetVisible.get(target);
    }

    @SuppressWarnings("unused")
    public HashMap<String, Integer> getTargetAngle() {
        return targetAngle;
    }

    /**
     * @param target Name of the target of interest. Valid targets will also be visible per
     *               {@link #getVisible(String)} getVisible(target)}
     * @return The angle to the target's plane relative to the plane of the phone's image sensor
     * (i.e. 0° is dead-on, negative sign denotes right-of-center)
     */
    @SuppressWarnings("WeakerAccess")
    public int getTargetAngle(String target) {
        return targetAngle.get(target);
    }

    /**
     * @param target Name of the target of interest.
     * @return The Vuforia targetable index for the named target.
     */
    public int getTargetIndex(String target) {
        return targetIndex.get(target);
    }

    /**
     * @param index CONFIG_TARGETS index.
     * @return Live VuforiaTrackable for the indexed target.
     */
    @SuppressWarnings("unused")
    public VuforiaTrackable getTrackable(int index) {
        return targets.get(index);
    }

    /**
     * @param name CONFIG_TARGETS name.
     * @return Live VuforiaTrackable for the named target.
     */
    public VuforiaTrackable getTrackable(String name) {
        return targets.get(getTargetIndex(name));
    }

    /**
     * @return System.currentTimeMillis() as reported at the time of the last location update
     */
    @SuppressWarnings("unused")
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return True when the last location update was more than trackingTimeout milliseconds ago
     */
    public boolean isStale() {
        return (timestamp + trackingTimeout < System.currentTimeMillis());
    }

    @SuppressWarnings("unused")
    public int[] getLocation() {
        return location;
    }

    @SuppressWarnings("unused")
    public int[] getOrientation() {
        return orientation;
    }

    /**
     * @return The X component of the robot's last known location relative to the field center.
     * Negative values denote blue alliance side of field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("WeakerAccess")
    public int getX() {
        return location[0];
    }

    /**
     * @return The Y component of the robot's last known location relative to the field center.
     * Negative sign denotes audience side of field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("WeakerAccess")
    public int getY() {
        return location[1];
    }

    /**
     * @return The robot's last known heading relative to the field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("WeakerAccess")
    public int getHeading() {
        int heading = orientation[2];
        if (orientation[0] < 0) {
            heading -= FULL_CIRCLE / 2;
        }
        return normalizeHeading(cartesianToCardinal(heading));
    }

    /**
     * @param x X component of destination in the field plane
     * @param y Y component of destination in the field plane
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("WeakerAccess")
    public int bearing(int x, int y) {
        return bearing(new int[]{getX(), getY()}, new int[]{x, y});
    }

    /**
     * @param dest X,Y array of destination in the field plane
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("unused")
    public int bearing(int[] dest) {
        return bearing(dest[0], dest[1]);
    }

    /**
     * @param index CONFIG_TARGETS index. Syntax helper for {@link #bearing(int, int)} bearing(int, int)}
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int bearing(int index) {
        return bearing(CONFIG_TARGETS[index].adjusted[0], CONFIG_TARGETS[index].adjusted[1]);
    }

    /**
     * @param x X component of destination in the field plane
     * @param y Y component of destination in the field plane
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("WeakerAccess")
    public int distance(int x, int y) {
        return distance(new int[]{getX(), getY()}, new int[]{x, y});
    }

    /**
     * @param index CONFIG_TARGETS index. Syntax helper for {@link #distance(int, int)} distance(int, int)}
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int distance(int index) {
        return distance(CONFIG_TARGETS[index].adjusted[0], CONFIG_TARGETS[index].adjusted[1]);
    }

    /**
     * @param dest X,Y array of destination in the field plane
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    @SuppressWarnings("unused")
    public int distance(int[] dest) {
        return distance(dest[0], dest[1]);
    }

    @SuppressWarnings("unused")
    public void setTrackingTimeout(int timeout) {
        trackingTimeout = timeout;
    }

    @SuppressWarnings("unused")
    public int getTrackingTimeout() {
        return trackingTimeout;
    }

    /**
     * Helpers
     */

    // Bearing from x1,y1 to x2,y2 in degrees
    // Motion from south to north is correlated with increasing Y components in field locations
    private int bearing(int[] src, int[] dest) {
        double bearing = Math.atan2(dest[1] - src[1], dest[0] - src[0]);
        bearing = Math.toDegrees(bearing);
        return normalizeHeading(cartesianToCardinal((int) bearing));
    }

    // Distance from x1,y1 to x2,y2 in field location units (millimeters)
    private int distance(int[] src, int[] dest) {
        return (int) Math.hypot((dest[1] - src[1]), (dest[0] - src[0]));
    }

    // It's like a macro, but for Java
    private OpenGLMatrix positionRotationMatrix(float[] position, float[] rotation, AxesOrder order) {
        return OpenGLMatrix
                .translation(position[0], position[1], position[2])
                .multiplied(Orientation.getRotationMatrix(
                        AXES_REFERENCE, order, ANGLE_UNIT,
                        rotation[0], rotation[1], rotation[2]));
    }

    // More Java blasphemy
    private void initTrackable(VuforiaTrackables trackables, int index) {
        if (index >= trackables.size() || index < 0) {
            RobotLog.a("Invalid VuforiaFTC trackable index: %d", index);
            return;
        }

        // Per-target hashmaps, by name
        targetIndex.put(CONFIG_TARGETS[index].name, index);
        targetVisible.put(CONFIG_TARGETS[index].name, false);
        targetAngle.put(CONFIG_TARGETS[index].name, 0);

        // Location model parameters
        VuforiaTrackable trackable = trackables.get(index);
        trackable.setName(CONFIG_TARGETS[index].name);
        OpenGLMatrix location = positionRotationMatrix(CONFIG_TARGETS[index].raw,
                CONFIG_TARGETS[index].rotation, CONFIG_TARGETS[index].axesOrder);
        trackable.setLocation(location);
    }

    private int normalizeHeading(int heading) {
        return ((heading % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }

    private int cartesianToCardinal(int heading) {
        return FULL_CIRCLE - (heading + HEADING_OFFSET);
    }
}
