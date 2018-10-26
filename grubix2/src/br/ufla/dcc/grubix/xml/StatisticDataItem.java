package br.ufla.dcc.grubix.xml;

public class StatisticDataItem {
	
	public String xValue, yValue;
	public String xAxes, yAxes;
	public String SenderId;
	
	public StatisticDataItem() {

	}

	public StatisticDataItem(StatisticDataItem sdi) {
		this.SenderId = sdi.SenderId;
		this.xAxes = sdi.xAxes;
		this.xValue = sdi.xValue;
		this.yAxes = sdi.yAxes;
		this.yValue = sdi.yValue;
	}
	public void setxValue(double xvalue) {
		this.xValue = String.valueOf(xvalue);
	}

	public void setyValue(double yvalue) {
		this.yValue = String.valueOf(yvalue);
	}

	public void setSenderId(int senderid) {
		this.SenderId = String.valueOf(senderid);
	}	
	public double getxValue() {
		return Double.parseDouble(this.xValue);
	}

	public double getyValue() {
		return Double.parseDouble(this.yValue);
	}

	public int getSenderId() {
		return Integer.parseInt(this.SenderId);
	}

	/* TODO This looks as if the method should be named "toString",
	 * 		but I`m not that sure to fix it myself.
	 */
	public String ToString() {
		return SenderId + " xachs" + xAxes + " yachs" + yAxes + " " + xValue
				+ " " + yValue;
	}
	
	public void printST() {
		System.out.println(ToString());
	}
}
