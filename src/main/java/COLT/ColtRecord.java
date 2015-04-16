/* ====================================================================
 * Limited Evaluation License:
 *
 * This software is open source, but licensed. The license with this package
 * is an evaluation license, which may not be used for productive systems. If
 * you want a full license, please contact us.
 *
 * The exclusive owner of this work is the OpenRate project.
 * This work, including all associated documents and components
 * is Copyright of the OpenRate project 2006-2014.
 *
 * The following restrictions apply unless they are expressly relaxed in a
 * contractual agreement between the license holder or one of its officially
 * assigned agents and you or your organisation:
 *
 * 1) This work may not be disclosed, either in full or in part, in any form
 *    electronic or physical, to any third party. This includes both in the
 *    form of source code and compiled modules.
 * 2) This work contains trade secrets in the form of architecture, algorithms
 *    methods and technologies. These trade secrets may not be disclosed to
 *    third parties in any form, either directly or in summary or paraphrased
 *    form, nor may these trade secrets be used to construct products of a
 *    similar or competing nature either by you or third parties.
 * 3) This work may not be included in full or in part in any application.
 * 4) You may not remove or alter any proprietary legends or notices contained
 *    in or on this work.
 * 5) This software may not be reverse-engineered or otherwise decompiled, if
 *    you received this work in a compiled form.
 * 6) This work is licensed, not sold. Possession of this software does not
 *    imply or grant any right to you.
 * 7) You agree to disclose any changes to this work to the copyright holder
 *    and that the copyright holder may include any such changes at its own
 *    discretion into the work
 * 8) You agree not to derive other works from the trade secrets in this work,
 *    and that any such derivation may make you liable to pay damages to the
 *    copyright holder
 * 9) You agree to use this software exclusively for evaluation purposes, and
 *    that you shall not use this software to derive commercial profit or
 *    support your business or personal activities.
 *
 * This software is provided "as is" and any expressed or impled warranties,
 * including, but not limited to, the impled warranties of merchantability
 * and fitness for a particular purpose are disclaimed. In no event shall
 * The OpenRate Project or its officially assigned agents be liable to any
 * direct, indirect, incidental, special, exemplary, or consequential damages
 * (including but not limited to, procurement of substitute goods or services;
 * Loss of use, data, or profits; or any business interruption) however caused
 * and on theory of liability, whether in contract, strict liability, or tort
 * (including negligence or otherwise) arising in any way out of the use of
 * this software, even if advised of the possibility of such damage.
 * This software contains portions by The Apache Software Foundation, Robert
 * Half International.
 * ====================================================================
 */
package COLT;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import OpenRate.process.AbstractRUMTimeMatch;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.RecordError;
import OpenRate.record.TimePacket;
import escaux.EscauxRecord;

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
 * unmapOriginalData() [mandatory if you wish to write output files]
 * ------------------- Transformation from a formatted record to a flat record
 * ready for output.
 *
 * getDumpInfo() [optional] ------------- Preparation of the dump equivalent of
 * the formatted record, ready for dumping out to a dump file. The
 * OpenRate.process.Dump module uses this information.
 *
 * In this simple example, we require only to read the "B-Number", and write the
 * "Destination" as a result of this. Because of the simplicity of the example
 * we do not perform a full mapping, we just handle the fields we want directly,
 * which is one of the advantages of the BBPA model (map as much as you want or
 * as little as you have to).
 *
 */
public final class ColtRecord extends EscauxRecord {

  private static final long serialVersionUID = 1L;
  /*
   #0 BCN (supplier contract)
   #1 Invoice number
   #2 Local area code of Subscription
   #3 Subscription without local area code
   #4 Origin number including extension without leading 0
   #5 Date
   #6 Start time including hundreds of second
   #7 Name of destination
   #8 Band
   #9 Dialed Number
   #10 Duration in seconds
   #11 Invoice amount
   #12 Discount
   #13 Invoice Date
   #14 Currency
   #15 Destination number
   #16 Call Type (Service Class)
   #17 Tariff Class
   #18 Origin in case of IN Calls
   #19 IN SAN
   */
  public static final int BCN_IDX = 0;
  public static final int INVOICE_IDX = 1;
  public static final int SUBSCRIBER_AREA_IDX = 2;
  public static final int SUBSCRIBER_LOCAL_IDX = 3;
  public static final int ORIGIN_NUMBER_IDX = 4;
  public static final int DATE_IDX = 5;
  public static final int TIME_IDX = 6;
  public static final int DESTINATION_NAME_IDX = 7;
  public static final int BAND_IDX = 8;
  public static final int DIALED_NUMBER_IDX = 9;
  public static final int DURATION_SECS_IDX = 10;
  public static final int INVOICE_AMOUNT_IDX = 11;
  public static final int DISCOUNT_IDX = 12;
  public static final int INVOICE_DATE_IDX = 13;
  public static final int CURRENCY_IDX = 14;
  public static final int DESTINATION_NUMBER_IDX = 15;
  public static final int CALL_TYPE_IDX = 16;
  public static final int TARIFF_CLASS_IDX = 17;
  public static final int IN_ORIGIN_IDX = 18;
  public static final int IN_SAN_IDX = 19;

