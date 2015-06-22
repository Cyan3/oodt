/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.crawl.daemon;

//OODT imports
import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlException;

//JDK imports
import java.io.IOException;
import java.net.*;
import java.util.Vector;

//APACHE imports
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import protocol.crawlDaemonProtocol.AbsCrawlDaemon;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Controller class for the Crawl Daemons
 * </p>.
 */
public class CrawlDaemonController {

    /* our xml rpc client */
    private XmlRpcClient client = null;
    //private Transceiver ntClient = null;
    AbsCrawlDaemon proxy = null;


    public CrawlDaemonController(String crawlUrlStr)
            throws InstantiationException {
        try {
            //client = new XmlRpcClient(new URL(crawlUrlStr));
            URL url = new URL(crawlUrlStr);
            Transceiver ntClient = new HttpTransceiver(url);
            this.proxy = (AbsCrawlDaemon) SpecificRequestor.getClient(AbsCrawlDaemon.class, ntClient);
        } catch (IOException e)
        {
            throw new InstantiationException(e.getMessage());
        }
    }

    public double getAverageCrawlTime() throws CrawlException {

        double avgCrawlTime = -1.0d;

        try {
            avgCrawlTime = proxy.getAverageCrawlTime();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }
        return avgCrawlTime;
    }

    public int getMilisCrawling() throws CrawlException {
        Vector argList = new Vector();

        int milisCrawling = -1;

        try {
            milisCrawling = proxy.getMilisCrawling();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }

        return milisCrawling;
    }

    public int getWaitInterval() throws CrawlException {

        int waitInterval = -1;

        try {
            waitInterval = proxy.getWaitInterval();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }

        return waitInterval;
    }

    public int getNumCrawls() throws CrawlException {

        int numCrawls = -1;

        try {
            numCrawls = proxy.getWaitInterval();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }

        return numCrawls;

    }

    public boolean isRunning() throws CrawlException {
        boolean running = false;

        try {
            running = proxy.isRunning();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }

        return running;
    }

    public void stop() throws CrawlException {
        Vector argList = new Vector();
        boolean running = false;

        try {
            running = proxy.stop();
        } catch (AvroRemoteException e) {
            throw new CrawlException(e.getMessage());
        }

        if (running) {
            throw new CrawlException("Stop attempt failed: crawl daemon: ["
                    + this.client.getURL() + "] still running");
        }
    }

    public static void main(String[] args) throws Exception {
        String avgCrawlOperation = "--getAverageCrawlTime\n";
        String getMilisCrawlOperation = "--getMilisCrawling\n";
        String getNumCrawlsOperation = "--getNumCrawls\n";
        String getWaitIntervalOperation = "--getWaitInterval\n";
        String isRunningOperation = "--isRunning\n";
        String stopOperation = "--stop\n";

        String usage = "CrawlController --url <url to xml rpc service> --operation [<operation> [params]]\n"
                + "operations:\n"
                + avgCrawlOperation
                + getMilisCrawlOperation
                + getNumCrawlsOperation
                + getWaitIntervalOperation
                + isRunningOperation + stopOperation;

        String operation = null, url = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--operation")) {
                operation = args[++i];
            } else if (args[i].equals("--url")) {
                url = args[++i];
            }
        }

        if (operation == null) {
            System.err.println(usage);
            System.exit(1);
        }

        // create the controller
        CrawlDaemonController controller = new CrawlDaemonController(url);

        if (operation.equals("--getAverageCrawlTime")) {
            double avgCrawlTime = controller.getAverageCrawlTime();
            System.out.println("Average Crawl Time: [" + avgCrawlTime + "]");
        } else if (operation.equals("--getMilisCrawling")) {
            int crawlTime = controller.getMilisCrawling();
            System.out.println("Total Crawl Time: [" + crawlTime
                    + "] miliseconds");
        } else if (operation.equals("--getNumCrawls")) {
            int numCrawls = controller.getNumCrawls();
            System.out.println("Num Crawls: [" + numCrawls + "]");
        } else if (operation.equals("--getWaitInterval")) {
            int waitInterval = controller.getWaitInterval();
            System.out.println("Wait Interval: [" + waitInterval + "]");
        } else if (operation.equals("--isRunning")) {
            boolean running = controller.isRunning();
            System.out.println(running ? "Yes" : "No");
        } else if (operation.equals("--stop")) {
            controller.stop();
            System.out.println("Crawl Daemon: [" + controller.client.getURL()
                    + "]: shutdown successful");
        } else
            throw new IllegalArgumentException("Unknown Operation!");

    }

}
