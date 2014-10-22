import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileCreator {
	
	public static void main(String[] args) throws IOException {
		int[] values = null;
		boolean random = false;
		boolean zero = false;
		int rep = 1;
		String prefix = "SinCos";
		String postfix = ".csv";
		String randfix = "Rand";
		String zerofix = "Zero";
		String repfix = "";
		for(int i = 0; i < args.length; i += 2){
			switch(args[i]){
			case "-n":
				String[] input = args[i+1].split(",");
				values = new int[input.length];
				for(int j = 0; j < input.length; j++){
					input[j] = input[j].trim();
					values[j] = Integer.parseInt(input[j]);
				}
				break;
			case "-prefix":
				prefix = args[i+1].trim();
				break;
			case "-postfix":
				postfix = args[i+1].trim();
				break;
			case "-randfix":
				randfix = args[i+1].trim();
				break;
			case "-zerofix":
				zerofix = args[i+1].trim();
				break;
			case "-r":
				i = i-1;
				random = true;
				break;
			case "-z":
				i = i-1;
				zero = true;
				break;
			case "-rep":
				rep = Integer.parseInt(args[i+1]);
				repfix = "_"+rep+"_";
				break;
			default:
				System.out.println("Did not recognise the command \""+args[i].trim()+"\".");
			}
		}
		
		if(values == null){
			System.out.print("No values specified! Use the -n flag with numbers required seperated by ,.");
			return;
		}
				
		for(int j = 0; j < values.length; j++){
			createSinCosDataSetFile(values[j], prefix+repfix+values[j]+postfix, rep);
		}
		
		if(random){
			for(int j = 0; j < values.length; j++){
				createExtraRandomDataFile(1, prefix+repfix+values[j]+postfix, prefix+repfix+randfix+values[j]+postfix);
			}
		}
		
		if(zero){
			for(int j = 0; j < values.length; j++){
				createExtraZeroDataFile(1, prefix+repfix+values[j]+postfix, prefix+repfix+zerofix+values[j]+postfix);
			}
		}
		
	}
	
	/**
	 * Takes a file of comma separated data and add a random column. Then saves this file.
	 * @param beforeColumn The random data will be inserted before this column.
	 * @param prefix The string prefix for both random and non random data.
	 * @param randomfix The random part of the string. Goes after prefix.
	 * @param postfix The end of the file name for both random and non random data.
	 */
	public static void createExtraRandomDataFile(int beforeColumn, String in, String out){
		try {
			BufferedReader nonRandomReader = new BufferedReader(new FileReader(in));
			BufferedWriter randomWriter = new BufferedWriter(new FileWriter(out));
			
			String inputLine = null;
			while ((inputLine = nonRandomReader.readLine()) != null) {
				int splitIndex = 0;
				for(int i = 0; i < beforeColumn; i++){
					splitIndex = inputLine.indexOf(",", splitIndex+1);
				}
				String output = inputLine.substring(0, splitIndex+1) + (Math.random()*2-1) + inputLine.substring(splitIndex, inputLine.length());
				randomWriter.write(output+"\n");
			}
			
			nonRandomReader.close();
			randomWriter.close();
		} catch (IOException e) {
			System.out.println("Error generating dataset file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a file of comma separated data and add a random column. Then saves this file.
	 * @param beforeColumn The random data will be inserted before this column.
	 * @param prefix The string prefix for both random and non random data.
	 * @param randomfix The random part of the string. Goes after prefix.
	 * @param postfix The end of the file name for both random and non random data.
	 */
	public static void createExtraZeroDataFile(int beforeColumn, String in, String out){
		try {
			BufferedReader nonRandomReader = new BufferedReader(new FileReader(in));
			BufferedWriter randomWriter = new BufferedWriter(new FileWriter(out));
			
			String inputLine = null;
			while ((inputLine = nonRandomReader.readLine()) != null) {
				int splitIndex = 0;
				for(int i = 0; i < beforeColumn; i++){
					splitIndex = inputLine.indexOf(",", splitIndex+1);
				}
				String output = inputLine.substring(0, splitIndex+1) + "0" + inputLine.substring(splitIndex, inputLine.length());
				randomWriter.write(output+"\n");
			}
			
			nonRandomReader.close();
			randomWriter.close();
		} catch (IOException e) {
			System.out.println("Error generating dataset file.");
			e.printStackTrace();
		}
	}
	
	public static void createSinCosDataSetFile(int size, String file){
		createSinCosDataSetFile(size, file, 1);
	}
	
	/**
	 * Creates a file with sin(x)cos(2x) and data between 0 and 2pi in the format
	 *  x,sin(x)cos(2x)
	 * @param size the number of steps between 0 and 2pi
	 * @param file The name of the file the data will be stored in.
	 */
	public static void createSinCosDataSetFile(int size, String file, int rep){
		try {
			BufferedWriter rw = new BufferedWriter(new FileWriter(file));

			double x = 0;
			double step = (2*Math.PI)/size;
			for(int i = 0 ; i < size; i++){
				String toWrite = x+","+(Math.sin(x)*Math.cos(2*x))+"\n";
				for(int j = 0; j < rep; j++){
					rw.write(toWrite);
				}
				x += step;
			}
			rw.close();

		} catch (IOException e) {
			System.out.println("Error generating dataset file.");
			e.printStackTrace();
		}
	}

}
