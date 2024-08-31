package dataCollection;

import agent.Agent;
import message.State;
import repast.simphony.data2.AggregateDataSource;

public class NeutralAggregateCount implements AggregateDataSource {

	@Override
	public String getId() {
		
		return "neutralAgents";
	}

	@Override
	public Class<?> getDataType() {
		
		return Integer.class;
	}

	@Override
	public Class<?> getSourceType() {
		
		return Agent.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		
		int res = 0;
		for(Object ob : objs) {

			if (((Agent)ob).getMainState() == State.NEUTRAL) res++;
				
			
		}
		
		return res;
	}

	@Override
	public void reset() {
		

	}
}
