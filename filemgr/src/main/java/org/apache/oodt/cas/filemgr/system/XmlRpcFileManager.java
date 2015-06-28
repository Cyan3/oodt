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

package org.apache.oodt.cas.filemgr.system;

//APACHE imports

//OODT imports
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.exceptions.*;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.util.XmlRpcStructFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.WebServer;


//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
        import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based File manager.
 * </p>
 * 
 */

public class XmlRpcFileManager extends FileManager {

    /* our xml rpc web server */
    private WebServer webServer = null;


    /**
     * <p>
     * Creates a new XmlRpcFileManager with the given metadata store factory,
     * and the given data store factory, on the given port.
     * </p>
     *
     * @param port The web server port to run the XML Rpc server on, defaults to
     *             1999.
     */
    public XmlRpcFileManager(int port) throws Exception {
        super(port);
    }

    @Override
    public void startUpServer(int port) {
        // start up the web server
        this.port = port;
        webServer = new WebServer(this.port);
        webServer.addHandler("filemgr", this);
        webServer.start();
    }


    public boolean xmlrpc_transferringProduct(Hashtable<String, Object> productHash) {
        return this.transferringProduct(XmlRpcStructFactory.getProductFromXmlRpc(productHash));
    }

    public Hashtable<String, Object> xmlrpc_getCurrentFileTransfer() {
        FileTransferStatus status = this.getCurrentFileTransfer();
        if (status == null) {
            return new Hashtable<String, Object>();
        } else
            return XmlRpcStructFactory.getXmlRpcFileTransferStatus(status);
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getCurrentFileTransfers() {
        List<FileTransferStatus> currentTransfers = this.getCurrentFileTransfers();

        if (currentTransfers != null && currentTransfers.size() > 0) {
            return XmlRpcStructFactory
                    .getXmlRpcFileTransferStatuses(currentTransfers);
        } else
            return new Vector<Hashtable<String, Object>>();
    }

    public double xmlrpc_getRefPctTransferred(Hashtable<String, Object> refHash) {
        return this.getRefPctTransferred(XmlRpcStructFactory
                .getReferenceFromXmlRpc(refHash));
    }

    public boolean xmlrpc_removeProductTransferStatus(Hashtable<String, Object> productHash) {
        return this.removeProductTransferStatus(XmlRpcStructFactory.getProductFromXmlRpc(productHash));
    }

    public boolean xmlrpc_isTransferComplete(Hashtable<String, Object> productHash) {
        return this.isTransferComplete(XmlRpcStructFactory.getProductFromXmlRpc(productHash)) ;
    }

    public Hashtable<String, Object> xmlrpc_pagedQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash,
            int pageNum) throws CatalogException {

        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(this.pagedQuery(query,type,pageNum));
    }


