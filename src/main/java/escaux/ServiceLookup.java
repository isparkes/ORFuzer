package escaux;

import java.util.ArrayList;
import java.util.List;

import OpenRate.exception.InitializationException;
import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import OpenRate.utils.PropertyUtils;

public class ServiceLookup extends AbstractRegexMatch {

	private List<String> tmpSearchParameters;
	private int[] keyIndexes = null;
	
	@Override
	public void init(String PipelineName, String ModuleName) throws InitializationException{
		// TODO Auto-generated method stub
		super.init(PipelineName, ModuleName);
		String keys = PropertyUtils.getPropertyUtils().getPluginPropertyValue(getPipeName(), this.getClass().getSimpleName(), "Keys");;
		if(keys == null)
			throw new InternalError("Cannot read property <Keys> of <" + this.getClass().getSimpleName() + ">");
		
		String[] keyIndexesString = keys.split(",");
		keyIndexes = new int[keyIndexesString.length];
		
		for(int i = 0; i < keyIndexesString.length; ++i) {
			keyIndexes[i] = Integer.parseInt(keyIndexesString[i]);
		}
	}
	 	  
	@Override
	public IRecord procValidRecord(IRecord r){
		ArrayList<String> results = new ArrayList<>();
		tmpSearchParameters = new ArrayList<>();
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		if (currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			for(int keyIndex : keyIndexes)
				tmpSearchParameters.add(currentRecord.getField(keyIndex));
			results = getRegexMatchWithChildData("DEF", tmpSearchParameters.toArray(new String[tmpSearchParameters.size()]));
			if(isValidRegexMatchResult(results)) {
				currentRecord.service = results.get(0);
			} else {
				currentRecord.addError(new RecordError("ERR_BASE_SERVICE_NOT_FOUND", ErrorType.DATA_NOT_FOUND));
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
