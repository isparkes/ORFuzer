package escaux;

import java.util.ArrayList;

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

public class ZonesLookup extends AbstractRegexMatch{
	private final String[] tmpSearchParameters = new String[1];

	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		
		if (currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			ArrayList<String> results;
			tmpSearchParameters[0] = currentRecord.destination;
			
			results = getRegexMatchWithChildData("DEF", tmpSearchParameters);
			
			if(isValidRegexMatchResult(results)) {
				currentRecord.toZone = results.get(0);
			} else {
				currentRecord.addError(new RecordError("Unable to determine the destination zone"));
			}
			
			tmpSearchParameters[0] = currentRecord.origin;
			results = getRegexMatchWithChildData("DEF", tmpSearchParameters);
			
			if(isValidRegexMatchResult(results)) {
				currentRecord.fromZone = results.get(0);
			} else {
				currentRecord.addError(new RecordError("Unable to determine the origin zone"));
			}
		}
		return r;
	}

	@Override
	public IRecord procErrorRecord(IRecord r) {
		// TODO Auto-generated method stub
		return r;
	}

}
