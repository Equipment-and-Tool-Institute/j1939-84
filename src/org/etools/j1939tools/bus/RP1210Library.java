/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * The interface for a {@link Library} used by a vehicle communications
 * {@link Adapter}
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public interface RP1210Library extends Library {

    /**
     * For Blocking I/O, block until command completes
     */
    public static final short BLOCKING = 1;

    /**
     * For Blocking I/O, do not block
     */
    public static final short BLOCKING_NONE = 0;

    /**
     * J1939 Address claim, wait until done
     */
    public static final int CLAIM_BLOCK_UNTIL_DONE = 0;

    /**
     * Pass through address claim messages
     */
    public static final int CLAIM_MESSAGES_PASS = 0x01;

    /**
     * Do not pass through address claim messages
     */
    public static final int CLAIM_MESSAGES_SILENT = 0x00;

    /**
     * J1939 Address claim, don't wait
     */
    public static final int CLAIM_RETURN_BEFORE_COMPLETION = 2;

    public static final short CMD_CLEAR_ISO_15765_FLOW_CONTROL = 35;
    public static final short CMD_DISALLOW_FURTHER_CONNECTIONS = 29;
    public static final short CMD_ECHO_TRANSMITTED_MESSAGES = 16;
    public static final short CMD_GENERIC_DRIVER_COMMAND = 14;
    public static final short CMD_GET_PROTOCOL_CONNECTION_SPEED = 45;
    public static final short CMD_PROTECT_J1939_ADDRESS = 19;
    public static final short CMD_RELEASE_J1939_ADDRESS = 31;
    public static final short CMD_RESET_DEVICE = 0;
    public static final short CMD_SET_ALL_FILTERS_STATES_TO_DISCARD = 17;
    public static final short CMD_SET_ALL_FILTERS_STATES_TO_PASS = 3;
    public static final short CMD_SET_BLOCK_TIMEOUT = 215;
    public static final short CMD_SET_BROADCAST_FOR_CAN = 21;
    public static final short CMD_SET_BROADCAST_FOR_ISO_15765 = 33;
    public static final short CMD_SET_BROADCAST_FOR_J1708 = 20;
    public static final short CMD_SET_BROADCAST_FOR_J1850 = 23;
    public static final short CMD_SET_BROADCAST_FOR_J1939 = 22;
    public static final short CMD_SET_CAN_FILTER_TYPE = 26;
    public static final short CMD_SET_ISO_15765_BAUD = 38;
    public static final short CMD_SET_ISO_15765_FILTER_TYPE = 32;
    public static final short CMD_SET_ISO_15765_FLOW_CONTROL = 34;
    public static final short CMD_SET_ISO_15765_LINK_TYPE = 36;
    public static final short CMD_SET_J1708_BAUD = 305;
    public static final short CMD_SET_J1708_FILTER_TYPE = 24;
    public static final short CMD_SET_J1708_MODE = 15;
    public static final short CMD_SET_J1850_FILTER_TYPE = 30;
    public static final short CMD_SET_J1939_BAUD = 37;
    public static final short CMD_SET_J1939_FILTER_TYPE = 25;
    public static final short CMD_SET_J1939_INTERPACKET_TIME = 27;
    public static final short CMD_SET_MAX_ERROR_MSG_SIZE = 28;
    public static final short CMD_SET_MESSAGE_FILTERING_FOR_CAN = 5;
    public static final short CMD_SET_MESSAGE_FILTERING_FOR_ISO15765 = 9;
    public static final short CMD_SET_MESSAGE_FILTERING_FOR_J1708 = 7;
    public static final short CMD_SET_MESSAGE_FILTERING_FOR_J1850 = 8;
    public static final short CMD_SET_MESSAGE_FILTERING_FOR_J1939 = 4;
    public static final short CMD_SET_MESSAGE_RECEIVE = 18;

    public static final byte ECHO_OFF = 0;
    public static final byte ECHO_ON = 1;

    public static final int ERR_ADAPTER_NOT_RESPONDING = 453;
    public static final int ERR_ADDRESS_CLAIM_FAILED = 146;
    public static final int ERR_ADDRESS_LOST = 153;
    public static final int ERR_ADDRESS_NEVER_CLAIMED = 157;
    public static final int ERR_ADDRESS_RELEASE_FAILED = 227;
    public static final int ERR_BLOCK_NOT_ALLOWED = 155;
    public static final int ERR_BUS_OFF = 151;
    public static final int ERR_CAN_BAUD_SET_NON_STANDARD = 454;
    public static final int ERR_CANNOT_CLAIM_BROADCAST_ADDRESS = 225;
    public static final int ERR_CANNOT_SET_CAN_BAUDRATE = 224;
    public static final int ERR_CANNOT_SET_PRIORITY = 147;
    public static final int ERR_CHANGE_MODE_FAILED = 150;
    public static final int ERR_CLIENT_ALREADY_CONNECTED = 130;
    public static final int ERR_CLIENT_AREA_FULL = 131;
    public static final int ERR_CLIENT_DISCONNECTED = 148;
    public static final int ERR_CODE_NOT_FOUND = 154;
    public static final int ERR_COMM_DEVICE_IN_USE = 230;
    public static final int ERR_COMMAND_NOT_SUPPORTED = 143;
    public static final int ERR_COMMAND_QUEUE_IS_FULL = 222;
    public static final int ERR_COMMAND_TIMED_OUT = 213;
    public static final int ERR_CONNECT_NOT_ALLOWED = 149;
    public static final int ERR_COULD_NOT_TX_ADDRESS_CLAIMED = 152;
    public static final int ERR_DATA_LINK_CONFLICT = 441;
    public static final int ERR_DEVICE_IN_USE = 135;
    public static final int ERR_DEVICE_NOT_SUPPORTED = 207;
    public static final int ERR_DLL_NOT_INITIALIZED = 128;
    public static final int ERR_FREE_MEMORY = 132;
    public static final int ERR_HARDWARE_NOT_RESPONDING = 142;
    public static final int ERR_HARDWARE_STATUS_CHANGE = 162;
    public static final int ERR_INI_FILE_NOT_IN_WIN_DIR = 202;
    public static final int ERR_INI_KEY_NOT_FOUND = 205;
    public static final int ERR_INI_SECTION_NOT_FOUND = 204;
    public static final int ERR_INVALID_CLIENT_ID = 129;
    public static final int ERR_INVALID_COMMAND = 144;
    public static final int ERR_INVALID_DEVICE = 134;
    public static final int ERR_INVALID_KEY_STRING = 206;
    public static final int ERR_INVALID_PORT_PARAM = 208;
    public static final int ERR_INVALID_PROTOCOL = 136;
    public static final int ERR_ISO_15765_BAUD_SET_NON_STANDARD = 458;
    public static final int ERR_J1708_BAUD_SET_NON_STANDARD = 456;
    public static final int ERR_J1939_BAUD_SET_NON_STANDARD = 457;
    public static final int ERR_MAX_FILTERS_EXCEEDED = 161;
    public static final int ERR_MAX_NOTIFY_EXCEEDED = 160;
    public static final int ERR_MESSAGE_NOT_SENT = 159;
    public static final int ERR_MESSAGE_TOO_LONG = 141;
    public static final int ERR_MULTIPLE_CLIENTS_CONNECTED = 156;
    public static final int ERR_MULTIPLE_CONNECTIONS_NOT_ALLOWED_NOW = 455;
    public static final int ERR_NONE = 0;
    public static final int ERR_NOT_ENOUGH_MEMORY = 133;
    public static final int ERR_OS_NOT_SUPPORTED = 220;
    public static final int ERR_OUT_OF_ADDRESS_RESOURCES = 226;
    public static final int ERR_RX_QUEUE_CORRUPT = 140;
    public static final int ERR_RX_QUEUE_FULL = 139;
    public static final int ERR_TX_MESSAGE_STATUS = 145;
    public static final int ERR_TX_QUEUE_CORRUPT = 138;
    public static final int ERR_TX_QUEUE_FULL = 137;
    public static final int ERR_WINDOW_HANDLE_REQUIRED = 158;

    public static final int FILTER_EXCLUSIVE = 0x01;
    public static final int FILTER_INCLUSIVE = 0x00;

    /**
     * Filter state = PASS ALL
     */
    public static final int FILTER_PASS_ALL = 2;
    /**
     * Filter state = DISCARD ALL MESSAGES
     */
    public static final int FILTER_PASS_NONE = 0;

    /**
     * Filter state = PASS SOME (some filters)
     */
    public static final int FILTER_PASS_SOME = 1;

    public static final int FILTER_TYPE_DESTINATION = 0x00000008;
    public static final int FILTER_TYPE_PGN = 0x00000001;
    public static final int FILTER_TYPE_PRIORITY = 0x00000002;
    public static final int FILTER_TYPE_SOURCE = 0x00000004;

    /**
     * Maximum size of J1939 message (+1)
     */
    public static final int MAX_J1939_MESSAGE_LENGTH = 1796;

    public static final short NOTIFICATION_NONE = 0;
    public static final short NOTIFICATION_ON = 1;

    /**
     * Loads the {@link Library} for the given {@link Adapter}
     *
     * @param  adapter
     *                     the {@link Adapter} to use for communications
     * @return         an {@link RP1210Library} library
     */
    static RP1210Library load(Adapter adapter) {
        System.setProperty("java.library.path", RP1210.WINDOWS_PATH);
        return (RP1210Library) Native.load(adapter.getDLLName(), RP1210Library.class);
    }

    /**
     * This function is called by the client application seeking connection with
     * a DLL.
     *
     * This connection corresponds to the implementation of the API for the VDA
     * being selected. Inside the API DLL, the function allocates and
     * initializes any client data structures, and loads, initializes, or
     * activates any device drivers (virtual or otherwise) to communicate with
     * the hardware and sets all client application variables to their defaults
     * (EchoMode, FilterStates, etc.).
     *
     * If the connection is successful, the function shall return a unique
     * identifier, corresponding to the ID of the client application, as
     * assigned by the API DLL.
     *
     * @param  hwndClient
     *                                           This parameter is no longer necessary and is unused. The value
     *                                           should be set to NULL (0x00).
     * @param  nDeviceID
     *                                           the ID of the device that's being used for the communications
     * @param  fpchProtocol
     *                                           Pointer to a null-terminated string of the protocol name to be
     *                                           used by the device designated in the previous parameter.
     * @param  lTxBufferSize
     *                                           A long integer for the requested size (in bytes) of the client
     *                                           transmit buffer to be allocated by the API for the queuing of
     *                                           messages sought to be transmitted by the client application.
     * @param  lRcvBufferSize
     *                                           A long integer for the requested size (in bytes) of the client
     *                                           receive buffer to be allocated by the API for the queuing of
     *                                           messages meant to be received by the client application.
     *                                           Should be passed as 0 if the application does not want to
     *                                           dictate the buffer size and the API DLL default of 8K is
     *                                           acceptable.
     * @param  nIsAppPacketizingIncomingMsgs
     *                                           0 to let the adapter assemble transport protocol packets;
     *                                           non-zero to pass through the packets and let the application
     *                                           assemble them
     * @return                               If the connection is successful, then the function returns a
     *                                       value between 0 and 127, corresponding to the client identifier
     *                                       that the application program is assigned by the API DLL.
     *
     *                                       The application program must save this return value in order to
     *                                       conduct future transactions with the DLL.
     *
     *                                       If the connection is unsuccessful, then an error code is returned
     *                                       that corresponds to a number greater than 127.
     */
    short RP1210_ClientConnect(int hwndClient,
                               short nDeviceID,
                               String fpchProtocol,
                               int lTxBufferSize,
                               int lRcvBufferSize,
                               short nIsAppPacketizingIncomingMsgs);

    /**
     * Disconnects the client from the adapter
     *
     * @param  nClientID
     *                       the client identifier as returned from RP1210_ClientConnect
     *
     * @return           0 if the call was successful; less than zero indicating an error
     *                   if there was a problem.
     */
    short RP1210_ClientDisconnect(short nClientID);

    /**
     * Translates an RP1210 error code into {@link String}
     *
     * @param  errCode
     *                         the error code returned by another library call that needs
     *                         translated
     * @param  fpchMessage
     *                         the buffer that will contain the String error message
     * @return             0 if the call was successful; less than zero indicating an error
     *                     if there was a problem
     */
    short RP1210_GetErrorMsg(short errCode, byte[] fpchMessage);

    /**
     * Returns information state of the connection and data link.
     *
     * @param  nClientID
     *                             the client identifier as returned from RP1210_ClientConnect
     * @param  fpchClientInfo
     *                             A pointer to the buffer (allocated by the client application)
     *                             where hardware status information is to be placed.
     * @param  nInfoSize
     *                             Always set to 18 bytes.
     * @param  nBlockOnRequest
     *                             A flag to indicate whether the function must block on
     *                             requesting the hardware status or not.
     * @return                 0 if the call was successful; less than zero indicating an error
     *                         if there was a problem
     */
    short RP1210_GetHardwareStatus(short nClientID, String fpchClientInfo, short nInfoSize, short nBlockOnRequest);

    short RP1210_GetLastErrorMsg(short errCode, int[] subErrorCode, String fpchMessage);

    /**
     * More comprehensive version information. This preferred over
     * RP1210_ReadVersion
     *
     * @param  nClientID
     *                                the client identifier as returned from RP1210_ClientConnect
     * @param  fpchAPIVersionInfo
     *                                API version info. Pointer to a buffer that is 17 bytes long.
     *                                The API may return up to 16 bytes in this field (with NULL
     *                                terminator) to describe the version of the API interface.
     * @param  fpchDLLVersionInfo
     *                                Pointer to a buffer that is 17 bytes long. The API may return
     *                                up to 16 bytes in this field (with NULL terminator) to
     *                                describe the version of the DLL.
     * @param  fpchFWVersionInfo
     *                                Firmware version info. Pointer to a buffer that is 17 bytes
     *                                long. The API may return up to 16 bytes in this field (with
     *                                NULL terminator) to describe the version of the firmware that
     *                                is present in the device.
     * @return                    0 if the call was successful; less than zero indicating an error
     *                            if there was a problem
     */
    short RP1210_ReadDetailedVersion(short nClientID,
                                     String fpchAPIVersionInfo,
                                     String fpchDLLVersionInfo,
                                     String fpchFWVersionInfo);

    /**
     * Reads a message from the adapter
     *
     * @param  nClientID
     *                            the client identifier as returned from RP1210_ClientConnect
     * @param  fpchAPIMessage
     *                            the buffer that the message will be place into.
     * @param  nBufferSize
     *                            the capacity of the supplied message buffer
     * @param  nBlockOnSend
     *                            if 0, the command will return immediately if there is no
     *                            message. If non-zero the method will wait until data is
     *                            available or the connection is closed.
     * @return                0 at no message, less than 192 for message size, greater than 192
     *                        for error code.
     */
    short RP1210_ReadMessage(short nClientID, byte[] fpchAPIMessage, short nBufferSize, short nBlockOnSend);

    /**
     * Reads version information about the DLL and the API
     *
     * @param fpchDLLMajorVersion
     *                                contains the resulting DLL Major version number
     * @param fpchDLLMinorVersion
     *                                contains the resulting DLL Minor version number
     * @param fpchAPIMajorVersion
     *                                contains the resulting API Major version number
     * @param fpchAPIMinorVersion
     *                                contains the resulting API Minor version number
     *
     */
    void RP1210_ReadVersion(String fpchDLLMajorVersion,
                            String fpchDLLMinorVersion,
                            String fpchAPIMajorVersion,
                            String fpchAPIMinorVersion);

    /**
     * Sends a command library to setup/remove filter and to claim address
     *
     * @param  nCommandNumber
     *                               the command to execute.
     * @param  nClientID
     *                               the client identifier as returned from RP1210_ClientConnect
     * @param  fpchClientCommand
     *                               the data that corresponds to the command
     * @param  nMessageSize
     *                               the size of the data that is being included
     * @return                   0 if the call was successful; less than zero indicating an error
     *                           if there was a problem
     */
    short RP1210_SendCommand(short nCommandNumber, short nClientID, byte[] fpchClientCommand, short nMessageSize);

    /**
     * Sends a message
     *
     * @param  nClientID
     *                               the client identifier as returned from RP1210_ClientConnect
     * @param  fpchClientMessage
     *                               the message that is being sent
     * @param  nMessageSize
     *                               the size of the message that's being sent
     * @param  nNotifyStatusOnTx
     *                               0 for no notification; 1 to get back a message identifier
     * @param  nBlockOnSend
     *                               Non-zero to block until the message has successfully sent or
     *                               failed to sent. A value of zero, and the method return
     *                               immediately.
     * @return                   0 if the call was successful; less than zero indicating an error
     *                           if there was a problem. If nNotifyStatusOnTx is set to 1, a value
     *                           of 1 to 127 is returned indicating the message identifier
     */
    short RP1210_SendMessage(short nClientID,
                             byte[] fpchClientMessage,
                             short nMessageSize,
                             short nNotifyStatusOnTx,
                             short nBlockOnSend);
}
