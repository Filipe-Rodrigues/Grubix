import br.ufla.dcc.grubix.simulator.kernel.Simulator;


public class Main {

	public static void main(String[] args) {
		//JOptionPane.showMessageDialog(null, "STARTING");
		try {
			long startTime = System.currentTimeMillis();
			//String path = "application_exmac_gerador.xml";
			//String path = "application_exmac_teste.xml";
			//String path = "application_exmac_teste_georouting.xml";
			//String path = "application_backbone1.xml";
			//String path = "application_backbone2.xml";
			//String path = args[0];
			//args = new String[1];
			//args[0] = path;
			
			
			//Simulator.main(new String[]{"application_usamac_gerador.xml", "true", "false"});
			
			
			Simulator.main(new String[]{"application_usamac_teste.xml", "false", "true"});
			//Simulator.main(new String[]{"application_usamac_teste_georouting.xml", "false", "true"});
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			
			//System.out.println("\nTempo de execução do processador: " + (totalTime/1000.0) + "s");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
