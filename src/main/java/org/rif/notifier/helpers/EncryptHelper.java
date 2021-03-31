package org.rif.notifier.helpers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.rif.notifier.boot.configuration.NotifierConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class EncryptHelper {

    private String secretKey;
    public EncryptHelper(@Qualifier("providerPrivateKey") String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * uses stronger AES/GCM to encrypt the given text
     * @param plainText
     * @return
     */
    public String encrypt(String plainText) {
        if(StringUtils.isBlank(plainText)) {
            return plainText;
        }
        String salt = KeyGenerators.string().generateKey();

        BytesEncryptor encryptor = Encryptors.stronger(this.secretKey, salt);
        byte[] encrypted = encryptor.encrypt(plainText.getBytes());

        byte[] saltAndSecret = ArrayUtils.addAll(Hex.decode(salt), encrypted);
        return Base64.getEncoder().encodeToString(saltAndSecret);
    }

    /**
     * decrypts AES/GCM encrypted to plainText
     * @param base64Data
     * @return
     */
    public String decrypt(String base64Data) {
        if(StringUtils.isBlank(base64Data)) {
            return base64Data;
        }
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        byte[] saltBytes = ArrayUtils.subarray(bytes, 0, 8);
        byte[] encryptedBytes = ArrayUtils.subarray(bytes, 8, bytes.length);
        String salt = new String(Hex.encode(saltBytes));
        BytesEncryptor encryptor = Encryptors.stronger(this.secretKey, salt);
        return new String(encryptor.decrypt(encryptedBytes));
    }
}
