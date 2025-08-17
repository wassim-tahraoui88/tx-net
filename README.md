# TXNet

### Table of Contents:
- [Description](#description)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [To Do](#to-do)

### Description:
This package provides a simple and efficient way to handle TCP socket connections in Java.<br>
It abstracts the complexities of socket programming, allowing developers to focus on building their applications.<br>
It includes features like connection management, data transmission, and error handling.

### Installation:
You can install the package with maven by adding the following dependency to your `pom.xml`:

```xml
<dependency>
	<groupId>dev.wassim-tahraoui</groupId>
	<artifactId>tx-net</artifactId>
	<version>1.0.0</version>
</dependency>
```

### Configuration:
The properties of **TXNet** and `TXNetConfig` be set using `application.properties` file in your `resources` directory.
Here is a table of the available properties:

| Property                       | Default Value   | Description                                      |
|--------------------------------|-----------------|--------------------------------------------------|
| tx-net.server.url              | (none)          | The server URL to connect to.                    |
| tx-net.server.port             | (none)          | The server port to connect to.                   |
| tx-net.server.size             | (none)          | The maximum number of concurrent connections.    |
| tx-net.server.generated_id     | seq             | Packet ID generation mode: `hash` or `seq`.      |
| tx-net.security.enabled        | false           | Enable encryption for data transmission.         |
| tx-net.security.key.aes.length | 256             | AES encryption key length (bits).                |
| tx-net.security.key.rsa.length | 2048            | RSA encryption key length (bits).                |
| tx-net.security.iv.length      | 16              | Initialization vector length for encryption.     |
| tx-net.file.size.max           | 10485760 (10MB) | Maximum allowed file size for transfers (bytes). |
| tx-net.debug.enabled           | true            | Logging activity status.                         |
| tx-net.debug.level             | DEBUG           | Logging level (`DEBUG`, `INFO`, `WARN`, etc.).   |

_*Properties with `(none)` as default value are required and must be set or not be used at all._

**Example:**
```properties
tx-net.server.url=127.0.0.1
tx-net.server.port=8080
tx-net.server.size=100
tx-net.server.generated_id=seq
tx-net.security.enabled=true
tx-net.security.key.aes.length=256
tx-net.security.key.rsa.length=2048
tx-net.security.iv.length=16
tx-net.file.size.max=10485760
tx-net.debug.enabled=true
tx-net.debug.level=DEBUG
```

### Usage
To use **TXNet**, you need to create your own implementations of the TXNetPacketRequestHandler and TXNetServiceListener.
Here is a simple example:
```java
import com.tahraoui.txnet.packet.TXNetPacketRequestHandler;
import com.tahraoui.txnet.core.TXNetResponsePacketDispatcher;
import com.tahraoui.txnet.core.TXNetServiceListener;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;
import com.tahraoui.txnet.packet.response.TXNetResponsePacket;

public class MyRequestHandler extends TXNetPacketRequestHandler {
	@Override
	public void handleRequest(TXNetRequestPacket packet, TXNetResponsePacketDispatcher dispatcher) {
		System.out.println("Received packet: " + packet);
		TXNetResponsePacket responsePacket = processPacket(packet);
		dispatcher.broadcastPacket(responsePacket); // Example of broadcasting the response to all connections
		dispatcher.unicastPacket(2, responsePacket); // Example of unicast to a specific connection id
	}
}

public class MyConnectionService implements TXNetServiceListener {

	// Fields for interacting with your application and TXNetService

	@Override public void onConnected() {
		System.out.println("Connected");

	}
	@Override public void onDisconnected() {
		System.out.println("Disconnected");

	}
	@Override public void writeRequestPacket(TXNetRequestPacket request) {
		// This method is not needed unless you want to modify the request packet before sending it.
		// In that case, you will need to add your logic here and delegate the call to
		// TXNetService.sendRequestPacket(modifiedRequest);
	}
	@Override public void readResponsePacket(TXNetResponsePacket response) {
		processResponse(response); // A method that interacts with your application
	}

	// Public methods accessed by the rest of your application
}
```

Then, you need to initialize the `TXNetService` with an instance of each of the previous classes:

```java
import com.tahraoui.txnet.core.TXNetService;

private void startup() {
	// MyRequestHandler and MyConnectionService are your custom implementations, and they should be singletons.
	TXNetService.init(new MyRequestHandler(), new MyConnectionService());
}
```

To connect to a server or host a server, you can use the following methods:

```java
import com.tahraoui.txnet.model.UserCredentials;

private void connect() {
	// In Host/Server application
	TXNetService.host(8080, new UserCredentials("some-username", "some-password"));
	// In Client application
	TXNetService.join("some-username", new UserCredentials("client-username", "client-password"));

	// In both `host()` and `join()`, the `username` in `UserCredentials` is used for p2p architecture, you can leave it as an empty string if you don't need it.
}
```

To send a request packet to the server or another client, you can use the following method:

```java
import com.tahraoui.txnet.core.TXNetService;
import com.tahraoui.txnet.packet.request.TXNetRequestPacket;

class MessageRequestPacket extends TXNetRequestPacket {
	// Custom fields and methods for your request packet
}

private void sendRequest() {
	var message = new MessageRequestPacket();
	TXNetService.sendRequestPacket(message);
}
```

To receive a response packet, you need to implement the `readResponsePacket` method in your `TXNetServiceListener` implementation, as shown in the previous example.

### To Do:
- [ ] Integrate FTP.
- [ ] Support for more encryption algorithms.