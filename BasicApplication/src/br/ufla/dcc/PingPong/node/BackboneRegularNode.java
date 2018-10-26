package br.ufla.dcc.PingPong.node;

import br.ufla.dcc.PingPong.Pacote;
import br.ufla.dcc.PingPong.testing.SingletonTestResult;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class BackboneRegularNode extends ApplicationLayer {

	/** Tempo de ciclo em segundos (obrigatório estar aqui) */
	@ShoXParameter(description = " Tempo de Ciclo em segundos")
	private double appStartingCycle;
	
	@Override
	public int getPacketTypeCount() {
		return 1;
	}

	@Override
	public void processEvent(TrafficGeneration tg) {
	}

	@Override
	public void lowerSAP(Packet packet){
		if (packet instanceof Pacote) {		
		    //System.out.println("PACOTE RECEBIDO!!!!!!!!!!!!!!!!");
		    SimulationManager.logNodeState(this.node.getId(), "Chegou pacote", "int", String.valueOf(125));
		    
		    //SingletonTestResult.getInstance().writeResults();
		   
		    SingletonTestResult.getInstance().setEndingTime(SimulationManager.getInstance().getCurrentTime());
			System.out.print(SingletonTestResult.getInstance().getTime() + "\t");
		   
		    //   System.out.println(packet.getDirection());
		    /*
		       Pacote pac = (Pacote)packet;
		       
		       if(pac.getCont() != 621){
		    	   PingPongwuc wuc = new PingPongwuc(sender, 1000);
		    	   wuc.setCont(pac.getCont());
		       	   sendEventSelf(wuc);
		       }
		       */
		}
	}
	
	protected void processEvent(StartSimulation start) {
		if (this.node.getId().asInt() == 1) {
		   PingPongwuc wuc = new PingPongwuc(sender, appStartingCycle);
		   sendEventSelf(wuc);
		}
	}
	   
	protected void processEvent(Finalize finalize) {
	}
	
	public void processWakeUpCall(WakeUpCall wuc) {
		//System.out.println("########################");
		if (wuc instanceof PingPongwuc) {
			double currentTime = SimulationManager.getInstance().getCurrentTime();
			SingletonTestResult.getInstance().setEnabled(true);
			SingletonTestResult.getInstance().setStartingTime(currentTime);
			
			Pacote pk2 = new Pacote(sender,NodeId.get(2));
			pk2.setCont(1);
			sendPacket(pk2);
			SimulationManager.logNodeState(this.node.getId(), "Primeiro-envio", "int", String.valueOf(10));
			FinalizeEnergyMeasurementWUC femwuc = new FinalizeEnergyMeasurementWUC(sender, 15000);
			sendEventSelf(femwuc);
		} else if (wuc instanceof FinalizeEnergyMeasurementWUC) {
			System.out.println(SingletonTestResult.getInstance().getTotalEnergyConsumption());
			SingletonTestResult.getInstance().printAllStats();
		}
	}
}
