package escaux;

import OpenRate.process.AbstractRUMRateCalc;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

public class RateLookup extends AbstractRUMRateCalc {

  @Override
  public IRecord procValidRecord(IRecord r) {
    int index;
    ChargePacket tmpCP;
    EscauxRecord CurrentRecord;
    RecordError tmpError;
    CurrentRecord = (EscauxRecord) r;

    if (CurrentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
      try {
        performRating(CurrentRecord);
      } catch (Exception e) {
        tmpError = new RecordError("ERR_RATE_LOOKUP", ErrorType.SPECIAL);
        CurrentRecord.addError(tmpError);
      }

      for (index = 0; index < CurrentRecord.getChargePacketCount(); index++) {
        tmpCP = CurrentRecord.getChargePacket(index);
        CurrentRecord.ratedAmount += tmpCP.chargedValue;
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
