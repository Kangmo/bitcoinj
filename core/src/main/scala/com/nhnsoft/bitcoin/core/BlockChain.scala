/**
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

import com.google.common.base.Preconditions.checkArgument;

import com.nhnsoft.bitcoin.store.BlockStore;
import com.nhnsoft.bitcoin.store.BlockStoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A BlockChain implements the <i>simplified payment verification</i> mode of the Bitcoin protocol. It is the right
 * choice to use for programs that have limited resources as it won't verify transactions signatures or attempt to store
 * all of the block chain. Really, this class should be called SPVBlockChain but for backwards compatibility it is not.
 * </p>
 */
/**
 * Constructs a BlockChain connected to the given list of listeners and a store.
 */
class BlockChain(   params : NetworkParameters, 
                    wallets : List[BlockChainListener],
                    /** Keeps a map of block hashes to StoredBlocks. */
                    blockStore : BlockStore) extends AbstractBlockChain( params, wallets, blockStore) {



    /**
     * <p>Constructs a BlockChain connected to the given wallet and store. To obtain a {@link Wallet} you can construct
     * one from scratch, or you can deserialize a saved wallet from disk using {@link Wallet#loadFromFile(java.io.File)}
     * </p>
     *
     * <p>For the store, you should use {@link com.nhnsoft.bitcoin.store.SPVBlockStore} or you could also try a
     * {@link com.nhnsoft.bitcoin.store.MemoryBlockStore} if you want to hold all headers in RAM and don't care about
     * disk serialization (this is rare).</p>
     */
    @throws( classOf[BlockStoreException] )
    def this(params : NetworkParameters, wallet : Wallet, blockStore : BlockStore) {
        this(params, new ArrayList[BlockChainListener](), blockStore);
        if (wallet != null)
            addWallet(wallet);
    }

    /**
     * Constructs a BlockChain that has no wallet at all. This is helpful when you don't actually care about sending
     * and receiving coins but rather, just want to explore the network data structures.
     */
    @throws( classOf[BlockStoreException] )
    def this(params : NetworkParameters, blockStore : BlockStore) {
        this(params, new ArrayList[BlockChainListener](), blockStore);
    }


    @throws( classOf[BlockStoreException] )
    @throws( classOf[VerificationException] )
    override protected def addToBlockStore(storedPrev : StoredBlock, blockHeader : Block, txOutChanges : TransactionOutputChanges) : StoredBlock = {
        val newBlock : StoredBlock = storedPrev.build(blockHeader);
        blockStore.put(newBlock);
        newBlock;
    }
    
    @throws( classOf[BlockStoreException] )
    @throws( classOf[VerificationException] )
    override protected def addToBlockStore(storedPrev : StoredBlock, blockHeader : Block) : StoredBlock = {
        val newBlock : StoredBlock = storedPrev.build(blockHeader);
        blockStore.put(newBlock);
        newBlock;
    }

    @throws( classOf[BlockStoreException] )
    override protected def rollbackBlockStore(height : Int) {
        lock.lock();
        try {
            val currentHeight : Int = getBestChainHeight();
            checkArgument(height >= 0 && height <= currentHeight, "Bad height: %s", Integer.valueOf(height));
            if (height == currentHeight)
                return; // nothing to do

            // Look for the block we want to be the new chain head
            var newChainHead : StoredBlock = blockStore.getChainHead();
            while (newChainHead.getHeight() > height) {
                newChainHead = newChainHead.getPrev(blockStore);
                if (newChainHead == null)
                    throw new BlockStoreException("Unreachable height");
            }

            // Modify store directly
            blockStore.put(newChainHead);
            this.setChainHead(newChainHead);
        } finally {
            lock.unlock();
        }
    }

    override protected def shouldVerifyTransactions() : Boolean = false

    override protected def connectTransactions(height :  Int, block : Block) : TransactionOutputChanges = {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    override protected def connectTransactions(newBlock : StoredBlock) : TransactionOutputChanges = {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    override protected def disconnectTransactions(block : StoredBlock) {
        // Don't have to do anything as this is only called if(shouldVerifyTransactions())
        throw new UnsupportedOperationException();
    }

    @throws( classOf[BlockStoreException] )
    override protected def doSetChainHead(chainHead : StoredBlock) {
        blockStore.setChainHead(chainHead);
    }

    @throws( classOf[BlockStoreException] )
    override protected def notSettingChainHead() {
        // We don't use DB transactions here, so we don't need to do anything
    }

    @throws( classOf[BlockStoreException] )
    override protected def getStoredBlockInCurrentScope(hash : Sha256Hash) : StoredBlock = {
        blockStore.get(hash);
    }

    @throws( classOf[VerificationException] )
    @throws( classOf[PrunedException] )
    override def add(block : FilteredBlock) : Boolean = {
        val success : Boolean = super.add(block);
        if (success) {
            trackFilteredTransactions(block.getTransactionCount());
        }
        success;
    }
}
