/*
 * Copyright 2013 Google Inc.
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

package com.nhnsoft.bitcoin.tools;

import com.nhnsoft.bitcoin.core.*;
import com.nhnsoft.bitcoin.kits.WalletAppKit;
import com.nhnsoft.bitcoin.params.MainNetParams;
import com.nhnsoft.bitcoin.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A program that sends a transaction with the specified fee and measures how long it takes to confirm.
 */
public class TestFeeLevel {
    private static Logger log = LoggerFactory.getLogger(TestFeeLevel.class);

    public static final MainNetParams PARAMS = MainNetParams.get();
    private static WalletAppKit kit;

    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        if (args.length == 0) {
            System.err.println("Specify the fee level to test in satoshis as the first argument.");
            return;
        }

        Coin feeToTest = Coin.valueOf(Long.parseLong(args[0]));

        kit = new WalletAppKit(PARAMS, new File("."), "testfeelevel");
        kit.startAsync();
        kit.awaitRunning();
        try {
            go(feeToTest);
        } finally {
            kit.stopAsync();
            kit.awaitTerminated();
        }
    }

    private static void go(Coin feeToTest) throws InterruptedException, java.util.concurrent.ExecutionException, InsufficientMoneyException {
        kit.peerGroup().setMaxConnections(50);

        final Address address = kit.wallet().currentReceiveKey().toAddress(PARAMS);

        if (kit.wallet().getBalance().compareTo(feeToTest) < 0) {
            log.info("Send some money to {}", address);
            log.info("... and wait for it to confirm");
            kit.wallet().getBalanceFuture(feeToTest, Wallet.BalanceType.AVAILABLE).get();
        }

        int heightAtStart = kit.chain().getBestChainHeight();
        log.info("Height at start is {}", heightAtStart);

        Wallet.SendRequest request = Wallet.SendRequest.to(address, kit.wallet().getBalance().subtract(feeToTest));
        request.feePerKb = feeToTest;
        request.ensureMinRequiredFee = false;
        kit.wallet().completeTx(request);
        log.info("Fee paid is {}", request.fee);
        log.info("TX is {}", request.tx);
        kit.peerGroup().broadcastTransaction(request.tx).get();
        log.info("Send complete, waiting for confirmation");
        request.tx.getConfidence().getDepthFuture(1).get();

        int heightNow = kit.chain().getBestChainHeight();
        log.info("Height after confirmation is {}", heightNow);
        log.info("Result: took {} blocks to confirm at this fee level", heightNow - heightAtStart);
    }
}
