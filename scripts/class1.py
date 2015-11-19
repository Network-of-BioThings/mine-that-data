#!/usr/bin/python
import sys, getopt

def main(argv):
	inputfile = ''
	outputfile = ''
	try:
		opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
	except getopt.GetoptError:
			print ('test.py -i <inputfile> -o <outputfile>')
			sys.exit(2)
	for opt, arg in opts:
			if opt == '-h':
				print ('test.py -i <inputfile> -o <outputfile>')
				sys.exit()
			elif opt in ("-i", "--ifile"):
				inputfile = arg
			elif opt in ("-o", "--ofile"):
				outputfile = arg
	print ('Input file is ', inputfile)
	print ('Output file is ', outputfile)

	with open('output.txt','w') as f:
		print('a,b,c', file=f)
		print('1,2,3', file=f)

main(sys.argv[1:])