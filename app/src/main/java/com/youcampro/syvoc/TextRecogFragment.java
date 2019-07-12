package com.youcampro.syvoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
//import com.google.firebase.codelab.mlkit.GraphicOverlay.Graphic;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TextRecogFragment extends Fragment
        implements View.OnClickListener
{

    private ImageView mImageView;
    private GraphicOverlay mGraphicOverlay;
    private Bitmap mSelectedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_recog, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        //mImageView = view.findViewById(R.id.image_view);
        mGraphicOverlay = view.findViewById(R.id.graphic_overlay);
        view.findViewById(R.id.button_text).setOnClickListener(this);
        String photoPath = getActivity().getExternalFilesDir(null) + "/pic.jpg";

        mSelectedImage = BitmapFactory.decodeFile(photoPath);
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            int w=mSelectedImage.getWidth();
            int h=mSelectedImage.getHeight();
            mSelectedImage = Bitmap.createBitmap(mSelectedImage, 0, 0, mSelectedImage.getWidth(), mSelectedImage.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {

        }

        ((ImageView)getActivity().findViewById(R.id.image_view)).setImageBitmap(mSelectedImage);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_text:
                //String photoPath = getActivity().getExternalFilesDir(null) + "/pic.jpg";
                //mSelectedImage = BitmapFactory.decodeFile(photoPath);

                //ImageView imageView = (ImageView) getActivity().findViewById(R.id.imamImageView.setImageBitmap(BitmapFactory.decodeFile(photoPath));ge_view);


                runTextRecognition();
                break;
        }
    }

    private void runTextRecognition() {
        //FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        /*String photoPath = getActivity().getExternalFilesDir(null) + "/pic.jpg";
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this.getContext(), Uri.parse(getActivity().getExternalFilesDir(null) + "/pic.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);

        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        //mTextButton.setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                //mTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                //mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
