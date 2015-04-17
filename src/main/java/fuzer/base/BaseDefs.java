package fuzer.base;

/**
 * Definitions for the Base record type.
 * 
 * @author TGDSPIA1
 */
public class BaseDefs 
{
  // Field Splitter in the input records
	public static final String BASE_FIELD_SPLITTER = "\\|";
	public static final String BASE_HEADER = "HEADER";
	public static final String BASE_TRAILER = "TRAILER";

  //These are the mappings to header fields we are going to be using
	public static final int BASE_HDR_RECORD_TYPE_IDX              = 0;
	public static final int BASE_HDR_RESELLER_NAME_IDX            = 1;
	public static final int BASE_HDR_VERSION_IDX                  = 2;
	public static final int BASE_HDR_DATETIME_START_IDX           = 3;
	public static final int BASE_HDR_DATETIME_STOP_IDX            = 4;
	public static final int BASE_HDR_BATCH_SEQUENCE_NUMBER_IDX    = 5;

  //These are the mappings to trailer fields we are going to be using
	public static final int BASE_DTL_RECORD_FIELD_COUNT           = 42;
	public static final int BASE_DTL_RECORD_FIELD_COUNT_8_92      = 41;
	public static final int BASE_DTL_RECORD_ID_IDX                = 0;
	public static final int BASE_DTL_RECORD_TYPE_IDX              = 1;
	public static final int BASE_DTL_RECORD_DETAIL_IDX            = 2;
	public static final int BASE_DTL_LINKED_RECORD_ID_IDX         = 3;
	public static final int BASE_DTL_LAST_EXPORTED_DATETIME_IDX   = 4;
	public static final int BASE_DTL_ORIGINATING_CLI_IDX          = 5;
	public static final int BASE_DTL_ORIGINATING_POINT_IDX        = 6;
	public static final int BASE_DTL_DESTINATION_POINT_IDX        = 7;
	public static final int BASE_DTL_NETWORK_GROUP_IDX            = 8;
	public static final int BASE_DTL_NETWORK_SUBGROUP_IDX         = 9;
	public static final int BASE_DTL_LOCATION_ID_IDX              = 10;
	public static final int BASE_DTL_CELL_ID_IDX                  = 11;
	public static final int BASE_DTL_DETAIL_1_IDX                 = 12;
	public static final int BASE_DTL_DETAIL_2_IDX                 = 13;
	public static final int BASE_DTL_TIMEZONE_IDX                 = 14;
	public static final int BASE_DTL_CALL_MODE_IDX                = 15;
	public static final int BASE_DTL_DATETIME_START_IDX           = 16;
	public static final int BASE_DTL_DATETIME_END_IDX             = 17;
	public static final int BASE_DTL_SESSION_DURATION_IDX         = 18;
	public static final int BASE_DTL_SESSION_SIZE_IDX             = 19;
	public static final int BASE_DTL_EVENTS_IDX                   = 20;
	public static final int BASE_DTL_BUNDLE_ID_IDX                = 21;
	public static final int BASE_DTL_BUNDLE_UNITS_DEFINTION_IDX   = 22;
	public static final int BASE_DTL_BUNDLE_UNITS_USAGE_IDX       = 23;
	public static final int BASE_DTL_TARIFF_ID_IDX                = 24;
	public static final int BASE_DTL_PACKAGE_ID_IDX               = 25;
	public static final int BASE_DTL_REGION_ID_IDX                = 26;
	public static final int BASE_DTL_PRICE_TYPE_IDX               = 27;
	public static final int BASE_DTL_SETUP_FEE_IDX                = 28;
	public static final int BASE_DTL_PRICE_IDX                    = 29;
	public static final int BASE_DTL_TAX_IDX                      = 30;
	public static final int BASE_DTL_END_BALANCE_IDX              = 31;
	public static final int BASE_DTL_EXTERNAL_REFERENCE_ID_IDX    = 32;
	public static final int BASE_DTL_PREMIUM_NUMBER_IDX           = 33;
	public static final int BASE_DTL_PREMIUM_SERVICE_DESCR_IDX    = 34;
	public static final int BASE_DTL_PROVIDER_NAME_IDX            = 35;
	public static final int BASE_DTL_CUSTOMER_CARE_CONTACT_IDX    = 36;
	public static final int BASE_DTL_CLIP_PRESENTATION_IND_IDX    = 37;
	public static final int BASE_DTL_EXTERNAL_ID_IDX              = 38;
	public static final int BASE_DTL_IMSI_IDX                     = 39;
	public static final int BASE_DTL_SIM_IDX                      = 40;
	public static final int BASE_ACCESS_TYPE_ID_IDX               = 41;
  

