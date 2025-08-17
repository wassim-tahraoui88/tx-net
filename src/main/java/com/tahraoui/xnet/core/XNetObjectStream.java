package com.tahraoui.xnet.core;

import com.tahraoui.xnet.exception.XNetObjectStreamException;
import com.tahraoui.xnet.exception.XNetReadingException;
import com.tahraoui.xnet.exception.XNetWritingException;
import com.tahraoui.xnet.packet.XNetPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * {@link XNetObjectStream} is an XNet wrapper class for {@link java.net.Socket}'s input/output streams.
 */
public final class XNetObjectStream {

	private static final Logger LOGGER = LogManager.getLogger(XNetObjectStream.class);

	private final ObjectInputStream reader;
	private final ObjectOutputStream writer;

	/**
	 * Create the input/output object streams for server side client socket connection.
	 * This method is not supposed to be called directly by the user, as it is intended to be used by XNet's server instance to create a stream for each client connection.
	 * @param in the input stream to read from.
	 * @param out the output stream to write to.
	 * @throws XNetObjectStreamException if an I/O error occurs when creating the object streams
	 * @see #createClientStream(OutputStream, InputStream)
	 */
	public static XNetObjectStream createHostStream(InputStream in, OutputStream out) throws XNetObjectStreamException {
		return new XNetObjectStream(out, in);
	}

	/**
	 * Create the input/output object streams for client-side socket connection.
	 * This method is not supposed to be called directly by the user, as it is intended to be used by XNet's client instance to create a stream for the server connection.
	 * @param in the input stream to read from.
	 * @param out the output stream to write to.
	 * @throws XNetObjectStreamException if an I/O error occurs when creating the object streams
	 * @see #createHostStream(InputStream, OutputStream)
	 */
	public static XNetObjectStream createClientStream(OutputStream out, InputStream in) throws XNetObjectStreamException {
		return new XNetObjectStream(in, out);
	}

	private XNetObjectStream(InputStream in, OutputStream out) throws XNetObjectStreamException {
		try {
			this.reader = new ObjectInputStream(in);
			this.writer = new ObjectOutputStream(out);
			this.writer.flush();
		}
		catch (IOException e) {
			throw new XNetObjectStreamException();
		}
	}
	private XNetObjectStream(OutputStream out, InputStream in) throws XNetObjectStreamException {
		try {
			this.writer = new ObjectOutputStream(out);
			this.reader = new ObjectInputStream(in);
			this.writer.flush();
		}
		catch (IOException e) {
			throw new XNetObjectStreamException();
		}
	}

	/**
	 * Reads a {@link XNetPacket packet} from the input stream.
	 * @return the {@link XNetPacket packet} read from the input stream.
	 * @throws XNetReadingException if an I/O error occurs when reading from the input stream.
	 */
	public XNetPacket read() throws XNetReadingException {
		try {
			var packet = reader.readObject();
			if (packet instanceof XNetPacket) return (XNetPacket) packet;
			else return null;
		}
		catch (IOException | ClassNotFoundException | ClassCastException e) {
			LOGGER.error(e);
			throw new XNetReadingException();
		}
	}

	/**
	 * Writes a {@link XNetPacket packet} to the output stream.
	 * @param packet the {@link XNetPacket packet} to write (send).
	 * @throws XNetWritingException if an I/O error occurs when writing to the output stream.
	 */
	public void write(XNetPacket packet) throws XNetWritingException {
		try {
			writer.writeObject(packet);
			writer.flush();
		}
		catch (IOException _) {
			throw new XNetWritingException();
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
