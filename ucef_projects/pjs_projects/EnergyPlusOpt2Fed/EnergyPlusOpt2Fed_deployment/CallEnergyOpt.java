
// package RandomForestModel.TestA;
import java.io.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CallEnergyOpt {
    public static void main(String[] args){

        String s = null;
		String dataString = "";
		boolean	startSaving = false;
		String separator = ",";
		int block =1;
		String sblock = null;

		int numofdays = 7;
		// String data = num1+separator+num2;
		// System.out.println(data);
		// looping for 20 times (24 hr period)	
		while (block <numofdays*24+1){

			try {

				sblock= String.valueOf(block);
				dataString = sblock;
				Process p = Runtime.getRuntime().exec("python ./energyOpt.py " +sblock);

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				System.out.println("Here is the result");
		
				while ((s = stdInput.readLine()) != null) {
					System.out.println(s);
					if (startSaving == true) {
						dataString = dataString + separator + s;
					}		

					if (s .equals("Optimal solution found.")){
						startSaving = true;
					}
					// System.out.println(dataString);
				
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			// System.out.println(dataString);

			String vars[] = dataString.split(separator);

			// Writing data to file
			try{
				// Create new file
				
				String path="/home/vagrant/Desktop/Projects/RCModelWoWGME/RCModelData.txt";
				File file = new File(path);

				// If file doesn't exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				BufferedWriter bw = new BufferedWriter(fw);

				// Write in file
				bw.write(vars[0] + "\t");
				for (int in =1;in<49;in++) {
					if (in<47){
						bw.write(vars[in]+"\t");
					}
					else{
						bw.write(vars[in]+"\n");
					}

				}

				// Close connection
				bw.close();
			}
			catch(Exception e){
				System.out.println(e);
			}

			// Writing data to file
			try{
				// Create new file
				
				String path="/home/vagrant/Desktop/Projects/RCModelWoWGME/RCModelDataSummary.txt";
				File file = new File(path);

				// If file doesn't exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				BufferedWriter bw = new BufferedWriter(fw);

				// Write in file
				for (int in =1;in<13;in++) {
					bw.write(vars[in]+"\n");
				}

				// Close connection
				bw.close();
			}
			catch(Exception e){
				System.out.println(e);
			}

			// resetting 
			startSaving = false;
			dataString = "";
			block = block+1;
		}
    }
}
