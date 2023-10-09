package netbase;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import netbase.impl.NeuralNet;
import netbase.impl.DoubleIterator;

public class Net implements Serializable {

  @Serial
  private static final long serialVersionUID = -8043151241592634009L;

  private final NeuralNet nn;
  private final double[] signature;

  public Net(int size, int inputSize, int outputSize, Supplier<RandomGenerator> rnd, Function<Double, ? extends ActivationFunction> functionFactory) {
    this(size, inputSize, outputSize, DoubleIterator.of(rnd), functionFactory);
  }

  public Net(int size, int inputSize, int outputSize, Supplier<RandomGenerator> rnd) {
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

  public synchronized double[] calc(double[] input) {
    return nn.calc(input);
  }

  public double[] getSignature() {
    return signature;
  }
}
