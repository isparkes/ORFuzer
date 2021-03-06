package escaux;

import OpenRate.lang.DiscountInformation;
import OpenRate.process.AbstractBalanceHandlerPlugIn;
import OpenRate.record.IRecord;
import OpenRate.utils.ConversionUtils;

/**
 * Now that we have the prioritised list of products and promotions, we ca work
 * out the consuming of the balances that there might be, before we pass into
 * rating the values of what is left after consumption. This will decrement
 * balances, passing the results on for rating.
 */
public class PromoCalcPreRating extends AbstractBalanceHandlerPlugIn {

  // Used for calculating the validites of the counters
  private final ConversionUtils conversionUtils = new ConversionUtils();

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    EscauxRecord CurrentRecord;
    DiscountInformation tmpDiscInfo;

    CurrentRecord = (EscauxRecord) r;

    // Used for balance creation
    long tmpStartDate = 0;
    long tmpEndDate = 0;

    if (CurrentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
      // No discounts found
      if (CurrentRecord.discountRule == null || CurrentRecord.discountRule.isEmpty()) {
        // no work to do - go home
        return r;
      }

      // ****************************** MIN **********************************
      if ((CurrentRecord.discountRUM.equals("MIN")) && (CurrentRecord.getRUMValue("MIN") > 0)) {
        // Create the daily or monthly balance
        if (CurrentRecord.discountPeriod.equalsIgnoreCase("m")) {
          tmpStartDate = conversionUtils.getUTCMonthStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCMonthEnd(CurrentRecord.EventStartDate);
        } else if (CurrentRecord.discountPeriod.equalsIgnoreCase("d")) {
          tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
        }

        // perform the discount
        tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.discountRule, CurrentRecord.balanceGroup, "MIN", CurrentRecord.discountCounter, CurrentRecord.discountInitValue, tmpStartDate, tmpEndDate);

        // put the info back in the Record
        if (tmpDiscInfo.isDiscountApplied()) {
          CurrentRecord.discountApplied = true;
          CurrentRecord.discountRecId = tmpDiscInfo.getRecId();
          CurrentRecord.discountGranted += tmpDiscInfo.getDiscountedValue();

          /* There is no need to manually discount anything, the OpenRate engine already does it! */
        }
      }

      // *************************** Volume *******************************
      if ((CurrentRecord.discountRUM.equals("SIZE")) && (CurrentRecord.getRUMValue("SIZE") > 0)) {
        if (CurrentRecord.discountPeriod.equalsIgnoreCase("m")) {	
          tmpStartDate = conversionUtils.getUTCMonthStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCMonthEnd(CurrentRecord.EventStartDate);
        } else if (CurrentRecord.discountPeriod.equalsIgnoreCase("d")) {
          tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
        }

        // perform the discount
        tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.discountRule, CurrentRecord.balanceGroup, "SIZE", CurrentRecord.discountCounter, CurrentRecord.discountInitValue, tmpStartDate, tmpEndDate);
        // put the info back in the Record
        if (tmpDiscInfo.isDiscountApplied()) {
          CurrentRecord.discountApplied = true;
          CurrentRecord.discountRecId = tmpDiscInfo.getRecId();
          CurrentRecord.discountGranted += tmpDiscInfo.getDiscountedValue();
          /* There is no need to manually discount anything, the OpenRate engine already does it! */
        }
      }
      
      // ******************************** DUR **********************************
      if ((CurrentRecord.discountRUM.equals("DUR")) && (CurrentRecord.getRUMValue("DUR") > 0)) {
        if (CurrentRecord.discountPeriod.equalsIgnoreCase("m")) {
          tmpStartDate = conversionUtils.getUTCMonthStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCMonthEnd(CurrentRecord.EventStartDate);
        } else if (CurrentRecord.discountPeriod.equalsIgnoreCase("d")) {
          tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
        }

        // perform the discount
        tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.discountRule, CurrentRecord.balanceGroup, "DUR", CurrentRecord.discountCounter, CurrentRecord.discountInitValue, tmpStartDate, tmpEndDate);

        // put the info back in the Record
        if (tmpDiscInfo.isDiscountApplied()) {
          CurrentRecord.discountApplied = true;
          CurrentRecord.discountRecId = tmpDiscInfo.getRecId();
          CurrentRecord.discountGranted += tmpDiscInfo.getDiscountedValue();
        }
      }

      // ******************************** EVT **********************************
      if ((CurrentRecord.discountRUM.equals("EVT")) && (CurrentRecord.getRUMValue("EVT") > 0)) {
        if (CurrentRecord.discountPeriod.equalsIgnoreCase("m")) {
          tmpStartDate = conversionUtils.getUTCMonthStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCMonthEnd(CurrentRecord.EventStartDate);
        } else if (CurrentRecord.discountPeriod.equalsIgnoreCase("d")) {
          tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
          tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
        }

        // perform the discount
        tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.discountRule, CurrentRecord.balanceGroup, "EVT", CurrentRecord.discountCounter, CurrentRecord.discountInitValue, tmpStartDate, tmpEndDate);
        // put the info back in the Record
        if (tmpDiscInfo.isDiscountApplied()) {
          CurrentRecord.discountApplied = true;
          CurrentRecord.discountRecId = tmpDiscInfo.getRecId();
          CurrentRecord.discountGranted += tmpDiscInfo.getDiscountedValue();
        }
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
