package org.bj.deeplearning.executables;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.bj.deeplearning.dataobjects.FileSystem;
import org.bj.deeplearning.dataobjects.TrainingData;
import org.bj.deeplearning.tools.ImageTool;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Evaluator {

	private static final int BATCH_SIZE = 32;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Trainer.init();
		FileSystem.createModelsFolders();
		Path modelPath;
		if(args.length != 1) {
			modelPath = FileSystem.getPathOfLatestModelFile();
		} else {
			modelPath = FileSystem.getPathOfModelFile(Integer.parseInt(args[0]));
		}
		MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(new FileInputStream(modelPath.toFile()));
		System.out.println(network.getLayerWiseConfigurations().toString());
		System.out.format("Evaluating model %s \n", modelPath.toString());
		double accuracy = getAccuracy(network);
	}

	public static double getAccuracy(MultiLayerNetwork network) throws IOException {
		Files.createDirectories(Paths.get("misses"));
		int firstId = Trainer.lastValidationIndex()+1;
		int lastId = Trainer.lastTestIndex();
		if(lastId-firstId < 0) {
			throw new IllegalArgumentException("Test set is empty");
		}

		@SuppressWarnings("unused")
		int hits = 0, misses = 0;

		List<TrainingData> testSet;
		for(int fromId = firstId; fromId <= lastId; fromId += BATCH_SIZE) {
			int toId = Math.min(fromId + BATCH_SIZE, lastId);
			testSet = FileSystem.load(fromId, toId);
			for(TrainingData td : testSet) {
			    System.out.println("Test id: " + td.getId());
				double[] groundTruths = td.getFeatures();
                INDArray in = Nd4j.create(ImageTool.toScaledDoubles(td.getPixelData()), new int[] {1, 3, td.getWidth(), td.getHeight()});

                INDArray output = network.output(in, false);
                System.out.println(output);
/*
                double[] binaryVector = NNTool.toBinaryVector(INDArrayTool.toFlatDoubleArray(output));
				if(ArrayUtils.isEquals(binaryVector, groundTruths)) {
					hits++;
				} else {
					int expectedIdx = getIndex(groundTruths);
					int actualIdx = getIndex(binaryVector);
					//File file = new File(String.format("misses/id%d-expected%d-actual%d.png", td.getId(), expectedIdx, actualIdx));
					//ImageTool.printColoredPngImage(td.getPixelData(), td.getWidth(), file);
					misses++;
				}*/
			}
			System.out.println(String.format("Evaluated %d images", toId-firstId));
		}

		return ((double) hits) / ((double) lastId-firstId+1);
	}

	private static int getIndex(double[] arr) {
		for(int idx = 0; idx < arr.length; idx++) {
			if(1d - arr[idx] < 1E-6) {
				return idx;
			}
		}
		throw new RuntimeException("Did not found a 1 in the array");
	}
}
