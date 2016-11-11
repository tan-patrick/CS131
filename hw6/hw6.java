/* Name: Patrick Tan

   UID: 204158646

   Others With Whom I Discussed Things:

   Other Resources I Consulted: CS 131 Piazza
   
*/

import java.io.*;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.*;
import java.util.Arrays;

// a marker for code that you need to implement
class ImplementMe extends RuntimeException {}

// an RGB triple
class RGB {
    public int R, G, B;

    RGB(int r, int g, int b) {
	R = r;
	G = g;
	B = b;
    }

    public String toString() { return "(" + R + "," + G + "," + B + ")"; }

}

// code for creating a Gaussian filter
class Gaussian {

    protected static double gaussian(int x, int mu, double sigma) {
	return Math.exp( -(Math.pow((x-mu)/sigma,2.0))/2.0 );
    }

    public static double[][] gaussianFilter(int radius, double sigma) {
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

// an object representing a single PPM image
class PPMImage {
    protected int width, height, maxColorVal;
    protected RGB[] pixels;

    PPMImage(int w, int h, int m, RGB[] p) {
	width = w;
	height = h;
	maxColorVal = m;
	pixels = p;
    }

	// parse a PPM file to produce a PPMImage
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

	return new PPMImage(width, height, max, pixels);
    }

	// write a PPMImage object to a file
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
    }

	// implement using Java 8 Streams
    public PPMImage negate() {
    	RGB[] negated = new RGB[pixels.length];

		negated = Arrays.stream(pixels).parallel()
			.map(p -> new RGB(maxColorVal - p.R, maxColorVal - p.G, maxColorVal - p.B))
			.toArray(RGB[]::new); //use to array to turn RGB values back into array
		    	
		return new PPMImage(width, height, maxColorVal, negated);
    }
    
	// implement using Java's Fork/Join library
    public PPMImage mirrorImage() {
    	RGB[] mirrored = new RGB[pixels.length];

		Mirror t = new Mirror(width, height, 0, pixels.length, pixels, mirrored);
		t.compute();
		return new PPMImage(width, height, maxColorVal, mirrored);
    }

	// implement using Java's Fork/Join library
    public PPMImage gaussianBlur(int radius, double sigma) {
    	RGB[] blurred = new RGB[pixels.length];
		double[][] gaussianF = Gaussian.gaussianFilter(radius, sigma);

		GaussianRec t = new GaussianRec(width, height, 0, pixels.length, pixels, blurred, gaussianF, radius);
		t.compute();
		return new PPMImage(width, height, maxColorVal, blurred);
	    }

	// implement using Java 8 Streams
    public PPMImage gaussianBlur2(int radius, double sigma) {
    	RGB[] blurred = new RGB[pixels.length];
		double[][] gaussianF = Gaussian.gaussianFilter(radius, sigma);

		//use IntStream.range to iterate over all values in pixels.length
		//use mapToObj and use the gaussian blur filter on the given pixel
		//turn back into array using toArray

		blurred = IntStream.range(0, pixels.length).parallel()
			.mapToObj(i -> helperBlur2(i, pixels, radius, gaussianF))
			.toArray(RGB[]::new); //use to array to turn RGB values back into array

		return new PPMImage(width, height, maxColorVal, blurred);
		// blurred = Arrays.stream(pixels).parallel()
    }

    public RGB helperBlur2(int i, RGB[] pixels, int radius, double[][] gaussianF){
		int negRad = radius * -1;
    	int row = i / width;
		int col = i % width;
		double curR = 0;
		double curG = 0;
		double curB = 0;
		int horizontalRad = 0;
		int verticalRad = 0;

		for(horizontalRad = negRad; horizontalRad <= radius; horizontalRad++){
			for(verticalRad = negRad; verticalRad <= radius; verticalRad++){ //to calculate gaussian blur, find the blur added for each pixel within the given radius and add to curR/B/G
				//note: from part 4: if we go past the edges, we pick the furthest possible location
				//if less than 0, set to 0. if greater than max, set to max. otherwise leave as is
				int r = 0;
				int c = 0;
				if(row + horizontalRad < 0)
					r = 0;
				else if(row + horizontalRad > height - 1)
					r = height - 1;
				else
					r = row + horizontalRad;

				if(col + verticalRad < 0)
					c = 0;
				else if(col + verticalRad > width - 1)
					c = width - 1;
				else
					c = col + verticalRad;

				RGB curLoc = pixels[width * r + c]; //set loc to the current location in old array
				double blurBy = gaussianF[radius + horizontalRad][radius + verticalRad];

				curR += curLoc.R * blurBy;
				curG += curLoc.G * blurBy;
				curB += curLoc.B * blurBy;
			}
		}
		return new RGB((int) Math.round(curR), (int) Math.round(curG), (int) Math.round(curB)); //change doubles into ints and put into blurred array
    }
}

class Mirror extends RecursiveAction {
    protected int width;
    protected int height;
    protected int low;
    protected int hi;
    protected RGB[] oldRGB;
    protected RGB[] newRGB;

    // Can put old rgb values into the new array of values, allowing us to set any cutoff, rather than the cutoff being a single row

    protected static final int SEQUENTIAL_CUTOFF = 10000;

    Mirror(int width, int height, int low, int hi, RGB[] oldRGB, RGB[] newRGB) {
		this.width = width;
		this.height = height;
		this.low = low;
		this.hi = hi;
		this.oldRGB = oldRGB;
		this.newRGB = newRGB;
    }
    
