/**
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

package com.nhnsoft.bitcoin.core;

import java.util.List;

/**
 * Default no-op implementation of {@link BlockChainListener}.
 */
class AbstractBlockChainListener extends BlockChainListener {
    override def notifyNewBestBlock(block : StoredBlock) /* throws VerificationException */ {}

    override def reorganize(splitPoint : StoredBlock, oldBlocks : List[StoredBlock], newBlocks : List[StoredBlock]) /* throws VerificationException */ {}

    override def isTransactionRelevant(tx : Transaction) /* throws ScriptException */ = false

    override def receiveFromBlock(  tx : Transaction, block : StoredBlock, blockType : AbstractBlockChain.NewBlockType,
                                    relativityOffset : Int ) /* throws VerificationException */ {}

    override def notifyTransactionIsInBlock(txHash : Sha256Hash, block : StoredBlock, blockType : AbstractBlockChain.NewBlockType,
                                            relativityOffset : Int) /* throws VerificationException */  = false
}
