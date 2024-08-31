package dataCollection;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import exception.IncompatibleParameterException;
import message.Message;
import message.Thread;

public class OutputThread {
	
	/**
	 * Returns a json with the information needed for our analysis.
	 * @param th
	 * @param batchNumber
	 * @param runNumber
	 * @return
	 * @throws IncompatibleParameterException
	 * @throws IOException
	 */
	public static String getJsonThread(Thread th, int batchNumber, int runNumber) throws IncompatibleParameterException, IOException {

		
				
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode newsNode = mapper.createObjectNode(); // {}

			newsNode.put("newsID", th.getNewsPiece().getNewsID()); // {"newsID": val}
			newsNode.put("batch_number", batchNumber); // {"newsID": val, "batch_number":val}
			newsNode.put("run_number", runNumber); // {"newsID": val, "batch_number":val}
				
			newsNode.put("started", th.getIniTick()); // {"newsID": val, "batch_number":val, "started":tick}

			
			ArrayNode diffusionList = mapper.createArrayNode(); // []
				
			Map<Double, List<Message>> msg = th.getMessages();
			double prevTick = th.getIniTick() - 1; //edited so if 0 -> -1, else start-1
			
			for (Double tick : msg.keySet()) {
					prevTick += 1;
					while (prevTick < tick) {
						//System.out.println("Filling tick! From "+prevTick+", to: "+tick);
						ObjectNode diffusionTick = mapper.createObjectNode(); // fill with empty objs
						diffusionTick.put("tick", prevTick);
						//diffusionTick.set("neutral", mapper.createArrayNode()); //empty users
						diffusionTick.set("infected", mapper.createArrayNode());
						diffusionTick.set("debunker", mapper.createArrayNode());
						diffusionList.add(diffusionTick); 
						prevTick += 1;
					}
					
					ObjectNode diffusionTick = mapper.createObjectNode(); // {}
					diffusionTick.put("tick", tick);// {"tick": val}
					List<Message> msgList = msg.get(tick);
					ArrayNode /*neutral = mapper.createArrayNode(),*/ inf = mapper.createArrayNode(), debun = mapper.createArrayNode();
					for (Message m : msgList) {
						//System.out.println("Filling neutral, inf, deb for each message in tick "+tick+", message: "+m.toString());
						//if (m.getNeutral() != 0) neutral.add(m.getUserID().getLabel());
						if (m.getInfected() != 0) inf.add(m.getUserID().getLabel());
						if (m.getDebunker() != 0) debun.add(m.getUserID().getLabel());
					}
					//diffusionTick.set("neutral", neutral); // {"tick": val, "infected": val, ...}
					diffusionTick.set("infected", inf);
					diffusionTick.set("debunker", debun);
					diffusionList.add(diffusionTick); //[{"tick": val, "infected": val, ...}, {"tick": val, "infected": val, ...}, ... ]
			}
				
			newsNode.set("diffusion", diffusionList);// {"newsID": val, "started":tick, "diffusion": [{...}, {...}, ...]}
				
			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newsNode);
			return jsonString;

			
		
		}
	}

