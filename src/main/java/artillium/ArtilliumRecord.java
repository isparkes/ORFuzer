package artillium;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import OpenRate.process.AbstractRUMTimeMatch;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.RecordError;
import OpenRate.record.TimePacket;
import escaux.EscauxRecord;

public class ArtilliumRecord extends EscauxRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int RECORD_DETAIL = 2;
	public static final int LINKED_RECORD_ID = 3;
	public static final int ORIGINATING_CLI = 5;
	public static final int ORIGINATING_POINT = 6;
	public static final int DESTINATION_POINT = 7;
	public static final int DESTINATION_NETWORK_GROUP = 8;
	public static final int DESTINATION_NETWORK_SUBGROUP = 9;
	public static final int CALL_MODE = 15;
	public static final int DATETIME_START = 16;
	public static final int SESSION_DURATION = 18;
	public static final int SESSION_SIZE = 19;
	public static final int EVENTS = 20;
	public static final int PRICE_TYPE = 27;
	public static final int SETUP_FEE = 28;
	public static final int PRICE = 29;
	//public static final int IMSI = 45;

	public int callScenario;


	@Override
	public void mapData() {
		// TODO Auto-generated method stub
		
		this.fields = this.getOriginalData().split("\\|");

		if(getField(0).equals("HEADER")) {
			this.RECORD_TYPE = HEADER_RECORD;
		} else if(getField(0).equals("TRAILER")){
			this.RECORD_TYPE = TRAILER_RECORD;
		}
		else {
			this.RECORD_TYPE = DETAIL_RECORD;
			this.subscriberNumber = getField(ORIGINATING_CLI);
			this.dialedNumber = getField(DESTINATION_POINT);
			this.billsec = Integer.parseInt(getField(SESSION_DURATION));
			this.events = Integer.parseInt(getField(EVENTS));
			this.size = Integer.parseInt(getField(SESSION_SIZE));
			this.linkedRecord = getField(LINKED_RECORD_ID);
			this.originalPoint = getField(ORIGINATING_POINT);
			this.priceRetail = Double.parseDouble(getField(PRICE));
			//this.callType = getField(CALL_TYPE_IDX);

			// Get the event date 2014 06 03 103804

			SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMdHHmmss");
			try {
				EventStartDate = sdfInput.parse(getField(DATETIME_START));
				UTCEventDate = EventStartDate.getTime() / 1000;
			} catch (ParseException ex) {
				RecordError tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
				tmpError.setModuleName("ErgatelRecord");
				addError(tmpError);
			}

			// Set the Duration RUM
			this.setRUMValue("DUR", this.billsec);
			this.setRUMValue("EVT", this.events);
			this.setRUMValue("SIZE", this.size);
			this.setRUMValue("RETAIL", this.priceRetail);

			// Add a charge packet
			ChargePacket tmpCP = new ChargePacket();

			// Mark the charge packet as a "Retail" packet
			tmpCP.packetType = "R";

			// Set the zone model - Assuming we have a standard zone model
			tmpCP.zoneModel = "Default";

			// We need this for the PriceLookup
			tmpCP.ratePlanName = "Default";

			// Set the service to something reasonable
			tmpCP.service = "TEL";

			// Set the time model AND the result (we will skip time lookup at the moment)
			tmpCP.timeModel = "Default";
			tmpCP.timeSplitting = AbstractRUMTimeMatch.TIME_SPLITTING_NO_CHECK;

			// Set up the time packet as if we did the time lookup (just enough to make it work)
			TimePacket tmpTZ = new TimePacket();
			tmpTZ.timeModel = "Default";
			tmpTZ.timeResult = "FLAT";

			// Put this in the record
			tmpCP.addTimeZone(tmpTZ);
			addChargePacket(tmpCP);
		}
	}

	@Override
	public String unmapOriginalData() {
		int i;
		StringBuilder buf = new StringBuilder(1024);

		/*write the destination information back
	     this.setField(ACCOUNT_IDX, Account);
	     this.setField(DESTINATION_IDX, Destination);
		 */
		buf.append(this.fields[0]);
		for (i = 1; i < this.fields.length; i++) {
			buf.append(",");
			buf.append(this.fields[i]);
		}
		buf.append(",");
		buf.append(this.dialedNumberNorm);
		buf.append(",");
		buf.append(this.destination);
		buf.append(",");
		buf.append(this.account);
		buf.append(",");
		buf.append(this.ratedAmount);
		buf.append(",");
		buf.append(this.service);
		if (this.isErrored()) {
			buf.append(",");
			buf.append(this.getErrors().get(0).getMessage());
		}
		return buf.toString();
	}

}