  //These are the mappings to trailer fields we are going to be using
	public static final int BASE_TLR_RECORD_TYPE_IDX              = 0;
	public static final int BASE_TLR_NUMBER_OF_RECORDS_IDX        = 1;
	public static final int BASE_TLR_TRAILER_INFO_IDX             = 2;
	public static final int BASE_TLR_TOTAL_PRICE_IDX              = 3;
  
  // Indexes for input columns
  public final static int IDX_RR_id                             =  0;
  public final static int IDX_RR_userId                         =  1;
  public final static int IDX_RR_orderId                        =  2;
  public final static int IDX_RR_processId                      =  3;
  public final static int IDX_RR_category                       =  4;
  public final static int IDX_RR_destination                    =  5;
  public final static int IDX_RR_base_tariff                    =  6;
  public final static int IDX_RR_tariff                         =  7;
  public final static int IDX_RR_ratedAmount                    =  8;
  public final static int IDX_RR_discountname                   =  9;
  public final static int IDX_RR_discountapplied                = 10;
  public final static int IDX_RR_discountrule                   = 11;
  public final static int IDX_RR_discountrum                    = 12;
  public final static int IDX_RR_counterId                      = 13;
  public final static int IDX_RR_recId                          = 14;
  public final static int IDX_RR_SubscriptionId                 = 15;
  public final static int IDX_RR_ANumber                        = 16;
  public final static int IDX_RR_BNumber                        = 17;
  public final static int IDX_RR_quantity                       = 18;
  public final static int IDX_RR_invoiceId                      = 19;
  public final static int IDX_RR_EventTS                        = 20;
  public final static int IDX_RR_FileRef                        = 21;
  public final static int IDX_RR_LINKED_RECORD_ID               = 22;
  public final static int IDX_RR_balanceImpacts                 = 23;
  public final static int IDX_RR_originalRecord                 = 24;
  public final static int IDX_RR_rerateOperation                = 25;

  // The call scenarios
	public static final int BASE_VOICE_CALL                       = 1;
	public static final int BASE_DATA_SESSION                     = 2;
	public static final int BASE_CALLBACK                         = 3;
	public static final int BASE_CONFERENCING                     = 4;
	public static final int BASE_SMS                              = 5;
	public static final int BASE_INSTANT_MESSAGE                  = 6;
	public static final int BASE_MMS                              = 7;
  
  // The call modes
	public static final int BASE_MODE_MO                          = 1;
	public static final int BASE_MODE_MT                          = 2;
	public static final int BASE_MODE_FWD                         = 3;
	public static final int BASE_MODE_ROAM_MO                     = 11;
	public static final int BASE_MODE_ROAM_MT                     = 12;
	public static final int BASE_MODE_ROAM_FWD                    = 13;
	public static final int BASE_MODE_POC                         = 21;
	public static final int BASE_MODE_PTC                         = 22;
  
  // for the records from KPN
	public static final int BASE_HEADER_TYPE   = 40;
	public static final int BASE_DETAIL_TYPE   = 41;
	public static final int BASE_TRAILER_TYPE  = 49;  
	public static final int BASE_BACKOUT_TYPE  = 48;
  
  // Field Splitter in the balance records
  public static final String BASE_BALIMP_SPLITTER = "$";
}
