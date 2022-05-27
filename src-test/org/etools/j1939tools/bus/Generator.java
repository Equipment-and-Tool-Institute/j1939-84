package org.etools.j1939tools.bus;

import static org.etools.j1939tools.bus.RP1210Library.BLOCKING_NONE;
import static org.etools.j1939tools.bus.RP1210Library.NOTIFICATION_NONE;

public class Generator {
    public static void main(String... args) throws BusException {
        String name = "NXULNK32";
        // String name = "NULN2R32";
        short devid = 1;
        short address = 1;

        RP1210 rp1210 = new RP1210();
        rp1210.getAdapters()
              .stream()
              .filter(a -> a.getDLLName().equals(name) && a.getDeviceId() == devid)
              .findFirst()
              .ifPresent(a -> {
                  try {
                      RP1210Library rp1210Library = RP1210Library.load(a);
                      short clientId = rp1210Library.RP1210_ClientConnect(0,
                                                                          a.getDeviceId(),
                                                                          "J1939:Baud=Auto",
                                                                          0,
                                                                          0,
                                                                          (short) 0);
                      int count = 0;
                      long start = 0;
                      while (true) {
                          if (count % 1000 == 0) {
                              long now = System.currentTimeMillis();
                              System.err.println(count * 1000.0 / (now - start) + " packets/s");
                              start = now;
                              count = 0;
                          }
                          count++;
                          Packet packet = Packet.create(0xFF11,
                                                              address,
                                                              count >> 24,
                                                              count >> 16,
                                                              count >> 8,
                                                              count,
                                                              5,
                                                              6,
                                                              7,
                                                              8);
                          byte[] data = RP1210Bus.encode(packet);
                          rp1210Library.RP1210_SendMessage(clientId,
                                                           data,
                                                           (short) data.length,
                                                           NOTIFICATION_NONE,
                                                           BLOCKING_NONE);
                          // while we are sending faster than 1500 packet/s, wait
                          while (count * 1000.0 / (System.currentTimeMillis() - start) > 3000) {
                          }
                      }
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              });
        System.err.println("Adapter not found:" + name + ": " + devid);
    }
}
