package artillium;

import escaux.EscauxRecord;
import escaux.GenericInputAdapter;

public class ArtilliumInputAdapter extends GenericInputAdapter {

	@Override
	public EscauxRecord getNewRecord() {
		return new ArtilliumRecord();
	}

}
