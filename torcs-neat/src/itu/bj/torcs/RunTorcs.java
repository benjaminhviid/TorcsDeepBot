package itu.bj.torcs;

import org.bj.deeplearning.dataobjects.TrainingData;
import org.bj.deeplearning.executables.Evaluator;

import java.io.IOException;

public class RunTorcs {


	public static void main(String[] args) {

		String[] arguments = {
				"itu.bj.torcs.TorcsNeatController",
				"host:localhost",
				"port:3001",
				"maxEpisodes:1",
				"maxSteps:10000",
				"trackName:aalborg",
				"stage:2"
		};
		//Client.setDriver(new DrunkKitt());
        //Client.main(arguments);


		for (int i = 0; i < 100; i++){
			TrainingData td = new TrainingData(i);
			try {
				double[] output = Evaluator.sendOutput(td.getPixelData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}


	}
