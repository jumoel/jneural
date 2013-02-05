import java.util.ArrayList;
import java.util.Random;

import javax.swing.text.Position.Bias;


public class NeuralNetwork {
	private class Layer {
		public ArrayList<Neuron> neurons;
		
		public Layer(int neuron_number) {
			neurons = new ArrayList<Neuron>();
			
			for (int n = 0; n < neuron_number; n++) {
				neurons.add(new Neuron());
			}
		}
	}
	
	private class Neuron {
		public double bias_weight;
		public double[] neuron_weights = null;
		private double output;
		
		public Neuron() {	
		}
		
		public void initializeWeightArray(int weight_count) {
			neuron_weights = new double[weight_count];
		}
	}
	
	
	private ArrayList<Layer> layers;
	private int weight_count;
	public static int DUMMY_INPUT = 1;
	
	public NeuralNetwork(int[] topology) {
		if (topology.length < 2) {
			throw new IllegalArgumentException("A minimum of two (2) layers (input and output) are required");
		}
		
		layers = new ArrayList<Layer>();
		
		for (int neurons_in_layer : topology) {
			layers.add(new Layer(neurons_in_layer));
		}
		
		weight_count = CalculateWeightCount(topology);
		InitializeWeightArrays(layers);
	}
	
	private static void InitializeWeightArrays(ArrayList<Layer> layers) {
		for (int i = 0; i < layers.size() - 1; i++) {
			int next_layer_size = layers.get(i + 1).neurons.size();
			
			for (Neuron n : layers.get(i).neurons) {
				n.initializeWeightArray(next_layer_size);
			}
		}
	}
	
	private static int CalculateWeightCount(int[] topology) {
		int weight_count = 0;
		
		// Regular weights
		for (int i = 0; i < topology.length - 1; i++) {
			weight_count += topology[i] * topology[i + 1];
		}
		
		// Dummy weights
		for (int layer_neuron_count : topology) {
			weight_count += layer_neuron_count;
		}
		
		return weight_count;
	}
	
	public void setWeights(double[] weights) {
		if (weights.length != weight_count) {
			throw new IllegalArgumentException("Number of supplied weights does not match number of weights in network.");
		}
		
		int weight_index = 0;
		
		for (int layer = 0; layer < layers.size() - 1; layer++) {
			for (Neuron neuron : layers.get(layer).neurons) {
				// Regular weights
				for (int i = 0; i < neuron.neuron_weights.length; i++) {
					neuron.neuron_weights[i] = weights[weight_index];
					weight_index++;
				}
				
				neuron.bias_weight = weights[weight_index];
				weight_index++;				
			}
		}
	}
	
	public void setRandomWeights() {
		Random r = new Random();
		
		double[] random_weights = new double[weight_count];
		for (int i = 0; i < weight_count; i++) {
			random_weights[i] = r.nextDouble();
		}
		
		setWeights(random_weights);
	}
	
	public static double Sigmoid(double x) {
		return 1.0 / ( 1.0 + Math.exp(-x));
	}
	
	private void setInput(double[] input) {
		ArrayList<Neuron> input_neurons = layers.get(0).neurons;
		
		if (input_neurons.size() != input.length) {
			throw new IllegalArgumentException("Given input size does not match network input size");
		}
		
		for (int i = 0; i < input_neurons.size(); i++) {
			input_neurons.get(i).output = input[i];
		}
	}
	
	private void propagateOutputs() {
		for (int layer = 1; layer < layers.size(); layer++) {
			
			ArrayList<Neuron> neurons = layers.get(layer).neurons;
			ArrayList<Neuron> previous_layer = layers.get(layer - 1).neurons;
			
			for (int neuron = 0; neuron < neurons.size(); neuron++) {
				double sum = 0;
				
				sum += neurons.get(neuron).bias_weight * DUMMY_INPUT;
				
				for (int previous_layer_neuron = 0; previous_layer_neuron < previous_layer.size(); previous_layer_neuron++) {
					Neuron p = previous_layer.get(previous_layer_neuron);
					sum += p.output * p.neuron_weights[neuron];
				}
				
				neurons.get(neuron).output = Sigmoid(sum);
			}
		}
	}
	
	private double[] getOutputs() {
		ArrayList<Neuron> output_neurons = layers.get(layers.size() - 1).neurons;
		
		double[] outputs = new double[output_neurons.size()];
		
		for (int i = 0; i < output_neurons.size(); i++) {
			outputs[i] = output_neurons.get(i).output;
		}
		
		return outputs;
	}
	
	public double[] calculateOutput(double[] input) {
		setInput(input);
		propagateOutputs();
		return getOutputs();
	}
	
	public static void main(String[] args) {
	
	}
}
