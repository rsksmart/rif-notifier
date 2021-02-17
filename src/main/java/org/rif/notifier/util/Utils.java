package org.rif.notifier.util;

import org.web3j.crypto.*;
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
     * generates a 64 byte sha3 hash
     * @param content
     * @return
     */
    public static String generateHash(String content)   {
        return Hash.sha3String(content);
    }

    /**
     * Signs the given message with the private key signature
     * @param hash
     * @param privateKey
     * @return
     */
    public static String sign(String hash, String privateKey) {
        byte [] msg = Numeric.hexStringToByteArray(hash);
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        Sign.SignatureData data = Sign.signPrefixedMessage(msg, keyPair);
        byte [] sigbytes = new byte[65];
        System.arraycopy(data.getR(), 0, sigbytes, 0, 32);
        System.arraycopy(data.getS(), 0, sigbytes, 32, 32);
        sigbytes[64] = data.getV();
        String result = Numeric.toHexString(sigbytes);
        return result;
    }

    /**
     * Signs the given hash with privatekey and returns a string representation of the signature
     * @param hash
     * @param privateKey
     * @return
     */
    public static String signAsString(String hash, String privateKey)   {
        Sign.SignatureData signatureData = signAsSignatureData(hash, privateKey);
        String sig = Numeric.toHexString(getSignatureBytes(signatureData));
        return sig;
    }

    public static Sign.SignatureData signAsSignatureData(String hash, String privateKey)    {
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        Sign.SignatureData signatureData =
                Sign.signPrefixedMessage(Numeric.hexStringToByteArray(hash), ecKeyPair);
        return signatureData;
    }

    /**
     * Returns the r s v bytes of the signature
     * @param signatureData
     * @return
     */
    public static byte[] getSignatureBytes(Sign.SignatureData signatureData) {
        byte[] sig = new byte[65];
        System.arraycopy(signatureData.getR(), 0, sig, 0, 32);
        System.arraycopy(signatureData.getS(), 0, sig, 32, 32);
        //sig[64] = (byte) ((signatureData.getV() & 0xFF) - 27);
        //v is already compatible with ecrecover so no conversion needed
        sig[64] = (byte)signatureData.getV();
        return sig;
    }

    public static Sign.SignatureData getSignatureData(String signature) {
        byte [] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += (byte)27;
        }

        Sign.SignatureData sd = new Sign.SignatureData(
                v,
                (byte[]) Arrays.copyOfRange(signatureBytes, 0, 32),
                (byte[]) Arrays.copyOfRange(signatureBytes, 32, 64));
        return sd;
    }

    public static void verify(String privateKey)   {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
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
                v += (byte)27;
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
