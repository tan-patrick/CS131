import math
import struct

# PROBLEM 1

# parse the file named fname into a dictionary of the form 
# {'width': int, 'height' : int, 'max' : int, 'pixels' : (int * int * int) list}
def parsePPM(fname):
    ppm = {}
    f = open(fname, "rb")
    p6 = f.readline()
    dimensions = f.readline().split()
    ppm['width'] = int(dimensions[0])
    ppm['height'] = int(dimensions[1])
    ppm['max'] = int(f.readline())

    pixels = []
    rgbB = f.read(3)
    while rgbB != "":
        # we're assuming that the max val is less than 256
        # hence each pixel value fits in a single byte
        # for greater than 256, 2 bytes are used
        pixels.append(struct.unpack('BBB', rgbB))
        rgbB = f.read(3)
    ppm['pixels'] = pixels
    f.close()
    return ppm

# a test to make sure you have the right format for your dictionaries
def testParsePPM():
    return parsePPM("example.ppm") == {'width': 2, 'max': 255, 'pixels': [(10, 23, 52), (82, 3, 215), (30, 181, 101), (33, 45, 205), (40, 68, 92), (111, 76, 1)], 'height': 3}

# write the given ppm dictionary as a PPM image file named fname
# the function should not return anything
def unparsePPM(ppm, fname):
    f = open(fname, "wb")
    f.write("P6\n")
    f.write(str(ppm['width']) + " " + str(ppm['height']) + "\n")
    f.write(str(ppm['max']) + "\n")
    pixels = [struct.pack('BBB', p[0], p[1], p[2]) for p in ppm['pixels']]
    f.writelines(pixels)
    f.close()


# PROBLEM 2
def negate(ppm): 
    negppm = ppm.copy()
    max = negppm['max']
    negppm['pixels'] = [(max - rgb[0], max - rgb[1], max - rgb[2]) for rgb in ppm['pixels']]
    return negppm


# PROBLEM 3
def mirrorImage(ppm):
    mirror = ppm.copy()
    width = ppm['width']
    numPixels = width * ppm['height']
    rows = [ppm['pixels'][i:i+width] for i in range(0, numPixels, width)]
    rowsRev = list(rows)
    map(lambda r : r.reverse(), rowsRev)
    pixels = [e for l in rowsRev for e in l]
    mirror['pixels'] = pixels
    return mirror


# PROBLEM 4

# produce a greyscale version of the given ppm dictionary.
# the resulting dictionary should have the same format, 
# except it will only have a single value for each pixel, 
# rather than an RGB triple.
def greyscale(ppm):
    pgm = ppm.copy()
    # map over the ppm pixels to create the pgm pixels!
    pgm['pixels'] = [int(round(.299 * rgb[0] + .587 * rgb[1] + .114 * rgb[2])) for rgb in ppm['pixels']]
    return pgm


# take a dictionary produced by the greyscale function and write it as a PGM image file named fname
# the function should not return anything
def unparsePGM(pgm, fname):
    f = open(fname, "wb")
    f.write("P5\n")
    f.write(str(pgm['width']) + " " + str(pgm['height']) + "\n")
    f.write(str(pgm['max']) + "\n")
    pixels = [struct.pack('B', p) for p in pgm['pixels']]
    f.writelines(pixels)
    f.close()


# PROBLEM 5

# gaussian blur code adapted from:
# http://stackoverflow.com/questions/8204645/implementing-gaussian-blur-how-to-calculate-convolution-matrix-kernel
def gaussian(x, mu, sigma):
  return math.exp( -(((x-mu)/(sigma))**2)/2.0 )

def gaussianFilter(radius, sigma):
    # compute the actual kernel elements
    hkernel = [gaussian(x, radius, sigma) for x in range(2*radius+1)]
    vkernel = [x for x in hkernel]
    kernel2d = [[xh*xv for xh in hkernel] for xv in vkernel]

    # normalize the kernel elements
    kernelsum = sum([sum(row) for row in kernel2d])
    kernel2d = [[x/kernelsum for x in row] for row in kernel2d]
    return kernel2d

# blur a given ppm dictionary, returning a new dictionary  
# the blurring uses a gaussian filter produced by the above function
def gaussianBlur(ppm, radius, sigma):
    # obtain the filter
    gfilter = gaussianFilter(radius, sigma)
    pixels = ppm['pixels']
    width = ppm['width']
    height = ppm['height']
    blurred = list(pixels)
    for i in range(height):
        for j in range(width):
            blurredR = 0.0
            blurredG = 0.0
            blurredB = 0.0
            for di in range(-radius, radius+1):
                newi = min(max(i+di,0),height-1);
                for dj in range(-radius, radius+1):
                    newj = min(max(j+dj,0),width-1);
                    blurredR += pixels[newi*width+newj][0] * gfilter[di+radius][dj+radius]
                    blurredG += pixels[newi*width+newj][1] * gfilter[di+radius][dj+radius]
                    blurredB += pixels[newi*width+newj][2] * gfilter[di+radius][dj+radius]
            blurred[i*width+j] = (int(round(blurredR)), int(round(blurredG)), int(round(blurredB)))
    result = ppm.copy()
    result['pixels'] = blurred
    return result
