/**
 * Copyright 2011 Steve Coughlan.
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

/**
 * Parent class for header only messages that don't have a payload.
 * Currently this includes getaddr, verack and special bitcoinj class UnknownMessage.
 */
@SerialVersionUID(8240801253854151802L)
class EmptyMessage(
    _params : NetworkParameters, 
    _payload : Array[Byte], 
    _offset : Int) extends Message(_params, _payload, _offset) {
    length = 0;

    def this() = this(null, null, -1)
    def this(params : NetworkParameters) = this(params, null, -1)


    @throws( classOf[IOException] )
    override final protected def bitcoinSerializeToStream(stream : OutputStream) {
    }

    override def getMessageSize() : Int  = 0

    /* (non-Javadoc)
      * @see Message#parse()
      */
    @throws( classOf[ProtocolException] )
    override def parse() {
    }

    /* (non-Javadoc)
      * @see Message#parseLite()
      */
    @throws( classOf[ProtocolException] )
    override protected def parseLite() {
        length = 0;
    }

    /* (non-Javadoc)
      * @see Message#ensureParsed()
      */
    @throws( classOf[ProtocolException] )
    override def ensureParsed() {
        parsed = true;
    }

    /* (non-Javadoc)
      * @see Message#bitcoinSerialize()
      */
    override def bitcoinSerialize() : Array[Byte] = {
        return Array[Byte]();
    }


}
