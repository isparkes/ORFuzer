package fuzer.base;

import OpenRate.record.BalanceImpact;
import OpenRate.record.ErrorType;
import OpenRate.record.RatingRecord;
import OpenRate.record.RecordError;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A Record corresponds to a unit of work that is being processed by the
 * pipeline. Records are created in the InputAdapter, pass through the Pipeline,
 * and written out in the OutputAdapter. Any stage of the pipeline my update the
 * record in any way, provided that later stages in the processing and the
 * output adapter know how to treat the record they receive.
 *
 * As an alternative, you may define a less flexible record format as you wish
 * and fill in the fields as required, but this costs performance.
 *
 * Generally, the record should know how to handle the following operations by
 * linking the appropriate method:
 *
 * mapOriginalData() [mandatory] ----------------- Transformation from a flat
 * record as read by the input adapter to a formatted record.
 *
 * unmapColtData() [mandatory if you wish to write output files]
 * ------------------- Transformation from a formatted record to a flat record
 * ready for output.
 *
 * getDumpInfo() [optional] ------------- Preparation of the dump equivalent of
 * the formatted record, ready for dumping out to a dump file.
 *
 * In this simple example, we require only to read the "B-Number", and write the
 * "Destination" as a result of this. Because of the simplicity of the example
 * we do not perform a full mapping, we just handle the fields we want directly,
 * which is one of the advantages of the BBPA model (map as much as you want or
 * as little as you have to).
 *
 */
