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

package org.apache.oodt.cas.filemgr.util;

import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.system.rpc.FileManagerClientFactory;
import org.apache.oodt.cas.filemgr.system.rpc.FileManagerServerFactory;

import java.io.IOException;
import java.net.URL;

public class RpcCommunicationFactory {

    // true - avro
    // false - xmlrpc
    public static boolean SERVER = true;

    private static String getClientFactoryName(){
        return "org.apache.oodt.cas.filemgr.system.rpc."
                + (SERVER ? "AvroFileManagerClientFactory":"XmlRpcFileManagerClientFactory");
    }

    public static FileManagerClient createClient(URL filemgrUrl) throws ConnectionException {
        try {
            FileManagerClientFactory fmcf= (FileManagerClientFactory) Class.forName(getClientFactoryName()).newInstance();
            fmcf.setTestConnection(true);
            fmcf.setUrl(filemgrUrl);
            return fmcf.createFileManagerClient();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FileManagerClient createClient(URL filemgrUrl, boolean testConnection) throws ConnectionException {
        try {
            FileManagerClientFactory fmcf= (FileManagerClientFactory) Class.forName(getClientFactoryName()).newInstance();
            fmcf.setTestConnection(testConnection);
            fmcf.setUrl(filemgrUrl);
            return fmcf.createFileManagerClient();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FileManagerServer createServer(int port) throws IOException {
        String serverFactory = "org.apache.oodt.cas.filemgr.system.rpc." +
                (SERVER ? "AvroFileManagerServerFactory":"XmlRpcFileManagerServerFactory");
        try {
            FileManagerServerFactory fmsf= (FileManagerServerFactory) Class.forName(serverFactory).newInstance();
            fmsf.setPort(port);
            return fmsf.createFileManagerServer();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
