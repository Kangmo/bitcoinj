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

import java.util.List;

/**
 * Convenience implementation of {@link PeerEventListener}.
 */
class AbstractPeerEventListener extends PeerEventListener {
    override def onBlocksDownloaded(peer : Peer, block : Block, blocksLeft : Int) {}

    override def onChainDownloadStarted(peer : Peer, blocksLeft : Int) {}

    override def onPeerConnected(peer : Peer, peerCount : Int) {}

    override def onPeerDisconnected(peer : Peer, peerCount : Int) {}

    // Just pass the message right through for further processing.
    override def onPreMessageReceived(peer : Peer, m : Message) = m

    override def onTransaction(peer : Peer, t : Transaction) {}

    override def getData(peer : Peer, m : GetDataMessage) : List[Message] = null
}
