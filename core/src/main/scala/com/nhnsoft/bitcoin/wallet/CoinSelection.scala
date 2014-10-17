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

package com.nhnsoft.bitcoin.wallet;

import com.nhnsoft.bitcoin.core.Coin;
import com.nhnsoft.bitcoin.core.TransactionOutput;

import java.util.Collection;

/**
 * Represents the results of a
 * {@link com.nhnsoft.bitcoin.wallet.CoinSelector#select(Coin, java.util.LinkedList)} operation. A
 * coin selection represents a list of spendable transaction outputs that sum together to give valueGathered.
 * Different coin selections could be produced by different coin selectors from the same input set, according
 * to their varying policies.
 */
// TODO : Document interface change : valueGathered and gathered were fields but now they are methods on CoinSelection.
class CoinSelection(val valueGathered : Coin, val gathered : Collection[TransactionOutput])