  /**
   * Default Constructor for SimpleRecord.
   */
  public ColtRecord() {
    super();
//    month = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    month.setLenient(false);
  }

  /**
   * Overloaded Constructor for SimpleRecord.
   *
   * @param OriginalData - the flat record we are to map
   *
   */
  public ColtRecord(String OriginalData) {
    super(OriginalData);
//    month = new SimpleDateFormat("yyyy-MM");
//    month.setLenient(false);
  }

  public void mapData() {
    // Set the recoprd type - this helps us separate headers/trailers 
    // and different record types later
    this.RECORD_TYPE = DETAIL_RECORD;

    // Store the original information
    this.fields = this.getOriginalData().split(";");

    subscriberNumber = getField(SUBSCRIBER_AREA_IDX) + getField(SUBSCRIBER_LOCAL_IDX);

    dialedNumber = getField(DIALED_NUMBER_IDX);
    /*   
     try {
     month.parse(getField(DATE_IDX));
     } catch (ParseException e) {
     addError(new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION));
     }
     */
    billsec = Integer.parseInt(getField(DURATION_SECS_IDX));
    callType = getField(CALL_TYPE_IDX);

    // Get the event date
    SimpleDateFormat sdfInput = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    try {
      EventStartDate = sdfInput.parse(getField(DATE_IDX) + " " + getField(TIME_IDX));
      UTCEventDate = EventStartDate.getTime() / 1000;
    } catch (ParseException ex) {
      RecordError tmpError = new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION);
      tmpError.setModuleName("ErgatelRecord");
      addError(tmpError);
    }

    // Set the Duration RUM
    setRUMValue("DUR", billsec);
    setRUMValue("EVT", billsec);

    // Add a charge packet
    ChargePacket tmpCP = new ChargePacket();

    // Mark the charge packet as a "Retail" packet
    tmpCP.packetType = "R";

    // Set the zone model - Assuming we have a standard zone model
    tmpCP.zoneModel = "Default";

    // We need this for the PriceLookup
    tmpCP.ratePlanName = "Default";

    // Set the service to something reasonable
    tmpCP.service = "TEL";

    // Set the time model AND the result (we will skip time lookup at the moment)
    tmpCP.timeModel = "Default";
    tmpCP.timeSplitting = AbstractRUMTimeMatch.TIME_SPLITTING_NO_CHECK;

    // Set up the time packet as if we did the time lookup (just enough to make it work)
    TimePacket tmpTZ = new TimePacket();
    tmpTZ.timeModel = "Default";
    tmpTZ.timeResult = "FLAT";

    // Put this in the record
    tmpCP.addTimeZone(tmpTZ);
    addChargePacket(tmpCP);
  }

  /**
   * Reconstruct the record from the field values, replacing the original
   * structure of tab separated records
   *
   * @return The unmapped original data
   */
  public String unmapOriginalData() {

    int i;
    StringBuilder buf = new StringBuilder(1024);

    /*write the destination information back
     this.setField(ACCOUNT_IDX, Account);
     this.setField(DESTINATION_IDX, Destination);
     */
    buf.append(this.fields[0]);
    for (i = 1; i < this.fields.length; i++) {
      buf.append(";");
      buf.append(this.fields[i]);
    }
    buf.append(";");
    buf.append(this.dialedNumberNorm);
    buf.append(";");
    buf.append(this.destination);
    buf.append(";");
    buf.append(this.account);
    buf.append(";");
    buf.append(this.ratedAmount);
    if (this.isErrored()) {
      buf.append(";");
      buf.append(this.getErrors().get(0).getMessage());
    }
    return buf.toString();
  }
}
