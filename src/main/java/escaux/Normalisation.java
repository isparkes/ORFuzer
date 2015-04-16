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
package escaux;

import java.util.ArrayList;

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

public class Normalisation extends AbstractRegexMatch {

  String[] tmpSearchParameters = new String[1];

  /**
   * This is called when a data record is encountered. You should do any normal
   * processing here.
   *
   * @return
   */
  @Override
  public IRecord procValidRecord(IRecord r) {

    EscauxRecord currentRecord;
    ArrayList<String> Results;
    int i;

    currentRecord = (EscauxRecord) r;

    if (currentRecord.RECORD_TYPE == EscauxRecord.DETAIL_RECORD) {
      String tmpNumber = currentRecord.dialedNumber;

      getPipeLog().debug("--- start normalize <" + tmpNumber + ">");

      // allow several phases of normalization
      for (i = 1; i <= 2; i++) {

        tmpSearchParameters[0] = tmpNumber;
        Results = getRegexMatchWithChildData(Integer.toString(i), tmpSearchParameters);
        getPipeLog().debug("  iteration " + i + " <" + tmpNumber + ">");

        if ((Results != null)) {
          if (!Results.get(0).equalsIgnoreCase("nomatch")) {
            if ((Results.size() > 1)) {
              if (Results.get(0).isEmpty()) {
                // just add the prefix
                tmpNumber = Results.get(1) + tmpNumber;
                getPipeLog().debug("  -> add prefix <" + Results.get(1) + "> = <" + tmpNumber + ">");
              } else {
                // remove an old prefix and add the new prefix
                tmpNumber = tmpNumber.replaceAll(Results.get(0), Results.get(1));
                getPipeLog().debug("  -> remove <" + Results.get(0) + "> and add prefix <" + Results.get(1) + "> = <" + tmpNumber + ">");
              }
            } else {
              getPipeLog().debug("  -> error: result size < 2 result(0)=" + Results.get(0));
              currentRecord.addError(new RecordError("ERR_NORMALISATION_LOOKUP", ErrorType.SPECIAL));
            }
          } else {
            getPipeLog().debug("  -> no match");
          }
        } else {
          getPipeLog().debug("  -> error: result=null");
          currentRecord.addError(new RecordError("ERR_NORMALISATION_LOOKUP", ErrorType.SPECIAL));
        }
      }
      currentRecord.dialedNumberNorm = tmpNumber;
      getPipeLog().debug("--- end normalize <" + tmpNumber + ">");
    }
    return r;
  }

  /**
   * This is called when a data record with errors is encountered. You should do
   * any processing here that you have to do for error records, e.g. statistics,
   * special handling, even error correction!
   *
   * @return
   */
  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
