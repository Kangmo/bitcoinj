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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

// TODO: Rename this to DownloadProgressTracker or something more appropriate.

object DownloadListener {
    private val log = LoggerFactory.getLogger(getClass)
}
/**
 * <p>An implementation of {@link AbstractPeerEventListener} that listens to chain download events and tracks progress
 * as a percentage. The default implementation prints progress to stdout, but you can subclass it and override the
 * progress method to update a GUI instead.</p>
 */
class DownloadListener extends AbstractPeerEventListener {
    import DownloadListener._

    private var originalBlocksLeft = -1;
    private var lastPercent = 0;
    private var done = new Semaphore(0);
    private var caughtUp = false;

    override def onChainDownloadStarted(peer : Peer, blocksLeft : Int) {
        startDownload(blocksLeft);
        // Only mark this the first time, because this method can be called more than once during a chain download
        // if we switch peers during it.
        if (originalBlocksLeft == -1)
            originalBlocksLeft = blocksLeft;
        else
            log.info("Chain download switched to {}", peer);
        if (blocksLeft == 0) {
            doneDownload();
            done.release();
        }
    }

    override def onBlocksDownloaded(peer : Peer, block : Block, blocksLeft : Int) {
        if (caughtUp)
            return;

        if (blocksLeft == 0) {
            caughtUp = true;
            doneDownload();
            done.release();
        }

        if (blocksLeft < 0 || originalBlocksLeft <= 0)
            return;

        val pct : Double = 100.0 - (100.0 * ( blocksLeft.toDouble / originalBlocksLeft.toDouble));
        if ( pct.toInt != lastPercent) {
            progress(pct, blocksLeft, new Date(block.getTimeSeconds() * 1000));
            lastPercent = pct.toInt;
        }
    }

    /**
     * Called when download progress is made.
     *
     * @param pct  the percentage of chain downloaded, estimated
     * @param date the date of the last block downloaded
     */
    protected def progress(pct : Double, blocksSoFar : Int, date : Date) {
        val blockDate = DateFormat.getDateTimeInstance().format(date)
        log.info(s"Chain download ${pct.toInt}% done with ${blocksSoFar} blocks to go, block date ${blockDate}");
    }

    /**
     * Called when download is initiated.
     *
     * @param blocks the number of blocks to download, estimated
     */
    protected def startDownload(blocks : Int ) {
        if (blocks > 0 && originalBlocksLeft == -1)
            log.info("Downloading block chain of size " + blocks + ". " +
                    ( if (blocks > 1000) "This may take a while." else ""));

    }

    /**
     * Called when we are done downloading the block chain.
     */
    protected def doneDownload() {
    }

    /**
     * Wait for the chain to be downloaded.
     */
    @throws( classOf[InterruptedException] )
    def await() {
        done.acquire();
    }
}
