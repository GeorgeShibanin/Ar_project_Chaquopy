package com.example.chokopie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    ArFragment arFragment;
    private boolean shouldAddModel = false;
    byte[] input_frame;
    byte[] b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
    }


    private void placeObject(ArFragment arFragment, Anchor anchor, Uri uri) {
        ModelRenderable.builder()
                .setSource(arFragment.getContext(), uri)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable))
                .exceptionally(throwable -> {
                            Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }

                );
    }

    private void addNodeToScene(ArFragment arFragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);

    }



    private void onUpdate(FrameTime frameTime) {
        if(shouldAddModel) {
            return;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) {
            return;
        }
        try {
            if(callScript(frame.acquireCameraImage()).equals("same")) {
                Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
                for (Plane plane : planes) {
                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                        Anchor anchor = plane.createAnchor(plane.getCenterPose());
                        placeObject(arFragment, anchor, Uri.parse("dino.sfb"));
                        shouldAddModel = true;
                        break;

                    }
                }
            }

        }catch (NotYetAvailableException e) {
        }
    }

    public void wriretxt(String result) {
        TextView textView = findViewById(R.id.text);
        textView.setText(result);
    }

    private void makeCube(Anchor anchor) {
        shouldAddModel = true;
        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    ModelRenderable cubeRenderable= ShapeFactory.makeCube(new Vector3(0.3f,0.3f,0.3f),
                            new Vector3(0f, 0.3f, 0f), material);
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setRenderable(cubeRenderable);
                    arFragment.getArSceneView().getScene().addChild(anchorNode);
                });
    }



    public String callScript(Image image) {

        /*byte[] cameraJpeg = extractImageDataFromARCore(image);
        String cameraFileName = "photo2" + ".jpg";
        saveImage(cameraJpeg, cameraFileName);
        //Bitmap bit = getBitmapofImage(image);
        String baseDir = Environment.getExternalStorageState();
        String fileName = "photo2.jpg";
        File f = new File(baseDir+File.separator+fileName);
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Imgcodecs imgcodecs = new Imgcodecs();
        Mat img = imgcodecs.imread("photo2.jpg");

         */

        /*byte[] bitmap = extractImageDataFromARCore(image);
        Bitmap finalBit = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
        Bitmap convertedImage = getResizedBitmap(finalBit, 400);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        convertedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        b = baos.toByteArray();
        imgStr = Base64.encodeToString(b, android.util.Base64.DEFAULT);
         */

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        Python python = Python.getInstance();
        PyObject callScript = python.getModule("myscript");
        PyObject callFunc = callScript.callAttr("func", getBitmapofImage(image), "photo6.jpg");
        image.close();

        if(callFunc.toString().equals("1")) {
            return "same";
        } else {
            return "notsame";
        }

    }

    public byte[] getBitmapofImage(Image image) {
        ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
        ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
        ByteBuffer bufferV = image.getPlanes()[2].getBuffer();

        byte[] bytes = new byte[bufferY.capacity()+bufferU.capacity()+bufferV.capacity()];

        bufferY.get(bytes,0,bufferY.capacity());
        bufferU.get(bytes, bufferY.capacity(), bufferU.capacity());
        bufferV.get(bytes, bufferY.capacity()+bufferU.capacity(), bufferV.capacity());


        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, image.getWidth(), image.getHeight(),null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0,0,image.getWidth(), image.getHeight()), 100, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /*
    private static byte[] convertYUV420888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }


    private static byte[] convertNV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        return out.toByteArray();
    }

    private static byte[] extractImageDataFromARCore(Image image) {
        byte[] nv21 = convertYUV420888toNV21(image);
        byte[] data = convertNV21toJPEG(nv21, image.getWidth(), image.getHeight());
        return data;

    }

    private int[] decodeGreyscale(byte[] nv21, int width, int height) {
        int pixelCount = width * height;
        int[] out = new int[pixelCount];
        for (int i = 0; i < pixelCount; ++i) {
            int luminance = nv21[i] & 0xFF;
            // out[i] = Color.argb(0xFF, luminance, luminance, luminance);
            out[i] = 0xff000000 | luminance <<16 | luminance <<8 | luminance;//No need to create Color object for each.
        }
        return out;
    }

     */

    /*private static File generateSaveFile(String fileName) {
        return new File(Environment.getExternalStorageState(), "/saved_images/" + fileName);
    }

    private static void saveImage(byte[] data, String fileName) {
        File file = generateSaveFile(fileName);
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     */




    /*public boolean setupAugmentedImagesDb(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap = loadAugmentedImage();
        if (bitmap == null) {
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("model1", bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadAugmentedImage() {
        try (InputStream is = getAssets().open("pc_photo.jpg")) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ImageLoad", "IO Exception", e);
        }

        return null;
    }*/

}
