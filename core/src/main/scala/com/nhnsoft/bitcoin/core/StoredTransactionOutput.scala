/**
 * Copyright 2012 Matt Corallo.
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

import java.io._;
import java.math.BigInteger;

/**
 * A StoredTransactionOutput message contains the information necessary to check a spending transaction.
 * It avoids having to store the entire parentTransaction just to get the hash and index.
 * Its only really useful for MemoryFullPrunedBlockStore, and should probably be moved there
 */
object StoredTransactionOutput {
    /** arbitrary value lower than -{@link NetworkParameters#spendableCoinbaseDepth}
     * (not too low to get overflows when we do blockHeight - NONCOINBASE_HEIGHT, though) */
    private val NONCOINBASE_HEIGHT = -200;

}
@SerialVersionUID(-8744924157056340509L)
class StoredTransactionOutput(
    /**
     *  A transaction output has some value and a script used for authenticating that the redeemer is allowed to spend
     *  this output.
     */
    private var value : Coin,
    private var scriptBytes : Array[Byte],
    /** Hash of the transaction to which we refer. */
    private var hash : Sha256Hash,
    /** Which output of that transaction we are talking about. */
    private var index : Long,
    /** The height of the creating block (for coinbases, NONCOINBASE_HEIGHT otherwise) */
    private var height : Int) extends Serializable {

    /**
     * Creates a stored transaction output
     * @param hash the hash of the containing transaction
     * @param index the outpoint
     * @param value the value available
     * @param height the height this output was created in
     * @param scriptBytes
     */
    def this(hash : Sha256Hash, index : Long, value : Coin, height : Int, isCoinbase : Boolean, scriptBytes : Array[Byte]) {
        this(value, scriptBytes, hash, index, (if (isCoinbase) height else StoredTransactionOutput.NONCOINBASE_HEIGHT) )
    }

    def this(hash : Sha256Hash, out : TransactionOutput, height : Int, isCoinbase : Boolean) {
        this(out.getValue(), out.getScriptBytes(), hash, out.getIndex(), (if (isCoinbase) height else StoredTransactionOutput.NONCOINBASE_HEIGHT) )
    }

    @throws( classOf[IOException] )
    def this(in : InputStream) {
        this(null, null, null, 0L, 0)

        val valueBytes : Array[Byte] = new Array[Byte](8);
        if (in.read(valueBytes, 0, 8) != 8)
            throw new EOFException();
        value = Coin.valueOf(Utils.readInt64(valueBytes, 0))

        val scriptBytesLength : Int = ((in.read() & 0xFF) << 0) |
                                ((in.read() & 0xFF) << 8) |
                                ((in.read() & 0xFF) << 16) |
                                ((in.read() & 0xFF) << 24);
        scriptBytes = new Array[Byte](scriptBytesLength);
        if (in.read(scriptBytes) != scriptBytesLength)
            throw new EOFException();

        val hashBytes = new Array[Byte](32);
        if (in.read(hashBytes) != 32)
            throw new EOFException();
        hash = new Sha256Hash(hashBytes);

        val indexBytes = new Array[Byte](4);
        if (in.read(indexBytes) != 4)
            throw new EOFException();
        index = Utils.readUint32(indexBytes, 0);

        height = ((in.read() & 0xFF) << 0) |
            ((in.read() & 0xFF) << 8) |
            ((in.read() & 0xFF) << 16) |
            ((in.read() & 0xFF) << 24);
    }

    /**
     * The value which this Transaction output holds
     * @return the value
     */
    def getValue() : Coin = value

    /**
     * The backing script bytes which can be turned into a Script object.
     * @return the scriptBytes
     */
    def getScriptBytes() : Array[Byte] = scriptBytes

    /**
     * The hash of the transaction which holds this output
     * @return the hash
     */
    def getHash() : Sha256Hash = hash

    /**
     * The index of this output in the transaction which holds it
     * @return the index
     */
    def getIndex() : Long = index

    /**
     * Gets the height of the block that created this output (or -1 if this output was not created by a coinbase)
     */
    def getHeight() : Int = height

    override def toString() : String = {
        String.format("Stored TxOut of %s (%s:%d)", value.toFriendlyString(), hash.toString(), Integer.valueOf(index.toInt));
    }

    override def hashCode() : Int = hash.hashCode() + index.toInt;

    override def equals(o : Any ) : Boolean = {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        val other = o.asInstanceOf[StoredTransactionOutput];
        return getHash().equals(other.getHash()) &&
               getIndex() == other.getIndex();
    }

    @throws( classOf[IOException] )
    def serializeToStream(bos : OutputStream) {
        Utils.uint64ToByteStreamLE(BigInteger.valueOf(value.value), bos);
        
        bos.write(0xFF & scriptBytes.length >> 0);
        bos.write(0xFF & scriptBytes.length >> 8);
        bos.write(0xFF & (scriptBytes.length >> 16));
        bos.write(0xFF & (scriptBytes.length >> 24));
        bos.write(scriptBytes);
        
        bos.write(hash.getBytes());
        Utils.uint32ToByteStreamLE(index, bos);
        
        bos.write(0xFF & (height >> 0));
        bos.write(0xFF & (height >> 8));
        bos.write(0xFF & (height >> 16));
        bos.write(0xFF & (height >> 24));
    }
}
