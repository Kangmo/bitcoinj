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

// TODO : Correctly implement throws clause for the constructor.
@throws(classOf[ProtocolException])
@SerialVersionUID(3614705938207918775L)
class UnknownMessage(params : NetworkParameters, name : String, payloadBytes : Array[Byte]) extends EmptyMessage(params, payloadBytes, 0) {
    override def toString() =
        "Unknown message [" + name + "]: " + (if (payload == null) "" else Utils.HEX.encode(payload));
}
