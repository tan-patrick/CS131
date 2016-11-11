import java.io.*;
import java.util.stream.*;
import java.util.Arrays;

import java.util.concurrent.RecursiveAction;


class RGB {
    public int R, G, B;

    RGB(int r, int g, int b) {
	R = r;
	G = g;
	B = b;
    }

    public String toString() { return "(" + R + "," + G + "," + B + ")"; }

}


class Gaussian {

    static protected double gaussian(int x, int mu, double sigma) {
	return Math.exp( -(Math.pow((x-mu)/sigma,2.0))/2.0 );
    }

    static double[][] gaussianFilter(int radius, double sigma) {
	int length = 2 * radius + 1;
	double[] hkernel = new double[length];
	for(int i=0; i < length; i++)
	    hkernel[i] = gaussian(i, radius, sigma);
	double[][] kernel2d = new double[length][length];
	double kernelsum = 0.0;
	for(int i=0; i < length; i++) {
	    for(int j=0; j < length; j++) {
		double elem = hkernel[i] * hkernel[j];
		kernelsum += elem;
		kernel2d[i][j] = elem;
	    }
	}
	for(int i=0; i < length; i++) {
	    for(int j=0; j < length; j++)
		kernel2d[i][j] /= kernelsum;
	}
	return kernel2d;
    }
}


class MirrorTask extends RecursiveAction {
    protected int low, high;
    protected int width;
    protected RGB[] input;
    protected RGB[] output;

    protected static final int SEQUENTIAL_CUTOFF = 10000;
	
    MirrorTask(int low, int high, int width, RGB[] input, RGB[] output) {
	this.low = low;
	this.high = high;
	this.width = width;
	this.input = input;
	this.output = output;
    }

    protected void compute() {
	if((high - low) <= SEQUENTIAL_CUTOFF) {
	    for (int i = low; i < high; i++) {
		int col = i % width;
		int mirCol = width - col - 1;
		int pos = (i / width) * width + mirCol;
		output[pos] = new RGB(input[i].R,
				      input[i].G,
				      input[i].B);
	    }
	    return;
	}
	int mid = (high - low) / 2 + low;
	MirrorTask left = new MirrorTask(low, mid, width, input, output);
	MirrorTask right = new MirrorTask(mid, high, width, input, output);
	left.fork();
	right.compute();
	left.join();
    }
    
}

class FilterTask extends RecursiveAction {
    protected int low, high;
    protected int width, height;
    protected double[][] filter;
    protected RGB[] input;
    protected RGB[] output;

    protected static final int SEQUENTIAL_CUTOFF = 10000;
	
    FilterTask(int low, int high, int width, int height,
	       double[][] filter, RGB[] input, RGB[] output) {
	this.low = low;
	this.high = high;
	this.width = width;
	this.height = height;
	this.filter = filter;
	this.input = input;
	this.output = output;
    }

    protected void compute() {
	int len = filter.length;
	int radius = len / 2;
	if((high - low) <= SEQUENTIAL_CUTOFF) {
	    for (int i = low; i < high; i++) {
		int row = i / width;
		int col = i % width;
		double blurredR = 0.0;
		double blurredG = 0.0;
		double blurredB = 0.0;
		for (int dr = -radius; dr <= radius; dr++) {
		    for (int dc = -radius; dc <= radius; dc++) {
			double f = filter[dr+radius][dc+radius];
			int irow = Math.min(Math.max(row + dr, 0), height - 1);
			int icol = Math.min(Math.max(col + dc, 0), width - 1);
			RGB curr = input[irow * width + icol];
			blurredR += curr.R * f;
			blurredG += curr.G * f;
			blurredB += curr.B * f;
		    }
		}
		output[i] = new RGB((int) Math.round(blurredR), (int) Math.round(blurredG), (int) Math.round(blurredB));
	    }
	    return;
	}
	int mid = (high - low) / 2 + low;
	FilterTask left = new FilterTask(low, mid, width, height, filter, input, output);
	FilterTask right = new FilterTask(mid, high, width, height, filter, input, output);
	left.fork();
	right.compute();
	left.join();
    }
    
}

class PPMImage {
    protected int width, height, maxColorVal;
    protected RGB[] pixels;

