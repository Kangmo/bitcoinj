/*
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Alerts are signed messages that are broadcast on the peer-to-peer network if they match a hard-coded signing key.
 * The private keys are held by a small group of core Bitcoin developers, and alerts may be broadcast in the event of
 * an available upgrade or a serious network problem. Alerts have an expiration time, data that specifies what
 * set of software versions it matches and the ability to cancel them by broadcasting another type of alert.<p>
 *
 * The right course of action on receiving an alert is usually to either ensure a human will see it (display on screen,
 * log, email), or if you decide to use alerts for notifications that are specific to your app in some way, to parse it.
 * For example, you could treat it as an upgrade notification specific to your app. Satoshi designed alerts to ensure
 * that software upgrades could be distributed independently of a hard-coded website, in order to allow everything to
 * be purely peer-to-peer. You don't have to use this of course, and indeed it often makes more sense not to.<p>
 *     
 * Before doing anything with an alert, you should check {@link AlertMessage#isSignatureValid()}.
 */
object AlertMessage {
    // Chosen arbitrarily to avoid memory blowups.
    private val MAX_SET_SIZE = 100L;
}

@throws( classOf[ProtocolException] )
class AlertMessage(
    _params : NetworkParameters, 
    _payload : Array[Byte],
    // We need to initialize all var fields changed by parse before the Message constructor is called.
    // For the details, check {@link AddressMessage}.
    private var content : Array[Byte] = null,
    private var signature : Array[Byte] = null,

    // See the getters for documentation of what each field means.
    private var version : Long = 1L,
    private var relayUntil : Date = null,
    private var expiration : Date = null,
    private var id : Long = 0L,
    private var cancel : Long = 0L,
    private var cancelSet : Set[Long] = null,
    private var minVer : Long = 0L,
    private var maxVer : Long = 0L,
    private var matchingSubVers : Set[String] = null,
    private var priority : Long = 0L,
    private var comment : String = null,
    private var statusBar : String = null,
    private var reserved : String = null

    ) extends Message(_params, _payload, 0) {

    @throws( classOf[ProtocolException] )
    def this(_params : NetworkParameters, _payload : Array[Byte]) = 
        this(_params, 
            _payload,
            null) // content 

    override def toString() : String = {
        "ALERT: " + getStatusBar();
    }

    @throws( classOf[ProtocolException] ) 
    override def parse() {
        // Alerts are formatted in two levels. The top level contains two byte arrays: a signature, and a serialized
        // data structure containing the actual alert data.
        val startPos : Int = cursor;
        content = readByteArray();
        signature = readByteArray();
        // Now we need to parse out the contents of the embedded structure. Rewind back to the start of the message.
        cursor = startPos;
        readVarInt();  // Skip the length field on the content array.
        // We're inside the embedded structure.
        version = readUint32();
        // Read the timestamps. Bitcoin uses seconds since the epoch.
        relayUntil = new Date(readUint64().longValue() * 1000);
        expiration = new Date(readUint64().longValue() * 1000);
        id = readUint32();
        cancel = readUint32();
        // Sets are serialized as <len><item><item><item>....
        val cancelSetSize : Long = readVarInt();
        if (cancelSetSize < 0 || cancelSetSize > AlertMessage.MAX_SET_SIZE) {
            throw new ProtocolException("Bad cancel set size: " + cancelSetSize);
        }
        // Using a hashset here is very inefficient given that this will normally be only one item. But Java doesn't
        // make it easy to do better. What we really want is just an array-backed set.
        cancelSet = new HashSet[Long](cancelSetSize.toInt);
        
        1L to cancelSetSize foreach { _ =>
            cancelSet.add(readUint32());
        }

        minVer = readUint32();
        maxVer = readUint32();
        // Read the subver matching set.
        val subverSetSize : Long = readVarInt();
        if (subverSetSize < 0 || subverSetSize > AlertMessage.MAX_SET_SIZE) {
            throw new ProtocolException("Bad subver set size: " + subverSetSize);
        }
        matchingSubVers = new HashSet[String](subverSetSize.toInt);

        1L to subverSetSize foreach { _ => 
            matchingSubVers.add(readStr());
        }

        priority = readUint32();
        comment = readStr();
        statusBar = readStr();
        reserved = readStr();

        length = cursor - offset;
    }

    /**
     * Returns true if the digital signature attached to the message verifies. Don't do anything with the alert if it
     * doesn't verify, because that would allow arbitrary attackers to spam your users.
     */
    def isSignatureValid() : Boolean = {
        ECKey.verify(Utils.doubleDigest(content), signature, params.getAlertSigningKey());
    }

    @throws( classOf[ProtocolException] )
    override protected def parseLite() {
        // Do nothing, lazy parsing isn't useful for alerts.
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Field accessors.

    /**
     * The time at which the alert should stop being broadcast across the network. Note that you can still receive
     * the alert after this time from other nodes if the alert still applies to them or to you.
     */
    def getRelayUntil() : Date = relayUntil;

    def setRelayUntil(relayUntil : Date) {
        this.relayUntil = relayUntil;
    }

    /**
     * The time at which the alert ceases to be relevant. It should not be presented to the user or app administrator
     * after this time.
     */
    def getExpiration() : Date = expiration;

    def setExpiration(expiration : Date) {
        this.expiration = expiration;
    }

    /**
     * The numeric identifier of this alert. Each alert should have a unique ID, but the signer can choose any number.
     * If an alert is broadcast with a cancel field higher than this ID, this alert is considered cancelled.
     * @return uint32
     */
    def getId() : Long = return id;

    def setId(id : Long) {
        this.id = id;
    }

    /**
     * A marker that results in any alerts with an ID lower than this value to be considered cancelled.
     * @return uint64
     */
    def getCancel() : Long = cancel;

    def setCancel(cancel : Long) {
        this.cancel = cancel;
    }

    /**
     * The inclusive lower bound on software versions that are considered for the purposes of this alert. The Satoshi
     * client compares this against a protocol version field, but as long as the subVer field is used to restrict it your
     * alerts could use any version numbers.
     * @return uint64
     */
    def getMinVer() : Long = return minVer;

    def setMinVer(minVer : Long) {
        this.minVer = minVer;
    }

    /**
     * The inclusive upper bound on software versions considered for the purposes of this alert. The Satoshi
     * client compares this against a protocol version field, but as long as the subVer field is used to restrict it your
     * alerts could use any version numbers.
     * @return
     */
    def getMaxVer() : Long = maxVer;

    def setMaxVer(maxVer : Long) {
        this.maxVer = maxVer;
    }

    /**
     * Provides an integer ordering amongst simultaneously active alerts.
     * @return uint64
     */
    def getPriority() : Long = priority;

    def setPriority(priority : Long) {
        this.priority = priority;
    }

    /**
     * This field is unused. It is presumably intended for the author of the alert to provide a justification for it
     * visible to protocol developers but not users.
     */
    def getComment() : String = comment;

    def setComment(comment : String) {
        this.comment = comment;
    }

    /**
     * A string that is intended to display in the status bar of the official GUI client. It contains the user-visible
     * message. English only.
     */
    def getStatusBar() : String = statusBar;

    def setStatusBar(statusBar : String) {
        this.statusBar = statusBar;
    }

    /**
     * This field is never used.
     */
    def getReserved() : String = reserved;

    def setReserved(reserved : String) {
        this.reserved = reserved;
    }
    
    def getVersion() : Long = version;
}
