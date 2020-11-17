/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.modules.DateTimeModule;

/**
 * Sends a Packet containing an id with data from a source onto the bus
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class Packet {

    /**
     * The indication that a packet was transmitted
     */
    public static final String TX = " (TX)";

    public static Packet create(int id, int source, boolean transmitted, int... data) {
        return new Packet(LocalDateTime.now(), 6, id, source, transmitted, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param bytes
     *            the data bytes of the packet
     * @return Packet
     */
    public static Packet create(int id, int source, byte... bytes) {
        return create(6, id, source, false, bytes);
    }

    /**
     * Creates an instance of Packet
     *
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param data
     *            the data of the packet
     * @return Packet
     */
    public static Packet create(int id, int source, int... data) {
        return create(id, source, false, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param priority
     *            the priority of the packet
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param transmitted
     *            indicates the packet was sent by the application
     * @param bytes
     *            the data bytes of the packet
     * @return Packet
     */
    public static Packet create(int priority, int id, int source, boolean transmitted, byte... bytes) {
        int[] data = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i];
        }
        return new Packet(LocalDateTime.now(), priority, id, source, transmitted, data);
    }

    /**
     * Converts the value produced by Packet.toString() back into a Packet
     *
     * @param string
     *            the {@link String} to parse
     * @return a Packet or null if the string could not be parsed
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

            byte[] bytes = new byte[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                bytes[i - 1] = (byte) (Integer.parseInt(parts[i].trim(), 16) & 0xFF);
            }

            return Packet.create(priority, id, source, tx, bytes);
        } catch (Exception e) {
            J1939_84.getLogger().log(Level.SEVERE, string + " could not be parsed into a Packet", e);
        }
        return null;
    }

    public static Collection<Packet> parseCollection(String string) {
        return Stream.of(string.split("\n")).map(p -> parsePacket(p)).collect(Collectors.toList());
    }

    public static Packet parsePacket(String p) {
        String[] a = p.split("[,\\s]+");
        int id = Integer.parseInt(a[0], 16);
        return Packet.create(0xFFFFFF & (id >> 8),
                0xFF & id,
                Stream.of(Arrays.copyOfRange(a, 1, a.length, String[].class))
                        .mapToInt(s -> Integer.parseInt(s, 16)).toArray());
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
                            .mapToInt(s -> Integer.parseInt(s, 16)).toArray());
        }
        return null;
    }

    private final int[] data;

    private final int id;

    private final int priority;

    private final int source;

    private final LocalDateTime timestamp;

    private final boolean transmitted;

    /**
     * Creates a Packet
     *
     * @param priority
     *            the priority of the packet
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param transmitted
     *            indicates the packet was sent by the application
     * @param data
     *            the data of the packet
     */
    private Packet(LocalDateTime timestamp, int priority, int id, int source, boolean transmitted, int... data) {
        this.timestamp = timestamp;
        this.priority = priority;
        this.id = id;
        this.source = source;
        this.transmitted = transmitted;
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = (0xFF & data[i]);
        }
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
                && Objects.deepEquals(data, that.data);
    }

    /**
     * Returns one byte (8-bits) from the data at the given index
     *
     * @param i
     *            the index
     * @return int
     */
    public int get(int i) {
        return data[i];
    }

    /**
     * Returns two bytes (16-bits) from the data at the given index and index+1
     *
     * @param i
     *            the index
     * @return int
     */
    public int get16(int i) {
        return (data[i + 1] << 8) | data[i];
    }

    /**
     * Returns two bytes (16-bits) from the data in Big-endian format at the
     * given index and index+1
     *
     * @param i
     *            the index
     * @return int
     */
    public int get16Big(int i) {
        return (data[i] << 8) | data[i + 1];
    }

    /**
     * Returns three bytes (24-bits) from the data at the given index, index+1
     * and index+2
     *
     * @param i
     *            the index
     * @return int
     */
    public int get24(int i) {
        return (data[i + 2] << 16) | (data[i + 1] << 8) | data[i];
    }

    /**
     * Returns three bytes (24-bits) from the data in Big-endian format at the
     * given index, index+1, and index+2
     *
     * @param i
     *            the index
     * @return int
     */
    public int get24Big(int i) {
        return (data[i] << 16) | (data[i + 1] << 8) | data[i + 2];
    }

    /**
     * Returns four bytes (32-bits) from the data at the given index, index+1,
     * index+2, and index+3
     *
     * @param i
     *            the index
     * @return int
     */
    public long get32(int i) {
        return ((data[i + 3] & 0xFF) << 24) | ((data[i + 2] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8)
                | (data[i] & 0xFF);
    }

    /**
     * Returns four bytes (32-bits) from the data in Big-endian format at the
     * given index, index+1, index+2, and index+3
     *
     * @param i
     *            the index
     * @return int
     */
    public long get32Big(int i) {
        return (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | data[i + 3];
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
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) data[i];
        }
        return bytes;
    }

    /**
     * Returns the data from the beginIndex to the endIndex (inclusive).
     *
     * @param beginIndex
     *            the first data value to return
     * @param endIndex
     *            the last data value to return
     * @return int[]
     */
    public int[] getData(int beginIndex, int endIndex) {
        return Arrays.copyOfRange(data, beginIndex, endIndex);
    }

    /**
     * Returns the destination address
     *
     * @param requestPacket
     *            the request
     * @return the destination specific address or GLOBAL_ADDR
     */
    public int getDestination() {
        return getId(0xFFFF) < 0xF000 ? getId(0xFF) : J1939.GLOBAL_ADDR;
    }

    @Deprecated
    public int getId() {
        return id;
    }

    /**
     * Returns the ID of the packet
     *
     * @param mask
     *            Because the whole id rarely ever used, provide the mask.
     * @return int
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
        return data.length;
    }

    public int getPgn() {
        // FIXME should this mask be 7FFFF?
        int id = getId(0xFFFF);
        if (id < 0xF000) {
            id &= 0xFF00;
        }
        return id;
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

    @Override
    public int hashCode() {
        return Objects.hash(id, priority, source, transmitted, Arrays.hashCode(data));
    }

    /**
     * Returns true if this packet was transmitted by the application
     *
     * @return boolean
     */
    public boolean isTransmitted() {
        return transmitted;
    }

    public boolean matchesPgn(int pgn) {
        return (id < 0xF000 ? (id & 0xFF00) : id) == pgn;
    }

    @Override
    public String toString() {
        return String.format("%06X%02X %s",
                priority << 18 | id,
                source,
                Arrays.stream(data).mapToObj(x -> String.format("%02X", x)).collect(Collectors.joining(" "))
                        + (transmitted ? TX : ""));
    }

    /**
     * Creates the {@link String} of the Packet including the time received
     * formatted by the {@link DateTimeFormatter}. If the formatter is null, the
     * time is not included
     *
     * @param formatter
     *            the {@link DateTimeFormatter} to format the time received
     * @return a {@link String}
     */
    public String toTimeString() {
        return DateTimeModule.getInstance()
                .getTimeFormatter().format(timestamp) + " " + toString();
    }
}
