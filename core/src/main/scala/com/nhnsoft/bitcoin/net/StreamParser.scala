/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Kangmo Kim 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhnsoft.bitcoin.net;

import java.nio.ByteBuffer;

/**
 * A generic handler which is used in {@link NioServer}, {@link NioClient} and {@link BlockingClient} to handle incoming
 * data streams.
 */
trait StreamParser {
    /** Called when the connection socket is closed */
    def connectionClosed() : Unit

    /** Called when the connection socket is first opened */
    def connectionOpened() : Unit

    /**
     * <p>Called when new bytes are available from the remote end. This should only ever be called by the single
     * writeTarget associated with any given StreamParser, multiple callers will likely confuse implementations.</p>
     *
     * Implementers/callers must follow the following conventions exactly:
     * <ul>
     * <li>buff will start with its limit set to the position we can read to and its position set to the location we
     *     will start reading at (always 0)</li>
     * <li>May read more than one message (recursively) if there are enough bytes available</li>
     * <li>Uses some internal buffering to store message which are larger (incl their length prefix) than buff's
     *     capacity(), ie it is up to this method to ensure we dont run out of buffer space to decode the next message.
     *     </li>
     * <li>buff will end with its limit the same as it was previously, and its position set to the position up to which
     *     bytes have been read (the same as its return value)</li>
     * <li>buff must be at least the size of a Bitcoin header (incl magic bytes).</li>
     * </ul>
     *
     * @return The amount of bytes consumed which should not be provided again
     */
    @throws( classOf[Exception] )
    def receiveBytes(buff : ByteBuffer) : Int

    /**
     * Called when this parser is attached to an upstream write target (ie a low-level connection handler). This
     * writeTarget should be stored and used to close the connection or write data to the socket.
     */
    def setWriteTarget(writeTarget : MessageWriteTarget) : Unit

    /**
     * Returns the maximum message size of a message on the socket. This is used in calculating size of buffers to
     * allocate.
     */
    def getMaxMessageSize() : Int
}