    protected void compute() {
	if (hi - low < SEQUENTIAL_CUTOFF) {
	    for (int i = low; i < hi; i++) { //for each pixel, find it's current row/column, then place it in mirrored spot in the new rgb array
	    	int row = i / width;
			int col = i % width;
			int newCol = width - col - 1; //need to subtract 1 for offset, out of bounds otherwise
			int loc = row * width + newCol;
			newRGB[loc] = new RGB(oldRGB[i].R, oldRGB[i].G, oldRGB[i].B);
	    }
	    return;
	}

	int mid = (low + hi)/2;
	Mirror t1 = new Mirror(width, height, low, mid, oldRGB, newRGB);
	Mirror t2 = new Mirror(width, height, mid, hi, oldRGB, newRGB);

	//run t1 on another thread
	t1.fork();
	t2.compute();
	//after t1 finishes
	t1.join();
    }

}

class GaussianRec extends RecursiveAction {
    protected int width;
    protected int height;
    protected int low;
    protected int hi;
    protected RGB[] oldRGB;
    protected RGB[] newRGB;
    protected double[][] gaussianF;
    protected int radius;

    protected static final int SEQUENTIAL_CUTOFF = 1000; //1000 seems to be the optimal cutoff, at least for tests with 5,2 and 20,4

    GaussianRec(int width, int height, int low, int hi, RGB[] oldRGB, RGB[] newRGB, double[][] gaussianF, int radius) {
		this.width = width;
		this.height = height;
		this.low = low;
		this.hi = hi;
		this.oldRGB = oldRGB;
		this.newRGB = newRGB;
		this.gaussianF = gaussianF;
		this.radius = radius;
    }
    
    protected void compute() {
	if (hi - low < SEQUENTIAL_CUTOFF) {
		int negRad = radius * -1;
	    for (int i = low; i < hi; i++) { //for each pixel, calculate the gaussian blur using gaussianF and oldRGB, and input result into newRGB
	    	int row = i / width;
			int col = i % width;
			double curR = 0;
			double curG = 0;
			double curB = 0;
			int horizontalRad = 0;
			int verticalRad = 0;
			for(horizontalRad = negRad; horizontalRad <= radius; horizontalRad++){
				for(verticalRad = negRad; verticalRad <= radius; verticalRad++){ //to calculate gaussian blur, find the blur added for each pixel within the given radius and add to curR/B/G
					//note: from part 4: if we go past the edges, we pick the furthest possible location
					//if less than 0, set to 0. if greater than max, set to max. otherwise leave as is
					int r = 0;
					int c = 0;
					if(row + horizontalRad < 0)
						r = 0;
					else if(row + horizontalRad > height - 1)
						r = height - 1;
					else
						r = row + horizontalRad;

					if(col + verticalRad < 0)
						c = 0;
					else if(col + verticalRad > width - 1)
						c = width - 1;
					else
						c = col + verticalRad;

					RGB curLoc = oldRGB[width * r + c]; //set loc to the current location in old array
					double blurBy = gaussianF[radius + horizontalRad][radius + verticalRad];

					//calculate blur
					curR += curLoc.R * blurBy;
					curG += curLoc.G * blurBy;
					curB += curLoc.B * blurBy;
				}
			}
			newRGB[i] = new RGB((int) Math.round(curR), (int) Math.round(curG), (int) Math.round(curB)); //change doubles into ints and put into blurred array
			//Note: https://piazza.com/class/i10rs0q03qt7lj?cid=243 says it is okay to use (int) in this case [also https://piazza.com/class/i10rs0q03qt7lj?cid=244]
	    }
	    return;
	}

	int mid = (low + hi)/2;
	GaussianRec t1 = new GaussianRec(width, height, low, mid, oldRGB, newRGB, gaussianF, radius);
	GaussianRec t2 = new GaussianRec(width, height, mid, hi, oldRGB, newRGB, gaussianF, radius);

	//run t1 on another thread
	t1.fork();
	t2.compute();
	//after t1 finishes
	t1.join();
    }

}

//Tests
// class TestBasic {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("example.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		try{
// 			test.toFile("copy.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }

// class TestNegated {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("example.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		PPMImage negated = test.negate();
		
// 		try{
// 			negated.toFile("negated.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }

// class TestMirrored {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("example.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		PPMImage mirrored = test.mirrorImage();
		
// 		try{
// 			mirrored.toFile("mirrored.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }

// class TestGaussian {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("florence.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		PPMImage mirrored = test.gaussianBlur(20,4);

		
// 		try{
// 			mirrored.toFile("gaussian.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }

// class TestGaussian2 {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("florence.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		PPMImage mirrored = test.gaussianBlur2(20,4);

		
// 		try{
// 			mirrored.toFile("gaussian2.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }

// class TestAll {

//     public static void main(String[] args) {
// 		PPMImage ppm = new PPMImage(0,0,0, new RGB[0]);
// 		PPMImage test;
// 		try{
// 			test = ppm.fromFile("florence.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}

// 		PPMImage negated = test.negate();
// 		PPMImage mirrored = test.mirrorImage();
// 		PPMImage blur1 = test.gaussianBlur(5,2);
// 		PPMImage blur2 = test.gaussianBlur2(5,2);
		
// 		try{
// 			test.toFile("1copy.ppm");
// 			negated.toFile("1negate.ppm");
// 			mirrored.toFile("1mirror.ppm");
// 			blur1.toFile("1blur1.ppm");
// 			blur2.toFile("1blur2.ppm");
// 		}
// 		catch(Exception e){
// 			return;
// 		}		
//     }
// }
