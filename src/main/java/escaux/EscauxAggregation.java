package escaux;

import java.util.ArrayList;

import OpenRate.exception.ProcessingException;
import OpenRate.process.AbstractAggregation;
import OpenRate.record.IRecord;

public class EscauxAggregation extends AbstractAggregation {

	private static String[] fieldList = new String[1];
	private static ArrayList<String> keysToAggregate;

	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		keysToAggregate = new ArrayList<>();
		if(currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			try {
				keysToAggregate.add(currentRecord.getField(3));
				fieldList[0] = currentRecord.getField(27);
				Aggregate(fieldList, keysToAggregate);
			} catch (ProcessingException pe) {
				getPipeLog().error("Error processing aggregation", pe);
			}

		}
		return r;
	}

	@Override
	public IRecord procErrorRecord(IRecord r) {
		// TODO Auto-generated method stub
		return null;
	}

}
