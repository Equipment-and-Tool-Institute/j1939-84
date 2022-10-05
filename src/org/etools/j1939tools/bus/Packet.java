/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.modules.DateTimeModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Sends a Packet containing an id with data from a source onto the bus
 *
 * @author Joe Batt (joe@soliddesign.net)
 */
public class Packet {
    // FIXME, eventually change to (RX)
    public static final String RX = "";
    /**
     * The indication that a packet was transmitted
     */
    public static final String TX = " (TX)";
    private final int id;
    private final int priority;
    private final int source;
    private final boolean transmitted;
    private int[] data;
    private List<Packet> fragments = Collections.singletonList(this);
    private LocalDateTime timestamp;

    /**
     * Creates a Packet
     *
     * @param priority
     *                        the priority of the packet
     * @param id
     *                        the ID of the packet
     * @param source
     *                        the source address of the packet
     * @param transmitted
     *                        indicates the packet was sent by the application
     * @param data
     *                        the data of the packet
     */
    public Packet(LocalDateTime timestamp, int priority, int id, int source, boolean transmitted, int... data) {
        this.timestamp = timestamp;
        this.priority = priority;
        this.id = id;
        this.source = source;
        this.transmitted = transmitted;
        this.data = data;
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                data[i] &= 0xFF;
            }
        }
    }

    public static Packet create(int id, int source, boolean transmitted, int... data) {
        return new Packet(LocalDateTime.now(), 6, id, source, transmitted, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param  id
     *                    the ID of the packet
     * @param  source
     *                    the source address of the packet
     * @param  bytes
     *                    the data bytes of the packet
     * @return        Packet
     */
    public static Packet create(int id, int source, byte... bytes) {
        return create(6, id, source, false, bytes);
    }

    /**
     * Creates an instance of Packet
     *
     * @param  id
     *                    the ID of the packet
     * @param  source
     *                    the source address of the packet
     * @param  data
     *                    the data of the packet
     * @return        Packet
     */
    public static Packet create(int id, int source, int... data) {
        return create(id, source, false, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param  priority
     *                         the priority of the packet
     * @param  id
     *                         the ID of the packet
     * @param  source
     *                         the source address of the packet
     * @param  transmitted
     *                         indicates the packet was sent by the application
     * @param  bytes
     *                         the data bytes of the packet
     * @return             Packet
     */
    public static Packet create(int priority, int id, int source, boolean transmitted, byte... bytes) {
        return create(LocalDateTime.now(), priority, id, source, transmitted, bytes);
    }

    public static Packet create(LocalDateTime time,
                                int priority,
                                int id,
                                int source,
                                boolean transmitted,
                                byte... bytes) {
        if (bytes.length < 3) {
            // a body of 0 length indicates that the packet was a failure.
            throw new IllegalArgumentException("Packets must have a body of at least 3 bytes.");
        }
        int[] data = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = 0xFF & bytes[i];
        }
        return new Packet(time, priority, id, source, transmitted, data);
    }

    public static Packet create(LocalDateTime time,
                                int priority,
                                int id,
                                int source,
                                boolean transmitted,
                                int... data) {
        if (data.length < 3) {
            // a body of 0 length indicates that the packet was a failure.
            throw new IllegalArgumentException("Packets must have a body of at least 3 bytes.");
        }
        return new Packet(time, priority, id, source, transmitted, data);
    }

    /**
     * Converts the value produced by Packet.toString() back into a Packet
     *
     * @param  string
     *                    the {@link String} to parse
     * @return        a Packet or null if the string could not be parsed
     */
    public static Packet parse(String string) {
        try {
            boolean tx = string.contains(TX);
            if (tx) {
                string = string.replace(TX, "");
            }
            String[] parts = string.split(" ");
            int header = Integer.parseInt(parts[0].trim(), 16);
            int priority = (header & 0xFF000000) >> 26;
            int id = (header & 0xFFFF00) >> 8;
            int source = header & 0xFF;

            // skip the length indication
            int offset = parts[1].startsWith("[") ? 2 : 1;
            byte[] bytes = new byte[parts.length - offset];
            for (int i = offset; i < parts.length; i++) {
                bytes[i - offset] = (byte) (Integer.parseInt(parts[i].trim(), 16) & 0xFF);
            }

            return Packet.create(priority, id, source, tx, bytes);
        } catch (Exception e) {
            J1939_84.getLogger().log(Level.SEVERE, string + " could not be parsed into a Packet", e);
        }
        return null;
    }

    public static Collection<Packet> parseCollection(String string) {
        return Stream.of(string.split("\n")).map(Packet::parsePacket).collect(Collectors.toList());
    }

    public static Packet parsePacket(String p) {
        String[] a = p.split("[,\\s]+");
        int id = Integer.parseInt(a[0], 16);
        return Packet.create(0xFFFFFF & (id >> 8),
                             0xFF & id,
                             Stream.of(Arrays.copyOfRange(a, 1, a.length, String[].class))
                                   .mapToInt(s -> Integer.parseInt(s, 16))
                                   .toArray());
    }

    public static Packet parseVector(LocalDateTime start, String line) {
        String[] a = line.trim().split("\\s+");
        if (a.length > 5 && a[1].equals("1") && a[3].equals("Rx")) {
            int id = Integer.parseInt(a[2].substring(0, a[2].length() - 1), 16);

            return new Packet(start.plusNanos((long) (Double.parseDouble(a[0]) * 1000000000)),
                              6,
                              0xFFFFFF & (id >> 8),
                              0xFF & id,
                              false,
                              Stream.of(Arrays.copyOfRange(a, 6, 6 + Integer.parseInt(a[5]), String[].class))
                                    .mapToInt(s -> Integer.parseInt(s, 16))
                                    .toArray());
        }
        return null;
    }

    synchronized public void fail() {
        data = new int[0];
        notifyAll();
    }

    /**
     * Returns one byte (8-bits) from the data at the given index
     *
     * @param  i
     *               the index
     * @return   int
     */
    public int get(int i) {
        return getData()[i];
    }

    /**
     * Returns two bytes (16-bits) from the data at the given index and index+1
     *
     * @param  i
     *               the index
     * @return   int
     */
    public int get16(int i) {
        int[] d = getData();
        return (d[i + 1] << 8) | d[i];
    }

    /**
     * Returns two bytes (16-bits) from the data in Big-endian format at the
     * given index and index+1
     *
     * @param  i
     *               the index
     * @return   int
     */
    public int get16Big(int i) {
        int[] d = getData();
        return (d[i] << 8) | d[i + 1];
    }

    /**
     * Returns three bytes (24-bits) from the data at the given index, index+1
     * and index+2
     *
     * @param  i
     *               the index
     * @return   int
     */
    public int get24(int i) {
        int[] d = getData();
        return (d[i + 2] << 16) | (d[i + 1] << 8) | d[i];
    }

    /**
     * Returns three bytes (24-bits) from the data in Big-endian format at the
     * given index, index+1, and index+2
     *
     * @param  i
     *               the index
     * @return   int
     */
    public int get24Big(int i) {
        int[] d = getData();
        return (d[i] << 16) | (d[i + 1] << 8) | d[i + 2];
    }

    /**
     * Returns four bytes (32-bits) from the data at the given index, index+1,
     * index+2, and index+3
     *
     * @param  i
     *               the index
     * @return   int
     */
    public long get32(int i) {
        int[] d = getData();
        return ((long) (d[i + 3] & 0xFF) << 24) | ((d[i + 2] & 0xFF) << 16)
                | ((d[i + 1] & 0xFF) << 8) | (d[i] & 0xFF);
    }

    /**
     * Returns four bytes (32-bits) from the data in Big-endian format at the
     * given index, index+1, index+2, and index+3
     *
     * @param  i
     *               the index
     * @return   int
     */
    public long get32Big(int i) {
        int[] d = getData();
        return ((long) d[i] << 24) | ((long) d[i + 1] << 16) | ((long) d[i + 2] << 8)
                | d[i + 3];
    }

    public long get64() {
        return ((get32(0)) << 32) | get32(4);
    }

    /**
     * Returns the data as an array of bytes
     *
     * @return byte[]
     */
    public byte[] getBytes() {
        int[] d = getData();
        byte[] bytes = new byte[d.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) d[i];
        }
        return bytes;
    }

    synchronized private int[] getData() {
        if (!isValid()) {
            throw new PacketException(String.format("Failed Packet: %s %06X%02X [?]%n%s",
                                                    DateTimeModule.getInstance().getTimeFormatter().format(timestamp),
                                                    priority << 18 | id,
                                                    source,
                                                    getFragments().stream()
                                                                  .map(p -> p.toString())
                                                                  .collect(Collectors.joining(System.lineSeparator()))));
        }
        return data;
    }

    synchronized public boolean isValid() {
        while (data == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // No worries
            }
        }
        return data.length > 0;
    }

    synchronized public void setData(byte... data) {
        if (isComplete()) {
            throw new PacketException("Packet already initialized.");
        }
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = (0xFF & data[i]);
        }
        notifyAll();
    }

    /**
     * Returns the data from the beginIndex to the endIndex (inclusive).
     *
     * @param  beginIndex
     *                        the first data value to return
     * @param  endIndex
     *                        the last data value to return
     * @return            int[]
     */
    @SuppressFBWarnings(value = "UG_SYNC_SET_UNSYNC_GET", justification = "This method is not a reciprocal of the setData method")
    public int[] getData(int beginIndex, int endIndex) {
        return Arrays.copyOfRange(getData(), beginIndex, endIndex);
    }

    public int getPgn() {
        int id = getId(0x3FFFF);
        if (id < 0xF000) {
            id &= 0xFF00;
        }
        return id;
    }

    /**
     * Returns the destination address
     *
     * @return the destination specific address or GLOBAL_ADDR
     */
    public int getDestination() {
        return getId(0x3FFFF) < 0xF000 ? getId(0xFF) : J1939.GLOBAL_ADDR;
    }

    public List<Packet> getFragments() {
        return fragments;
    }

    public void setFragments(List<Packet> fragments) {
        this.fragments = fragments;
    }

    /**
     * Returns the ID of the packet
     *
     * @param  mask
     *                  Because the whole id rarely ever used, provide the mask.
     * @return      int
     */
    public int getId(int mask) {
        return id & mask;
    }

    /**
     * Returns the total number of data bytes in the packet
     *
     * @return int
     */
    public int getLength() {
        return getData().length;
    }

    /**
     * Returns the priority of the packet
     *
     * @return int
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the source address of the packet
     *
     * @return int
     */
    public int getSource() {
        return source;
    }

    /**
     * Returns the Time the packet was received
     *
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp2) {
        timestamp = timestamp2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, priority, source, transmitted, Arrays.hashCode(getData()));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Packet)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        Packet that = (Packet) obj;
        return id == that.id && priority == that.priority && source == that.source && transmitted == that.transmitted
                && Objects.deepEquals(getData(), that.getData());
    }

    @Override
    public String toString() {
        return String.format("%06X%02X [%s] %s",
                             priority << 18 | id,
                             source,
                             getLength(),
                             hexData() + (transmitted ? TX : RX));
    }

    private String hexData() {
        return Arrays.stream(getData())
                     .mapToObj(x -> String.format("%02X", x))
                     .collect(Collectors.joining(" "));
    }

    public boolean isComplete() {
        return data != null;
    }

    /**
     * Returns true if this packet was transmitted by the application
     *
     * @return boolean
     */
    public boolean isTransmitted() {
        return transmitted;
    }

    /**
     * Creates the {@link String} of the Packet including the time received
     * formatted by the {@link DateTimeFormatter}. If the formatter is null, the
     * time is not included
     *
     * @return a {@link String}
     */
    public String toTimeString() {
        /*
         * Collect data first, because timestamp is dynamic until the data is collected. This will block on the data. We
         * want to report the timestamp of final packet.
         */
        String dataString = toString();
        return DateTimeModule.getInstance().getTimeFormatter().format(timestamp) + " " + dataString;
    }

    /**
     * Vector compatible log record.
     * 
     * @param start
     */
    public String toVectorString(Temporal start) {
        final ZoneOffset offset = OffsetDateTime.now().getOffset();
        getData(); // wait for all data before formatting time
        return String.format("%4.6f 1  %06X%02Xx %s d %d %s",
                             Duration.between(start, getTimestamp().toInstant(offset)).toNanos() / 1000000000.0,
                             priority << 18 | id,
                             getSource(),
                             isTransmitted() ? "Tx" : "Rx",
                             getLength(),
                             hexData());
    }

    static public class PacketException extends RuntimeException {

        public PacketException(String string) {
            super(string);
        }

    }

    private String toSingleDeltaTimeString(Packet sent) {
        return String.format("%s [%.1f ms]",
                             toTimeString(),
                             Duration.between(sent.getTimestamp(), getTimestamp()).toNanos() / 1000000.0);
    }

    public String toDeltaTimeString(Packet sent) {
        if (!getFragments().isEmpty() && getFragments().get(0) != this) {
            String thisString;
            try {
                thisString = toSingleDeltaTimeString(sent);
            } catch (PacketException e) {
                return "Error: " + e.getMessage();
            }
            StringBuilder sb = new StringBuilder();
            Packet last = sent;
            for (Packet p : getFragments()) {
                sb.append(p.toSingleDeltaTimeString(last));
                sb.append(System.lineSeparator());
                last = p;
            }
            return sb.append(thisString).toString();
        } else {
            return toSingleDeltaTimeString(sent);
        }
    }
}
