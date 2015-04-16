package escaux;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import OpenRate.record.RatingRecord;

public abstract class EscauxRecord extends RatingRecord{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Record type - used to quickly locate records for rating later
	public static final int DETAIL_RECORD = 20;
	public static final int HEADER_RECORD = 10;
	public static final int TRAILER_RECORD = 30;

	// original parsed cdr fields
	public String subscriberNumber = null;
	public String dialedNumber = null;
	public int billsec = 0;
	public int events = 0;
	public int size = 0;
	public String callType = null;
	public SimpleDateFormat month;
	public String service = null;
	public String type = null;
	public String direction = null;
	public String roaming = null;
	public String skip = null;
	// calculated fields
	public String dialedNumberNorm = null;
	public String account = null;
	public String destination = null;
	public String callCase = null;
	public double ratedAmount = 0;
	public String zone = null;
	// Discount stuff - inputs
	public String discount;         // The discount we want to apply
	public String discountRUM;      // The RUM we want to discount
	public String discountPeriod;   // The counter period, M = month, D = day
	public double discountInitValue;// The starting value of the counter
	// Discount stuff - results
	public boolean discountApplied;
	public double discountGranted;
	public long discountRecId;
	public String discountRule;
	public long balanceGroup;
	public int discountCounter;
	public String visibility;
	
	public EscauxRecord() {
		super();
	}
	
	public EscauxRecord(String OriginalData) {
		super();
		this.setOriginalData(OriginalData);
	}
	
	/**
	   * Return the dump-ready data
	   *
	   * @return The dump info strings
	   */
	  @Override
	  public ArrayList<String> getDumpInfo() {

	    ArrayList<String> tmpDumpList;
	    tmpDumpList = new ArrayList<>();

	    tmpDumpList.add("============ BEGIN RECORD ============");
	    tmpDumpList.add("  Record Number            = <" + this.RecordNumber + ">");
	    tmpDumpList.add("--------------------------------------");
	    tmpDumpList.add("  Service                  = <" + this.service + ">");
	    tmpDumpList.add("  Direction                = <" + this.direction + ">");
	    tmpDumpList.add("  Type                     = <" + this.type + ">");
	    tmpDumpList.add("  Subscriber Number        = <" + this.subscriberNumber + ">");
	    tmpDumpList.add("  Dialed Number            = <" + this.dialedNumber + ">");
	    tmpDumpList.add("  Call Type                = <" + this.callType + ">");
	    tmpDumpList.add("  Events                   = <" + this.events + ">");
	    tmpDumpList.add("  Duration (billsec)       = <" + this.billsec + ">");
	    tmpDumpList.add("  Session Size             = <" + this.size + ">");
	    tmpDumpList.add("  Month                    = <" + this.month + ">");
	    tmpDumpList.add("  Roaming                  = <" + this.roaming + ">");
	    tmpDumpList.add("--------------------------------------");
	    tmpDumpList.add("  Normalised Dialed Number = <" + this.dialedNumberNorm + ">");
	    tmpDumpList.add("  Account                  = <" + this.account + ">");
	    tmpDumpList.add("  Destination              = <" + this.destination + ">");
	    tmpDumpList.add("  Rated Amount             = <" + this.ratedAmount + ">");
	    tmpDumpList.add("--------------------------------------");
	    tmpDumpList.add("  Discount Rule            = <" + this.discountRule + ">");
	    tmpDumpList.add("  Discount RUM             = <" + this.discountRUM + ">");
	    tmpDumpList.add("  Discount Period          = <" + this.discountPeriod + ">");
	    tmpDumpList.add("  Discount Initial Value   = <" + this.discountInitValue + ">");
	    tmpDumpList.add("--------------------------------------");
	    tmpDumpList.add("  Discount Applied         = <" + this.discountApplied + ">");
	    tmpDumpList.add("  Discount Granted         = <" + this.discountGranted + ">");
	    tmpDumpList.add("============ END RECORD ==============");

	    // Get any charge packets
	    tmpDumpList.addAll(this.getChargePacketsDump());

	    // Add balance impacts
	    tmpDumpList.addAll(this.getBalanceImpactsDump());

	    // Use the standard function to get the error information
	    tmpDumpList.addAll(this.getErrorDump());

	    return tmpDumpList;
	  }
	  
	  public Object getSourceKey() {
		  return null;
	  }
	
	public abstract void mapData();
	public abstract String unmapOriginalData();
	
	

}
