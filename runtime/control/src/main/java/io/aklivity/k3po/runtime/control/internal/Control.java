/*
 * Copyright 2024 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.k3po.runtime.control.internal;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.aklivity.k3po.runtime.control.internal.command.AbortCommand;
import io.aklivity.k3po.runtime.control.internal.command.AwaitCommand;
import io.aklivity.k3po.runtime.control.internal.command.CloseCommand;
import io.aklivity.k3po.runtime.control.internal.command.Command;
import io.aklivity.k3po.runtime.control.internal.command.NotifyCommand;
import io.aklivity.k3po.runtime.control.internal.command.PrepareCommand;
import io.aklivity.k3po.runtime.control.internal.command.StartCommand;
import io.aklivity.k3po.runtime.control.internal.event.CommandEvent;
import io.aklivity.k3po.runtime.control.internal.event.ErrorEvent;
import io.aklivity.k3po.runtime.control.internal.event.FinishedEvent;
import io.aklivity.k3po.runtime.control.internal.event.NotifiedEvent;
import io.aklivity.k3po.runtime.control.internal.event.PreparedEvent;
import io.aklivity.k3po.runtime.control.internal.event.StartedEvent;

/**
 * Control class for controlling the robot.
 * This class establishes a tcp connection and can be fed commands to run.
 */
public final class Control {

    private static final int END_OF_STREAM = -1;
    private static final char END_OF_LINE = '\n';
    private static final String FINISHED_EVENT = "FINISHED";
    private static final String ERROR_EVENT = "ERROR";
    private static final String STARTED_EVENT = "STARTED";
    private static final String PREPARED_EVENT = "PREPARED";
    private static final String NOTIFIED_EVENT = "NOTIFIED";

    private static final Pattern HEADER_PATTERN = Pattern.compile("([a-z\\-]+):([^\n]+)");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final URL location;
    private URLConnection connection;
    private InputStream bytesIn;
    private ByteArrayOutputStream lineBuf;

    /**
     * @param location of k3po server to connect to.
     */
    public Control(URL location) {
        this.location = location;
    }

    /**
     * Connects to the k3po server.
     * @throws Exception if fails to connect.
     */
    public void connect() throws Exception {
        // connection must be null if the connection failed
        URLConnection newConnection = location.openConnection();
        newConnection.connect();
        connection = newConnection;

        bytesIn = connection.getInputStream();
        lineBuf = new ByteArrayOutputStream();
    }

