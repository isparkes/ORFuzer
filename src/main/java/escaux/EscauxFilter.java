package escaux;

import OpenRate.exception.InitializationException;
import OpenRate.process.AbstractStubPlugIn;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import OpenRate.utils.PropertyUtils;

public class EscauxFilter extends AbstractStubPlugIn{
	
	private String[] values;
	private int field;
	
	@Override
	public void init(String PipelineName, String ModuleName) throws InitializationException{
		super.init(PipelineName, ModuleName);
		
		String valuesToFilter = PropertyUtils.getPropertyUtils().getPluginPropertyValue(getPipeName(), ModuleName, "Values");
		
		if(valuesToFilter == null){
			throw new InternalError("Cannot read property <Values> of <" + ModuleName + ">");
		}
		
		String fieldString = PropertyUtils.getPropertyUtils().getPluginPropertyValue(getPipeName(), ModuleName, "Field");
		
		if(fieldString == null){
			throw new InternalError("Cannot read property <Field> of <" + ModuleName + ">");
		}
		
		try {
			field = Integer.parseInt(fieldString);
		} catch(NumberFormatException e) {
			throw new InternalError("Property <Field> of <" + ModuleName + "> is not a valid field index");
		}
		
		values = valuesToFilter.split(",");
	}

	public IRecord procValidRecord(IRecord r) {
		EscauxRecord currentRecord = (EscauxRecord) r;
		
		if(currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
			getPipeLog().error("Looking if we can skip that record");
			if("SMS".equals(currentRecord.service) && "PREMIUM".equals(currentRecord.type)) {
				getPipeLog().error("=> Record is SMS PREMIUM");
				for(String value : values) {
					getPipeLog().error("=> Looking if record field <" + this.field + "> is <" + value + ">");
					if(value.equals(currentRecord.getField(this.field))) {
						getPipeLog().error("=> It is...");
						//currentRecord.addError(new RecordError("Dropped because field index <" + this.field + "> = <" + value + ">" ));
						currentRecord.visibility = "NO";
					}
				}
			}
		}
		
		return r;
	}

	public IRecord procErrorRecord(IRecord r) {
		// TODO Auto-generated method stub
		return r;
	}

}
