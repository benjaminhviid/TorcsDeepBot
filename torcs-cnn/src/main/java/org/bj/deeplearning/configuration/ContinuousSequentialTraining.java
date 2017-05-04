package org.bj.deeplearning.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class ContinuousSequentialTraining extends ContinuousTraining {
    //final JavaSparkContext sc = new JavaSparkContext(new SparkConf().setMaster("local[*]").set("spark.driver.maxResultSize","3g")
      //      .setAppName("scenes"));

    public ContinuousSequentialTraining() throws FileNotFoundException, IOException {
        super();

    }

    @Override
    public void train(DataSetIterator trainIterator, DataSetIterator testIterator) {


        for(int i = latestEpoch; i <= nEpochs; i++) {

            model.fit(trainIterator);
            System.out.println(String.format("*** Completed epoch %d ***", i));
            testIterator.reset();

            saveModel(model, i);
            outputDeadNeurons(model);
        }
    }
}