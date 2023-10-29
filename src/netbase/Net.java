package netbase;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;
import java.util.function.Function;
import netbase.impl.NeuralNet;
import netbase.impl.DoubleIterator;

public class Net implements Serializable {

  @Serial
  private static final long serialVersionUID = -8043151241592634009L;

  private final NeuralNet nn;
  private final double[] signature;

  public Net(int size, int inputSize, int outputSize, Random rnd, Function<Double, ? extends ActivationFunction> functionFactory) {
    this(size, inputSize, outputSize, DoubleIterator.of(rnd), functionFactory);
  }

  public Net(int size, int inputSize, int outputSize, Random rnd) {
    this(size, inputSize, outputSize, DoubleIterator.of(rnd), Linear::new);
  }

  public Net(int size, int inputSize, int outputSize, double[] signature) {
    this(size, inputSize, outputSize, DoubleIterator.of(signature), Linear::new);
  }

  private Net(int size, int inputSize, int outputSize, DoubleIterator doubleIterator, Function<Double, ? extends ActivationFunction> functionFactory) {
    int[] numberOfHiddenNeurons = {size, size, size, size};
    ActivationFunction[] hiddenAcFnc = {
        functionFactory.apply(doubleIterator.nextDouble()),
        functionFactory.apply(doubleIterator.nextDouble()),
        functionFactory.apply(doubleIterator.nextDouble()),
        functionFactory.apply(doubleIterator.nextDouble())};
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
