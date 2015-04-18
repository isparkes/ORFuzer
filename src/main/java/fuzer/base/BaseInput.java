package fuzer.base;

import artillium.ArtilliumDefs;
import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.HeaderRecord;
import OpenRate.record.IRecord;
import OpenRate.record.TrailerRecord;

/**
 * This module processed the data from the file and determines the mapping
 * which should be applied to it.
 *
 * @author Afzaal
 */
public class BaseInput
  extends FlatFileInputAdapter
{
  //  This is the stream record number counter which tells us the number of the compressed records
  private int StreamRecordNumber;
  
  //  This is the object that is used to compress the records
  BaseRecord tmpDataRecord = null;
  
  // File reference
  private String FileRefID = "";

 /**
  * Constructor for ErgatelInput.
  */
  public BaseInput()
  {
    super();
  }
  
  //-----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

 /**
  * This is called when the synthetic Header record is encountered, and has the
  * meaning that the stream is starting. In this example we have nothing to do
  * 
  * @return the processed header record
  */
  @Override
  public IRecord procHeader(IRecord r)
  {
    HeaderRecord hr = (HeaderRecord)r;

    // reset the record numbering
    StreamRecordNumber = 0;

    // Get the file reference
    FileRefID = hr.getStreamName();

    return r;
  }

 /**
  * This is called when a data record is encountered. You should do any normal
  * processing here. For the input adapter, we probably want to change the 
  * record type from FlatRecord to the record(s) type that we will be using in
  * the processing pipeline.
  *
  * This is also the location for accumulating records into logical groups
  * (that is records with sub records) and placing them in the pipeline as
  * they are completed. If you receive a sub record, simply return a null record
  * in this method to indicate that you are handling it, and that it will be
  * purged at a later date.
  * 
  * @return the processed valid detail record
  */
  @Override
  public IRecord procValidRecord(IRecord r)
  {
    String tmpData;
    FlatRecord originalRecord = (FlatRecord)r;
    tmpData = originalRecord.getData();
    tmpDataRecord = new BaseRecord();

    if (tmpData.startsWith(ArtilliumDefs.BASE_HEADER))
    {
      tmpDataRecord.mapBaseHeaderRecord(tmpData);
    }
    else if (tmpData.startsWith(ArtilliumDefs.BASE_TRAILER))
    {
      tmpDataRecord.mapBaseTrailerRecord(tmpData);
    }
    else
    {
      tmpDataRecord.mapBaseDetailRecord(tmpData);
    }

    tmpDataRecord.RecordNumber = StreamRecordNumber;
    StreamRecordNumber++;

    // spread the file reference to each record
    tmpDataRecord.streamReference = FileRefID;

    return (IRecord)tmpDataRecord;
  }

 /**
  * This is called when a data record with errors is encountered. You should do
  * any processing here that you have to do for error records, e.g. statistics,
  * special handling, even error correction!
  * 
  * The input adapter is not expected to provide any records here.
  * 
  * @return the processed error detail record
  */
  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

 /**
  * This is called when the synthetic trailer record is encountered, and has the
  * meaning that the stream is now finished. In this example, all we do is 
  * pass the control back to the transactional layer.
  *
  * In models where record aggregation (records and sub records) is used, you
  * might want to check for any purged records here.
  * 
  * @return the processed trailer record
  */
  @Override
  public IRecord procTrailer(IRecord r)
  {
    TrailerRecord tmpTrailer;
    
    // set the trailer record count
    tmpTrailer = (TrailerRecord)r;
    
    tmpTrailer.setRecordCount(StreamRecordNumber);
    return (IRecord)tmpTrailer;
  }
}
