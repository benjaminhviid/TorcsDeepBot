package org.bj.deeplearning.dataobjects;

import org.bj.deeplearning.tools.PropertiesReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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

    static TrainingDataType type = TrainingDataType.MINIMAL;
    public static int iterator = 0;
    static Properties projectProperties = PropertiesReader.getProjectProperties();
    public static int width = Integer.parseInt(projectProperties.getProperty("training.image.width"));
    public static int height = Integer.parseInt(projectProperties.getProperty("training.image.height"));



    public static void main(String[] args) throws IOException {
        List<String> headers = getGroundTruthLabels();

        for (String header : headers){
            System.out.println(header);
        }

    }


    private static int numberOfNonGroundTruthsInTrainingDataSet= 0; // height, width, id;


    public static List<TrainingData> getTrainingData(int fromId, int toId) {

        List<TrainingData> result = new ArrayList<TrainingData>();
        getIndicesToIntList(fromId, toId).forEach(i->result.add(new TrainingData(i, type)));
        return result;
    }

    public static List<TrainingData> getRandomTrainingData(int numberOfSamples){

        List<TrainingData> result = new ArrayList<TrainingData>();
        getRandomIndicesIntToList(numberOfSamples).forEach(i->result.add(new TrainingData(i, type)));
        return result;
    }

    public static int getSampleCount(){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(TRAINING_DATA_PATH));
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
        if (type == TrainingDataType.EXTENSIVE || type == TrainingDataType.MINIMAL){
            return 4;
        }
        else{
            return 7;
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
            result.add(header.trim());
        }
    } catch (IOException e) {
        throw new IllegalStateException("Result was empty");
    }

    return result;
}

/*
    UNTESTED
*/
    public static String[] getSample(int id){

        try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {
            return lines.skip(id-1).findFirst().get().split(";");

        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
            return new String[0];

        }
    }

    private static List<Integer> indicesToIntList(Stream<Integer> indices, int limit){
        return indices.limit(limit).collect(Collectors.toList());
    }

    public static List<Integer> getIndicesToIntList(int from, int to){
        return indicesToIntList(IntStream.rangeClosed(from, to).boxed(), to-from + 1);
    }

    public static List<Integer> getRandomIndicesIntToList(int numberOfIndices){
        List<Integer> range = IntStream.rangeClosed(0, getSampleCount()).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        return  indicesToIntList(range.parallelStream(), numberOfIndices);
    }
}