    public Hashtable<String, Object> xmlrpc_getFirstPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(this.getFirstPage(type));
    }

    public Hashtable<String, Object> xmlrpc_getLastPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(this.getLastPage(type));
    }

    public Hashtable<String, Object> xmlrpc_getNextPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(this.getNextPage(type,currPage));
    }

    public Hashtable<String, Object> xmlrpc_getPrevPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(this.getPrevPage(type,currPage));
    }

    public String xmlrpc_addProductType(Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        ProductType productType = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return this.addProductType(productType);

    }

    public synchronized boolean xmlrpc_setProductTransferStatus(
            Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        return this.setProductTransferStatus(product);
    }

    public int xmlrpc_getNumProducts(Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return this.getNumProducts(type);
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getTopNProducts(int n)
            throws CatalogException {
            return XmlRpcStructFactory.getXmlRpcProductList(this.getTopNProducts(n));
    }


    public Vector<Hashtable<String, Object>> xmlrpc_getTopNProducts(int n,
                                                             Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
            return XmlRpcStructFactory.getXmlRpcProductList(this.getTopNProducts(n,type));
    }


    public boolean xmlrpc_hasProduct(String productName) throws CatalogException {
        return this.hasProduct(productName);
    }

    public Hashtable<String, Object> xmlrpc_getMetadata(
            Hashtable<String, Object> productHash) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.getMetadataPub(product).getHashtable();
    }

    public Hashtable<String, Object> xmlrpc_getReducedMetadata(
            Hashtable<String, Object> productHash, Vector<String> elements)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.getReducedMetadataPub(product, elements).getHashtable();
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getProductTypes()
            throws RepositoryManagerException {
            return XmlRpcStructFactory.getXmlRpcProductTypeList(this.getProductTypes());
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getProductReferences(
            Hashtable<String, Object> productHash)
            throws CatalogException {
            Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
            return XmlRpcStructFactory.getXmlRpcReferences(this.getProductReferences(product));
    }

    public Hashtable<String, Object> xmlrpc_getProductById(String productId)
            throws CatalogException {
        Product product = this.getProductById(productId);
        return XmlRpcStructFactory.getXmlRpcProduct(product);
    }

    public Hashtable<String, Object> xmlrpc_getProductByName(String productName)
            throws CatalogException {
        Product product = this.getProductByName(productName);
        return XmlRpcStructFactory.getXmlRpcProduct(product);
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getProductsByProductType(
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcProductList(this.getProductsByProductType(type));
    }

    public Vector<Hashtable<String, Object>> xmlrpc_getElementsByProductType(
            Hashtable<String, Object> productTypeHash) throws ValidationLayerException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcElementList(this.getElementsByProductType(type));
    }

    public Hashtable<String, Object> xmlrpc_getElementById(String elementId)
            throws ValidationLayerException {
            return XmlRpcStructFactory.getXmlRpcElement(this.getElementById(elementId));
    }

    public Hashtable<String, Object> xmlrpc_getElementByName(String elementName)
            throws ValidationLayerException {
            return XmlRpcStructFactory.getXmlRpcElement(this.getElementByName(elementName));
    }

    public Vector<Hashtable<String, Object>> xmrrpc_complexQuery(
            Hashtable<String, Object> complexQueryHash) throws CatalogException {
            ComplexQuery complexQuery = XmlRpcStructFactory
                    .getComplexQueryFromXmlRpc(complexQueryHash);
            return XmlRpcStructFactory.getXmlRpcQueryResults(this.complexQuery(complexQuery));
    }

    public Vector<Hashtable<String, Object>> xmlrpc_query(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcProductList(this.queryPub(query, type));
    }

    public Hashtable<String, Object> xmlrpc_getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
        return XmlRpcStructFactory.getXmlRpcProductType(this.getProductTypeByName(productTypeName));
    }

    public Hashtable<String, Object> xmlrpc_getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
            return XmlRpcStructFactory.getXmlRpcProductType(this.getProductTypeById(productTypeId));
    }

    public boolean xmlrpc_updateMetadata(Hashtable<String, Object> productHash,
                                               Hashtable<String, Object> metadataHash) throws CatalogException{
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata met = new Metadata();
        met.addMetadata(metadataHash);
        this.updateMetadata(product,met);
        return true;
    }

    public String xmlrpc_catalogProduct(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return catalogProductPub(p);
    }

    public synchronized boolean xmlrpc_addMetadata(Hashtable<String, Object> productHash,
                                            Hashtable<String, String> metadata) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata met = new Metadata();
        met.addMetadata((Hashtable)metadata);
        return this.addMetadataPub(product, met);
    }

    public synchronized boolean xmlrpc_addProductReferences(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return addProductReferencesPub(product);
    }

    public String xmlrpc_ingestProduct(Hashtable<String, Object> productHash,
                                Hashtable<String, String> metadata, boolean clientTransfer)
            throws VersioningException, RepositoryManagerException,
            DataTransferException, CatalogException {

        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        Metadata m = new Metadata();
        m.addMetadata((Hashtable)metadata);

        return this.ingestProduct(p,m,clientTransfer);
    }

    public byte[] xmlrpc_retrieveFile(String filePath, int offset, int numBytes)
            throws DataTransferException {
        return this.retrieveFile(filePath, offset, numBytes);
    }

    public boolean xmlrpc_transferFile(String filePath, byte[] fileData, int offset,
                                int numBytes) {
        return this.transferFile(filePath, fileData, offset, numBytes);
    }

    public boolean xmlrpc_moveProduct(Hashtable<String, Object> productHash, String newPath)
            throws DataTransferException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.moveProduct(p,newPath);
    }

    public boolean xmlrpc_removeFile(String filePath) throws DataTransferException, IOException {
        return this.removeFile(filePath);
    }

    public boolean xmlrpc_modifyProduct(Hashtable<?, ?> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.modifyProduct(p);
    }

    public boolean xmlrpc_removeProduct(Hashtable<String, Object> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.removeProduct(p);
    }

    public Hashtable<String, Object> xmlrpc_getCatalogValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return this.getCatalogValuesPub(m, productType).getHashtable();
    }

    public Hashtable<String, Object> xmlrpc_getOrigValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return this.getOrigValuesPub(m, productType).getHashtable();
    }

    public Hashtable<String, Object> xmlrpc_getCatalogQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException, QueryFormulationException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcQuery(this.getCatalogQueryPub(query, productType));
    }

    public boolean shutdown() {
        if (this.webServer != null) {
            this.webServer.shutdown();
            this.webServer = null;
            return true;
        } else
            return false;
    }
    public WebServer getWebServer(){
        return this.webServer;
    }


    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "FileManager --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }

        @SuppressWarnings("unused")
        FileManager manager = new XmlRpcFileManager(portNum);

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }


























}
