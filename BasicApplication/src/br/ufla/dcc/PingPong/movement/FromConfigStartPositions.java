package br.ufla.dcc.PingPong.movement;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ShoXParameter;

public class FromConfigStartPositions extends StartPositionGenerator {
	
	private String fileName = "startpositions.xml";
	
	private Map<Integer,Position> nos = new HashMap<Integer,Position>();
	
	public FromConfigStartPositions() {
		File file = new File(fileName);
		System.err.println(file);
		readXML(file);
	}

	@Override
	public Position newPosition(Node node) {
		Position pos = nos.get(node.getId().asInt());
		return pos;
	}
	
	public void readXML(File file)
	{
	   try {
		   SAXBuilder builder = new SAXBuilder();
		   Document doc = builder.build(file.getPath());
		   Element element = doc.getRootElement();
		   List<Element> listPositions = element.getChildren();//("positions").getChildren();
		   for (Element elem : listPositions)
		   {
               Position pos = new Position(Double.parseDouble(elem.getAttributeValue("x")), Double.parseDouble(elem.getAttributeValue("y")));
               nos.put(Integer.parseInt(elem.getAttributeValue("id")),pos);
		   }
	   } catch (Exception e) { System.out.println("Error in file reading"); }
	}

}
