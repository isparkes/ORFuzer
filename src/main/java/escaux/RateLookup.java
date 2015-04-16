package escaux;

import OpenRate.process.AbstractRUMRateCalc;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

public class RateLookup extends AbstractRUMRateCalc {

  @Override
  public IRecord procValidRecord(IRecord r) {
    int Index;
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

      for (Index = 0; Index < CurrentRecord.getChargePacketCount(); Index++) {
        tmpCP = CurrentRecord.getChargePacket(Index);
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