    /**
     * Disconnects from the k3po server.
     * @throws Exception if error in closing the connection.
     */
    public void disconnect() throws Exception {

        if (connection != null) {
            try {
                if (connection instanceof Closeable) {
                    ((Closeable) connection).close();
                } else {
                    try {
                        connection.getInputStream().close();
                    } catch (IOException e) {
                        // ignore
                    }

                    try {
                        connection.getOutputStream().close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Writes a command to the wire.
     * @param command to write to the wire
     * @throws Exception if the command is not recognized
     */
    public void writeCommand(Command command) throws Exception {

        checkConnected();

        switch (command.getKind()) {
        case PREPARE:
            writeCommand((PrepareCommand) command);
            break;
        case START:
            writeCommand((StartCommand) command);
            break;
        case ABORT:
            writeCommand((AbortCommand) command);
            break;
        case AWAIT:
            writeCommand((AwaitCommand) command);
            break;
        case NOTIFY:
            writeCommand((NotifyCommand) command);
            break;
        case CLOSE:
            writeCommand((CloseCommand) command);
            break;
        default:
            throw new IllegalArgumentException("Urecognized command kind: " + command.getKind());
        }
    }

    /**
     * Reads a CommandEvent from the connection with K3po.
     * @return CommandEvent
     * @throws Exception if no event is available to be read.
     */
    public CommandEvent readEvent() throws Exception {
        // defaults to infinite
        return readEvent(0, MILLISECONDS);
    }

    /**
     * Reads a command event from the wire.
     * @param timeout is the time to read from the connection.
     * @param unit of time for the timeout.
     * @return the CommandEvent read from the wire.
     * @throws Exception if no event is read before the timeout.
     */
    public CommandEvent readEvent(int timeout, TimeUnit unit) throws Exception {

        checkConnected();

        connection.setReadTimeout((int) unit.toMillis(timeout));

        CommandEvent event = null;
        String eventType = readLine();

        if (Thread.interrupted())
        {
            throw new InterruptedException("thread interrupted during blocking read");
        }

        if (eventType != null) {
            switch (eventType) {
            case PREPARED_EVENT:
                event = readPreparedEvent();
                break;
            case STARTED_EVENT:
                event = readStartedEvent();
                break;
            case ERROR_EVENT:
                event = readErrorEvent();
                break;
            case FINISHED_EVENT:
                event = readFinishedEvent();
                break;
            case NOTIFIED_EVENT:
                event = readNotifiedEvent();
                break;
            default:
                throw new IllegalStateException("Invalid protocol frame: " + eventType);
            }
        }

        return event;
    }

    private void checkConnected() throws Exception {
        if (connection == null) {
            throw new IllegalStateException("Not connected");
        }
    }
    
    public boolean isConnected() {
        return connection != null;
    }

    private void writeCommand(PrepareCommand prepare) throws Exception {
        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        Iterable<String> names = prepare.getNames();
        List<String> overriddenScriptProperties = prepare.getOverriddenScriptProperties();

        int contentLength = 0;
        StringBuilder content = new StringBuilder();
        if (overriddenScriptProperties != null) {
            for (String property : overriddenScriptProperties) {
                content.append(format("property %s\n", property));
            }
            contentLength = content.length();
        }

        textOut.append("PREPARE\n");
        textOut.append("version:2.0\n");
        textOut.append(String.format("content-length:%s\n", contentLength));
        for (String name : names) {
            textOut.append(format("name:%s\n", name));
        }
        textOut.append("\n");
        textOut.append(content.toString());
        textOut.flush();
    }

    private void writeCommand(StartCommand start) throws Exception {

        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        textOut.append("START\n");
        textOut.append("\n");
        textOut.flush();
    }

    private void writeCommand(AbortCommand abort) throws IOException, CharacterCodingException {
        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        textOut.append("ABORT\n");
        textOut.append("\n");
        textOut.flush();
    }

    private void writeCommand(NotifyCommand notify) throws IOException, CharacterCodingException {
        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        textOut.append("NOTIFY\n");
        textOut.append(format("barrier:%s\n", notify.getBarrier()));
        textOut.append("\n");
        textOut.flush();
    }

    private void writeCommand(AwaitCommand await) throws IOException, CharacterCodingException {
        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        textOut.append("AWAIT\n");
        textOut.append(format("barrier:%s\n", await.getBarrier()));
        textOut.append("\n");
        textOut.flush();
    }

    private void writeCommand(CloseCommand close) throws IOException, CharacterCodingException {
        OutputStream bytesOut = connection.getOutputStream();
        CharsetEncoder encoder = UTF_8.newEncoder();
        Writer textOut = new OutputStreamWriter(bytesOut, encoder);

        textOut.append("CLOSE\n");
        textOut.append("\n");
        textOut.flush();
    }

    private PreparedEvent readPreparedEvent() throws IOException {
        PreparedEvent prepared = new PreparedEvent();
        String line;
        int length = END_OF_STREAM;
        do {
            line = readLine();
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
                switch (headerName) {
                case "content-length":
                    length = parseInt(headerValue);
                    break;
                case "name":
                    // compatibility
                    break;
                case "barrier":
                    prepared.getBarriers().add(headerValue);
                    break;
                default:
                    // NOP allow unrecognized headers for future compatibility
                }
            }
        } while (!line.isEmpty());

        // note: zero-length script should be non-null
        if (length >= 0) {
            prepared.setScript(readContent(length));
        }

        return prepared;
    }

    private StartedEvent readStartedEvent() throws IOException {
        StartedEvent started = new StartedEvent();
        String line;
        do {
            line = readLine();
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String headerName = matcher.group(1);
                switch (headerName) {
                case "name":
                    // compatibility
                    break;
                default:
                    // NOP allow unrecognized headers for future compatibility
                }
            }
        } while (!line.isEmpty());

        return started;
    }

    private FinishedEvent readFinishedEvent() throws IOException {
        FinishedEvent finished = new FinishedEvent();
        String line;
        int length = END_OF_STREAM;
        do {
            line = readLine();
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
                switch (headerName) {
                case "content-length":
                    length = parseInt(headerValue);
                    break;
                case "name":
                    // compatibility
                    break;
                case "notified":
                    finished.getCompletedBarriers().add(headerValue);
                    break;
                case "awaiting":
                    finished.getIncompleteBarriers().add(headerValue);
                    break;
                default:
                    // NOP allow unrecognized headers for future compatibility
                }
            }
        } while (!line.isEmpty());

        // note: zero-length script should be non-null
        if (length >= 0) {
            finished.setScript(readContent(length));
        }

        return finished;
    }

    private NotifiedEvent readNotifiedEvent() throws IOException {
        NotifiedEvent notified = new NotifiedEvent();
        String line;
        do {
            line = readLine();
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
                switch (headerName) {
                case "barrier":
                    notified.setBarrier(headerValue);
                    break;
                default:
                    // NOP allow unrecognized headers for future compatibility
                }
            }
        } while (!line.isEmpty());
        return notified;
    }

    private ErrorEvent readErrorEvent() throws IOException {
        ErrorEvent error = new ErrorEvent();
        String line;
        int length = 0;
        do {
            line = readLine();
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
                switch (headerName) {
                case "content-length":
                    length = parseInt(headerValue);
                    break;
                case "summary":
                    error.setSummary(headerValue);
                    break;
                case "name":
                    // compatibility
                    break;
                default:
                    // NOP allow unrecognized headers for future compatibility
                }
            }
        } while (!line.isEmpty());

        if (length > 0) {
            error.setDescription(readContent(length));
        }

        return error;
    }

    private String readContent(final int length) throws IOException {
        final byte[] content = new byte[length];
        int bytesRead = 0;
        do {
            int result = bytesIn.read(content, bytesRead, length - bytesRead);
            if (result == END_OF_STREAM) {
                throw new EOFException("EOF detected before all content read");
            }
            bytesRead += result;
        } while (bytesRead != length);
        return new String(content, "UTF-8");
    }

    private String readLine() throws IOException {
        lineBuf.reset();
        int b;
        for (b = bytesIn.read(); b != END_OF_STREAM && b != END_OF_LINE; b = bytesIn.read()) {
            lineBuf.write(b);
        }

        String line = null;
        if (lineBuf.size() != 0 || b != END_OF_STREAM) {
            line = lineBuf.toString("UTF-8");
        }
        return line;
    }

    public void notifyBarrier(String barrierName) throws Exception {
        final NotifyCommand notifyCommand = new NotifyCommand();
        notifyCommand.setBarrier(barrierName);
        this.writeCommand(notifyCommand);
    }

    public void sendAwaitBarrier(String barrierName) throws Exception {
        final AwaitCommand awaitCommand = new AwaitCommand();
        awaitCommand.setBarrier(barrierName);
        this.writeCommand(awaitCommand);
    }
}
