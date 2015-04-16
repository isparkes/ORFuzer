package escaux;

import java.util.ArrayList;

import OpenRate.process.AbstractAggregation;
import OpenRate.record.IRecord;

public class EscauxAggregation extends AbstractAggregation {
	
	private static String[] fieldList = new String[1];
	private static ArrayList<String> keysToAggregate;

	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		if(currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			fieldList[0] = currentRecord.service;
			
			keysToAggregate.add("");
			
			//this.Aggregate(fieldList, keysToAggregate);
			
		}
		return r;
	}

	@Override
	public IRecord procErrorRecord(IRecord r) {
		// TODO Auto-generated method stub
		return null;
	}

}
