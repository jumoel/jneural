import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


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
		
		setRandomWeights();
	}
	
	public NeuralNetwork(int[] topology, double[] weights) {
		this(topology);
		
		setWeights(weights);
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
	
	public double[] getWeights() {
		double[] weights = new double[weight_count];
		
		int weight_index = 0;
		for (int layer = 0; layer < layers.size() - 1; layer++) {
			for (Neuron neuron : layers.get(layer).neurons) {
				// Regular weights
				for (int i = 0; i < neuron.neuron_weights.length; i++) {
					weights[weight_index] = neuron.neuron_weights[i];
					weight_index++;
				}
				
				weights[weight_index] = neuron.bias_weight;
				weight_index++;				
			}
		}
		
		return weights;
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
		final class Entry implements Comparable<Entry> {
			public NeuralNetwork nn;
			public double fitness;

			public int compareTo(Entry arg0) {
				return Double.compare(-fitness, -arg0.fitness);
			}
		}
		
		double inputs[][] = { {170}, {190}, {165}, {180}, {210} };
		double expectedOutputs[][] = { {1}, {0}, {1}, {0}, {1} };
		
		int number_of_entries = 30;
		ArrayList<Entry> entries = new ArrayList<Entry>(number_of_entries);
		int[] topology = new int[] {1, 2, 1};
		for (int i = 0; i < number_of_entries; i++) {
			Entry e = new Entry();
			e.nn = new NeuralNetwork(topology);
			
			entries.add(e);
		}
		
		int new_networks = 10;
		int mutate_networks = 10;
		int number_of_cycles = 0;
		
		int kill_count = new_networks + mutate_networks;
		
		for (int n = 0; n <= number_of_cycles; n++) {
			
			// Evaluate networks
			for (int entry = 0; entry < entries.size(); entry++) {
				double error = 0;
				
				for (int i = 0; i < inputs.length; i++) {
					error += Math.pow(expectedOutputs[i][0] - entries.get(entry).nn.calculateOutput(inputs[i])[0], 2);
				}
				
				entries.get(entry).fitness = error / inputs.length;
			}
			
			Collections.sort(entries);
			
			if (n != number_of_cycles) {
				// Kill, create new, etc.
				
				entries.subList(entries.size() - kill_count, entries.size()).clear();
				
				for (int i = 0; i < mutate_networks; i++) {
					Entry e = new Entry();
					e.nn = new NeuralNetwork(topology);
					e.nn.setWeights(entries.get(i).nn.getWeights());
					
					entries.add(e);
				}
				
				for (int i = 0; i < new_networks; i++) {
					Entry e = new Entry();
					e.nn = new NeuralNetwork(topology);
					
					entries.add(e);
				}
			}
		}
		
		System.out.println(entries.get(0).fitness);
	}
}
