/**
 * Copyright 2014 Kosta Korenkov
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
package com.nhnsoft.bitcoin.signers;

import com.nhnsoft.bitcoin.core.Transaction;
import com.nhnsoft.bitcoin.crypto.ChildNumber;
import com.nhnsoft.bitcoin.script.Script;
import com.nhnsoft.bitcoin.wallet.KeyBag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Implementations of this interface are intended to sign inputs of the given transaction. Given transaction may already
 * be partially signed or somehow altered by other signers.</p>
 * <p>To make use of the signer, you need to add it into the wallet by
 * calling {@link com.nhnsoft.bitcoin.core.Wallet#addTransactionSigner(TransactionSigner)}. Signer will be serialized
 * along with the wallet data. In order for a wallet to recreate signer after deserialization, each signer
 * should have no-args constructor</p>
 */

object TransactionSigner {
    /**
     * This class wraps transaction proposed to complete keeping a metadata that may be updated, used and effectively
     * shared by transaction signers.
     */
    class ProposedTransaction(val partialTx : Transaction) {

        /**
         * HD key paths used for each input to derive a signing key. It's useful for multisig inputs only.
         * The keys used to create a single P2SH address have the same derivation path, so to use a correct key each signer
         * has to know a derivation path of signing keys used by previous signers. For each input signers will use the
         * same derivation path and we need to store only one key path per input. As TransactionInput is mutable, inputs
         * are identified by their scriptPubKeys (keys in this map).
         */
        val keyPaths = new HashMap[Script, List[ChildNumber]]()
    }
}

trait TransactionSigner {

    /**
     * Returns true if this signer is ready to be used.
     */
    def isReady() : Boolean

    /**
     * Returns byte array of data representing state of this signer. It's used to serialize/deserialize this signer
     */
    def serialize() : Array[Byte]

    /**
     * Uses given byte array of data to reconstruct internal state of this signer
     */
    def deserialize(data : Array[Byte]) : Unit

    /**
     * Signs given transaction's inputs.
     * Returns true if signer is compatible with given transaction (can do something meaningful with it).
     * Otherwise this method returns false
     */
    def signInputs(propTx : TransactionSigner.ProposedTransaction, keyBag : KeyBag) : Boolean

}