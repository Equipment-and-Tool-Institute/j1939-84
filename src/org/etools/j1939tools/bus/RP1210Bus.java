/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939tools.bus.RP1210Library.BLOCKING_NONE;
import static org.etools.j1939tools.bus.RP1210Library.CLAIM_BLOCK_UNTIL_DONE;
import static org.etools.j1939tools.bus.RP1210Library.CMD_ECHO_TRANSMITTED_MESSAGES;
import static org.etools.j1939tools.bus.RP1210Library.CMD_PROTECT_J1939_ADDRESS;
import static org.etools.j1939tools.bus.RP1210Library.CMD_SET_ALL_FILTERS_STATES_TO_PASS;
import static org.etools.j1939tools.bus.RP1210Library.ECHO_ON;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * The RP1210 implementation of a {@link Bus}
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class RP1210Bus implements Bus {

    private static final long GIGA = 1000000000;

    /**
     * The source address for this tool
     */
    private final int address;

    /**
     * The id of this client for the {@link RP1210Library}
     */
    private final short clientId;

    /**
     * The thread pool used for polling
     */
    private final ExecutorService decodingExecutor;

    private final ExecutorService rp1210Executor;

    /**
     * The {@link Logger} for errors
     */
    private final Logger logger;

    /**
     * The {@link RP1210Library}
     */
    private final RP1210Library rp1210Library;

    /**
     * The Queue of {@link Packet}s
     */
    final private MultiQueue<Packet> queue;

    /**
     * An adjustment offset for the adapter time to system time. Adapters don't have batteries, so their clocks are
     * always wrong.
     */
    private long timestampStartNanoseconds;

    private long lastTimestamp = Long.MAX_VALUE;

    // from the .INI file.
    final private long timestampWeight;

    private boolean imposterDetected;

    public RP1210Bus(Adapter adapter, String connectionString, int address, boolean appPacketize) throws BusException {
        this(RP1210Library.load(adapter),
             Executors.newSingleThreadExecutor(nameThreadFactory("RP1210 decoding")),
             Executors.newSingleThreadExecutor(nameThreadFactory("RP1210 processing")),
             new MultiQueue<>(),
             adapter,
             connectionString,
             address,
             appPacketize,
             J1939_84.getLogger());
    }

    private static ThreadFactory nameThreadFactory(String name) {
        return r -> {
            final Thread thread = new Thread(r);
            thread.setName(name);
            return thread;
        };
    }

    /**
     * Constructor exposed for testing
     */
    public RP1210Bus(RP1210Library rp1210Library,
                     ExecutorService decodingExecutor,
                     ExecutorService rp1210Executor,
                     MultiQueue<Packet> queue,
                     Adapter adapter,
                     String connectionString,
                     int address,
                     boolean appPacketize,
                     Logger logger) throws BusException {
        this.rp1210Library = rp1210Library;
        this.decodingExecutor = decodingExecutor;
        this.rp1210Executor = rp1210Executor;
        this.queue = queue;
        this.address = address;
        this.logger = logger;
        timestampWeight = adapter.getTimestampWeight() * 1000L;
        timestampStartNanoseconds = 0;

        clientId = rp1210Library.RP1210_ClientConnect(0,
                                                      adapter.getDeviceId(),
                                                      connectionString,
                                                      0,
                                                      0,
                                                      (short) (appPacketize ? 1 : 0));
        checkReturnCode(clientId);
        try {
            sendCommand(CMD_PROTECT_J1939_ADDRESS,
                        new byte[] { (byte) address, 0, 0, (byte) 0xE0, (byte) 0xFF, 0,
                                (byte) 0x81, 0, 0, CLAIM_BLOCK_UNTIL_DONE });
            sendCommand(CMD_ECHO_TRANSMITTED_MESSAGES, ECHO_ON);
            sendCommand(CMD_SET_ALL_FILTERS_STATES_TO_PASS);

            this.rp1210Executor.submit(this::poll);
        } catch (Throwable e) {
            stop();
            throw new BusException("Failed to configure adapter.", e);
        }
    }

    @Override
    public void close() {
        rp1210Executor.shutdownNow();
        try {
            rp1210Executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warning("Unable to stop rp1210Executor.");
        }
        if (clientId >= 0) {
            rp1210Library.RP1210_ClientDisconnect(clientId);
        }
        decodingExecutor.shutdownNow();
        queue.close();
    }

    @Override
    public Stream<Packet> duplicate(Stream<Packet> stream, int time, TimeUnit unit) {
        return queue.duplicate(stream, time, unit);
    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public int getConnectionSpeed() throws BusException {
        try {
            return rp1210Executor.submit(() -> {
                byte[] bytes = new byte[128];
                sendCommand(RP1210Library.CMD_GET_PROTOCOL_CONNECTION_SPEED, bytes);
                return Integer.parseInt(new String(bytes, UTF_8).trim());
            }).get();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Unable to read bus speed.", e);
            throw new BusException("Unable to read bus speed.", e);
        } catch (ExecutionException e) {
            logger.log(Level.WARNING, "Unable to read bus speed.", e.getCause());
            throw new BusException("Unable to read bus speed.", e.getCause());
        }
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return queue.stream(timeout, unit);
    }

    /**
     * Reset stream timeout for stream created with bus.read(). To be used in a
     * stream call like peek, map or forEach.
     */
    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        queue.resetTimeout(stream, time, unit);
    }

    @Override
    public Packet send(Packet tx) throws BusException {
        byte[] data = encode(tx);
        try (Stream<Packet> stream = read(1000, TimeUnit.MILLISECONDS)) {
            // rp1210 libraries may not be thread safe
            Optional<String> error = rp1210Executor.submit(() -> {
                short rtn = sendRaw(data);
                if (rtn > 127 || rtn < 0) {
                    return Optional.of(getErrorMessage(rtn));
                }
                return Optional.<String>empty();
            }).get();
            if (error.isPresent()) {
                throw new BusException(error.get());
            }
            int id = tx.getId(0xFFFF);
            int source = tx.getSource();
            return stream
                         .filter(rx -> rx.isTransmitted() && id == rx.getId(0xFFFF) && rx.getSource() == source)
                         .findFirst()
                         .orElseThrow(() -> new BusException("Failed to send: " + tx));
        } catch (BusException e) {
            throw e;
        } catch (Throwable t) {
            throw new BusException("Failed to send: " + tx, t);
        }

    }

    /** exposed for tests */
    short sendRaw(byte[] data) {
        return rp1210Library.RP1210_SendMessage(clientId,
                                                data,
                                                (short) data.length,
                                                RP1210Library.NOTIFICATION_NONE,
                                                BLOCKING_NONE);
    }

    /**
     * Decodes the given byte array into a {@link Packet}
     *
     * @param  data
     *                    the byte array to decode
     * @param  length
     *                    the total length of the payload data
     * @return        {@link Packet}
     */
    private Packet decode(byte[] data, int length) {
        // only 32 bits used, but to get a u32, use a s64.
        long timestamp = (0xFF000000L & data[0] << 24) | (0xFF0000L & data[1] << 16) | (0xFF00L & data[2] << 8)
                | (0xFFL & data[3]);
        timestamp *= timestampWeight;
        // data[4] is echo
        int echoed = data[4];
        int pgn = ((data[7] & 0xFF) << 16) | ((data[6] & 0xFF) << 8) | (data[5] & 0xFF);
        int priority = data[8] & 0x07;
        int source = data[9] & 0xFF;
        if (pgn < 0xF000) {
            int destination = data[10];
            pgn = pgn | (destination & 0xFF);
        }

        // only recalibrate clocks when adapter rolls over
        if (timestamp < lastTimestamp) {
            Instant time = Instant.now();
            timestampStartNanoseconds = time.getNano() + time.getEpochSecond() * GIGA - timestamp;
            logger.log(Level.INFO,
                       String.format("adapter time offset: %,d ns %s", timestampStartNanoseconds, time));
        }
        lastTimestamp = timestamp;

        // update application clock offset
        long nanoseconds = timestamp + timestampStartNanoseconds;
        DateTimeModule.getInstance().setNanoTime(nanoseconds);

        // convert to LocalTime for Packet
        Instant time = Instant.ofEpochSecond( /* seconds */ nanoseconds / GIGA,
                                             /* nanoseconds */(nanoseconds % GIGA));
        return Packet.create(LocalDateTime.ofInstant(time, ZoneId.systemDefault()),
                             priority,
                             pgn,
                             source,
                             echoed != 0,
                             Arrays.copyOfRange(data, 11, length));
    }

    /**
     * Transforms the given {@link Packet} into a byte array so it can be sent
     * to the vehicle bus
     *
     * @param  packet
     *                    the {@link Packet} to encode
     * @return        a byte array of the encoded packet
     */
    static public byte[] encode(Packet packet) {
        byte[] buf = new byte[packet.getLength() + 6];
        int id = packet.getId(0xFFFF);
        buf[0] = (byte) id;
        buf[1] = (byte) (id >> 8);
        buf[2] = (byte) (id >> 16);
        buf[3] = (byte) packet.getPriority();
        buf[4] = (byte) packet.getSource();
        buf[5] = (id < 0xF000) ? (byte) (id & 0xFF) : 0;
        for (int i = 0; i < packet.getLength(); i++) {
            buf[6 + i] = (byte) packet.get(i);
        }
        return buf;
    }

    /**
     * Checks the {@link RP1210Library} for any incoming messages. Any incoming
     * messages are decoded and added to the queue
     */
    private void poll() {
        try {
            while (true) {
                byte[] data = new byte[32];
                short rtn = rp1210Library.RP1210_ReadMessage(clientId, data, (short) data.length, BLOCKING_NONE);
                if (rtn > 0) {
                    decodeDataAndQueuePacket(data, rtn);
                } else if (rtn == -RP1210Library.ERR_RX_QUEUE_FULL) {
                    // RX queue full, remedy is to reread.
                    logger.log(Level.SEVERE, getErrorMessage(rtn));
                } else {
                    checkReturnCode(rtn);
                    break;
                }
            }
            // this allows the other calls to have a chance
            Thread.yield();
            rp1210Executor.submit(this::poll);
        } catch (BusException e) {
            logger.log(Level.SEVERE, "Failed to read RP1210", e);
        }
    }

    private void decodeDataAndQueuePacket(byte[] data, short rtn) {
        decodingExecutor.submit(() -> {
            Packet packet = decode(data, rtn);
            // logger.log(Level.FINE, packet.toTimeString());
            if (packet.getSource() == getAddress() && !packet.isTransmitted()) {
                logger.log(Level.WARNING, "Another ECU is using this address: " + packet);
                imposterDetected = true;
            }
            queue.add(packet);
        });
    }

    /**
     * Helper method to send a command to the library
     *
     * @param  command
     *                          the command to send
     * @param  data
     *                          the data to include in the command
     * @throws BusException
     *                          if there result of the command was unsuccessful
     */
    private void sendCommand(short command, byte... data) throws BusException {
        short rtn = rp1210Library.RP1210_SendCommand(command, clientId, data, (short) data.length);
        checkReturnCode(rtn);
    }

    /**
     * Disconnects from the {@link RP1210Library}
     *
     * @throws BusException
     *                          if there is a problem disconnecting
     */
    public void stop() throws BusException {
        try {
            rp1210Executor.submit(() -> rp1210Library.RP1210_ClientDisconnect(clientId)).get();
        } catch (Exception e) {
            throw new BusException("Failed to stop RP1210.", e);
        } finally {
            rp1210Executor.shutdown();
        }
    }

    /**
     * Checks the code returned from calls to the adapter to determine if it's
     * an error.
     *
     * @param  rtnCode
     *                          the return code to check
     * @throws BusException
     *                          if the return code is an error
     */
    private void checkReturnCode(short rtnCode) throws BusException {
        if (rtnCode > 127 || rtnCode < 0) {
            String errorMessage = getErrorMessage(rtnCode);
            throw new BusException(errorMessage);
        }
    }

    private String getErrorMessage(short rtnCode) {
        rtnCode = (short) Math.abs(rtnCode);
        byte[] buffer = new byte[256];
        rp1210Library.RP1210_GetErrorMsg(rtnCode, buffer);
        return "Error (" + rtnCode + "): " + new String(buffer, UTF_8).trim();
    }

    @Override
    public boolean imposterDetected() {
        return imposterDetected;
    }
}
