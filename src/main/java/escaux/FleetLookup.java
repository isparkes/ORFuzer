package escaux;

import OpenRate.process.AbstractBestMatch;
import OpenRate.record.IRecord;

public class FleetLookup extends AbstractBestMatch{

	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		if(currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			try {
			//get the account linked to the dialer
			String dialerAccount = this.getBestMatch("DEF", currentRecord.dialedNumber);
			currentRecord.fleet = "NO";
			
			if(this.isValidBestMatchResult(dialerAccount)) {
				if(dialerAccount.equals(currentRecord.account))
					currentRecord.fleet = "YES"; 
			}
			} catch(Exception ignore) {
				//Do nothing
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
