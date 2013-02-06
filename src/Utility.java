import java.util.Random;

public class Utility {
	public static double[] mutate(double[] in, double percentage, Random r) {
		double[] out = new double[in.length];
		
		for (int i = 0; i < out.length; i++) {
			out[i] = mutate(in[i], percentage, r);
		}
		
		return out;
	}
	
	public static double mutate(double in, double percentage, Random r) {
		return -percentage + r.nextDouble() * (2 * percentage + 1.0);
	}
}
