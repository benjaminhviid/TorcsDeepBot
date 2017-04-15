package org.bj.deeplearning.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class ContinuousSequentialTraining extends ContinuousTraining {

    public ContinuousSequentialTraining() throws FileNotFoundException, IOException {
        super();

       /* //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        model.setListeners(new StatsListener(statsStorage));*/
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