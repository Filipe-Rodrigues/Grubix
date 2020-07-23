import br.ufla.dcc.grubix.simulator.kernel.Simulator;

public class Main {

	public static void main(String[] args) {
		//JOptionPane.showMessageDialog(null, "STARTING");
		try {
			//String path = "application_exmac_gerador.xml";
			//String path = "application_exmac_teste.xml";
			//String path = "application_exmac_teste_georouting.xml";
			//String path = "application_backbone1.xml";
			//String path = "application_backbone2.xml";
			//String path = args[0];
			//args = new String[1];
			//args[0] = path;

			
			//Simulator.main(new String[]{"GERADOR_USAMAC.xml", "true", "false", "USAMAC"});
			//Simulator.main(new String[]{"GERADOR_MXMAC.xml", "true", "false", "MXMAC"});
			
			
			//Simulator.main(new String[]{"TESTE_XMAC.xml", "false", "true", "N/A"});
			//Simulator.main(new String[]{"TESTE_EXMAC.xml", "false", "true", "N/A"});
			//Simulator.main(new String[]{"TESTE_USAMAC.xml", "false", "true", "USAMAC"});
			Simulator.main(new String[]{"TESTE_MXMAC.xml", "false", "true", "MXMAC"});
			
			//System.out.println("\nTempo de execução do processador: " + (totalTime/1000.0) + "s");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
