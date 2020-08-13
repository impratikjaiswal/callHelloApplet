package callhelloapplet;

import uicc.toolkit.EnvelopeHandler;
import uicc.toolkit.EnvelopeHandlerSystem;
import uicc.toolkit.EnvelopeResponseHandler;
import uicc.toolkit.EnvelopeResponseHandlerSystem;
import uicc.toolkit.ProactiveHandler;
import uicc.toolkit.ProactiveHandlerSystem;
import uicc.toolkit.ProactiveResponseHandler;
import uicc.toolkit.ProactiveResponseHandlerSystem;
import uicc.toolkit.ToolkitConstants;
import uicc.toolkit.ToolkitException;
import uicc.toolkit.ToolkitInterface;
import uicc.toolkit.ToolkitRegistry;
import uicc.toolkit.ToolkitRegistrySystem;
import uicc.usim.access.SIMConstants;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class callHelloApplet extends Applet
        implements ToolkitInterface, ToolkitConstants, uicc.usim.toolkit.ToolkitConstants, SIMConstants
{
    /**
     * USIM AID
     */
    private static final byte USIM_AID[] = { (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x87, (byte)0x10,
            (byte)0x02, (byte)0xFF, (byte)0x66, (byte)0xFF, (byte)0x01, (byte)0x89, (byte)0xB0, (byte)0x00, (byte)0x01,
            (byte)0xFF };

    /**
     * Loci Under MF / 3F00
     * Loci Under USIM / 7FFF
     */
    private static final byte LOCI_FILELIST[] = { (byte)0x02, (byte)0x3F, (byte)0x00, (byte)0x7F, (byte)0x20,
            (byte)0x6F, (byte)0x7E, (byte)0x3F, (byte)0x00, (byte)0x7F, (byte)0xFF, (byte)0x6F, (byte)0x7E };

    private static final byte SELECT_USSD_U[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U',
            (byte)'S', (byte)'S', (byte)'D', (byte)' ', (byte)'U', };

    private static final byte SELECT_USSD_P[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U',
            (byte)'S', (byte)'S', (byte)'D', (byte)' ', (byte)'P', };

    private static final byte SELECT_DT[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'D',
            (byte)'T' };

    private static final byte SELECT_PLI[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'P',
            (byte)'L', (byte)'I' };

    private static final byte SELECT_STATUS[] = { (byte)'S', (byte)'t', (byte)'a', (byte)'t', (byte)'u', (byte)'s' };

    private static final byte SELECT_USSD_DT_P[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U',
            (byte)'S', (byte)'S', (byte)'D', (byte)'A', (byte)'n', (byte)'d', (byte)' ', (byte)'D', (byte)'T',
            (byte)' ', (byte)'P', };

    private static final byte SELECT_USSD_DT_U[] = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U',
            (byte)'S', (byte)'S', (byte)'D', (byte)'A', (byte)'n', (byte)'d', (byte)' ', (byte)'D', (byte)'T',
            (byte)' ', (byte)'U', };

    private final static byte[] dtHello = { (byte)'T', (byte)'h', (byte)'i', (byte)'s', (byte)' ', (byte)'i',
            (byte)'s', (byte)' ', (byte)'f', (byte)'i', (byte)'r', (byte)'s', (byte)'t', (byte)' ', (byte)'H',
            (byte)'e', (byte)'l', (byte)'l', (byte)'o', (byte)' ', (byte)'f', (byte)'o', (byte)'r', (byte)'m',
            (byte)' ', (byte)'G', (byte)'l', (byte)'o', (byte)'b', (byte)'e', (byte)'T', (byte)'o', (byte)'u',
            (byte)'c', (byte)'h', (byte)'!', (byte)'!', (byte)'!' };

    private final static byte[] dtTP = { (byte)'T', (byte)'e', (byte)'r', (byte)'m', (byte)'i', (byte)'n', (byte)'a',
            (byte)'l', (byte)' ', (byte)'P', (byte)'r', (byte)'o', (byte)'f', (byte)'i', (byte)'l', (byte)'e' };

    private final static byte[] dtATR = { (byte)'A', (byte)'T', (byte)'R' };

    private final static byte[] dtCall = { (byte)'C', (byte)'a', (byte)'l', (byte)'l', (byte)' ', (byte)'C', (byte)'o',
            (byte)'n', (byte)'t', (byte)'r', (byte)'o', (byte)'l' };

    private final static byte[] dtBeforeUSSD = { (byte)'C', (byte)'l', (byte)'i', (byte)'c', (byte)'k', (byte)' ',
            (byte)'t', (byte)'o', (byte)' ', (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U',
            (byte)'S', (byte)'S', (byte)'D' };

    private static byte[] dtAfterUSSD = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)' ', (byte)'U', (byte)'S',
            (byte)'S', (byte)'D', (byte)' ', (byte)'w', (byte)'i', (byte)'t', (byte)'h', (byte)' ', (byte)'T',
            (byte)'R', (byte)' ', (byte)0x00 };

    private final static byte[] dtOTA = { (byte)'O', (byte)'T', (byte)'A' };

    private final static byte[] alpha = { (byte)'S', (byte)'e', (byte)'n', (byte)'d', (byte)'i', (byte)'n', (byte)'g',
            (byte)' ', (byte)'U', (byte)'S', (byte)'S', (byte)'D' };

    private final static byte[] dtStatus = { (byte)'S', (byte)'t', (byte)'a', (byte)'t', (byte)'u', (byte)'s' };

    private final static byte[] dtEF = { (byte)'E', (byte)'F', (byte)' ', (byte)'U', (byte)'p', (byte)'d', (byte)'a',
            (byte)'t', (byte)'e', (byte)'d' };

    /**
     * 0F AA182D3602
     * 44 2A31343223
     * 0F AA186D3602
     * 44 2A31343423
     */
    private static byte[] ussd = { (byte)0x2A, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x23 };

    private static byte[] ussdOrg = { (byte)0x2A, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x23 };

    /** Display Text Command Qualifier **/
    private final static byte DT_CQ = (byte)0x81;

    /** DCS 8 bit DATA */
    private final static byte DCS_8_BIT_DATA = (byte)0x04;

    /** DCS UNPACKED */
    private final static byte USSD_DCS_UNPACKED = (byte)0x44;

    /** DCS PACKED */
    private final static byte USSD_DCS_PACKED = (byte)0x0F;

    private static final byte VALUE_ZERO = (byte)0x00;

    private final static byte OFFSET_ZERO = (byte)0x00;

    /** PLI - Command Qualifier **/
    private final static byte PLI_CQ_IMEI = (byte)0x01;

    /** PLI - Command Qualifier **/
    private final static byte PLI_CQ_LOCATION = (byte)0x00;

    /** Constant for TAG_USSD_STRING **/
    private final static byte TAG_USSD_STRING = (byte)0x0A;

    /** Constant for PRO_CMD_SEND_USSD **/
    private final static byte PRO_CMD_SEND_USSD = (byte)0x12;

    private final static byte DURATION = (byte)0x1E;

    private byte install;

    private boolean isStatus;

    private byte countStatus;

    private ToolkitRegistry toolkitRegistry;

    protected static callHelloApplet instance;

    public callHelloApplet( )
    {
        this.install = VALUE_ZERO;
        // initialize object
        register();
        // register to the SIM Toolkit Framework
        this.toolkitRegistry = ToolkitRegistrySystem.getEntry();
        // register events
        this.toolkitRegistry.setEvent( EVENT_CALL_CONTROL_BY_NAA );
        install++;
        this.toolkitRegistry.setEvent( EVENT_FIRST_COMMAND_AFTER_ATR );
        install++;
        this.toolkitRegistry.setEvent( EVENT_PROFILE_DOWNLOAD );
        install++;
        install++;
        this.toolkitRegistry.setEvent( EVENT_EVENT_DOWNLOAD_MT_CALL );
        install++;
        // Initialize First Menu
        initMenu( SELECT_DT );
        initMenu( SELECT_PLI );
        initMenu( SELECT_USSD_P );
        initMenu( SELECT_USSD_U );
        initMenu( SELECT_USSD_DT_P );
        initMenu( SELECT_USSD_DT_U );
        initMenu( SELECT_STATUS );
        this.toolkitRegistry.registerFileEvent( EVENT_EXTERNAL_FILE_UPDATE,
                                                LOCI_FILELIST,
                                                OFFSET_ZERO,
                                                (short)LOCI_FILELIST.length,
                                                USIM_AID,
                                                OFFSET_ZERO,
                                                (byte)USIM_AID.length );
        this.toolkitRegistry.setEvent( EVENT_FORMATTED_SMS_PP_ENV );
        this.toolkitRegistry.requestPollInterval( DURATION );
    }// OptimusLaunchBrowser()

    private void initMenu( byte[] menuArray )
    {
        this.toolkitRegistry.initMenuEntry( menuArray,
                                            OFFSET_ZERO,
                                            (short)menuArray.length,
                                            VALUE_ZERO,
                                            false,
                                            VALUE_ZERO,
                                            VALUE_ZERO );
    }

    public static void install( byte bArray[], short bOffset, byte blength ) throws ISOException
    {
        new callHelloApplet();
    } // install()

    public void process( APDU apdu ) throws ISOException
    {
        if ( selectingApplet() )
        { // Select applet APDU command
            return;
        } // End of if ( selectingApplet() )
        byte buffer[] = apdu.getBuffer();
        // Proprietary Command
        // CLA INS P1 P2 P3
        // 00 44 00 00 32
        if ( buffer[ ISO7816.OFFSET_CLA ] != VALUE_ZERO )
        {
            ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED );
        } // End of if ( buffer[ ISO7816.OFFSET_CLA ] != VALUE_ZERO )
        if ( buffer[ ISO7816.OFFSET_INS ] != (byte)0x44 )
        {
            ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED );
        } // End of if ( buffer[ ISO7816.OFFSET_INS ] != (byte)0x44 )
          // apdu.setIncomingAndReceive();
        processDT( dtHello );
    } // End of public void process( APDU apdu ) throws ISOException

    private void processPLI( byte cq )
    {
        ProactiveHandler pro = ProactiveHandlerSystem.getTheHandler();
        pro.init( PRO_CMD_PROVIDE_LOCAL_INFORMATION, cq, DEV_ID_TERMINAL );
        pro.send();
    }

    /**
     * This function process the display text proactive command
     */
    private void processDT( byte text[] )
    {
        ProactiveHandler pro = ProactiveHandlerSystem.getTheHandler();
        pro.init( PRO_CMD_DISPLAY_TEXT, (byte)DT_CQ, DEV_ID_TERMINAL );
        pro.appendTLV( TAG_TEXT_STRING, DCS_8_BIT_DATA, text, OFFSET_ZERO, (short)text.length );
        pro.send();
        /*
         * proHdlr.initDisplayText( DT_CQ, // qualifier (wait for user, prio high)
         * DCS_8_BIT_DATA, // dcs
         * msg,
         * OFFSET_ZERO,
         * (short)msg.length ); // length
         */
    }

    private void processDT( byte text[], byte result )
    {
        text[ (short) ( text.length - 1 ) ] = result;
        processDT( text );
    }

    private byte processUSSD( byte ussdDcs, byte b, boolean alphaID )
    {
        b = (byte) ( b | 0x30 );
        Util.arrayCopyNonAtomic( ussdOrg, OFFSET_ZERO, ussd, OFFSET_ZERO, (byte)ussdOrg.length );
        ussd[ (short) ( ussd.length - 1 - 1 ) ] = b;
        byte length = (byte)ussd.length;
        ProactiveHandler pro = ProactiveHandlerSystem.getTheHandler();
        pro.init( PRO_CMD_SEND_USSD, (byte)VALUE_ZERO, DEV_ID_TERMINAL );
        if ( alphaID )
        {
            pro.appendTLV( (byte) ( TAG_ALPHA_IDENTIFIER | TAG_SET_CR ), alpha, OFFSET_ZERO, (short)alpha.length );
        }
        if ( ussdDcs == USSD_DCS_PACKED )
        {
            length = (byte)sevenBitCoding( ussd, OFFSET_ZERO, length, ussd, OFFSET_ZERO );
        }
        // append USSD String
        pro.appendTLV( (byte) ( TAG_USSD_STRING | TAG_SET_CR ), ussdDcs, ussd, OFFSET_ZERO, length );
        return pro.send();
    }

    /**
     * To process the toolkit Events
     */
    public void processToolkit( short event ) throws ToolkitException
    {
        switch ( event )
        {
            case EVENT_FIRST_COMMAND_AFTER_ATR: // '\177'
            {
                processPLI( PLI_CQ_LOCATION );
                processDT( dtATR );
                break;
            } // End of case EVENT_FIRST_COMMAND_AFTER_ATR: // '\177'
            case EVENT_PROFILE_DOWNLOAD:
            {
                processPLI( PLI_CQ_IMEI );
                processDT( dtTP );
                break;
            }
            case EVENT_CALL_CONTROL_BY_NAA:
            {
                processCallControlBySim();
                processDT( dtCall );
                break;
            }
            case EVENT_MENU_SELECTION: // '\007'
            {
                menuselection();
                break;
            }
            // Loci Updates
            case EVENT_EXTERNAL_FILE_UPDATE: // '|'
            {
                processDT( dtEF );
                break;
            } // End of case EVENT_EXTERNAL_FILE_UPDATE: // '|'
              // Status
            case EVENT_STATUS_COMMAND: // '\023'
            {
                if ( isStatus )
                {
                    if ( countStatus++ >= 0x05 )
                    {
                        isStatus = false;
                        processDT( dtStatus );
                    }
                }
                break;
            } // End of case EVENT_STATUS_COMMAND: // '\023'
              // secure OTA Packet
            case EVENT_FORMATTED_SMS_PP_ENV: // '\002'
            {
                processDT( dtOTA );
                break;
            } // End of case (byte)0x02: // '\002'
              // Menus
            default:
                break;
        }
    }

    /**
     * Menu options
     */
    private void menuselection()
    {
        byte result = VALUE_ZERO;
        EnvelopeHandler env = EnvelopeHandlerSystem.getTheHandler();
        switch ( env.getItemIdentifier() )
        {
// initMenu( SELECT_DT );
// initMenu( SELECT_PLI );
// initMenu( SELECT_USSD_P );
// initMenu( SELECT_USSD_U );
// initMenu( SELECT_USSD_DT_P );
// initMenu( SELECT_USSD_DT_U );
// initMenu( SELECT_STATUS );
            case (byte)0x01:
                processDT( dtHello );
                break;
            case (byte)0x02:
                processPLI( PLI_CQ_IMEI );
                break;
            case (byte)0x03:
                processDT( dtBeforeUSSD );
                result = processUSSD( USSD_DCS_PACKED, (byte)0x01, true );
                processDT( dtAfterUSSD, result );
                break;
            case (byte)0x04:
                processDT( dtBeforeUSSD );
                result = processUSSD( USSD_DCS_UNPACKED, (byte)0x02, true );
                processDT( dtAfterUSSD, result );
                break;
            case (byte)0x05:
                processDT( dtBeforeUSSD );
                result = processUSSD( USSD_DCS_PACKED, (byte)0x03, false );
                processDT( dtAfterUSSD, result );
                break;
            case (byte)0x06:
                processDT( dtBeforeUSSD );
                result = processUSSD( USSD_DCS_UNPACKED, (byte)0x04, false );
                processDT( dtAfterUSSD, result );
                break;
            case (byte)0x07:
                isStatus = true;
                countStatus = 0x00;
                break;
        } // End of switch ( env.getItemIdentifier() )
    } // End of private void menuselection()

    private boolean processCallControlBySim()
    {
        EnvelopeHandler envHdlr = EnvelopeHandlerSystem.getTheHandler();
        EnvelopeResponseHandler envRespHdlr = EnvelopeResponseHandlerSystem.getTheHandler();
        // Accept call
        // envRespHdlr.postAsBERTLV( true, (byte)0x00 );
        // Block call
        envRespHdlr.postAsBERTLV( true, (byte)0x01 );
        return true;
    }

    /**
     * packs 7 bit data
     * 
     * @param srcBuffer
     *            buffer which needs to convert
     * @param srcOffset
     *            offset from which data needs to convert
     * @param srcLength
     *            length of data
     * @param destBuffer
     *            after packing data will store in this buffer
     * @param destOffset
     *            from where to start storing data in destBuffer
     * @return total len after converting data
     */
    private short sevenBitCoding( byte[] srcBuffer, short srcOffset, short srcLength, byte[] destBuffer,
            short destOffset )
    {
        // check source for 'CR'
        if ( ( srcLength % 8 ) == 7 )
        {
            // add 'CR' byte
            srcBuffer[ (short) ( srcOffset + srcLength ) ] = (byte)0x0D;
            srcLength++;
        }
        short res = VALUE_ZERO;
        short j = VALUE_ZERO;
        short val = VALUE_ZERO;
        short val1 = VALUE_ZERO;
        short val2 = VALUE_ZERO;
        for ( short i = VALUE_ZERO; i < srcLength; i++ )
        {
            val = (short) ( srcBuffer[ (short) ( srcOffset + i ) ] & 0x00FF );
            if ( val < VALUE_ZERO )
            {
                val += (short)256;
            }
            val &= (short)0x7F;
            j = (short) ( i % 8 );
            if ( j == VALUE_ZERO )
            {
                destBuffer[ (short) ( destOffset + res ) ] = (byte)val;
            }
            else
            {
                val1 = (short) ( ( val << ( 8 - j ) ) & 0xFF );
                val2 = (short) ( ( val >> j ) & 0xFF );
                destBuffer[ (short) ( destOffset + res ) ] |= (byte)val1;
                res++;
                destBuffer[ (short) ( destOffset + res ) ] = (byte)val2;
            }
        }
        return (short) ( (short) ( (short) ( srcLength / (short)8 ) * (short)7 ) + (short) ( srcLength % (short)8 ) );
    }
}