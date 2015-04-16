package escaux;

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.IRecord;

public class ZoneLookup extends AbstractRegexMatch{
	private final String[] tmpSearchParameters = new String[1];

	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord CurrentRecord = (EscauxRecord) r;
		
		
		if (CurrentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			tmpSearchParameters[0] = CurrentRecord.destination;
			
			
		}
		return r;
	}

	@Override
	public IRecord procErrorRecord(IRecord r) {
		// TODO Auto-generated method stub
		return null;
	}

}