    PPMImage(int w, int h, int m, RGB[] p) {
	width = w;
	height = h;
	maxColorVal = m;
	pixels = p;
    }

    public static PPMImage fromFile(String fname) throws FileNotFoundException, IOException {
	FileInputStream is = new FileInputStream(fname);
	BufferedReader br = new BufferedReader(new InputStreamReader(is));
	br.readLine(); // read the P6
	String[] dims = br.readLine().split(" "); // read width and height
	int width = Integer.parseInt(dims[0]);
	int height = Integer.parseInt(dims[1]);
	int max = Integer.parseInt(br.readLine()); // read max color value
	br.close();

	is = new FileInputStream(fname);
	    // skip the first three lines
	int newlines = 0;
	while (newlines < 3) {
	    int b = is.read();
	    if (b == 10)
		newlines++;
	}

	int MASK = 0xff;
	int numpixels = width * height;
	byte[] bytes = new byte[numpixels * 3];
        is.read(bytes);
	RGB[] pixels = new RGB[numpixels];
	for (int i = 0; i < numpixels; i++) {
	    int offset = i * 3;
	    pixels[i] = new RGB(bytes[offset] & MASK, bytes[offset+1] & MASK, bytes[offset+2] & MASK);
	}
	is.close();
	return new PPMImage(width, height, max, pixels);
    }

    public void toFile(String fname) throws IOException {
	FileOutputStream os = new FileOutputStream(fname);

	String header = "P6\n" + width + " " + height + "\n" + maxColorVal + "\n";
	os.write(header.getBytes());

	int numpixels = width * height;
	byte[] bytes = new byte[numpixels * 3];
	int i = 0;
	for (RGB rgb : pixels) {
	    bytes[i] = (byte) rgb.R;
	    bytes[i+1] = (byte) rgb.G;
	    bytes[i+2] = (byte) rgb.B;
	    i += 3;
	}
	os.write(bytes);
	os.close();
    }

    public PPMImage negate() {
	RGB[] negPixels = 
	    Arrays.stream(pixels)
	    .parallel()
	    .map(rgb -> new RGB(maxColorVal-rgb.R,
				maxColorVal-rgb.G,
				maxColorVal-rgb.B))
	    .toArray(RGB[]::new);
	    
	return new PPMImage(width, height, maxColorVal, negPixels);
    }

    public PPMImage mirrorImage() {
	RGB[] output = new RGB[pixels.length];
	MirrorTask t = new MirrorTask(0, pixels.length, width, pixels, output);
	t.compute();
	return new PPMImage(width, height, maxColorVal, output);
    }

    public PPMImage gaussianBlur(int radius, double sigma) {
	RGB[] output = new RGB[pixels.length];
	for (int i = 0; i < pixels.length; i++)
	    output[i] = pixels[i];
	double[][] filter = Gaussian.gaussianFilter(radius, sigma);
	FilterTask t = new FilterTask(0, pixels.length, width, height, filter, pixels, output);
	t.compute();
	return new PPMImage(width, height, maxColorVal, output);
    }

    public PPMImage gaussianBlur2(int radius, double sigma) {
	double[][] filter = Gaussian.gaussianFilter(radius, sigma);
	RGB[] output = new RGB[pixels.length];
	int width = this.width;
	RGB[] pixels = this.pixels;
	IntStream.range(0, pixels.length).
	    parallel().
	    forEach(i ->
		    {
			int row = i / width;
			int col = i % width;
			double blurredR = 0.0;
			double blurredG = 0.0;
			double blurredB = 0.0;
			for (int dr = -radius; dr <= radius; dr++) {
			    for (int dc = -radius; dc <= radius; dc++) {
				double f = filter[dr+radius][dc+radius];
				int irow = Math.min(Math.max(row + dr, 0), height - 1);
				int icol = Math.min(Math.max(col + dc, 0), width - 1);
				RGB curr = pixels[irow * width + icol];
				blurredR += curr.R * f;
				blurredG += curr.G * f;
				blurredB += curr.B * f;
			    }
			}
			output[i] = new RGB((int) Math.round(blurredR),
					    (int) Math.round(blurredG),
					    (int) Math.round(blurredB));
		});
	return new PPMImage(width, height, maxColorVal, output);
    }


}
