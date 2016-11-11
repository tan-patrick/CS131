import subprocess
import hw4

def shell(cmd):
	subprocess.call(cmd, shell=True)

flr = hw4.parsePPM('florence.ppm')
print "Parsed florence.ppm successfully."

flr_negate = hw4.negate(flr)
print "Negated."
flr_mirror = hw4.mirrorImage(flr)
print "Mirrored."
flr_greyscale = hw4.greyscale(flr)
print "Greyscaled."
flr_gaussian = hw4.gaussianBlur(flr, 1, 2.0)
print "GaussianBlurred."

hw4.unparsePPM(flr_negate, 'tst-flr_negate.ppm')
print "Unparsed -> negate."
hw4.unparsePPM(flr_mirror, 'tst-flr_mirror.ppm')
print "Unparsed -> mirrorImage."
hw4.unparsePGM(flr_greyscale, 'tst-flr_greyscale.pgm')
print "Unparsed -> greyscale."
hw4.unparsePPM(flr_gaussian, 'tst-flr_gaussian.ppm')
print "Unparsed -> gaussianBlur."

shell('ppmtojpeg tst-flr_negate.ppm > o-tst-flr_negate.jpg')
shell('ppmtojpeg tst-flr_mirror.ppm > o-tst-flr_mirror.jpg')
shell('ppmtojpeg tst-flr_greyscale.pgm > o-tst-flr_greyscale.jpg')
shell('ppmtojpeg tst-flr_gaussian.ppm > o-tst-flr_gaussian.jpg')
print "Converted all PPMs to JPGs"