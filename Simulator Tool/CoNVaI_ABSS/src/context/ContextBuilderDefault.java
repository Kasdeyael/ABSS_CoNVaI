package context;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;

public class ContextBuilderDefault extends DefaultContext<Object> implements ContextBuilder<Object> {
	
	@Override
	public Context<Object> build(Context<Object> context) {
		
		context.setId("CoNVaI_ABSS"); 
		ModelCreator model = ModelCreator.getInstance();
		
		try {
			model.populateContext(context);
			context = model.getContext();
		} catch(Exception exc) { 
			System.err.println("ERROR CREATING THE MODEL: "+exc.getMessage());
			exc.printStackTrace();
			System.exit(0);
		}
		
		return context;
	}
	

}
