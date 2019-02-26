package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class DifferentialEncodeVideoFile {

	public static void main(String[] args) throws IOException {
		String input_file_name = "data/video/out.dat";
		String output_file_name = "data/video/differential-compressed.dat";

		int range_bit_width = 40;
		
		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
		int[] processed_bytes = new int[num_symbols];
		
		// Pre-process differential values
		FileInputStream fis = new FileInputStream(input_file_name);
		int next_byte = fis.read();
		processed_bytes[0] = next_byte;
		int prev_byte = next_byte;
		for (int i = 1; i<num_symbols; i++) {
			next_byte = fis.read();
			processed_bytes[i] = next_byte - prev_byte;
			prev_byte = next_byte;
		}
		fis.close();
		
		Integer[] symbols = new Integer[512];
		int curr = -256;
		for (int i=0; i<512; i++) {
			symbols[i] = curr;
			curr++;
		}

		// Create new model with default count of 1 for all symbols
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input		
		for (int i=0; i<num_symbols; i++) {
			int next_symbol = processed_bytes[i];
			encoder.encode(next_symbol, model, bit_sink);
						
			// Update model
			model.addToCount(next_symbol+256);
		}
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
