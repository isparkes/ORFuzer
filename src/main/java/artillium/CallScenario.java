package artillium;

import OpenRate.process.AbstractStubPlugIn;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * This module finds the call scenario we are working on.
 *
 * @author Ian
 */
public class CallScenario extends AbstractStubPlugIn {
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  /**
   * This is called when a data record is encountered. You should do any normal
   * processing here.
   *
   * @return
   */
  @Override
  public IRecord procValidRecord(IRecord r) {
    ArtilliumRecord CurrentRecord = (ArtilliumRecord) r;

    if (CurrentRecord.RECORD_TYPE == ArtilliumDefs.BASE_DETAIL_TYPE) {
      switch (CurrentRecord.Service) {
        case "SMS":
          if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MO) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Originating";
          } else if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_PTC) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Originating";

            // No rating for domestic terminating SMS
            RecordError tmpError = new RecordError("ERR_NON_BILLABLE_RECORD", ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          } else if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_POC)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MT)) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Terminating";

            // No rating for domestic terminating SMS
            RecordError tmpError = new RecordError("ERR_NON_BILLABLE_RECORD", ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          } else if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MO) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Originating";
          } else if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MT) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Terminating";
          } else {
            RecordError tmpError = new RecordError("ERR_SCENARIO_NOT_MAPPED:"
                    + CurrentRecord.Service + ":"
                    + CurrentRecord.callScenario,
                    ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          }
          break;
        case "TEL":
          if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_PTC)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MO)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_FWD)) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Originating";

            // No rating for domestic originating Telephony (covered by Broadsoft)
            RecordError tmpError = new RecordError("ERR_NON_BILLABLE_RECORD", ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          } else if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_POC)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MT)) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Terminating";

            // No rating for domestic terminating Telephony (covered by Broadsoft)
            RecordError tmpError = new RecordError("ERR_NON_BILLABLE_RECORD", ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          } else if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MT) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Terminating";
          } else if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MO)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_FWD)) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Originating";
          } else {
            RecordError tmpError = new RecordError("ERR_SCENARIO_NOT_MAPPED:"
                    + CurrentRecord.Service + ":"
                    + CurrentRecord.callScenario,
                    ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          }
          break;
        case "DATA":
          if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_PTC)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MO)) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Originating";
          } else if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_POC)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_MT)) {
            CurrentRecord.roaming = "NO";
            CurrentRecord.direction = "Originating";
          } else if (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MT) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Originating";
          } else if ((CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_MO)
                  || (CurrentRecord.callScenario == ArtilliumDefs.BASE_MODE_ROAM_FWD)) {
            CurrentRecord.roaming = "YES";
            CurrentRecord.direction = "Originating";
          } else {
            RecordError tmpError = new RecordError("ERR_SCENARIO_NOT_MAPPED:"
                    + CurrentRecord.Service + ":"
                    + CurrentRecord.callScenario,
                    ErrorType.SPECIAL, getSymbolicName());
            CurrentRecord.addError(tmpError);
          }
          break;
      }
    }

    return r;
  }

  /**
   * Skip error record processing
   *
   * @param r The record we are working on
   * @return The processed record
   */
  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
