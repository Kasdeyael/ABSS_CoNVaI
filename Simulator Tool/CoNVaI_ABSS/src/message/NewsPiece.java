package message;
import exception.IncompatibleParameterException;
public class NewsPiece {
	
	private String newsID;
	private String[] characteristics;
	
	/**
	 * Constructor
	 * @param newsID
	 * @param chars
	 */
	public NewsPiece(String newsID, String[] chars) throws IncompatibleParameterException{
		this.setNewsID(newsID);
		if (chars.length < 2) throw new IncompatibleParameterException("News characteristics must be: novelty, probInfluenceNews");
		System.out.println("Novelty: "+Double.valueOf(chars[0])+", prob: "+Double.valueOf(chars[1]));
		characteristics = chars;
		
	}

	/**
	 * Gets newsUID (tweet id)
	 * @return
	 */
	public String getNewsID() {
		return newsID;
	}

	/**
	 * Sets newsUID (tweet id)
	 * @param newsID
	 */
	public void setNewsID(String newsID) {
		this.newsID = newsID;
	}
	
	/**
	 * Novelty decreases with time
	 * @param init_tck
	 * @param current_tck
	 * @return
	 */
	public double getNovelty(double init_tck, double current_tck) {

		double val = Math.exp(-Math.pow(current_tck-init_tck, 2) / (2 * Math.pow(10, 2)));
		//System.out.println("Novelty (decreases from "+Double.valueOf(characteristics[0])+"): "+val* Double.valueOf(characteristics[0]));
		return val* Double.valueOf(characteristics[0]);
	}

	/**
	 * Returns prob influence
	 * @return
	 */
	public double getProbInfluence() {
		
		
		return Double.valueOf(characteristics[1]);
		
	}
	
	/**
	 * Returns all the characteristics
	 * @return
	 */
	public String[] getCharacteristics() {
		return characteristics;
	}
	
	@Override
	public boolean equals(Object newsObj) {
		if (newsObj instanceof NewsPiece) {
			NewsPiece news2 = (NewsPiece) newsObj;
			if (this.newsID.equals(news2.getNewsID())){ //same newsID (corresponds with tweetID)
				
				return true;
				//if (news2.getCharacteristics().length == this.characteristics.length) { //same length and content
				//	for (int i = 0; i < this.characteristics.length; i++) {
				//		if (!news2.getCharacteristics()[i].equals(this.characteristics[i])) return false;
				//	}
					
				//}
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {

		return  this.newsID.hashCode(); //newsID is tweetId, so it is a unique code and should only appear once

	}
	
}
