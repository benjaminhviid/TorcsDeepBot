package org.bj.deeplearning.dataobjects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by benjaminhviid on 31/03/2017.
 **/

public class TrainingDataHandler {

    private static final TrainingDataHandler _instance = new TrainingDataHandler();

    private TrainingDataHandler(){ }

    public static TrainingDataHandler instance(){
        return _instance;
    }

    //
    public static final String TRAINING_DATA_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/trainingdata.csv").getPath();

    public static final String GROUND_TRUTH_LABELS_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/traininglabels.csv").getPath();

    public static final String SCREENSHOTS_PATH = TrainingDataHandler.class.getClassLoader().getResource("torcsdata/screenshots/").getPath();

    public static int iterator = 0;

    public static void main(String[] args) throws IOException {
        List<String> headers = getGroundTruthLabels();

        for (String header : headers){
            System.out.println(header);
        }

        /*System.out.println("Num of images: " + getTotalNumberOfImages());
        System.out.println("Number of ground truths: " + getNumberOfGroundTruths());
        System.out.println("Random indices" + getRandomIndices(100));
        getRandomIndicesToList(100).forEach(System.out::println);
*/
        List<TrainingData> training_data = getRandomTrainingData(10);
        //training_data.forEach(sample->System.out.println(sample.getSpeed()));


    }

    private static int angle_index         = 0;
    private static int speed_index         = 1;
    private static int dist_RL_index       = 2;
    private static int dist_RR_index       = 3;
    private static int height_index        = 4;
    private static int width_index         = 5;
    private static int id_index            = 6;
    private static int pixeldata_index     = 7;

    private static int numberOfNonGroundTruthsInTrainingDataSet = 3; // height, width, id;


    public static List<TrainingData> getTrainingData(int fromId, int toId) {

        List<TrainingData> result = new ArrayList<TrainingData>();
        getIndicesToIntList(fromId, toId).forEach(i->result.add(new TrainingData(i)));
        return result;
    }

    public static List<TrainingData> getRandomTrainingData(int numberOfSamples){

        List<TrainingData> result = new ArrayList<TrainingData>();
        getRandomIndicesIntToList(numberOfSamples).forEach(i->result.add(new TrainingData(i)));
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

    public static int getNumberOfGroundTruths(){

        try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {
            String[] line = lines.skip(1).findFirst().get().split(";"); // skip header
            return line.length - numberOfNonGroundTruthsInTrainingDataSet;
        }
        catch(Exception e){
            System.out.println(e.getStackTrace());
            return -1;
        }

    }

    public static int getWidth(){
        try {
            try (Stream<String> lines = Files.lines(Paths.get(TRAINING_DATA_PATH))) {

                String[] line =  lines.skip(1).findFirst().get().split(";"); // skip header
                return Integer.getInteger(line[width_index]);
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
                return Integer.getInteger(line[height_index]);
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
            //if (id == 0)
            //return lines.skip(1).findFirst().get().split(";"); // skip header

            return lines.skip(id-1).findFirst().get().split(";");

        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
            return new String[0];

        }
    }

    public static String getRandomIndices(int numberOfImages) {
        List<Integer> range = IntStream.rangeClosed(0, getSampleCount()).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        return indicesToString(range.parallelStream(), numberOfImages);
    }

    public static String getIndices(int from, int to) {
        return indicesToString(IntStream.rangeClosed(from, to).boxed(), to-from + 1);
    }


    // Converts an array of indices to a string
    private static String indicesToString(Stream<Integer> indices, int limit) {
        return "(" + indices.limit(limit).map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }


    public static List<String> getIndicesToList(int from, int to){
        return indicesToList(IntStream.rangeClosed(from, to).boxed(), to-from + 1);
    }

    public static List<String> getRandomIndicesToList(int numberOfIndices){
        List<Integer> range = IntStream.rangeClosed(0, getSampleCount()).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        return  indicesToList(range.parallelStream(), numberOfIndices);
    }

    private static List<String> indicesToList(Stream<Integer> indices, int limit){
        return indices.limit(limit).map(Object::toString).collect(Collectors.toList());
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
