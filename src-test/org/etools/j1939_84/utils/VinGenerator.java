/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import java.util.Random;

/**
 * Generates VINs. While the are 17 valid characters and checksummed, the actual
 * values contained are meaningless
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VinGenerator {

    private static final String ALLOWED_CHARS = "0123456789ABCDEFGHJKLMNPRSTUVWXYZ";

    public static void main(String... strings) {
        VinGenerator generator = new VinGenerator();
        VinDecoder vinDecoder = new VinDecoder();
        int nextModelYear = 2010;
        while (nextModelYear < 2040) {
            String vin = generator.generateVin();
            int modelYear = vinDecoder.getModelYear(vin);
            if (modelYear == nextModelYear) {
                // This System.out.println() needs to be here for visibility of the VIN
                System.out.println("ModelYear " + nextModelYear + ": " + vin);
                nextModelYear++;
            }
        }
    }

    private final Random random = new Random();

    private final VinDecoder vinDecoder = new VinDecoder();

    /**
     * Generates a 17 character VIN
     *
     * @return the generated VIN
     */
    public String generateVin() {
        String vin = null;
        while (!vinDecoder.isVinValid(vin)) {
            vin = randomVin();
        }
        return vin;
    }

    private char getRandomVinChar() {
        return ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length()));
    }

    private String randomVin() {
        StringBuilder vinSb = new StringBuilder(17);
        for (int i = 0; i < 17; i++) {
            vinSb.append(getRandomVinChar());
        }
        String vin = vinSb.toString();
        char checkSumChar = new VinDecoder().calculateCheckSum(vin);
        return vin.substring(0, 8) + Character.toString(checkSumChar) + vin.substring(9, 17);
    }
}