public class BaseRecord
        extends RatingRecord {

  // The field splitter that is used to write the output records
  public static final String JBOUT_FIELD_SPLITTER = ";";

  // The recycle tag is used to detect records that have been recycled
  public static final String RECYCLE_TAG = "ORRECYCLE";

  private static final long serialVersionUID = 919691241L;

  //	Worker variables to save references during processing
  public String Circuit_ID = null;
  public boolean ChargeableRecord = true;
  public String guidingKey;                  // Used for customer account retrieval
  public String A_Number;                    // Originating number
  public String B_Number;                    // Terminating number
  public String B_Number_Normalised = null;  // Normalised B-Number
  public String StreamName = null;           // Stream name for the output
  public boolean isMVNORecord = true;         // True if the call is an MVNO call, used for voice

  public double Duration = 0;                // Call duration
  public double Volume = 0;                  // Session volume
  public double RatedAmount = 0;             // retail rated amount
  public int JBUserId;                    // A Number JB User
  public int JBUserIdB;                   // B Number JB User

  public int recycleCount = 0;           // Number of times we have recycled
  public boolean rerate = false;              // true if we are re-rating

  public String Category;                    // The call category, used for jb grouping
  public String InvDestination;              // The call destination, used for jb grouping
  public String UserTariff;                  // The tariff we have recovered for this subscriber
  public String BaseTariff;                  // The base tariff the user tariff is based on
  public String recoveredBCN;                // The BCN recovered from the A Number lookup
  public String Login;                       // Used for Xref of the A Number Lookup
  public long balanceGroup;                // Holds the ID of the balance group

  // Discount handling
  public int DiscountCounter;             // The ID of the discount counter to use
  public double DiscountInitValue;           // The inital value, used if we create a counter
  public String DiscountPeriod;              // The period of the discount
  public String UsedDiscount = "";           // The discount applied
  public String DiscountOption;              // The option that lead to the discount
  public String DiscountRUM = "";            // The RUM which was discounted
  public String DiscountRule = "";           // The rule which did the discounting
  public double DiscountGranted;             // The value of the discount granted
  public long RecId;                       // The counter record id of the counter impacted
  public int sharedBundleID;              // The ID of a shared bundle
  public int sharedBundleOwner;           // The owner of the shared bundle

  public int PackUsageFlag;               // Set on the first usage of a pack
  public boolean UserTariffOverridden;        // Set if we are using an override rate, set after discounting

  // Used to flag potential fraud cases
  public boolean FraudNotification = false;
  public double dailyBalance = 0;

  // Used to notify of 25MB spend limits
  public double monthlyData = 0;
  public double monthlyDataOld = 0;
  public boolean notifyMonthlyData = false;
  public String custEmailAddress = "";

  // File reference - used for correllation in JB
  public String streamReference;

  // Event TimeStamp - used for order date handling and presentation in JB
  public String EventTS;

  // Used in duplicate checking
  public String linkedRecID;

  // The call scenario for the call - determines the roaming status, direction and termination mode
  public int callScenario;

  // Direction of the call: "Originating" or "Terminating"
  public String direction;

  // the record type of the call - determines the service
  public int recordType;

  // The type of CDR: MO, MT, POC, PTC
  public String cdrType;

  // The roaming status: National, Roam
  public boolean roamingMode = false;

  // Used for handling volumes in Radius
  public double volumeUplink;
  public double volumeDownlink;
  public double totalVolume;

  // MVNO Used for locating customer in Radius
  public String origMCCMNC;
  public String sgsnAddress;

  // MVNO roaming origin info
  public String Orig_Number;              // Origin Number
  public String Orig_Number_Normalised;   // Normalised version of the Origin Number
  public String origTierCode;             // The native tier code of the origin
  public String origDescription;          // The description of the origin
  public String origWorldZone;            // The world zone of the origin

  // MVNO Destination
  public String destTierCode;             // The native tier code of the destination
  public String destDescription;          // The description of the destination
  public String destWorldZone;            // The world zone of the destination
  public String discCategory;             // Destination category for discounting

  // Used for deciding whether we backout and/or rerate
  public String rerateOperation;

  // used for backout - stores the fields we got from the DB
  public String[] backoutInfo;

  /**
   * Default Constructor for RateRecord, creating the empty record container
   */
  public BaseRecord() {
    super();
  }

  /**
   * Map the header record
   *
   * @param InputData The input data to map
   */
  public void mapBaseHeaderRecord(String InputData) {
    OriginalData = InputData;
    RECORD_TYPE = BaseDefs.BASE_HEADER_TYPE;
  }

  /**
   * Map the trailer record
   *
   * @param InputData The input data to map
   */
  public void mapBaseTrailerRecord(String InputData) {
    OriginalData = InputData;
    RECORD_TYPE = BaseDefs.BASE_TRAILER_TYPE;
  }

  /**
   * Utility function to map a main detail record
   *
   * @param InputData The input data to map
   */
  public void mapBaseDetailRecord(String InputData) {
    SimpleDateFormat sdfInputOutput = new SimpleDateFormat("yyyyMMddHHmmss");
    RecordError tmpError;

    // For this application, it is enough to know that the record is
    RECORD_TYPE = BaseDefs.BASE_DETAIL_TYPE;
    OriginalData = InputData;

    // Detect recycle case, and remove header info
    if (OriginalData.startsWith(RECYCLE_TAG)) {
       // ERROR_CODE String(50) The error code of the error which caused rejection
      // RECYCLE_COUNT
      StringBuffer record = new StringBuffer(OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(BaseDefs.BASE_FIELD_SPLITTER) + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(BaseDefs.BASE_FIELD_SPLITTER) + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(1, record.indexOf(BaseDefs.BASE_FIELD_SPLITTER));
      recycleCount = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(BaseDefs.BASE_FIELD_SPLITTER) + 2);

      // reset the original data
      OriginalData = record.toString();
    }

    // We normally expect 42 fields, old format 41 fields
    if ((OriginalData.split(BaseDefs.BASE_FIELD_SPLITTER, -1).length == BaseDefs.BASE_DTL_RECORD_FIELD_COUNT) || // new format
        (OriginalData.split(BaseDefs.BASE_FIELD_SPLITTER, -1).length == BaseDefs.BASE_DTL_RECORD_FIELD_COUNT_8_92)) // Old format
    {
      fields = OriginalData.split(BaseDefs.BASE_FIELD_SPLITTER, -1);
    } else {
      // default the fields array - stops nasty exceptions later
      fields = new String[41];

      // return an error
      tmpError = new RecordError("ERR_RECORD_FORMAT_INVALID", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
      return;
    }

    // Check the record type
    if (getField(BaseDefs.BASE_DTL_RECORD_TYPE_IDX).equals("1") == false) {
      // Unsupported type
      tmpError = new RecordError("ERR_RECORD_NOT_SUPPORTED", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
      return;
    }

    // Manage the different call scenarios
    recordType = Integer.valueOf(getField(BaseDefs.BASE_DTL_RECORD_DETAIL_IDX));

    // perform the mapping of the service
    switch (recordType) {
      case BaseDefs.BASE_VOICE_CALL:
      case BaseDefs.BASE_CALLBACK:
      case BaseDefs.BASE_CONFERENCING: {
        Service = "TEL";
        break;
      }
      case BaseDefs.BASE_SMS: {
        Service = "SMS";
        break;
      }
      case BaseDefs.BASE_MMS: {
        Service = "SMS";
        break;
      }
      case BaseDefs.BASE_DATA_SESSION: {
        Service = "DATA";
        break;
      }
      default: {
        // Unsupported type - should not happen
        tmpError = new RecordError("ERR_SERVICE_NOT_SUPPORTED", ErrorType.DATA_VALIDATION);
        tmpError.setModuleName("BaseRecord");
        addError(tmpError);
      }
    }

    // Manage the different call scenarios
    callScenario = Integer.valueOf(getField(BaseDefs.BASE_DTL_CALL_MODE_IDX));

    // Controls number inversions
    boolean invert = false;

    // perform the mapping of the service
    switch (callScenario) {
      case BaseDefs.BASE_MODE_MO:
      case BaseDefs.BASE_MODE_FWD: {
        cdrType = "MO";
        break;
      }
      case BaseDefs.BASE_MODE_MT: {
        cdrType = "MT";
        break;
      }
      case BaseDefs.BASE_MODE_ROAM_MO:
      case BaseDefs.BASE_MODE_ROAM_FWD: {
        cdrType = "RO";
        break;
      }
      case BaseDefs.BASE_MODE_ROAM_MT: {
        cdrType = "RT";
        invert = true;
        break;
      }
      case BaseDefs.BASE_MODE_POC: {
        cdrType = "POC";
        break;
      }
      case BaseDefs.BASE_MODE_PTC: {
        cdrType = "PTC";
        invert = true;
        break;
      }
      default: {
        // Unsupported type - should not happen
        tmpError = new RecordError("ERR_MODE_NOT_SUPPORTED", ErrorType.DATA_VALIDATION);
        tmpError.setModuleName("BaseRecord");
        addError(tmpError);
      }
    }

    // Deal with PTC from Ergatel - Identified by CLI blank and non mobile number in Orig point
    if ((cdrType.equals("PTC")) && (getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX).isEmpty())
            && (getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX).startsWith("32")) && (getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX).length() == 10)) {
      // This is a re-routed call - discard
      tmpError = new RecordError("ERR_MOBILE_REROUTE", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
    }

    // take care of number inversions
    if (invert) {
      if (getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX).isEmpty()) {
        A_Number = getField(BaseDefs.BASE_DTL_DESTINATION_POINT_IDX);
      } else {
        A_Number = "00" + getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX);
      }

      if (getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX).startsWith("00")) {
        B_Number = getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX);
      } else {
        B_Number = "00" + getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX);
      }
      Orig_Number = getField(BaseDefs.BASE_DTL_DESTINATION_POINT_IDX);

      // Change the prefix for account lookup
      guidingKey = A_Number.replaceAll("0032", "0");
    } else {
      A_Number = "00" + getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX);
      B_Number = getField(BaseDefs.BASE_DTL_DESTINATION_POINT_IDX);
      Orig_Number = getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX);

      if (A_Number.equals("00")) {
        A_Number = Orig_Number;
      }

      // Change the prefix for account lookup
      guidingKey = A_Number.replaceAll("0032", "0");
    }

    // deal with short numbers
    if (B_Number.length() == 6) {
      // Turn it back into a short number
      B_Number = B_Number.replaceAll("^00", "");
    }

    // deal with missing numbers from roaming cases
    if (B_Number.equals("00")) {
      // Put in a default number (but not 0032, because that causes leg detection)
      B_Number = "003240000000";
    }

    String DurationStr = getField(BaseDefs.BASE_DTL_SESSION_DURATION_IDX);
    String SizeStr = getField(BaseDefs.BASE_DTL_SESSION_SIZE_IDX);
    String Start_DateTime = getField(BaseDefs.BASE_DTL_DATETIME_START_IDX);

    // Parse the event start date
    try {
      EventStartDate = sdfInputOutput.parse(Start_DateTime);
      UTCEventDate = EventStartDate.getTime() / 1000;
      EventTS = sdfInputOutput.format(EventStartDate);
    } catch (ParseException ex) {
      tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
    }

    // Parse the size
    try {
      Volume = Double.parseDouble(SizeStr);
    } catch (NumberFormatException nfe) {
      Volume = 0;
      tmpError = new RecordError("ERR_SIZE_INVALID", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
    }

    // Parse the duration
    try {
      Duration = Double.parseDouble(DurationStr);
    } catch (NumberFormatException nfe) {
      Duration = 0;
      tmpError = new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("BaseRecord");
      addError(tmpError);
    }

    // Get the event end date
    EventEndDate = new Date(EventStartDate.getTime() + (int) Duration * 1000);

    // Set the chargeable flag for the dump
    ChargeableRecord = true;

    // used as a primary key
    linkedRecID = getField(BaseDefs.BASE_DTL_LINKED_RECORD_ID_IDX);

    // Get the wholesale price
    double wholesalePrice = Double.valueOf(getField(BaseDefs.BASE_DTL_PRICE_IDX));

    // Set the RUM Values
    setRUMValue("WEUR", wholesalePrice);
    setRUMValue("DUR", Duration);
    setRUMValue("VOL", Volume);
    setRUMValue("EVT", 1);
  }

  /**
   * Utility function to unmap a detail record for sending to jBilling
   *
   * @return
   */
  public String unmapBaseDetail() {
    // Round
    NumberFormat formatter = new DecimalFormat("0.0000");

    StringBuilder recordData = new StringBuilder();
    recordData.append(JBUserId);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    // Use the technology indicator to suppress order loading in JBilling
    if (rerate) {
      switch (Service) {
        case "SMS":
          recordData.append("KM"); // technology indicator - KPN Base SMS Rerate
          break;
        case "TEL":
          recordData.append("KE"); // technology indicator - KPN Base Telephony Rerate
          break;
        case "DATA":
          recordData.append("KA"); // technology indicator - KPN Base Data Rerate
          break;
      }
      recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
      recordData.append(backoutInfo[BaseDefs.IDX_RR_id]); // used for linking records in re-rating case
    } else {
      switch (Service) {
        case "SMS":
          recordData.append("KS"); // technology indicator - KPN Base SMS
          break;
        case "TEL":
          recordData.append("KT"); // technology indicator - KPN Base Telephony
          break;
        case "DATA":
          recordData.append("KD"); // technology indicator - KPN Base Data
          break;
      }
      recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
      recordData.append("0");
    }
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(Category);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(InvDestination);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(BaseTariff);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(UserTariff);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(formatter.format(RatedAmount));
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(0); // OverrideRatedAmount
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append("0"); // User Override
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(UsedDiscount);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(formatter.format(DiscountGranted));
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(DiscountRule);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(DiscountRUM);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(CounterCycle);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(RecId);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // recovered BCN
    recordData.append(recoveredBCN);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // calculated A and B Numbers
    recordData.append(guidingKey);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    recordData.append(B_Number);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);
    // calculated amount
    switch (this.Service) {
      case "TEL":
        recordData.append(Duration);
        break;
      case "SMS":
        recordData.append("1");
        break;
      case "DATA":
        recordData.append(Volume);
        break;
    }
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // CDR Date YYYYMMDDhhmmss
    recordData.append(EventTS);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // File Reference
    recordData.append(streamReference);
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // linked record ID - primary key
    if (rerate) {
      recordData.append(linkedRecID).append("-").append(streamReference);
    } else {
      recordData.append(linkedRecID);
    }
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // Balance updates
    for (int balCnt = 0; balCnt < getBalanceImpactCount(); balCnt++) {
      BalanceImpact tmpBalImp = getBalanceImpact(balCnt);

      if (balCnt > 0) {
        recordData.append(BaseDefs.BASE_BALIMP_SPLITTER);
      }
      recordData.append(tmpBalImp.balanceGroup).append("|");
      recordData.append(tmpBalImp.counterID).append("|");
      recordData.append(tmpBalImp.startDate).append("|");
      recordData.append(tmpBalImp.endDate).append("|");
      recordData.append(tmpBalImp.balanceDelta).append("|");
      recordData.append(tmpBalImp.ruleName);
    }
    recordData.append(BaseDefs.BASE_FIELD_SPLITTER);

    // Output original fields
    for (int idx = 0; idx < fields.length; idx++) {
      if (idx == fields.length - 1) {
        recordData.append(fields[idx]);
      } else {
        recordData.append(fields[idx]).append("|");
      }
    }

    return recordData.toString();
  }

  /**
   * Utility function to map a file trailer record
   *
   * @param newOriginalData The input data to map
   */
  public void mapHeader(String newOriginalData) {
    OriginalData = newOriginalData;
    RECORD_TYPE = 10;
  }

  /**
   * Utility function to map a file trailer record
   *
   * @param newOriginalData The input data to map
   */
  public void mapTrailer(String newOriginalData) {
    OriginalData = newOriginalData;
    RECORD_TYPE = 90;
  }

  /**
   * Return the dump-ready data
   *
   * @return The dump info string
   */
  @Override
  public ArrayList<String> getDumpInfo() {
    ArrayList<String> tmpDumpList;
    tmpDumpList = new ArrayList<>();

    // Perform for detail record types
    if (RECORD_TYPE == BaseDefs.BASE_DETAIL_TYPE) {
      // Format the fields
      tmpDumpList.add("============ BEGIN RECORD ============");
      tmpDumpList.add("  Record Number           = <" + RecordNumber + ">");
      tmpDumpList.add("  original record         = <" + OriginalData + ">");
      tmpDumpList.add("  Output Stream           = <" + getOutputs().toString() + ">");
      tmpDumpList.add("  Recycle Count           = <" + recycleCount + ">");
      tmpDumpList.add("--------------------------------------");

      tmpDumpList.add("  RECORD_ID               = <" + getField(BaseDefs.BASE_DTL_RECORD_ID_IDX) + ">");
      tmpDumpList.add("  RECORD_TYPE             = <" + getField(BaseDefs.BASE_DTL_RECORD_TYPE_IDX) + ">");
      tmpDumpList.add("  RECORD_DETAIL           = <" + getField(BaseDefs.BASE_DTL_RECORD_DETAIL_IDX) + ">");
      tmpDumpList.add("  LINKED_RECORD_ID        = <" + getField(BaseDefs.BASE_DTL_LINKED_RECORD_ID_IDX) + ">");
      tmpDumpList.add("  LAST_EXPORTED_DATETIME  = <" + getField(BaseDefs.BASE_DTL_LAST_EXPORTED_DATETIME_IDX) + ">");
      tmpDumpList.add("  ORIGINATING_CLI         = <" + getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX) + ">");
      tmpDumpList.add("  ORIGINATING_POINT       = <" + getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX) + ">");
      tmpDumpList.add("  DESTINATION_POINT       = <" + getField(BaseDefs.BASE_DTL_DESTINATION_POINT_IDX) + ">");
      tmpDumpList.add("  NETWORK_GROUP           = <" + getField(BaseDefs.BASE_DTL_NETWORK_GROUP_IDX) + ">");

      tmpDumpList.add("  NETWORK_SUBGROUP        = <" + getField(BaseDefs.BASE_DTL_NETWORK_SUBGROUP_IDX) + ">");
      tmpDumpList.add("  LOCATION_ID             = <" + getField(BaseDefs.BASE_DTL_LOCATION_ID_IDX) + ">");
      tmpDumpList.add("  CELL_ID                 = <" + getField(BaseDefs.BASE_DTL_CELL_ID_IDX) + ">");
      tmpDumpList.add("  DETAIL_1                = <" + getField(BaseDefs.BASE_DTL_DETAIL_1_IDX) + ">");
      tmpDumpList.add("  DETAIL_2                = <" + getField(BaseDefs.BASE_DTL_DETAIL_2_IDX) + ">");
      tmpDumpList.add("  TIMEZONE                = <" + getField(BaseDefs.BASE_DTL_TIMEZONE_IDX) + ">");
      tmpDumpList.add("  CALL_MODE               = <" + getField(BaseDefs.BASE_DTL_CALL_MODE_IDX) + ">");
      tmpDumpList.add("  DATETIME_START          = <" + getField(BaseDefs.BASE_DTL_DATETIME_START_IDX) + ">");
      tmpDumpList.add("  DATETIME_END            = <" + getField(BaseDefs.BASE_DTL_DATETIME_END_IDX) + ">");
      tmpDumpList.add("  SESSION_DURATION        = <" + getField(BaseDefs.BASE_DTL_SESSION_DURATION_IDX) + ">");
      tmpDumpList.add("  SESSION_SIZE            = <" + getField(BaseDefs.BASE_DTL_SESSION_SIZE_IDX) + ">");
      tmpDumpList.add("  EVENTS                  = <" + getField(BaseDefs.BASE_DTL_EVENTS_IDX) + ">");
      tmpDumpList.add("  BUNDLE_ID               = <" + getField(BaseDefs.BASE_DTL_BUNDLE_ID_IDX) + ">");
      tmpDumpList.add("  BUNDLE_UNITS_DEFINTION  = <" + getField(BaseDefs.BASE_DTL_BUNDLE_UNITS_DEFINTION_IDX) + ">");
      tmpDumpList.add("  BUNDLE_UNITS_USAGE      = <" + getField(BaseDefs.BASE_DTL_BUNDLE_UNITS_USAGE_IDX) + ">");
      tmpDumpList.add("  TARIFF_ID               = <" + getField(BaseDefs.BASE_DTL_TARIFF_ID_IDX) + ">");
      tmpDumpList.add("  PACKAGE_ID              = <" + getField(BaseDefs.BASE_DTL_PACKAGE_ID_IDX) + ">");
      tmpDumpList.add("  REGION_ID               = <" + getField(BaseDefs.BASE_DTL_REGION_ID_IDX) + ">");
      tmpDumpList.add("  PRICE_TYPE              = <" + getField(BaseDefs.BASE_DTL_PRICE_TYPE_IDX) + ">");
      tmpDumpList.add("  SETUP_FEE               = <" + getField(BaseDefs.BASE_DTL_SETUP_FEE_IDX) + ">");
      tmpDumpList.add("  PRICE                   = <" + getField(BaseDefs.BASE_DTL_PRICE_IDX) + ">");
      tmpDumpList.add("  TAX                     = <" + getField(BaseDefs.BASE_DTL_TAX_IDX) + ">");
      tmpDumpList.add("  END_BALANCE             = <" + getField(BaseDefs.BASE_DTL_END_BALANCE_IDX) + ">");
      tmpDumpList.add("  EXTERNAL_REFERENCE_ID   = <" + getField(BaseDefs.BASE_DTL_EXTERNAL_REFERENCE_ID_IDX) + ">");
      tmpDumpList.add("  PREMIUM_NUMBER          = <" + getField(BaseDefs.BASE_DTL_PREMIUM_NUMBER_IDX) + ">");
      tmpDumpList.add("  PREMIUM_SERVICE_DESCR   = <" + getField(BaseDefs.BASE_DTL_PREMIUM_SERVICE_DESCR_IDX) + ">");
      tmpDumpList.add("  PROVIDER_NAME           = <" + getField(BaseDefs.BASE_DTL_PROVIDER_NAME_IDX) + ">");
      tmpDumpList.add("  CUSTOMER_CARE_CONTACT   = <" + getField(BaseDefs.BASE_DTL_CUSTOMER_CARE_CONTACT_IDX) + ">");
      tmpDumpList.add("  CLIP_PRESENTATION_IND   = <" + getField(BaseDefs.BASE_DTL_CLIP_PRESENTATION_IND_IDX) + ">");
      tmpDumpList.add("  EXTERNAL_ID             = <" + getField(BaseDefs.BASE_DTL_EXTERNAL_ID_IDX) + ">");

      // deal with the transition time - only output the fields if they are there
      if (this.fields.length >= BaseDefs.BASE_DTL_SIM_IDX) {
        tmpDumpList.add("  IMSI                    = <" + getField(BaseDefs.BASE_DTL_IMSI_IDX) + ">");
        tmpDumpList.add("  SIM                     = <" + getField(BaseDefs.BASE_DTL_SIM_IDX) + ">");
      }
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  Service                 = <" + Service + ">");
      tmpDumpList.add("  CDR Type                = <" + cdrType + ">");
      tmpDumpList.add("  Guiding Key             = <" + guidingKey + ">");
      tmpDumpList.add("  A_Number                = <" + A_Number + ">");
      tmpDumpList.add("  B_Number                = <" + B_Number + ">");
      tmpDumpList.add("  Orig_Number             = <" + Orig_Number + ">");
      tmpDumpList.add("  B_Number Norm           = <" + B_Number_Normalised + ">");
      tmpDumpList.add("  Orig_Number Norm        = <" + Orig_Number_Normalised + ">");
      tmpDumpList.add("  Roaming Mode            = <" + roamingMode + ">");
      tmpDumpList.add("  Direction               = <" + direction + ">");
      tmpDumpList.add("  OrigZone                = <" + origTierCode + ">");
      tmpDumpList.add("  OrigDescription         = <" + origDescription + ">");
      tmpDumpList.add("  Orig World Zone         = <" + origWorldZone + ">");
      tmpDumpList.add("  DestZone                = <" + destTierCode + ">");
      tmpDumpList.add("  DestDescription         = <" + destDescription + ">");
      tmpDumpList.add("  Dest World Zone         = <" + destWorldZone + ">");
      tmpDumpList.add("  Discount Category       = <" + discCategory + ">");
      tmpDumpList.add("  Duration                = <" + Duration + ">");
      tmpDumpList.add("  Volume                  = <" + Volume + ">");
      tmpDumpList.add("  Category                = <" + Category + ">");
      tmpDumpList.add("  Invoice Description     = <" + InvDestination + ">");
      tmpDumpList.add("  BCN                     = <" + recoveredBCN + ">");
      tmpDumpList.add("  JB User Id              = <" + JBUserId + ">");
      tmpDumpList.add("  JB Login                = <" + Login + ">");
      tmpDumpList.add("  Rated Amount            = <" + RatedAmount + ">");
      tmpDumpList.add("  Discount Amount         = <" + DiscountGranted + ">");
      tmpDumpList.add("  Monthly Data Spend      = <" + monthlyData + ">");
      tmpDumpList.add("  Prev Monthly Data Spend = <" + monthlyDataOld + ">");
      tmpDumpList.add("  Monthly Data Notif      = <" + notifyMonthlyData + ">");
      tmpDumpList.add("  Cust Email Address      = <" + custEmailAddress + ">");

      if (ChargeableRecord) {
        tmpDumpList.add("--------------------------------------");

        // Add charge packets
        tmpDumpList.addAll(getChargePacketsDump(26));
      }

      // Add balance impacts
      tmpDumpList.addAll(getBalanceImpactsDump(26));

      // Add errors
      tmpDumpList.addAll(getErrorDump(26));

      tmpDumpList.add("============ END RECORD ============");
    } else if (RECORD_TYPE == BaseDefs.BASE_HEADER_TYPE) {
      tmpDumpList.add("============ BEGIN HEADER ============");
      tmpDumpList.add("  original record      = <" + OriginalData + ">");
    } else if (RECORD_TYPE == BaseDefs.BASE_TRAILER_TYPE) {
      tmpDumpList.add("============ BEGIN TRAILER ===========");
      tmpDumpList.add("  original record      = <" + OriginalData + ">");
    } else if (RECORD_TYPE == BaseDefs.BASE_BACKOUT_TYPE) {
      // Format the fields
      tmpDumpList.add("============ BEGIN RECORD ============");
      tmpDumpList.add("  Record Number           = <" + RecordNumber + ">");
      tmpDumpList.add("  original record         = <" + OriginalData + ">");
      tmpDumpList.add("  Output Stream           = <" + getOutputs().toString() + ">");
      tmpDumpList.add("--------------------------------------");

      tmpDumpList.add("  RECORD_ID               = <" + getField(BaseDefs.BASE_DTL_RECORD_ID_IDX) + ">");
      tmpDumpList.add("  RECORD_TYPE             = <" + getField(BaseDefs.BASE_DTL_RECORD_TYPE_IDX) + ">");
      tmpDumpList.add("  RECORD_DETAIL           = <" + getField(BaseDefs.BASE_DTL_RECORD_DETAIL_IDX) + ">");
      tmpDumpList.add("  LINKED_RECORD_ID        = <" + getField(BaseDefs.BASE_DTL_LINKED_RECORD_ID_IDX) + ">");
      tmpDumpList.add("  LAST_EXPORTED_DATETIME  = <" + getField(BaseDefs.BASE_DTL_LAST_EXPORTED_DATETIME_IDX) + ">");
      tmpDumpList.add("  ORIGINATING_CLI         = <" + getField(BaseDefs.BASE_DTL_ORIGINATING_CLI_IDX) + ">");
      tmpDumpList.add("  ORIGINATING_POINT       = <" + getField(BaseDefs.BASE_DTL_ORIGINATING_POINT_IDX) + ">");
      tmpDumpList.add("  DESTINATION_POINT       = <" + getField(BaseDefs.BASE_DTL_DESTINATION_POINT_IDX) + ">");
      tmpDumpList.add("  NETWORK_GROUP           = <" + getField(BaseDefs.BASE_DTL_NETWORK_GROUP_IDX) + ">");
      tmpDumpList.add("  NETWORK_SUBGROUP        = <" + getField(BaseDefs.BASE_DTL_NETWORK_SUBGROUP_IDX) + ">");
      tmpDumpList.add("  LOCATION_ID             = <" + getField(BaseDefs.BASE_DTL_LOCATION_ID_IDX) + ">");
      tmpDumpList.add("  CELL_ID                 = <" + getField(BaseDefs.BASE_DTL_CELL_ID_IDX) + ">");
      tmpDumpList.add("  DETAIL_1                = <" + getField(BaseDefs.BASE_DTL_DETAIL_1_IDX) + ">");
      tmpDumpList.add("  DETAIL_2                = <" + getField(BaseDefs.BASE_DTL_DETAIL_2_IDX) + ">");
      tmpDumpList.add("  TIMEZONE                = <" + getField(BaseDefs.BASE_DTL_TIMEZONE_IDX) + ">");
      tmpDumpList.add("  CALL_MODE               = <" + getField(BaseDefs.BASE_DTL_CALL_MODE_IDX) + ">");
      tmpDumpList.add("  DATETIME_START          = <" + getField(BaseDefs.BASE_DTL_DATETIME_START_IDX) + ">");
      tmpDumpList.add("  DATETIME_END            = <" + getField(BaseDefs.BASE_DTL_DATETIME_END_IDX) + ">");
      tmpDumpList.add("  SESSION_DURATION        = <" + getField(BaseDefs.BASE_DTL_SESSION_DURATION_IDX) + ">");
      tmpDumpList.add("  SESSION_SIZE            = <" + getField(BaseDefs.BASE_DTL_SESSION_SIZE_IDX) + ">");
      tmpDumpList.add("  EVENTS                  = <" + getField(BaseDefs.BASE_DTL_EVENTS_IDX) + ">");
      tmpDumpList.add("  BUNDLE_ID               = <" + getField(BaseDefs.BASE_DTL_BUNDLE_ID_IDX) + ">");
      tmpDumpList.add("  BUNDLE_UNITS_DEFINTION  = <" + getField(BaseDefs.BASE_DTL_BUNDLE_UNITS_DEFINTION_IDX) + ">");
      tmpDumpList.add("  BUNDLE_UNITS_USAGE      = <" + getField(BaseDefs.BASE_DTL_BUNDLE_UNITS_USAGE_IDX) + ">");
      tmpDumpList.add("  TARIFF_ID               = <" + getField(BaseDefs.BASE_DTL_TARIFF_ID_IDX) + ">");
      tmpDumpList.add("  PACKAGE_ID              = <" + getField(BaseDefs.BASE_DTL_PACKAGE_ID_IDX) + ">");
      tmpDumpList.add("  REGION_ID               = <" + getField(BaseDefs.BASE_DTL_REGION_ID_IDX) + ">");
      tmpDumpList.add("  PRICE_TYPE              = <" + getField(BaseDefs.BASE_DTL_PRICE_TYPE_IDX) + ">");
      tmpDumpList.add("  SETUP_FEE               = <" + getField(BaseDefs.BASE_DTL_SETUP_FEE_IDX) + ">");
      tmpDumpList.add("  PRICE                   = <" + getField(BaseDefs.BASE_DTL_PRICE_IDX) + ">");
      tmpDumpList.add("  TAX                     = <" + getField(BaseDefs.BASE_DTL_TAX_IDX) + ">");
      tmpDumpList.add("  END_BALANCE             = <" + getField(BaseDefs.BASE_DTL_END_BALANCE_IDX) + ">");
      tmpDumpList.add("  EXTERNAL_REFERENCE_ID   = <" + getField(BaseDefs.BASE_DTL_EXTERNAL_REFERENCE_ID_IDX) + ">");
      tmpDumpList.add("  PREMIUM_NUMBER          = <" + getField(BaseDefs.BASE_DTL_PREMIUM_NUMBER_IDX) + ">");
      tmpDumpList.add("  PREMIUM_SERVICE_DESCR   = <" + getField(BaseDefs.BASE_DTL_PREMIUM_SERVICE_DESCR_IDX) + ">");
      tmpDumpList.add("  PROVIDER_NAME           = <" + getField(BaseDefs.BASE_DTL_PROVIDER_NAME_IDX) + ">");
      tmpDumpList.add("  CUSTOMER_CARE_CONTACT   = <" + getField(BaseDefs.BASE_DTL_CUSTOMER_CARE_CONTACT_IDX) + ">");
      tmpDumpList.add("  CLIP_PRESENTATION_IND   = <" + getField(BaseDefs.BASE_DTL_CLIP_PRESENTATION_IND_IDX) + ">");
      tmpDumpList.add("  EXTERNAL_ID             = <" + getField(BaseDefs.BASE_DTL_EXTERNAL_ID_IDX) + ">");

      // deal with the transition time - only output the fields if they are there
      if (this.fields.length >= BaseDefs.BASE_DTL_SIM_IDX) {
        tmpDumpList.add("  IMSI                    = <" + getField(BaseDefs.BASE_DTL_IMSI_IDX) + ">");
        tmpDumpList.add("  SIM                     = <" + getField(BaseDefs.BASE_DTL_SIM_IDX) + ">");
      }
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  Service                 = <" + Service + ">");
      tmpDumpList.add("  CDR Type                = <" + cdrType + ">");
      tmpDumpList.add("  Guiding Key             = <" + guidingKey + ">");
      tmpDumpList.add("  A_Number                = <" + A_Number + ">");
      tmpDumpList.add("  B_Number                = <" + B_Number + ">");
      tmpDumpList.add("  Orig_Number             = <" + Orig_Number + ">");
      tmpDumpList.add("  JB User Id              = <" + JBUserId + ">");
      tmpDumpList.add("  Rated Amount            = <" + RatedAmount + ">");
      tmpDumpList.add("  Discount Amount         = <" + DiscountGranted + ">");

      // Add balance impacts
      tmpDumpList.addAll(getBalanceImpactsDump(26));

      // Add errors
      tmpDumpList.addAll(getErrorDump(26));

      tmpDumpList.add("============ END RECORD ============");
    }

    return tmpDumpList;
  }
}
