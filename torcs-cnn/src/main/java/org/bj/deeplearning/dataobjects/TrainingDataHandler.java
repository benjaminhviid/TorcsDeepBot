package org.bj.deeplearning.dataobjects;

import org.bj.deeplearning.tools.ImageTool;
import org.bj.deeplearning.tools.PropertiesReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by benjaminhviid on 31/03/2017.
 **/

public class TrainingDataHandler {
    //
    public static final String TRAINING_DATA_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/trainingdata.csv").getPath();

    public static final String GROUND_TRUTH_LABELS_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/traininglabels.csv").getPath();

    public static final String SCREENSHOTS_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/screenshots/").getPath();

    public static final String TEST_DATA_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/test/trainingdata.csv").getPath();
    public static final String TEST_SCREENSHOTS_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/test/screenshots/").getPath();


    static TrainingDataType trainingDataType = TrainingDataType.MINIMAL;
    public static RunType runType = RunType.TEST;

    public static int iterator = 0;
    static Properties projectProperties = PropertiesReader.getProjectProperties();
    public static int width = Integer.parseInt(projectProperties.getProperty("training.image.width"));
    public static int height = Integer.parseInt(projectProperties.getProperty("training.image.height"));



    public static void main(String[] args) throws IOException {
        List<String> headers = getGroundTruthLabels();

        for (String header : headers){
            System.out.println(header);
        }

       // writePixelDataToTextFile();
    }

    static FileWriter writer;
    public static void writePixelDataToTextFile() throws IOException {

        int counter = 0;
        File dir = new File(TrainingDataHandler.class.getClassLoader().getResource("torcsdata/screenshots").getPath());
        File[] directoryListing = dir.listFiles();


        Arrays.sort(directoryListing, new Comparator<File>() {
            public int compare(File f1, File f2) {
                try {

                    int i1 = Integer.parseInt(f1.getName().substring(10, f1.getName().length() - 4));
                    int i2 =  Integer.parseInt(f2.getName().substring(10, f2.getName().length() - 4));
                    return i1 - i2;
                } catch(NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });


        if (directoryListing != null) {
            for (File child : directoryListing) {

                byte[] pixelData = ImageTool.bufferedImageToByteArray(child.getPath());
                System.out.println(pixelData.length);
                //WriteTextToCSV(Arrays.toString(pixelData));
                //counter++;

                if (counter % 1000 == 0){
                    System.out.println(counter);
                }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }




    }

    static void WriteTextToCSV (String value){
        //String PIXELDATA_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/pixeldata.csv").getPath();
        String PIXELDATA_PATH = "/home/benjaminhviid/torcsdeepbot/torcs-cnn/src/main/resources/torcsdata/pixeldata.txt";
        try {
            writer = new FileWriter(PIXELDATA_PATH, true);
            StringBuilder sb = new StringBuilder();

            sb.append(value);

            writer.write(sb.toString());
            writer.write("\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static int numberOfNonGroundTruthsInTrainingDataSet= 0; // height, width, id;

    public static List<TrainingData> getTrainingData(int fromId, int toId) {

        List<TrainingData> result = new ArrayList<TrainingData>();
        getIndicesToIntList(fromId, toId).forEach(i->result.add(new TrainingData(i, trainingDataType, runType)));
        return result;
    }

    public static List<TrainingData> getRandomTrainingData(int numberOfSamples){

        List<TrainingData> result = new ArrayList<TrainingData>();
        getRandomIndicesIntToList(numberOfSamples).forEach(i->result.add(new TrainingData(i, trainingDataType, runType)));
        return result;
    }

    public static int getSampleCount(RunType runType){
        String path;
        if (runType == RunType.TRAINING)
            path = TRAINING_DATA_PATH;
        else
            path = TEST_DATA_PATH;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            return lines - 1; // subtract header
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getTotalNumberOfImages (){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(TRAINING_DATA_PATH));
            int numOfLines = (int)reader.lines().count() - 1;
            reader.close();
            return numOfLines;
        }catch(Exception e){
            System.out.println(e.getMessage() + "\n" + e.getStackTrace());
            return -1;
        }
    }

    /*public static int getNumberOfGroundTruths(){

        try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {
            String[] line = lines.skip(1).findFirst().get().split(";"); // skip header
            return line.length - numberOfNonGroundTruthsInTrainingDataSet;
        }
        catch(Exception e){
            System.out.println(e.getStackTrace());
            return -1;
        }

    }*/

    public static int getNumberOfGroundTruths(){
        if (trainingDataType == TrainingDataType.EXTENSIVE){
            return 7;
        }
        else if (trainingDataType == TrainingDataType.MINIMAL){
            return 2;
        }
        else{
            return 4;
        }
    }


public static int getWidth(){
    try {
        try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {

            String[] line =  lines.skip(1).findFirst().get().split(";"); // skip header
            return width;
        }
    }
    catch(Exception e){
        System.out.println(e.getStackTrace());
        return -1;
    }
}

public static int getHeight(){
    try {
        try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {
            String[] line =  lines.skip(1).findFirst().get().split(";"); // skip header
            return height;
        }
    }
    catch(Exception e){
        System.out.println(e.getStackTrace());
        return -1;
    }
}

public static int getWidthHeightProduct(){
    return getWidth() * getHeight();
}


public static List<String> getGroundTruthLabels() {

    List<String> result = new ArrayList<String>();
    try {
        BufferedReader reader = new BufferedReader(new FileReader(GROUND_TRUTH_LABELS_PATH));
        String headers = reader.readLine();
        reader.close();
        String[] headerArray = headers.split(";");
        for (String header : headerArray) {
            if (trainingDataType == TrainingDataType.MINIMAL && result.size() == 3)
                continue;
            result.add(header.trim());
        }
    } catch (IOException e) {
        throw new IllegalStateException("Result was empty");
    }

    return result;
}

    public static String[] getSample(int id, RunType runType){

    String path;
    if (runType == RunType.TRAINING)
        path = TRAINING_DATA_PATH;
    else
        path = TEST_DATA_PATH;

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines.skip(id-1).findFirst().get().split(";");
        }
        catch (Exception e){
            System.out.println("Error at ID " + id + "\n" + e.getStackTrace());
            return new String[0];

        }
    }

    private static List<Integer> indicesToIntList(Stream<Integer> indices, int limit){
        return indices.limit(limit).collect(Collectors.toList());
    }

    public static List<Integer> getIndicesToIntList(int from, int to){
        return indicesToIntList(IntStream.rangeClosed(from, to).boxed(), to-from + 1);
    }

    public static List<Integer> getRandomIndicesIntToList(int numberOfIndices){ // TODO check that both screenshots and samples are shuffled
        List<Integer> range = IntStream.rangeClosed(1, getSampleCount(runType)).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        return  indicesToIntList(range.parallelStream(), numberOfIndices);
    }
}
