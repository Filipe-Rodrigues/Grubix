import javax.swing.JOptionPane;

import br.ufla.dcc.grubix.simulator.kernel.Simulator;


public class Main {

	public static void main(String[] args) {
		//JOptionPane.showMessageDialog(null, "STARTING");
		try {
			long startTime = System.currentTimeMillis();
			//String path = "application_simples.xml";
			//String path = "application_backbone1.xml";
			String path = "application_backbone2.xml";
			//String path = args[0];
			args = new String[1];
			args[0] = path;
			Simulator.main(args);
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			
			//System.out.println("\nTempo de execução do processador: " + (totalTime/1000.0) + "s");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
