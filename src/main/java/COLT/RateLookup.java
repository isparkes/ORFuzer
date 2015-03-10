package COLT;

import OpenRate.process.AbstractRUMRateCalc;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import COLT.ColtRecord;

public class RateLookup extends AbstractRUMRateCalc{
	  @Override
	  public IRecord procValidRecord(IRecord r) {
	    int Index;
	    ChargePacket tmpCP;
	    ColtRecord CurrentRecord;
	    RecordError tmpError;

	    CurrentRecord = (ColtRecord) r;

	    try {
	    	performRating(CurrentRecord);
	    } catch (Exception e) {
	    	tmpError = new RecordError("ERR_RATE_LOOKUP", ErrorType.SPECIAL);
	    	CurrentRecord.addError(tmpError);
	    }

	    for (Index = 0; Index < CurrentRecord.getChargePacketCount(); Index++) {
	    	tmpCP = CurrentRecord.getChargePacket(Index);
	    	CurrentRecord.RatedAmount += tmpCP.chargedValue;
	    }

	    return r;
	  }

	  @Override
	  public IRecord procErrorRecord(IRecord r) {
	    return r;
	  }
}
