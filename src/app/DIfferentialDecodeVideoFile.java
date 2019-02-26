package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class DIfferentialDecodeVideoFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/video/differential-compressed.dat";
		String output_file_name = "data/video/reuncompressed.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[512];
		int curr = -256;
		for (int i=0; i<512; i++) {
			symbols[i] = curr;
			curr++;
		}

		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols);
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		int sym = decoder.decode(model, bit_source);
		fos.write(sym);
		model.addToCount(sym+256);
		int prev = sym;
		for (int i=0; i<num_symbols-1; i++) {
			sym = decoder.decode(model, bit_source);
			fos.write(prev+sym);
			model.addToCount(sym+256);
			prev = prev+sym;
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}
