# TXNet
This library provides a simple and efficient way to handle TCP socket connections in Java.<br>
It abstracts the complexities of socket programming, allowing developers to focus on building their applications.<br>
It includes features like connection management, data transmission, and error handling.

## Main Exposed Classes
- **XNetService**: Core service for managing TCP connections.
- **XNetPacket**: Represents base class for all TCP request/response packets.
- **XNetFTPHandler**: For FTP-like file transfers.
- **XNetConfig**: Handles configuration and property loading.

## Configuration: application.properties
Add the following properties to your `src/main/resources/application.properties` file to configure the library:

| Property                       | Default Value   | Description                                      |
|--------------------------------|-----------------|--------------------------------------------------|
| tx-net.server.url              | (none)          | The server URL to connect to.                    |
| tx-net.server.port             | (none)          | The server port to connect to.                   |
| tx-net.server.size             | (none)          | The maximum number of concurrent connections.    |
| tx-net.server.generated_id     | seq             | Packet ID generation mode: `hash` or `seq`.      |
| tx-net.security.enabled        | false           | Enable encryption for data transmission.         |
| tx-net.security.key.aes.length | 256             | AES encryption key length (bits).                |
| tx-net.security.key.rsa.length | 2024            | RSA encryption key length (bits).                |
| tx-net.security.iv.length      | 16              | Initialization vector length for encryption.     |
| tx-net.file.size.max           | 10485760 (10MB) | Maximum allowed file size for transfers (bytes). |
| tx-net.debug.enabled           | true            | Enable debug logging.                            |
| tx-net.debug.level             | DEBUG           | Logging level (`DEBUG`, `INFO`, `WARN`, etc.).   |

_*Properties with `(none)` as default value are required and must be set._
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
