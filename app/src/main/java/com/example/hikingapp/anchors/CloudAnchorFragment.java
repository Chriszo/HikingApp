/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hikingapp.anchors;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.hikingapp.NavigationActivity;
import com.example.hikingapp.R;
import com.example.hikingapp.domain.route.Route;
import com.example.hikingapp.utils.GlobalUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.codelab.cloudanchor.helpers.CameraPermissionHelper;
import com.google.ar.core.codelab.cloudanchor.helpers.CloudAnchorManager;
import com.google.ar.core.codelab.cloudanchor.helpers.DisplayRotationHelper;
import com.google.ar.core.codelab.cloudanchor.helpers.FirebaseManager;
import com.google.ar.core.codelab.cloudanchor.helpers.ResolveDialogFragment;
import com.google.ar.core.codelab.cloudanchor.helpers.SnackbarHelper;
import com.google.ar.core.codelab.cloudanchor.helpers.StorageManager;
import com.google.ar.core.codelab.cloudanchor.helpers.TapHelper;
import com.google.ar.core.codelab.cloudanchor.helpers.TrackingStateHelper;
import com.google.ar.core.codelab.cloudanchor.rendering.BackgroundRenderer;
import com.google.ar.core.codelab.cloudanchor.rendering.ObjectRenderer;
import com.google.ar.core.codelab.cloudanchor.rendering.ObjectRenderer.BlendMode;
import com.google.ar.core.codelab.cloudanchor.rendering.PlaneRenderer;
import com.google.ar.core.codelab.cloudanchor.rendering.PointCloudRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Main Fragment for the Cloud Anchors Codelab.
 *
 * <p>This is where the AR Session and the Cloud Anchors are managed.
 */
public class CloudAnchorFragment extends Fragment implements GLSurfaceView.Renderer {

    private static final String TAG = CloudAnchorFragment.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private final CloudAnchorManager cloudAnchorManager = new CloudAnchorManager();
    private DisplayRotationHelper displayRotationHelper;
    private TrackingStateHelper trackingStateHelper;
    private TapHelper tapHelper;
    private StorageManager storageManager = new StorageManager();
    private FirebaseManager firebaseManager;

    private FrameLayout bottomSheetView;


    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private ObjectRenderer virtualObject = new ObjectRenderer();
    private ObjectRenderer virtualObjectCopy = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private static final String SEARCHING_PLANE_MESSAGE = "Searching for surfaces...";
    //    private final float[] andyColor = {139.0f, 195.0f, 74.0f, 255.0f};
    private final float[] pinColor = {195.0f, 0.0f, 0.0f, 255.0f};

    private boolean modelsVisible;


    @Nullable
    private Anchor currentAnchor = null;

    private List<Anchor> anchors = null;

    private Long routeId;
    private String currentPath;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tapHelper = new TapHelper(context);
        trackingStateHelper = new TrackingStateHelper(requireActivity());

