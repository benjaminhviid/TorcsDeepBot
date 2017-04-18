package org.bj.deeplearning.configuration;

import java.util.Properties;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration.Builder;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.bj.deeplearning.tools.PropertiesReader;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.*;



public class BuilderFactory {
	private static double learningRate = 0.0;
	private static double l2 = 0.0;
	private static int seed = 0;
	private static boolean regularization = false;
	
	static {
		Properties pp = PropertiesReader.getProjectProperties();
		learningRate = Double.parseDouble(pp.getProperty("training.learningRate"));
		l2 = Double.parseDouble(pp.getProperty("training.l2"));
		seed = Integer.parseInt(pp.getProperty("training.seed"));
		regularization = Boolean.valueOf(pp.getProperty("training.regularization"));
	}

	public static Builder alexnetModel(int width, int height, int featureCount) {
		/**
		 * AlexNet model interpretation based on the original paper ImageNet Classification with Deep Convolutional Neural Networks
		 * and the imagenetExample code referenced.
		 * http://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf
		 **/

		double nonZeroBias = 1;
		double dropOut = 0.5;

        Builder builder = new NeuralNetConfiguration.Builder()
				.seed(seed)
				.weightInit(WeightInit.DISTRIBUTION)
				.dist(new NormalDistribution(0.0, 0.01))
				.activation("relu")
				.updater(Updater.NESTEROVS)
				.iterations(1)
				.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.learningRate(1e-2)
				.biasLearningRate(1e-2*2)
				.learningRateDecayPolicy(LearningRatePolicy.Step)
				.lrPolicyDecayRate(0.1)
				.lrPolicySteps(100000)
				.regularization(true)
				.l2(5 * 1e-3)
				.momentum(0.9)
				.miniBatch(false)
				.list()
				.layer(0, convInit("cnn1", 3, 32, new int[]{3, 3}, new int[]{2, 2}, new int[]{3, 3}, 1))
				.layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
				.layer(2, maxPool("maxpool1", new int[]{3,3}))
				.layer(3, conv5x5("cnn2", 32, new int[] {3,3}, new int[] {2,2}, nonZeroBias))
				.layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
				.layer(5, maxPool("maxpool2", new int[]{3,3}))
				.layer(6,conv3x3("cnn3", 32, 0))
				.layer(7,conv3x3("cnn4", 32, nonZeroBias))
				.layer(8,conv3x3("cnn5", 32, nonZeroBias))
				.layer(9, maxPool("maxpool3", new int[]{3,3}))
				.layer(10, fullyConnected("ffn1", 64, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
                .layer(11, fullyConnected("ffn2", 64, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))

                .layer(12, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
						.name("output")
						.nOut(featureCount)
						.activation("identity")
						.build())
				.backprop(true)
				.pretrain(false)
				.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;

	}

	private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
		return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv3x3(String name, int out, double bias) {
		return new ConvolutionLayer.Builder(new int[]{3,3}, new int[] {1,1}, new int[] {1,1}).name(name).nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
		return new ConvolutionLayer.Builder(new int[]{5,5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
	}

	private static SubsamplingLayer maxPool(String name,  int[] kernel) {
		return new SubsamplingLayer.Builder(kernel, new int[]{2,2}).name(name).build();
	}

	private static DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
		return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
	}
	
	public static Builder getVeryShallowConvNet(int height, int width, int featureCount) {
		int layerId = 0;
		
		Builder builder = new NeuralNetConfiguration.Builder()
		.seed(seed)
		.iterations(1)
		.regularization(regularization)
		.l2(l2)
		.learningRate(learningRate)
		.weightInit(WeightInit.XAVIER)
		.activation("leakyrelu")
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(Updater.NESTEROVS).momentum(0.9)
		.list()
		.layer(layerId++, new ConvolutionLayer.Builder(16, 16)
				.nIn(3)
				.stride(4, 4)
				.nOut(40)
				.padding(6,6)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new DenseLayer.Builder()
				.nOut(100)
				.build())
		.layer(layerId++, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
				.activation("identity")
				.nOut(featureCount)
				.build())
		.backprop(true).pretrain(false)
		.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;
	}
	
	public static Builder getShallowConvNet(int height, int width, int featureCount) {
		int layerId = 0;
		
		Builder builder = new NeuralNetConfiguration.Builder()
		.seed(seed)
		.iterations(1)
		.regularization(regularization)
		.l2(l2)
		.learningRate(learningRate)
		.weightInit(WeightInit.XAVIER)
		.activation("relu")
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(Updater.NESTEROVS).momentum(0.9)
		.list()
		.layer(layerId++, new ConvolutionLayer.Builder(8, 8)
				.nIn(3)
				.stride(2, 2)
				.nOut(20)
				.padding(3,3)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new ConvolutionLayer.Builder(4, 4)
				.nIn(20)
				.stride(2, 2)
				.nOut(40)
				.padding(1,1)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new DenseLayer.Builder()
				.nOut(100)
				.build())
		.layer(layerId++, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
				.activation("identity")
				.nOut(featureCount)
				.build())
		.backprop(true).pretrain(false)
		.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;
	}
	
	public static Builder getDeepConvNet(int height, int width, int featureCount) {
		int layerId = 0;
		
		Builder builder = new NeuralNetConfiguration.Builder()
		.seed(seed)
		.iterations(1)
		.regularization(regularization)
		.l2(l2)
		.learningRate(learningRate)
		.weightInit(WeightInit.XAVIER)
		.activation("relu")
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(Updater.NESTEROVS).momentum(0.9)
		.list()
		.layer(layerId++, new ConvolutionLayer.Builder(3, 3)
				.nIn(3)
				.stride(1, 1)
				.nOut(60)
				.padding(1,1)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new ConvolutionLayer.Builder(3, 3)
				.nIn(60)
				.stride(1, 1)
				.nOut(60)
				.padding(1,1)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new ConvolutionLayer.Builder(3, 3)
				.nIn(60)
				.stride(1, 1)
				.nOut(80)
				.padding(1,1)
				.build())
		.layer(layerId++,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new ConvolutionLayer.Builder(3, 3)
				.nIn(80)
				.stride(1, 1)
				.nOut(120)
				.padding(1,1)
				.build())
		.layer(layerId++, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(layerId++, new DenseLayer.Builder()
				.nOut(40)
				.build())
		.layer(layerId++, new DenseLayer.Builder()
				.nOut(40)
				.build())
		.layer(layerId++, new DenseLayer.Builder()
				.nOut(40)
				.build())
		.layer(layerId++, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
				.activation("identity")
				.nOut(featureCount)
				.build())
		.backprop(true).pretrain(false)
		.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;
	}
	
	public static Builder getConvNet(int height, int width, int featureCount) {
		Builder builder = new NeuralNetConfiguration.Builder()
		.seed(seed)
		.iterations(1)
		.regularization(regularization)
		.l2(l2)
		.learningRate(learningRate)
		.weightInit(WeightInit.XAVIER)
		.activation("relu")
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(Updater.NESTEROVS).momentum(0.9)
		.list()
		.layer(0, new ConvolutionLayer.Builder()
				.name("layer0")
				.kernelSize(4, 4)
				.nIn(3)
				.stride(2, 2)
				.nOut(20)
				.padding(1, 1)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(1,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.name("layer1")
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(2, new ConvolutionLayer.Builder()
				.name("layer2")
				.kernelSize(3, 3)
				.nIn(20)
				.stride(1, 1)
				.nOut(20)
				.padding(1,1)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(3,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.name("layer3")
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(4, new DenseLayer.Builder()
				.name("layer4")
				.activation("relu")
				.nOut(250)
				.build())
		.layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
				.name("layer5")
				.nOut(featureCount)
				.activation("identity")
				.build())
		.backprop(true).pretrain(false)
		.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;
	}
	
	public static Builder getReducingConvNet(int height, int width, int featureCount) {
		Builder builder = new NeuralNetConfiguration.Builder()
		.seed(seed)
		.iterations(1)
		.regularization(regularization)
		.l2(l2)
		.learningRate(learningRate)
		.weightInit(WeightInit.XAVIER)
		.activation("relu")
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(Updater.NESTEROVS).momentum(0.9)
		.list()
		.layer(0, new ConvolutionLayer.Builder()
				.name("layer0")
				.kernelSize(1, 1)
				.nIn(3)
				.stride(1, 1)
				.nOut(40)
				.padding(0, 0)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(1,
				new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				.name("layer1")
				.kernelSize(2, 2)
				.stride(2, 2)
				.build())
		.layer(2, new ConvolutionLayer.Builder()
				.name("layer2")
				.kernelSize(3, 3)
				.nIn(40)
				.stride(1, 1)
				.nOut(40)
				.padding(1,1)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(3, new ConvolutionLayer.Builder()
				.name("layer2")
				.kernelSize(2, 2)
				.nIn(40)
				.stride(2, 2)
				.nOut(40)
				.padding(0,0)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(4, new ConvolutionLayer.Builder()
				.name("layer3")
				.kernelSize(2, 2)
				.nIn(40)
				.stride(2, 2)
				.nOut(40)
				.padding(0,0)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(5, new ConvolutionLayer.Builder()
				.name("layer4")
				.kernelSize(2, 2)
				.nIn(40)
				.stride(2, 2)
				.nOut(40)
				.padding(0,0)
				.dropOut(0.5)
				.activation("relu")
				.build())
		.layer(6, new DenseLayer.Builder()
				.name("layer5")
				.activation("relu")
				.nOut(200)
				.build())
		.layer(7, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
				.name("layer6")
				.nOut(featureCount)
				.activation("identity")
				.build())
		.backprop(true).pretrain(false)
		.setInputType(InputType.convolutionalFlat(height, width, 3));
		return builder;
	}
}
