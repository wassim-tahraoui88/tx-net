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
	public String getServerURL() { return rootConfig.loadStringProperty(KEY_SERVER_URL); }
	public void getServerPort() { rootConfig.loadIntProperty(KEY_SERVER_PORT); }
	public int getServerSize() { return rootConfig.loadIntProperty(KEY_SERVER_SIZE); }
	public int getMaxFileSize() { return rootConfig.loadIntProperty(KEY_MAX_FILE_SIZE,DEFAULT_MAX_FILE_SIZE); }
	//endregion

	//region Encryption Properties
	public boolean isSecurityEnabled() { return rootConfig.loadBooleanProperty(KEY_SECURITY_ENABLED,false); }
	int getAESKeyLength() { return rootConfig.loadIntProperty(KEY_SECURITY_KEY_AES_LENGTH,256); }
	int getRSAKeyLength() { return rootConfig.loadIntProperty(KEY_SECURITY_KEY_RSA_LENGTH,2024); }
	int getIVLength() { return rootConfig.loadIntProperty(KEY_SECURITY_IV_LENGTH,16); }
	//endregion

}
