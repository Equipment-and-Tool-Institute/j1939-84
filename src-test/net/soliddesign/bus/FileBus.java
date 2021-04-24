/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package net.soliddesign.bus;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.MultiQueue;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.J1939TP;
import org.etools.j1939_84.resources.Resources;

/**
 * Packet bus using packets read from log files generated by this tool
 * The files need to be placed in src(-test)/org/etools/resources
 * It may be necessary to rebuild the project to ensure the files are copied to the build directory
 * The expected name is j1939_84x.log where is in the numbers
 */
public class FileBus implements Bus {

    public static void main(String... args) throws BusException {
        J1939TP bus = new J1939TP(new FileBus(0xF9));
        J1939 j1939 = new J1939(bus);
        bus.setJ1939(j1939);

        j1939.read(1, TimeUnit.DAYS)
             .forEach(System.out::println);

        System.exit(0);
    }

    private long firstNanos = -1;
    private long epochNanos = -1;

    private final List<Packet> packets = new ArrayList<>();

    private final MultiQueue<Packet> queue = new MultiQueue<>();

    private boolean isClosed = false;

    private final int address;

    public FileBus(int address) {
        this.address = address;
        readLogFiles();

        new Thread(this::queuePackets).start();
    }

    private void readLogFiles() {
        J1939DaRepository.getInstance().findPgnDefinition(0); // To initialize the J1939DaRepository

        // The packets can be out of order in the log files.
        // This reads them into memory to ensure they are played back in order
        System.out.println("Reading log files");
        for (int x = 20; x >= 0; x--) {
            try {
                readLogFile("j1939_84" + x + ".log");
            } catch (NullPointerException ignored) {
                // The file doesn't exist
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        packets.sort(Comparator.comparing(Packet::getTimestamp));
    }

    private void queuePackets() {
        System.out.println("Queuing Packets");
        for (Packet packet : packets) {
            waitUntilTimeToQueue(packet);
            queue.add(packet);
            if (isClosed) {
                break;
            }
        }

        close();
    }

    private void waitUntilTimeToQueue(Packet packet) {
        long packetNanos = packet.getTimestamp().toLocalTime().toNanoOfDay();

        if (firstNanos == -1) {
            firstNanos = packetNanos;
            epochNanos = currentNanos();
        }

        long nanosAtTimeToSend = epochNanos + (packetNanos - firstNanos);
        long delay = nanosAtTimeToSend - currentNanos();
        if (delay > 0) {
            long start = System.nanoTime();
            // noinspection StatementWithEmptyBody
            while (start + delay >= System.nanoTime()) {
            }
        }
    }

    private long currentNanos() {
        return LocalDateTime.now().toLocalTime().toNanoOfDay();
    }

    private void readLogFile(String fileName) throws IOException {
        try (BufferedReader reader = createReader(fileName)) {
            String line = reader.readLine();
            while (line != null) {
                String[] splits = line.split("lambda\\$decodeDataAndQueuePacket");
                if (splits.length == 2) {
                    String substring = splits[1].substring(17);
                    Packet packet = Packet.parse(substring);

                    String time = splits[1].substring(3, 16);
                    LocalTime localTime = LocalTime.parse(time);
                    LocalDateTime localDateTime = LocalDate.now().atTime(localTime);
                    requireNonNull(packet).setTimestamp(localDateTime);

                    packets.add(packet);
                }
                line = reader.readLine();
            }
        }
    }

    @Override
    public void close() {
        System.out.println("Closing");
        isClosed = true;
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
    public int getConnectionSpeed() {
        return 500;
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return queue.stream(timeout, unit);
    }

    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        queue.resetTimeout(stream, time, unit);
    }

    @Override
    public Packet send(Packet packet) throws BusException {
        return packet;
    }

    @Override
    public boolean imposterDetected() {
        return false;
    }

    private BufferedReader createReader(String fileName) {
        InputStream inputStream = Resources.class.getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(requireNonNull(inputStream), UTF_8));
    }
}
