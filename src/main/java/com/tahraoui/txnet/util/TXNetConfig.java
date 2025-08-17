package com.tahraoui.txnet.util;

import com.tahraoui.txcore.TXConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TXNetConfig {

	public enum GeneratedId {
		HASH, SEQ
	}

	private static final Logger LOGGER = LogManager.getLogger(TXNetConfig.class);
	private static final String LOGGER_PACKAGE = "com.tahraoui.txnet";

	private static final int DEFAULT_MAX_FILE_SIZE = 1024 * 1024 * 10;

	private static TXNetConfig instance;
	public static TXNetConfig getInstance() {
		if (instance == null) instance = new TXNetConfig();
		return instance;
	}

	//region Properties String Caches
	private static final String KEY_SERVER_URL = "tx-net.server.url", KEY_SERVER_PORT = "tx-net.server.port",
			KEY_SERVER_SIZE = "tx-net.server.size", KEY_SERVER_GENERATED_ID = "tx-net.server.generated_id",
			KEY_SECURITY_ENABLED = "tx-net.security.enabled",
			KEY_SECURITY_KEY_AES_LENGTH = "tx-net.security.key.aes.length",
			KEY_SECURITY_KEY_RSA_LENGTH = "tx-net.security.key.rsa.length",
			KEY_SECURITY_IV_LENGTH = "tx-net.security.iv.length",
			KEY_MAX_FILE_SIZE = "tx-net.file.size.max",
			KEY_DEBUG_ENABLED = "tx-net.debug.enabled", KEY_DEBUG_LEVEL = "tx-net.debug.level";
	//endregion

	private final TXConfig rootConfig;

	private TXNetConfig() {
		this.rootConfig = TXConfig.getInstance();
		initLogger();
	}
	private void initLogger() {
		var isDebugEnabled = rootConfig.loadBooleanProperty(KEY_DEBUG_ENABLED,true);
		var debugLevel = Level.getLevel(rootConfig.loadStringProperty(KEY_DEBUG_LEVEL,"DEBUG"));
		rootConfig.setLogger(LOGGER_PACKAGE, isDebugEnabled, debugLevel);
	}

	//region Server Properties
	/**
	 * Returns the type of generated ID used by the server.
	 * Default is SEQ (Sequence).
	 *
	 * @return the GeneratedId type
	 */
	public GeneratedId getGeneratedId() {
		var id = rootConfig.loadStringProperty(KEY_SERVER_GENERATED_ID,"seq").toUpperCase();
		try {
			return GeneratedId.valueOf(id);
		}
		catch (IllegalArgumentException e) {
			LOGGER.warn("Invalid generated ID type: {}, defaulting to SEQ (Sequence)", id);
			return GeneratedId.SEQ;
		}
	}
	/**
	 * Returns the server URL.
	 *
	 * @return the server URL
	 */
	public String getServerURL() { return rootConfig.loadStringProperty(KEY_SERVER_URL); }
	/**
	 * Returns the server port.
	 *
	 * @return the server port
	 */
	public int getServerPort() { return rootConfig.loadIntProperty(KEY_SERVER_PORT); }
	/**
	 * Returns the server size.
	 * This is the maximum number of clients that can connect to the server.
	 *
	 * @return the server size
	 */
	public int getServerSize() { return rootConfig.loadIntProperty(KEY_SERVER_SIZE); }
	/**
	 * Returns the maximum size allowed for file transfers.
	 * Default is 10 MB (10 * 1024 * 1024 bytes).
	 *
	 * @return the maximum file size.
	 */
	public int getMaxFileSize() { return rootConfig.loadIntProperty(KEY_MAX_FILE_SIZE, DEFAULT_MAX_FILE_SIZE); }
	//endregion

	//region Encryption Properties
	/**
	 * Indicates whether security features are enabled.
	 * If true, encryption and decryption will be applied to data transfers.
	 * Default is false.
	 *
	 * @return true if security is enabled, false otherwise
	 */
	public boolean isSecurityEnabled() { return rootConfig.loadBooleanProperty(KEY_SECURITY_ENABLED,false); }
	/**
	 * Returns the length of the AES key used for encryption.
	 * Default is 256 bits.
	 *
	 * @return the AES key length in bits
	 */
	int getAESKeyLength() { return rootConfig.loadIntProperty(KEY_SECURITY_KEY_AES_LENGTH,256); }
	/**
	 * Returns the length of the RSA key used for key sharing.
	 * Default is 2048 bits.
	 *
	 * @return the RSA key length in bits
	 */
	int getRSAKeyLength() { return rootConfig.loadIntProperty(KEY_SECURITY_KEY_RSA_LENGTH,2048); }
	/**
	 * Returns the length of the IV (Initialization Vector) used for AES encryption.
	 * Default is 16 bytes (128 bits).
	 *
	 * @return the IV length in bytes
	 */
	int getIVLength() { return rootConfig.loadIntProperty(KEY_SECURITY_IV_LENGTH,16); }
	//endregion

}
