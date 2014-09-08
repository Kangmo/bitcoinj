/**
 * Copyright 2011 Google Inc.
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

package com.nhnsoft.bitcoin.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import collection.JavaConversions._;

/**
 * Represents an "addr" message on the P2P network, which contains broadcast IP addresses of other peers. This is
 * one of the ways peers can find each other without using the DNS or IRC discovery mechanisms. However storing and
 * using addr messages is not presently implemented.
 */
/**
 * Contruct a new 'addr' message.
 * @param params NetworkParameters object.
 * @param offset The location of the first payload byte within the array.
 * @param parseLazy Whether to perform a full parse immediately or delay until a read is requested.
 * @param parseRetain Whether to retain the backing byte array for quick reserialization.  
 * If true and the backing byte array is invalidated due to modification of a field then 
 * the cached bytes may be repopulated and retained if the message is serialized again in the future.
 * @param length The length of message if known.  Usually this is provided when deserializing of the wire
 * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
 * @throws ProtocolException
 */
@SerialVersionUID(8058283864924679460L)
@serializable
@throws( classOf[ProtocolException] )
class AddressMessage(
    _params : NetworkParameters, 
    _payload : Array[Byte], 
    _offset : Int, 
    _parseLazy : Boolean, 
    _parseRetain : Boolean, 
    _length : Int) extends Message(_params, _payload, _offset, _parseLazy, _parseRetain, _length) {

    @transient
    private val MAX_ADDRESSES = 1024L

    private var addresses : List[PeerAddress] = null
    
    @transient
    private var numAddresses = -1L

    /**
     * Contruct a new 'addr' message.
     * @param params NetworkParameters object.
     * @param parseLazy Whether to perform a full parse immediately or delay until a read is requested.
     * @param parseRetain Whether to retain the backing byte array for quick reserialization.  
     * If true and the backing byte array is invalidated due to modification of a field then 
     * the cached bytes may be repopulated and retained if the message is serialized again in the future.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    
    @throws( classOf[ProtocolException] )
    def this(params : NetworkParameters, payload : Array[Byte], parseLazy :  Boolean, parseRetain : Boolean, length : Int) = 
        this(params, payload, 0, parseLazy, parseRetain, length);
    

    @throws( classOf[ProtocolException] )
    def this(params : NetworkParameters, payload : Array[Byte], offset : Int) = 
        this(params, payload, offset, false, false, Message.UNKNOWN_LENGTH);
    

    @throws( classOf[ProtocolException] )
    def this(_params : NetworkParameters, _payload : Array[Byte]) = 
        this(_params, _payload, 0, false, false, Message.UNKNOWN_LENGTH);
    

    @throws( classOf[ProtocolException] )
    override protected def parseLite() {
    }

    @throws( classOf[ProtocolException] )
    override def parse() {
        numAddresses = readVarInt();
        // Guard against ultra large messages that will crash us.
        if (numAddresses > MAX_ADDRESSES)
            throw new ProtocolException("Address message too large.");
        
        addresses = new ArrayList[PeerAddress](numAddresses.toInt);

        var i = 0L;
        for (i <- 0L until numAddresses) {
            val addr = new PeerAddress(params, payload, cursor, protocolVersion, this, parseLazy, parseRetain);
            addresses.add(addr);
            cursor += addr.getMessageSize();
        }
        length = cursor - offset;
    }

    /* (non-Javadoc)
      * @see Message#bitcoinSerializeToStream(java.io.OutputStream)
      */
    @throws( classOf[IOException] )
    override def bitcoinSerializeToStream(stream : OutputStream) {
        if (addresses == null)
            return;
      
        stream.write(new VarInt(addresses.size()).encode());
      
        addresses.map { addr : PeerAddress => 
            addr.bitcoinSerialize(stream);
        }
    }

    override def getMessageSize() : Int = {
        if (length != Message.UNKNOWN_LENGTH) {
            length
        }
        else {
            if (addresses != null) {
                length = new VarInt(addresses.size()).getSizeInBytes();
                // The 4 byte difference is the uint32 timestamp that was introduced in version 31402
                length += addresses.size() * ( if (protocolVersion > 31402) PeerAddress.MESSAGE_SIZE else PeerAddress.MESSAGE_SIZE - 4);
            }
            length;          
        }
    }

    /**
     * AddressMessage cannot cache checksum in non-retain mode due to dynamic time being used.
     */
    override def setChecksum(checksum : Array[Byte]) {
        if (parseRetain)
            super.setChecksum(checksum);
        else
            this.checksum = null;
    }

    /**
     * @return An unmodifiableList view of the backing List of addresses.  Addresses contained within the list may be safely modified.
     */
    def getAddresses() : List[PeerAddress] = {
        maybeParse();
        Collections.unmodifiableList(addresses);
    }

    def addAddress(address : PeerAddress) {
        unCache();
        maybeParse();
        address.setParent(this);
        addresses.add(address);
        if (length == Message.UNKNOWN_LENGTH)
            getMessageSize();
        else
            length += address.getMessageSize();
    }

    def removeAddress(index : Int) {
        unCache();
        val address : PeerAddress = addresses.remove(index);
        address.setParent(null);
        if (length == Message.UNKNOWN_LENGTH)
            getMessageSize();
        else
            length -= address.getMessageSize();
    }

    override def toString() : String = {
        val builder = new StringBuilder();
        builder.append("addr: ");
        addresses.map{ a : PeerAddress => 
            builder.append(a.toString());
            builder.append(" ");
        }
        builder.toString();
    }
}
