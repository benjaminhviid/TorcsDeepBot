package org.bj.deeplearning.tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.stream.DoubleStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;


//import static com.sun.tools.doclint.Entity.image;

public class ImageTool extends RandomAccessFile {

    public ImageTool(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    /**
     * Prints an image as a .png image. Assumes three color channels
     *
     * @param flattened The pixel data
     * @param width     The width of the image
     * @param file      The file to save the image to
     * @throws IOException @see {@link javax.imageio.ImageIO#write(java.awt.image.RenderedImage, String, File)}
     */
    public static void printColoredPngImage(byte[] flattened, int width, File file) throws IOException {
        int height = flattened.length / width / 3;

        byte[] flipped = flipImageBytes(flattened, width, 3);

        DataBuffer buffer = new DataBufferByte(flipped, flattened.length);

        //3 bytes per pixel: red, green, blue
        WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 3 * width, 3, new int[]{0, 1, 2}, (Point) null);
        ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(cm, raster, true, null);

        ImageIO.write(image, "png", file);
    }

    public static void printRedColoredPngImage(byte[] flattened, int width, File file) throws IOException {
        flattened = zeroOutGreenValues(flattened);
        flattened = zeroOutBlueValues(flattened);
        printColoredPngImage(flattened, width, file);
    }

    public static void printGreenColoredPngImage(byte[] flattened, int width, File file) throws IOException {
        flattened = zeroOutRedValues(flattened);
        flattened = zeroOutBlueValues(flattened);
        printColoredPngImage(flattened, width, file);
    }

    public static void printBlueColoredPngImage(byte[] flattened, int width, File file) throws IOException {
        flattened = zeroOutRedValues(flattened);
        flattened = zeroOutGreenValues(flattened);
        printColoredPngImage(flattened, width, file);
    }

    public static void printGreyScalePngImage(double[] flattened, int width, File file) throws IOException {
        byte[] greyScale = DoubleStream.of(flattened)
                .boxed()
                .map(d -> toGreyScale(d))
                .reduce(new byte[0], (cumArr, curArr) -> ArrayUtils.addAll(cumArr, curArr));
        printColoredPngImage(greyScale, width, file);
    }

    private static byte toByte(double d) {
        double cutOff = 5.0;
        if (d < 0.0) {
            d = 0.0;
        }
        if (d > cutOff) {
            d = cutOff;
        }
        int colorValue = (int) Math.round((255.0 / cutOff) * d);
        if (colorValue > 255 || colorValue < 0) {
            throw new IllegalStateException();
        }
        return (byte) colorValue;
    }

    private static byte[] toGreyScale(double d) {
        return new byte[]{toByte(d), toByte(d), toByte(d)};
    }

    /**
     * Flips a flattened image vertically
     *
     * @param flattened     The pixel data
     * @param width         The width of the image
     * @param colorChannels The amount of color channels used
     * @return The image flipped vertically
     */
    public static byte[] flipImageBytes(byte[] flattened, int width, int colorChannels) {
        byte[] result = new byte[flattened.length];

        int height = flattened.length / width / colorChannels;
        int rowWidth = width * colorChannels;
        int offset;

        for (int row = 0; row < height; row++) {
            offset = row * rowWidth;
            System.arraycopy(flattened, offset, result, flattened.length - (rowWidth * (row + 1)), rowWidth);
        }
        return result;
    }

    public static double[] toScaledDoubles(byte[] bs) {
        final int channelLength = bs.length / 3;

        double[] r = new double[channelLength];
        double[] g = new double[channelLength];
        double[] b = new double[channelLength];

        for (int i = 0; i < channelLength; i++) {
            r[i] = scale(bs[i * 3 + 0]);
            g[i] = scale(bs[i * 3 + 1]);
            b[i] = scale(bs[i * 3 + 2]);
        }

        double[] result = new double[bs.length];
        System.arraycopy(r, 0, result, channelLength * 0, channelLength);
        System.arraycopy(g, 0, result, channelLength * 1, channelLength);
        System.arraycopy(b, 0, result, channelLength * 2, channelLength);
        return result;
    }

    public static byte[] zeroOutRedValues(byte[] bs) {
        return zeroOutValues(bs, 0);
    }

    public static byte[] zeroOutGreenValues(byte[] bs) {
        return zeroOutValues(bs, 1);
    }

    public static byte[] zeroOutBlueValues(byte[] bs) {
        return zeroOutValues(bs, 2);
    }

    private static byte[] zeroOutValues(byte[] bs, int value) {
        if (2 < value || value < 0) {
            throw new RuntimeException("Value has to be either 0, 1 or 2");
        }
        for (int idx = value; idx < bs.length; idx += 3) {
            bs[idx] = 0x0;
        }
        return bs;
    }

    public static double scale(double d) {
        return d / (double) 0xFF;
    }

    public static double scale(byte b) {
        return scale(toDouble(b));
    }

    public static double toDouble(byte b) {
        return (b & 0xFF);
    }


    private static void log(String s) {
        System.out.println(s);
    }

    private static String toByteString(int color) {
        // Perform a bitwise AND for convenience while printing.
        // Otherwise Integer.toHexString() interprets values as integers and a negative byte 0xFF will be printed as "ffffffff"
        return Integer.toHexString(color & 0xFF);
    }

    public static byte[] bufferedImageToByteArray(String path){


        // Load the image. This expects the image to be in the same package with this class
        BufferedImage image;
        try {

            image = ImageIO.read(new FileImageInputStream(new File(path)));
            int iw = image.getWidth();
            int ih = image.getHeight();

        int bands = image.getSampleModel().getNumBands();
        if (bands != 3) {
            throw new RuntimeException("The image does not have 3 color bands.");
        }

        byte bytes[] = new byte[3 * iw * ih];
        int index = 0;

        // note that image is processed row by row top to bottom
        for(int y = 0; y < ih; y++) {
            for(int x = 0; x < iw; x++) {

                int pixel = image.getRGB(x, y);

                // Get pixels
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;
                bytes[index++] = (byte) red;
                bytes[index++] = (byte) green;
                bytes[index++] = (byte) blue;
            }
        }
            return bytes;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws IOException {
        // testing:
        //byte[] img = bufferedImageToByteArray();
        //System.out.println(java.util.Arrays.toString(img));


        /*TrainingData images = TrainingDataHandler.instance().getTrainingData(10, 10).get(0);
        printColoredPngImage(images.getPixelData(), images.getWidth(), new File("img.png"));
        printRedColoredPngImage(images.getPixelData().clone(), images.getWidth(), new File("red.png"));
        printGreenColoredPngImage(images.getPixelData().clone(), images.getWidth(), new File("green.png"));
        printBlueColoredPngImage(images.getPixelData().clone(), images.getWidth(), new File("blue.png"));
        */
    }
}