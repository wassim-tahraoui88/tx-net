package com.tahraoui.xnet.util;

import com.tahraoui.xnet.exception.XNetEncryptionException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EncryptionUtils {

	private static final String KEY_ALGORITHM = "AES";
	private static final String KEY_SHARING_ALGORITHM = "RSA";
	private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

	public static SecretKey generateKey() throws XNetEncryptionException {
		try {
			var keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
			keyGenerator.init(XNetConfig.getInstance().getAESKeyLength());
			return keyGenerator.generateKey();
		}
		catch (GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}

	public static byte[] encrypt(Object data, SecretKey key, IvParameterSpec iv) throws XNetEncryptionException {
		try {
			return encrypt(serialize(data), key, iv);
		}
		catch (IOException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}
	public static byte[] encrypt(byte[] bytes, SecretKey key, IvParameterSpec iv) throws XNetEncryptionException {
		try {
			var cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			return cipher.doFinal(bytes);
		}
		catch (GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}
	public static Object decrypt(byte[] encrypted, SecretKey key, IvParameterSpec iv) throws XNetEncryptionException {
		try {
			var cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			return deserialize(cipher.doFinal(encrypted));
		}
		catch (IOException | ClassNotFoundException | GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}

	public static KeyPair generateKeyPair() throws XNetEncryptionException {
		try {
			var keyPairGenerator = KeyPairGenerator.getInstance(KEY_SHARING_ALGORITHM);

			keyPairGenerator.initialize(XNetConfig.getInstance().getRSAKeyLength());
			return keyPairGenerator.generateKeyPair();
		}
		catch (GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}
	public static byte[] encryptRSA(byte[] message, PublicKey key) throws XNetEncryptionException {
		try {
			var cipher = Cipher.getInstance(KEY_SHARING_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(message);
		}
		catch (GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}
	public static SecretKey decryptRSA(byte[] encrypted, PrivateKey key) throws XNetEncryptionException {
		try {
			var cipher = Cipher.getInstance(KEY_SHARING_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			var decryptedAESKey = cipher.doFinal(encrypted);
			return new SecretKeySpec(decryptedAESKey, 0, decryptedAESKey.length, KEY_ALGORITHM);
		}
		catch (GeneralSecurityException e) {
			throw new XNetEncryptionException(e.getMessage());
		}
	}

	public static IvParameterSpec generateIV() {
		var iv = new byte[XNetConfig.getInstance().getIVLength()];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	private static byte[] serialize(Object data) throws IOException {
		try (var baos = new ByteArrayOutputStream(); var oos = new ObjectOutputStream(baos)) {
			oos.writeObject(data);
			return baos.toByteArray();
		}
	}
	private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		try (var bais = new ByteArrayInputStream(data); var ois = new ObjectInputStream(bais)) {
			return ois.readObject();
		}
	}

	public record XNetKeySpecs(KeyPair rsaKeyPair, SecretKey aesKey, IvParameterSpec iv) {}
}
