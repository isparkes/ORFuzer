package escaux;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.omg.CORBA.FieldNameHelper;

import OpenRate.exception.InitializationException;
import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import OpenRate.utils.PropertyUtils;

public class EscauxSimpleLookup extends AbstractRegexMatch{

	private List<String> tmpSearchParameters;
	private int[] keyIndexes = null;
	private List<Field> fields;
	private String message;
	
	@Override
	public void init(String PipelineName, String ModuleName) throws InitializationException{
		// TODO Auto-generated method stub
		super.init(PipelineName, ModuleName);
		
		String cacheName = PropertyUtils.getPropertyUtils().getPluginPropertyValue(getPipeName(), ModuleName, "DataCache");
		if(cacheName == null){
			throw new InternalError("Cannot read property <DataCache> of <" + ModuleName + ">");
		}
		
		String keys = PropertyUtils.getPropertyUtils().getDataCachePropertyValue("CacheFactory", cacheName, "Keys");
		if(keys == null)
			throw new InternalError("Cannot read property <Keys> of <" + ModuleName + ">");
		String fields = PropertyUtils.getPropertyUtils().getDataCachePropertyValue("CacheFactory", cacheName, "Fields");
		if(fields == null)
			throw new InternalError("Cannot read property <Fields> of <" + ModuleName + ">");
		
		this.message = PropertyUtils.getPropertyUtils().getDataCachePropertyValue("CacheFactory", cacheName, "ErrorMessage");
		if(this.message == null)
			throw new InternalError("Cannot read property <ErrorMessage> of <" + ModuleName + ">");
		
		String[] fieldArray = fields.split(",");
		this.fields = new ArrayList<>();
		try {
			for(String fieldName : fieldArray)
				this.fields.add(EscauxRecord.class.getField(fieldName));
		} catch(NoSuchFieldException e) {
			throw new InitializationException(e.getMessage(), ModuleName);
		}
		
		String[] keyIndexesString = keys.split(",");
		keyIndexes = new int[keyIndexesString.length];		
		for(int i = 0; i < keyIndexesString.length; ++i)
			keyIndexes[i] = Integer.parseInt(keyIndexesString[i]);
	} 
	
	@Override
	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		if(currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			ArrayList<String> results = new ArrayList<>();
			tmpSearchParameters = new ArrayList<>();
			for(int keyIndex : keyIndexes) {
				tmpSearchParameters.add(currentRecord.getField(keyIndex));
			}
			
			results = getRegexMatchWithChildData("DEF", tmpSearchParameters.toArray(new String[tmpSearchParameters.size()]));
			
			if(isValidRegexMatchResult(results)) {
				try {
					for(int i = 0; i < results.size(); ++i) {
						this.fields.get(i).set(currentRecord, results.get(i));
					}
				} catch (IllegalArgumentException e) {
					currentRecord.addError(new RecordError(e.getMessage(), ErrorType.FATAL));
				} catch (IllegalAccessException e) {
					currentRecord.addError(new RecordError(e.getMessage(), ErrorType.FATAL));
				}
			} else {
				currentRecord.addError(new RecordError(this.message, ErrorType.DATA_NOT_FOUND));
			}
		}
		
		return r;
	}

	@Override
	public IRecord procErrorRecord(IRecord r) {
		return r;
	}
}
