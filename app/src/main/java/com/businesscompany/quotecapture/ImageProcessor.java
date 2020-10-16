package com.businesscompany.quotecapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileInputStream;

public class ImageProcessor {

    static Bitmap LoadImageIntoBitmap(String uriString, Point displaySize){

        Bitmap b;
        try {
            File f = new File(uriString);
            b = BitmapFactory.decodeStream(new FileInputStream(f)).copy(Bitmap.Config.ARGB_8888, true);

            //Rotate and scale the display
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            float scaleX = ((float) displaySize.x / (float) b.getHeight());
            float scaleY = ((float) (displaySize.y * 0.9) / (float) b.getWidth());

            if (scaleX <= scaleY)
                matrix.postScale(scaleX, scaleX);
            else
                matrix.postScale(scaleY, scaleY);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }
        catch(Exception e){
            b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        return b;
    }

    static Bitmap LoadImageIntoBitmapAndRotate(String uriString, Point displaySize, int rotations){

        Bitmap b = LoadImageIntoBitmap(uriString, displaySize);
        try {
            //Rotate and scale the display
            Matrix matrix = new Matrix();
            matrix.postRotate(-90 * (rotations+1));
            float scaleX = ((float) displaySize.x / (float) b.getHeight());
            float scaleY = ((float) (displaySize.y * 0.9) / (float) b.getWidth());

            if (scaleX <= scaleY)
                matrix.postScale(scaleX, scaleX);
            else
                matrix.postScale(scaleY, scaleY);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }
        catch(Exception e){
            b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        return b;
    }


    static Bitmap LoadBlankImageIntoBitmap(String uriString, Point displaySize){

        Bitmap b;
        try {
            File f = new File(uriString);
            b = BitmapFactory.decodeStream(new FileInputStream(f)).copy(Bitmap.Config.ARGB_8888, true);

            //Rotate and scale the display
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            float scaleX = ((float) displaySize.x / (float) b.getHeight());
            float scaleY = ((float) (displaySize.y * 0.9) / (float) b.getWidth());

            if (scaleX <= scaleY)
                matrix.postScale(scaleX, scaleX);
            else
                matrix.postScale(scaleY, scaleY);

            b = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
        }
        catch(Exception e){
            b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        return b;
    }

    static Bitmap LoadHighlightedImageIntoBitmap(String uriString, Point displaySize){

        Bitmap b;
        try {
            File f = new File(uriString);
            b = BitmapFactory.decodeStream(new FileInputStream(f)).copy(Bitmap.Config.ARGB_8888, true);

            //Rotate and scale the display
            Matrix matrix = new Matrix();
            float scaleX = ((float) displaySize.x / (float) b.getWidth());
            float scaleY = ((float) (displaySize.y * 0.9) / (float) b.getHeight());

            if (scaleX <= scaleY)
                matrix.postScale(scaleX, scaleX);
            else
                matrix.postScale(scaleY, scaleY);

            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }
        catch(Exception e){
            b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        return b;
    }

    static Bitmap applyHighlighting(Bitmap image, Bitmap highlighting){
        Bitmap returnBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        returnBitmap.eraseColor(Color.WHITE); // Sets bitmap to be all white

        int startY =  Math.abs((highlighting.getHeight() - image.getHeight()) / 2);
        //int endY = startY + image.getHeight();

        int startX =  Math.abs((highlighting.getWidth() - image.getWidth()) / 2);
        //int endX = startX + image.getWidth();

        //Build the image and find where the cropping should start from
        int cropStartX = 0, cropStartY = returnBitmap.getWidth(), cropEndX = 0, cropEndY = 0;
        boolean noHighlighting = true;
        for (int x = 0; x < returnBitmap.getWidth(); x++){
            for (int y = 0; y < returnBitmap.getHeight(); y++){
                //If that pixel is highlighted
                if (highlighting.getPixel(startX + x, startY + y) != 0) {
                    returnBitmap.setPixel(x, y, image.getPixel(x, y));
                    if (cropStartX == 0)
                        cropStartX = x;
                    if (y < cropStartY)
                        cropStartY = y;
                    if (x > cropEndX)
                        cropEndX = x;
                    if (y > cropEndY)
                        cropEndY = y;
                    noHighlighting  = false;
                }
                //If that pixel is not highlighted
            }
        }

        //Just return the full image if no highlighting was done
        if (noHighlighting == true)
            return image;

        return Bitmap.createBitmap(returnBitmap, cropStartX,  cropStartY, cropEndX - cropStartX, cropEndY - cropStartY);
    }

    static String OpticalCharacterRecognition(Bitmap image, Context context){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame frame = new Frame.Builder().setBitmap(image).build();
        SparseArray<TextBlock> text = textRecognizer.detect(frame);
        String returnText = "";
        if (text.size() > 0)
            for (int i = 0; i < text.size(); i++){
                returnText += text.get(i).getValue();
                if (i != text.size()-1)
                    returnText += "\n";
            }

            return returnText;
    }
}
