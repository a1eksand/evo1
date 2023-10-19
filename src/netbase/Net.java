package netbase;

import java.io.Serializable;
import java.util.Random;
import netbase.impl.NeuralNet;
import netbase.impl.DoubleIterator;

public class Net implements Serializable {
  private final NeuralNet nn;
  private final double[] signature;

  public Net(int size, int inputSize, int outputSize, Random rnd) {
    this(size, inputSize, outputSize, DoubleIterator.of(rnd));
  }

  public Net(int size, int inputSize, int outputSize, double[] signature) {
    this(size, inputSize, outputSize, DoubleIterator.of(signature));
  }

  private Net(int size, int inputSize, int outputSize, DoubleIterator doubleIterator) {
    int[] numberOfHiddenNeurons = {size, size, size, size};
    ActivationFunction[] hiddenAcFnc = {
        new Linear(doubleIterator.nextDouble()),
        new Linear(doubleIterator.nextDouble()),
        new Linear(doubleIterator.nextDouble()),
        new Linear(doubleIterator.nextDouble())};
    var outputAcFnc = new Linear(doubleIterator.nextDouble());
    nn = new NeuralNet(
        inputSize,
        outputSize,
        numberOfHiddenNeurons,
        hiddenAcFnc,
        outputAcFnc,
        doubleIterator
    );
    signature = doubleIterator.getArray();
  }

  public double[] calc(double[] input) {
    nn.setInputs(input);
    nn.calc();
    return nn.getOutputs().getArray();
  }

  public double[] getSignature() {
    return signature;
  }
}
