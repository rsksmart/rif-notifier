package org.rif.notifier.util;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class Utils {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe

    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    private static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    /**
     * Try to parse the classname, returns true if it's a valid one
     * @param className
     * @return
     */
    public static Boolean isClass(String className){
        try  {
            Class.forName(className);
            return true;
        }  catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Used to generate apiKey for users
     * @return apiKey
     */
    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Unsings a signature and compares with the given address, if everything goes well, it returns true, otherwise returns false.
     * @param address Address to be compared with the unsigned signature
     * @param signature Address signed
     * @return
     */
    public static boolean canRecoverAddress(String address, String signature){
        try {
            String prefix = PERSONAL_MESSAGE_PREFIX + address.length();
            byte[] msgHash = Hash.sha3((prefix + address).getBytes());

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }

            Sign.SignatureData sd = new Sign.SignatureData(
                    v,
                    (byte[]) Arrays.copyOfRange(signatureBytes, 0, 32),
                    (byte[]) Arrays.copyOfRange(signatureBytes, 32, 64));

            String addressRecovered = null;
            boolean match = false;

            // Iterate for each possible key to recover
            for (int i = 0; i < 4; i++) {
                BigInteger publicKey = Sign.recoverFromSignature(
                        (byte) i,
                        new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
                        msgHash);

                if (publicKey != null) {
                    addressRecovered = "0x" + Keys.getAddress(publicKey);

                    if (addressRecovered.toLowerCase().equals(address.toLowerCase())) {
                        return true;
                    }
                }
            }

        }catch (Exception ignored){
            //If it enters here is because the user send incorrect params
        }
        return false;
    }
}
