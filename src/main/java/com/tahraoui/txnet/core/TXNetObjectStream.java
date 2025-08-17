package com.tahraoui.txnet.core;

import com.tahraoui.txnet.exception.TXNetObjectStreamException;
import com.tahraoui.txnet.exception.TXNetReadingException;
import com.tahraoui.txnet.exception.TXNetWritingException;
import com.tahraoui.txnet.packet.TXNetPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * {@link TXNetObjectStream} is an XNet wrapper class for {@link java.net.Socket}'s input/output streams.
 */
public final class TXNetObjectStream {

	private static final Logger LOGGER = LogManager.getLogger(TXNetObjectStream.class);

	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;

	/**
	 * Create the input/output object streams for server side client socket connection.
	 * This method is not supposed to be called directly by the user, as it is intended to be used by XNet's server instance to create a stream for each client connection.
	 * @param in the input stream to read from.
	 * @param out the output stream to write to.
	 * @throws TXNetObjectStreamException if an I/O error occurs when creating the object streams
	 * @see #createClientStream(OutputStream, InputStream)
	 */
	public static TXNetObjectStream createHostStream(InputStream in, OutputStream out) throws TXNetObjectStreamException {
		return new TXNetObjectStream(out, in);
	}

	/**
	 * Create the input/output object streams for client-side socket connection.
	 * This method is not supposed to be called directly by the user, as it is intended to be used by XNet's client instance to create a stream for the server connection.
	 * @param in the input stream to read from.
	 * @param out the output stream to write to.
	 * @throws TXNetObjectStreamException if an I/O error occurs when creating the object streams
	 * @see #createHostStream(InputStream, OutputStream)
	 */
	public static TXNetObjectStream createClientStream(OutputStream out, InputStream in) throws TXNetObjectStreamException {
		return new TXNetObjectStream(in, out);
	}

	private TXNetObjectStream(InputStream in, OutputStream out) throws TXNetObjectStreamException {
		try {
			this.reader = new ObjectInputStream(in);
			this.writer = new ObjectOutputStream(out);
			this.writer.flush();
		}
		catch (IOException e) {
			throw new TXNetObjectStreamException();
		}
	}
	private TXNetObjectStream(OutputStream out, InputStream in) throws TXNetObjectStreamException {
		try {
			this.writer = new ObjectOutputStream(out);
			this.reader = new ObjectInputStream(in);
			this.writer.flush();
		}
		catch (IOException e) {
			throw new TXNetObjectStreamException();
		}
	}

	/**
	 * Reads a {@link TXNetPacket packet} from the input stream.
	 * @return the {@link TXNetPacket packet} read from the input stream.
	 * @throws TXNetReadingException if an I/O error occurs when reading from the input stream.
	 */
	public TXNetPacket read() throws TXNetReadingException {
		try {
			var packet = reader.readObject();
			if (packet instanceof TXNetPacket) return (TXNetPacket) packet;
			else return null;
		}
		catch (IOException | ClassNotFoundException | ClassCastException e) {
			LOGGER.error(e);
			throw new TXNetReadingException();
		}
	}

	/**
	 * Writes a {@link TXNetPacket packet} to the output stream.
	 * @param packet the {@link TXNetPacket packet} to write (send).
	 * @throws TXNetWritingException if an I/O error occurs when writing to the output stream.
	 */
	public void write(TXNetPacket packet) throws TXNetWritingException {
		try {
			writer.writeObject(packet);
			writer.flush();
		}
		catch (IOException _) {
			throw new TXNetWritingException();
		}
	}

	/**
	 * Closes the object streams.
	 * @throws IOException if an I/O error occurs when closing the object streams.
	 */
	public void close() throws IOException {
		reader.close();
		writer.close();
	}

	public ObjectOutput getWriter() { return writer; }
}
