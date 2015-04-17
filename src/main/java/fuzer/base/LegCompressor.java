package fuzer.base;

import OpenRate.process.AbstractPersistentObjectProcess;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * This module gets one leg of a two leg call, correlating the A and B numbers
 * to make a rateble record out of it. It is skipped for re-rating. We need to
 * get the other party number and the price from the first leg.
 * 
 * The AbstractPersistentObjectProcess stores the partial information in an
 * internal hash table, which is persisted to disk on shutdown and read into
 * memory again on startup.
 * 
 * As records are matched, they are removed from the store, therefore the store
 * should not normally grow.
 * 
 * The first twin of each record type is stored into the persistent store and
 * marked with an ERR_DROPPED_LEG error, which causes it to skip the rest of the
 * processing. We store away the information that is needed to be enriched into
 * the second twin.
 * 
 * When the second twin arrives, the stored information is put into the record,
 * and the cached information is removed.
 * 
 * Properties file:
 * 
 * 
      <!-- Compress the A B legs by discarding one -->
      <ALegCompressor>
        <ClassName>fuzer.base.LegCompressor</ClassName>
        <DataCache>LegCache</DataCache>
        <BatchSize>5000</BatchSize>
      </ALegCompressor>

 * Cache:

        <!-- Persistent store for remembering two let call info -->
        <LegCache>
          <ClassName>OpenRate.cache.PersistentIndexedObject</ClassName>
          <DataSourceType>File</DataSourceType>
          <DataFile>Data/call_legs.dat</DataFile>
        </LegCache>

 * 
 */
public class LegCompressor extends AbstractPersistentObjectProcess {
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  @Override
  public IRecord procValidRecord(IRecord r) {
    BaseRecord CurrentRecord = (BaseRecord) r;

    if ((CurrentRecord.RECORD_TYPE == BaseDefs.BASE_DETAIL_TYPE) && CurrentRecord.roamingMode) {
      // do nothing for invalid, errored, not chargeable records
      if ((CurrentRecord.Service.equals("SMS")) || (CurrentRecord.Service.equals("TEL")) || (CurrentRecord.Service.equals("DATA"))) {
        if (getObject(CurrentRecord.linkedRecID) == null) {
          // We do not have a matching twin for this record, we have to assume that it is the first
          // We have to work out which is the number to keep. We know that it
          // will not be 0032 (Belgium), so we take the *other* number.
          if (CurrentRecord.Orig_Number.equals("0032")) {
            putObject(CurrentRecord.linkedRecID, "D:" + CurrentRecord.B_Number);

            // Drop the call, we stored the info
            RecordError tmpError = new RecordError("ERR_DROPPED_LEG", ErrorType.SPECIAL);
            tmpError.setModuleName(getSymbolicName());
            tmpError.setErrorDescription(CurrentRecord.Service + "," + CurrentRecord.roamingMode + "," + CurrentRecord.linkedRecID);
            CurrentRecord.addError(tmpError);
          } else if (CurrentRecord.B_Number.equals("0032")) {
            putObject(CurrentRecord.linkedRecID, "O:" + CurrentRecord.Orig_Number + ";P:" + CurrentRecord.fields[BaseDefs.BASE_DTL_PRICE_IDX]);

            // Drop the call, we stored the info
            RecordError tmpError = new RecordError("ERR_DROPPED_LEG", ErrorType.SPECIAL);
            tmpError.setModuleName(getSymbolicName());
            tmpError.setErrorDescription(CurrentRecord.Service + "," + CurrentRecord.roamingMode + "," + CurrentRecord.linkedRecID);
            CurrentRecord.addError(tmpError);
          }
        } else {
          // We have already stored the matching twin, so take it back out of the 
          // store, and enrich the record.
          String otherNumber = (String) getObject(CurrentRecord.linkedRecID);

          // Remove the entry from the cache
          deleteObject(CurrentRecord.linkedRecID);

          // Enrich this record with the inforamtion we stored away from the
          // first twin.
          if (CurrentRecord.Orig_Number.equals("0032")) {
            if (otherNumber.startsWith("O:")) {
              // Split the price and number information
              String[] otherNumberParts = otherNumber.split(";");
              String tmpOtherOrigNumber = otherNumberParts[0].replaceAll("^O:", "");
              String tmpPrice = otherNumberParts[1].replaceAll("^P:", "");
              CurrentRecord.Orig_Number = tmpOtherOrigNumber;

              // Save the compressed information back to the input record for re-rating
              CurrentRecord.fields[BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX] = CurrentRecord.Orig_Number;
              CurrentRecord.fields[BaseDefs.BASE_DTL_PRICE_IDX] = tmpPrice;
            } else {
              RecordError tmpError = new RecordError("ERR_LEG_REPLACEMENT_O", ErrorType.SPECIAL);
              tmpError.setModuleName(getSymbolicName());
              tmpError.setErrorDescription(CurrentRecord.Service + "," + CurrentRecord.roamingMode + "," + CurrentRecord.linkedRecID);
              CurrentRecord.addError(tmpError);
            }
          } else {
            if (otherNumber.startsWith("D:")) {
              CurrentRecord.B_Number = otherNumber.replaceAll("^D:", "");

              // Save the compressed information back to the input record for re-rating
              CurrentRecord.fields[BaseDefs.BASE_DTL_DESTINATION_POINT_IDX] = CurrentRecord.B_Number;
            } else {
              RecordError tmpError = new RecordError("ERR_LEG_REPLACEMENT_D", ErrorType.SPECIAL);
              tmpError.setModuleName(getSymbolicName());
              tmpError.setErrorDescription(CurrentRecord.Service + "," + CurrentRecord.roamingMode + "," + CurrentRecord.linkedRecID);
              CurrentRecord.addError(tmpError);
            }
          }
        }
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
    // do nothing
    return r;
  }
}
