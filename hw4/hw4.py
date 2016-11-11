
# Name: Patrick Tan
#
# UID: 204158646
#
# People I interacted with:
#
# Resources I used: Piazza Q & A (CS 131)
#


import math
import struct

# PROBLEM 1

# parse the file named fname into a dictionary of the form 
# {'width': int, 'height' : int, 'max' : int, 'pixels' : (int * int * int) list}
def parsePPM(fname):
    parsedPPM = {}
    file = open(fname, 'r')

    P6line = file.readline()
    dimLine = file.readline()
    dimensions = dimLine.split()
    maxLine = file.readline()

    pixels = file.read() #Read rest of file

    parsedPPM['width'] = int(dimensions[0])
    parsedPPM['height'] = int(dimensions[1])
    parsedPPM['max'] = int(maxLine)

    numPixels = len(pixels)
    intPixels = struct.unpack('%dB' % numPixels, pixels)

    counter = 0
    pixelArr = []

    for i in range(0, numPixels/3):
        pixelArr.append((intPixels[i*3], intPixels[i*3 + 1], intPixels[i*3 + 2]))

    parsedPPM['pixels'] = pixelArr

    return parsedPPM

# a test to make sure you have the right format for your dictionaries
def testParsePPM():
    return parsePPM("example.ppm") == {'width': 2, 'max': 255, 'pixels': [(10, 23, 52), (82, 3, 215), (30, 181, 101), (33, 45, 205), (40, 68, 92), (111, 76, 1)], 'height': 3}

# write the given ppm dictionary as a PPM image file named fname
# the function should not return anything
def unparsePPM(ppm, fname):
    toWrite = 'P6\n%d %d\n%d\n' % (ppm['width'], ppm['height'], ppm['max'])

    for tup in ppm['pixels']:
        for i in tup:
            toWrite += struct.pack('B', i)

    file = open(fname, 'w')
    file.write(toWrite)


# PROBLEM 2
def negate(ppm):
    pixels = ppm['pixels']
    maxValue = ppm['max']
    negated = [(maxValue - v[0], maxValue - v[1], maxValue - v[2]) for v in pixels]
    negatedPPM = {}
    negatedPPM['height'] = ppm['height']
    negatedPPM['width'] = ppm['width']
    negatedPPM['max'] = ppm['max']
    negatedPPM['pixels'] = negated
    return negatedPPM


# PROBLEM 3
def mirrorImage(ppm):
    pixels = ppm['pixels']
    width = ppm['width']
    height = ppm['height']
    mirroredArr = []
    curArr = []

    #take each row one at a time and reverse it, then add it to mirroredArr, which holds the final set of pixels
    for i in range (0, height):
        curArr = pixels[i * width : (i + 1) * width]
        curArr.reverse()
        mirroredArr.extend(curArr)

    mirroredPPM = {}
    mirroredPPM['height'] = ppm['height']
    mirroredPPM['width'] = ppm['width']
    mirroredPPM['max'] = ppm['max']
    mirroredPPM['pixels'] = mirroredArr
    return mirroredPPM



# PROBLEM 4

# produce a greyscale version of the given ppm dictionary.
# the resulting dictionary should have the same format, 
# except it will only have a single value for each pixel, 
# rather than an RGB triple.
def greyscale(ppm):
    pixels = ppm['pixels']
    greyed = [round(.299 * v[0] + .587 * v[1] + .114 * v[2]) for v in pixels]
    greyedPPM = {}
    greyedPPM['height'] = ppm['height']
    greyedPPM['width'] = ppm['width']
    greyedPPM['max'] = ppm['max']
    greyedPPM['pixels'] = greyed
    return greyedPPM

# take a dictionary produced by the greyscale function and write it as a PGM image file named fname
# the function should not return anything
def unparsePGM(pgm, fname):
    toWrite = 'P5\n%d %d\n%d\n' % (pgm['width'], pgm['height'], pgm['max'])

    for i in pgm['pixels']:
        toWrite += struct.pack('B', i)

    file = open(fname, 'w')
    file.write(toWrite)


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
    arr = []
    curArr = []

    #Creates a two dimensional array of our pixels
    #hint: extend the boundary of our 2-d array instead of checking for boundary later

    #for loops inside are for the front and back of each row (first and last tuple in each row copied 'radius' times)
    # the first and last top level for loops are for the top and bottom rows copied 'radius' times

    for i in range (0, radius):
        for l in range (0, radius):
            curArr.append(pixels[0])
        curArr.extend(pixels[0 : width])
        for l in range (0, radius):
            curArr.append(pixels[width - 1])
        arr.append(curArr)
        curArr = []

    for i in range (0, height):
        for l in range (0, radius):
            curArr.append(pixels[i * width])
        curArr.extend(pixels[i * width : (i + 1) * width])
        for l in range (0, radius):
            curArr.append(pixels[((i + 1) * width) - 1])
        arr.append(curArr)
        curArr = []

    for i in range (0, radius):
        for l in range (0, radius):
            curArr.append(pixels[(height - 1) * width])
        curArr.extend(pixels[(height - 1) * width : height * width])
        for l in range (0, radius):
            curArr.append(pixels[(height * width) - 1])
        arr.append(curArr)
        curArr = []

    blurR = 0
    blurG = 0
    blurB = 0
    blurredArr = []
    # goes through all of our original tuples (pixels)
    for curRow in range (radius, height + radius):
        for curCol in range (radius, width + radius):
            #calculate blur in this loop
            for i in range (radius * -1, radius + 1):
                for j in range (radius * -1, radius + 1):
                    #note: the center of the gfilter is always at gfilter[radius][radius]
                    blurR += arr[curRow + i][curCol + i][0] * gfilter[radius + i][radius + j]
                    blurG += arr[curRow + i][curCol + i][1] * gfilter[radius + i][radius + j]
                    blurB += arr[curRow + i][curCol + i][2] * gfilter[radius + i][radius + j]
            blurredArr.append((blurR, blurG, blurB))
            blurR = 0
            blurG = 0
            blurB = 0

    blurredPPM = {}
    blurredPPM['height'] = ppm['height']
    blurredPPM['width'] = ppm['width']
    blurredPPM['max'] = ppm['max']
    blurredPPM['pixels'] = blurredArr
    return blurredPPM