        firebaseManager = new FirebaseManager(context);

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate from the Layout XML file.
        View rootView = inflater.inflate(R.layout.cloud_anchor_fragment, container, false);
        GLSurfaceView surfaceView = rootView.findViewById(R.id.surfaceView);
        this.surfaceView = surfaceView;
        displayRotationHelper = new DisplayRotationHelper(requireContext());
        surfaceView.setOnTouchListener(tapHelper);

        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);


        routeId = getArguments().getLong("routeId");

        modelsVisible = true;

        FirebaseDatabase.getInstance()
                .getReference()
                .child("shared_anchor_codelab_root")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getValue();
                        if (data != null) {
                            data.forEach((key, cloudAnchorId) -> {

                                if (!key.trim().equals("next_short_code")) {

                                    String[] cloudAnchorTokens = key.split(";");
                                    if (cloudAnchorTokens.length == 3) {
                                        Long retrievedRouteId = Long.parseLong(cloudAnchorTokens[1]);
                                        int shortCode = Integer.parseInt(cloudAnchorTokens[2]);
                                        if (routeId.equals(retrievedRouteId)) {
                                            cloudAnchorManager.resolveCloudAnchor(session, (String) cloudAnchorId, anchor -> onResolvedAnchorAvailable(anchor, shortCode));
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        bottomSheetView = rootView.findViewById(R.id.bottom_sheet_ar);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        FloatingActionButton cameraButton = bottomSheetView.findViewById(R.id.cameraButton);
        FloatingActionButton arButton = bottomSheetView.findViewById(R.id.ar_button);

        cameraButton.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Camera button clicked", Toast.LENGTH_LONG).show();

            if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.CAMERA}, GlobalUtils.CAMERA_REQUEST);

            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(photoIntent, GlobalUtils.CAMERA_REQUEST);
        });

        arButton.setOnClickListener(view -> {

            if (modelsVisible) {
                Gson json = new Gson();
                virtualObjectCopy = json.fromJson(json.toJson(virtualObject), ObjectRenderer.class);
                virtualObject = new ObjectRenderer();
                modelsVisible = false;
                Toast.makeText(getContext(), "3D Models removed", Toast.LENGTH_LONG).show();
                arButton.setImageResource(R.drawable.ar_2_icon);
            } else {
                virtualObject = virtualObjectCopy;
                modelsVisible = true;
                Toast.makeText(getContext(), "3D Models added", Toast.LENGTH_LONG).show();
                arButton.setImageResource(R.drawable.ar_2_icon_remove);
            }
        });

        ImageView backButton = rootView.findViewById(R.id.back_btn);

        backButton.setOnClickListener(v -> {

            Intent navigationIntent = new Intent(getContext(), NavigationActivity.class);
            navigationIntent.putExtra("route", (Route)getArguments().get("route"));
            navigationIntent.putExtra("authInfo", (FirebaseUser)getArguments().get("authInfo"));
            startActivity(navigationIntent);
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: Called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: CALLED");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (requestCode == GlobalUtils.CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            storage.getReference("routes/" + routeId.toString() + "/photos")
                    .listAll()
                    .addOnSuccessListener(listResult -> {

                        int nextIndex = listResult.getItems().size() + 1;
                        storage.getReference("routes/" + routeId.toString() + "/photos/photo_" + routeId.toString() + "_" + nextIndex + ".jpg")
                                .putBytes(byteArray)
                                .addOnSuccessListener(taskSnapshot -> {
                                    System.out.println("Successfully uploaded image.");
                                    Toast.makeText(getContext(), "Photo saved successfully.", Toast.LENGTH_LONG).show();
                                });
                    });
//            dispatchTakePictureIntent()
//            imageView.setImageBitmap(imageBitmap)
        }
    }


    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.resolveActivity(getActivity().getPackageManager());
        File photoFile = createImageFile();

        Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider", photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, GlobalUtils.CAMERA_REQUEST);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File newPhotoFile = File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );

        currentPath = newPhotoFile.getAbsolutePath();
        return newPhotoFile;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(requireActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    CameraPermissionHelper.requestCameraPermission(requireActivity());
                    return;
                }

                // Create the session.
                session = new Session(requireActivity());

                Config config = new Config(session);
                config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
                session.configure(config);

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(requireActivity(), message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper
                    .showError(requireActivity(), "Camera not available. Try restarting the app.");
            session = null;
            return;
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            Toast.makeText(requireActivity(), "Camera permission is needed to run this application",
                    Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(requireActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(requireActivity());
            }
            requireActivity().finish();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(getContext());
            planeRenderer.createOnGlThread(getContext(), "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(getContext());

            virtualObject.createOnGlThread(getContext(), "models/map_pin.obj", "models/andy.png");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);


            virtualObjectShadow
                    .createOnGlThread(getContext(), "models/andy_shadow.obj", "models/andy_shadow.png");
            virtualObjectShadow.setBlendMode(BlendMode.Shadow);
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            cloudAnchorManager.onUpdate();
            Camera camera = frame.getCamera();

            // Handle one tap per frame.
            handleTap(frame, camera);

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame);

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            // If not tracking, don't draw 3D objects, show tracking failure reason instead.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                messageSnackbarHelper.showMessage(
                        getActivity(), TrackingStateHelper.getTrackingFailureReasonString(camera));
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize tracked points.
            // Use try-with-resources to automatically release the point cloud.
            try (PointCloud pointCloud = frame.acquirePointCloud()) {
                pointCloudRenderer.update(pointCloud);
                pointCloudRenderer.draw(viewmtx, projmtx);
            }

            // No tracking error at this point. If we didn't detect any plane, show searchingPlane message.
//            if (!hasTrackingPlane()) {
//                messageSnackbarHelper.showMessage(getActivity(), SEARCHING_PLANE_MESSAGE);
//            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            if (!anchors.isEmpty()) {
                for (Anchor anchor : anchors) {
                    drawModel(anchor, viewmtx, projmtx,colorCorrectionRgba);
                }
            }

            drawModel(currentAnchor, viewmtx,projmtx,colorCorrectionRgba);
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void drawModel(Anchor anchor, float[] viewmtx, float[] projmtx, float[] colorCorrectionRgba) {
        if (anchor != null && anchor.getTrackingState() == TrackingState.TRACKING) {
            anchor.getPose().toMatrix(anchorMatrix, 0);
            // Update and draw the model and its shadow.
            virtualObject.updateModelMatrix(anchorMatrix, 1f);
            virtualObjectShadow.updateModelMatrix(anchorMatrix, 1f);

            virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, pinColor);
            virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, pinColor);
        }
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(Frame frame, Camera camera) {
        if (currentAnchor != null) {
            return; // Do nothing if there was already an anchor.
        }

        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                Trackable trackable = hit.getTrackable();
                // Creates an anchor if a plane or an oriented point was hit.
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.

                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor is created on the Plane to place the 3D model
                    // in the correct position relative both to the world and to the plane.
                    currentAnchor = hit.createAnchor();
                    messageSnackbarHelper.showMessage(getActivity(), "Hosting anchor...");
                    cloudAnchorManager.hostCloudAnchor(session, currentAnchor, 300, this::onHostedAnchorAvailable);
                    break;
                }
            }
        }
    }

    /**
     * Checks if we detected at least one plane.
     */
    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private synchronized void onClearButtonPressed() {
        // Clear the anchor from the scene.
        cloudAnchorManager.clearListeners();
        currentAnchor = null;
    }

    private synchronized void onHostedAnchorAvailable(Anchor anchor) {

        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            String cloudAnchorId = anchor.getCloudAnchorId();
            firebaseManager.nextShortCode(shortCode -> {
                if (shortCode != null && routeId != null) {
                    firebaseManager.storeUsingShortCode(routeId + ";" + shortCode, cloudAnchorId);
                    messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Hosted. Short code: " + shortCode);
                } else {
                    // Firebase could not provide a short code.
                    messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Hosted, but could not "
                            + "get a short code from Firebase.");
                }
            });
            currentAnchor = anchor;
        } else {
            messageSnackbarHelper.showMessage(getActivity(), "Error while hosting: " + cloudState.toString());
        }
    }

    private synchronized void onResolveButtonPressed() {
        ResolveDialogFragment dialog = ResolveDialogFragment.createWithOkListener(this::onShortCodeEntered);
        dialog.show(getActivity().getSupportFragmentManager(), "Resolve");
    }

    private synchronized void onShortCodeEntered(int shortCode) {
        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> {
            if (cloudAnchorId == null || cloudAnchorId.isEmpty()) {
                messageSnackbarHelper.showMessage(
                        getActivity(),
                        "A Cloud Anchor ID for the short code " + shortCode + " was not found.");
                return;
            }
            cloudAnchorManager.resolveCloudAnchor(
                    session,
                    cloudAnchorId,
                    anchor -> onResolvedAnchorAvailable(anchor, shortCode));
        });
    }

    private void onResolvedAnchorAvailable(Anchor anchor, int shortCode) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Resolved. Short code: " + shortCode);
            currentAnchor = anchor;

            if (Objects.isNull(anchors)) {
                anchors = new ArrayList<>();
            }
            anchors.add(anchor);
        } else {
            messageSnackbarHelper.showMessage(
                    getActivity(),
                    "Error while resolving anchor with short code " + shortCode + ". Error: "
                            + cloudState.toString());
        }
    }


}