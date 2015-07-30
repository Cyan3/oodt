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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.*;
import org.apache.oodt.cas.filemgr.structs.avrotypes.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.*;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.AvroTypeFactory;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.metadata.Metadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvroFileManagerClient implements FileManagerClient {

    private static Logger LOG = Logger.getLogger(AvroFileManagerClient.class
            .getName());

    /* Avro-Rpc client */
    Transceiver client;

    /* proxy for the server */
    AvroFileManager proxy;

    /* URL where the fileManager is */
    private URL fileManagerUrl;

    /*DataTransfer class for transferring products*/
    private DataTransfer dataTransfer = null;

    public AvroFileManagerClient(final URL url) throws ConnectionException {
        this(url, true);
    }

    public AvroFileManagerClient(final URL url, boolean testConnection) throws ConnectionException {
        // set up the configuration, if there is any

        if (System.getProperty("org.apache.oodt.cas.filemgr.properties") != null) {
            String configFile = System.getProperty("org.apache.oodt.cas.filemgr.properties");

            LOG.log(Level.INFO, "Loading File Manager Configuration Properties from: [" + configFile + "]");

            try {
                System.getProperties().load(new FileInputStream(new File(configFile)));
            } catch (Exception e) {
                LOG.log(Level.INFO, "Error loading configuration properties from: [" + configFile + "]");
            }

        }
        //setup the and start the client
        try {
            this.fileManagerUrl = url;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(this.fileManagerUrl.getPort());
            this.client = new NettyTransceiver(inetSocketAddress);
            proxy = (AvroFileManager) SpecificRequestor.getClient(AvroFileManager.class, client);

        } catch (IOException e) {
            LOG.log(Level.WARNING, "IOException when connecting to filemgr: ["
                    + this.fileManagerUrl + "]");
        }

        if (testConnection && !isAlive()) {
            throw new ConnectionException("Exception connecting to filemgr: ["
                    + this.fileManagerUrl + "]");
        }
    }

    @Override
    public boolean refreshConfigAndPolicy() {
        boolean success = false;

        try {
            success = proxy.refreshConfigAndPolicy();
        } catch (AvroRemoteException e) {
            LOG.log(Level.WARNING, "AvroRemoteException when connecting to filemgr: ["
                    + this.fileManagerUrl + "]");
            success = false;
        }

        return success;
    }

    @Override
    public boolean isAlive() {
        boolean success;

        try {
            if(proxy != null)
                success = proxy.isAlive();
            else return false;
        } catch (AvroRemoteException e) {
            LOG.log(Level.WARNING, "AvroRemoteException when connecting to filemgr: ["
                    + this.fileManagerUrl + "]");
            success = false;
        }

        return success;
    }

    @Override
    public boolean transferringProduct(Product product) throws DataTransferException {
        boolean success;

        try {
            success = proxy.transferringProduct(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            e.printStackTrace();
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

    @Override
    public boolean removeProductTransferStatus(Product product) throws DataTransferException {
        boolean success;
        try {
            success = proxy.removeProductTransferStatus(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
        return success;
    }

    @Override
    public boolean isTransferComplete(Product product) throws DataTransferException {
        boolean success;
        try {
            success = this.proxy.isTransferComplete(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
        return success;
    }

    @Override
    public boolean moveProduct(Product product, String newPath) throws DataTransferException {
        boolean success;
        try {
            success = this.proxy.moveProduct(AvroTypeFactory.getAvroProduct(product), newPath);
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
        return success;
    }

    @Override
    public boolean modifyProduct(Product product) throws CatalogException {
        boolean success;
        try {
            success = this.proxy.modifyProduct(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return success;
    }

    @Override
    public boolean removeProduct(Product product) throws CatalogException {
        boolean success;
        try {
            success = this.proxy.removeProduct(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return success;
    }

    @Override
    public FileTransferStatus getCurrentFileTransfer() throws DataTransferException {
        try {
            return AvroTypeFactory.getFileTransferStatus(this.proxy.getCurrentFileTransfer());
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @Override
    public List<FileTransferStatus> getCurrentFileTransfers() throws DataTransferException {
        List<FileTransferStatus> fileTransferStatuses = new ArrayList<FileTransferStatus>();
        try {
            for (AvroFileTransferStatus afts : this.proxy.getCurrentFileTransfers()) {
                fileTransferStatuses.add(AvroTypeFactory.getFileTransferStatus(afts));
            }
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
        return fileTransferStatuses;
    }

    @Override
    public double getProductPctTransferred(Product product) throws DataTransferException {
        try {
            return this.proxy.getProductPctTransferred(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @Override
    public double getRefPctTransferred(Reference reference) throws DataTransferException {
        try {
            return this.proxy.getRefPctTransferred(AvroTypeFactory.getAvroReference(reference));
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @Override
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum) throws CatalogException {
        try {

            return AvroTypeFactory.getProductPage(this.proxy.pagedQuery(
                    AvroTypeFactory.getAvroQuery(query),
                    AvroTypeFactory.getAvroProductType(type),
                    pageNum
            ));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public ProductPage getFirstPage(ProductType type) throws CatalogException {
        try {
            return AvroTypeFactory.getProductPage(this.proxy.getFirstPage(AvroTypeFactory.getAvroProductType(type)));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public ProductPage getLastPage(ProductType type) throws CatalogException {
        try {
            return AvroTypeFactory.getProductPage(this.proxy.getLastPage(AvroTypeFactory.getAvroProductType(type)));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public ProductPage getNextPage(ProductType type, ProductPage currPage) throws CatalogException {
        try {
            return AvroTypeFactory.getProductPage(this.proxy.getNextPage(
                    AvroTypeFactory.getAvroProductType(type),
                    AvroTypeFactory.getAvroProductPage(currPage)
            ));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public ProductPage getPrevPage(ProductType type, ProductPage currPage) throws CatalogException {
        try {
            return AvroTypeFactory.getProductPage(this.proxy.getPrevPage(
                    AvroTypeFactory.getAvroProductType(type),
                    AvroTypeFactory.getAvroProductPage(currPage)
            ));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public String addProductType(ProductType type) throws RepositoryManagerException {
        try {
            return this.proxy.addProductType(AvroTypeFactory.getAvroProductType(type));
        } catch (AvroRemoteException e) {
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    @Override
    public boolean hasProduct(String productName) throws CatalogException {
        try {
            return this.proxy.hasProduct(productName);
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public int getNumProducts(ProductType type) throws CatalogException {
        try {
            return this.proxy.getNumProducts(AvroTypeFactory.getAvroProductType(type));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public List<Product> getTopNProducts(int n) throws CatalogException {
        List<Product> products = new ArrayList<Product>();
        try {
            for (AvroProduct p : this.proxy.getTopNProducts(n)) {
                products.add(AvroTypeFactory.getProduct(p));
            }
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return products;
    }

    @Override
    public List<Product> getTopNProductsByProductType(int n, ProductType type) throws CatalogException {
        List<Product> products = new ArrayList<Product>();
        try {
            for (AvroProduct p : this.proxy.getTopNProductsByProductType(n, AvroTypeFactory.getAvroProductType(type))) {
                products.add(AvroTypeFactory.getProduct(p));
            }
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return products;
    }

    @Override
    public void setProductTransferStatus(Product product) throws CatalogException {
        try {
            this.proxy.setProductTransferStatus(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public void addProductReferences(Product product) throws CatalogException {
        try {
            this.proxy.addProductReferences(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public void addMetadata(Product product, Metadata metadata) throws CatalogException {
        try {
            this.proxy.addMetadata(AvroTypeFactory.getAvroProduct(product),
                    AvroTypeFactory.getAvroMetadata(metadata));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }

    }

    @Override
    public boolean updateMetadata(Product product, Metadata met) throws CatalogException {
        try {
            return this.proxy.updateMetadata(
                    AvroTypeFactory.getAvroProduct(product),
                    AvroTypeFactory.getAvroMetadata(met)
            );
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public String catalogProduct(Product product) throws CatalogException {
        try {
            return this.proxy.catalogProduct(AvroTypeFactory.getAvroProduct(product));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public Metadata getMetadata(Product product) throws CatalogException {
        try {
            return AvroTypeFactory.getMetadata(this.proxy.getMetadata(AvroTypeFactory.getAvroProduct(product)));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public Metadata getReducedMetadata(Product product, List<?> elements) throws CatalogException {
        try {
            return AvroTypeFactory.getMetadata(
                    this.proxy.getReducedMetadata(AvroTypeFactory.getAvroProduct(product), (List<String>) elements));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public boolean removeFile(String filePath) throws DataTransferException {
        try {
            return this.proxy.removeFile(filePath);
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @Override
    public byte[] retrieveFile(String filePath, int offset, int numBytes) throws DataTransferException {
        try {
            return this.proxy.retrieveFile(filePath, offset, numBytes).array();
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @Override
    public void transferFile(String filePath, byte[] fileData, int offset, int numBytes) throws DataTransferException {
        try {
            this.proxy.transferFile(filePath, ByteBuffer.wrap(fileData), offset, numBytes);
        } catch (AvroRemoteException e) {
            throw new DataTransferException(e.getMessage());
        }

    }

    @Override
    public List<Product> getProductsByProductType(ProductType type) throws CatalogException {
        List<Product> products = new ArrayList<Product>();
        try {
            for (AvroProduct ap : this.proxy.getProductsByProductType(AvroTypeFactory.getAvroProductType(type))) {
                products.add(AvroTypeFactory.getProduct(ap));
            }
            return products;

        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }

    }

    @Override
    public List<Element> getElementsByProductType(ProductType type) throws ValidationLayerException {
        List<Element> products = new ArrayList<Element>();
        try {
            for (AvroElement ap : this.proxy.getElementsByProductType(AvroTypeFactory.getAvroProductType(type))) {
                products.add(AvroTypeFactory.getElement(ap));
            }
        } catch (AvroRemoteException e) {
            throw new ValidationLayerException(e.getMessage());
        }
        return products;
    }

    @Override
    public Element getElementById(String elementId) throws ValidationLayerException {
        try {
            return AvroTypeFactory.getElement(this.proxy.getElementById(elementId));
        } catch (AvroRemoteException e) {
            throw new ValidationLayerException(e.getMessage());
        }
    }

    @Override
    public Element getElementByName(String elementName) throws ValidationLayerException {
        try {
            return AvroTypeFactory.getElement(this.proxy.getElementByName(elementName));
        } catch (AvroRemoteException e) {
            throw new ValidationLayerException(e.getMessage());
        }
    }

    @Override
    public Element getElementByName(String elementName, ProductType type) throws ValidationLayerException {
        return null;
    }

    @Override
    public List<QueryResult> complexQuery(ComplexQuery complexQuery) throws CatalogException {
        List<QueryResult> queryResults = new ArrayList<QueryResult>();
        try {
            List<AvroQueryResult> avroQueryResults = this.proxy.complexQuery(AvroTypeFactory.getAvroComplexQuery(complexQuery));
            for (AvroQueryResult aqr : avroQueryResults) {
                queryResults.add(AvroTypeFactory.getQueryResult(aqr));
            }
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return queryResults;
    }

    @Override
    public List<Product> query(Query query, ProductType type) throws CatalogException {
        List<Product> products = new ArrayList<Product>();
        try {
            for (AvroProduct ap : this.proxy.query(AvroTypeFactory.getAvroQuery(query), AvroTypeFactory.getAvroProductType(type))) {
                products.add(AvroTypeFactory.getProduct(ap));
            }
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return products;
    }

    @Override
    public ProductType getProductTypeByName(String productTypeName) throws RepositoryManagerException {
        try {
            return AvroTypeFactory.getProductType(this.proxy.getProductTypeByName(productTypeName));
        } catch (AvroRemoteException e) {
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    @Override
    public ProductType getProductTypeById(String productTypeId) throws RepositoryManagerException {
        try {
            return AvroTypeFactory.getProductType(this.proxy.getProductTypeById(productTypeId));
        } catch (AvroRemoteException e) {
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    @Override
    public List<ProductType> getProductTypes() throws RepositoryManagerException {
        List<ProductType> productTypes = new ArrayList<ProductType>();
        try {
            for (AvroProductType apt : this.proxy.getProductTypes()) {
                productTypes.add(AvroTypeFactory.getProductType(apt));
            }
        } catch (AvroRemoteException e) {
            throw new RepositoryManagerException(e.getMessage());
        }
        return productTypes;
    }

    @Override
    public List<Reference> getProductReferences(Product product) throws CatalogException {
        List<Reference> references = new ArrayList<Reference>();
        try {
            for (AvroReference ar : this.proxy.getProductReferences(AvroTypeFactory.getAvroProduct(product))) {
                references.add(AvroTypeFactory.getReference(ar));
            }
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
        return references;
    }

    @Override
    public Product getProductById(String productId) throws CatalogException {
        try {
            return AvroTypeFactory.getProduct(this.proxy.getProductById(productId));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public Product getProductByName(String productName) throws CatalogException {
        try {
            return AvroTypeFactory.getProduct(this.proxy.getProductByName(productName));
        } catch (AvroRemoteException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    @Override
    public String ingestProduct(Product product, Metadata metadata,
                                boolean clientTransfer) throws Exception {
        try {
            // ingest product
            String productId = this.proxy.ingestProduct(
                    AvroTypeFactory.getAvroProduct(product),
                    AvroTypeFactory.getAvroMetadata(metadata),
                    clientTransfer);

            if (clientTransfer) {
                LOG.log(Level.FINEST,
                        "File Manager Client: clientTransfer enabled: "
                                + "transfering product ["
                                + product.getProductName() + "]");

                // we need to transfer the product ourselves
                // make sure we have the product ID
                if (productId == null) {
                    throw new Exception("Request to ingest product: "
                            + product.getProductName()
                            + " but no product ID returned from File "
                            + "Manager ingest");
                }

                if (dataTransfer == null) {
                    throw new Exception("Request to ingest product: ["
                            + product.getProductName()
                            + "] using client transfer, but no "
                            + "dataTransferer specified!");
                }

                product.setProductId(productId);

                if (!Boolean.getBoolean("org.apache.oodt.cas.filemgr.serverside.versioning")) {
                    // version the product
                    Versioner versioner = GenericFileManagerObjectFactory
                            .getVersionerFromClassName(product.getProductType()
                                    .getVersioner());
                    versioner.createDataStoreReferences(product, metadata);

                    // add the newly versioned references to the data store
                    try {
                        addProductReferences(product);
                    } catch (CatalogException e) {
                        LOG
                                .log(
                                        Level.SEVERE,
                                        "ingestProduct: RepositoryManagerException "
                                                + "when adding Product References for Product : "
                                                + product.getProductName()
                                                + " to RepositoryManager: Message: "
                                                + e.getMessage());
                        throw e;
                    }
                } else {
                    product.setProductReferences(getProductReferences(product));
                }

                // now transfer the product
                try {
                    dataTransfer.transferProduct(product);
                    // now update the product's transfer status in the data
                    // store
                    product.setTransferStatus(Product.STATUS_RECEIVED);

                    try {
                        setProductTransferStatus(product);
                    } catch (CatalogException e) {
                        LOG
                                .log(
                                        Level.SEVERE,
                                        "ingestProduct: RepositoryManagerException "
                                                + "when updating product transfer status for Product: "
                                                + product.getProductName()
                                                + " Message: " + e.getMessage());
                        throw e;
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE,
                            "ingestProduct: DataTransferException when transfering Product: "
                                    + product.getProductName() + ": Message: "
                                    + e.getMessage());
                    throw new DataTransferException(e);
                }

            }
            return productId;

            // error versioning file
        } catch (VersioningException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "ingestProduct: VersioningException when versioning Product: "
                            + product.getProductName() + " with Versioner "
                            + product.getProductType().getVersioner()
                            + ": Message: " + e.getMessage());
            throw new VersioningException(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Failed to ingest product [" + product
                    + "] : " + e.getMessage() + " -- rolling back ingest");
            try {
                AvroProduct avroProduct = AvroTypeFactory.getAvroProduct(product);
                this.proxy.removeProduct(avroProduct);
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, "Failed to rollback ingest of product ["
                        + product + "] : " + e.getMessage());
            }
            throw new Exception("Failed to ingest product [" + product + "] : "
                    + e.getMessage());
        }

    }

    @Override
    public Metadata getCatalogValues(Metadata metadata, ProductType productType) throws Exception {
        return AvroTypeFactory.getMetadata(this.proxy.getCatalogValues(
                AvroTypeFactory.getAvroMetadata(metadata),
                AvroTypeFactory.getAvroProductType(productType)));
    }

    @Override
    public Metadata getOrigValues(Metadata metadata, ProductType productType) throws Exception {
        return AvroTypeFactory.getMetadata(this.proxy.getOrigValues(
                AvroTypeFactory.getAvroMetadata(metadata),
                AvroTypeFactory.getAvroProductType(productType)));
    }

    @Override
    public Query getCatalogQuery(Query query, ProductType productType) throws Exception {
        return AvroTypeFactory.getQuery(this.proxy.getCatalogQuery(
                AvroTypeFactory.getAvroQuery(query),
                AvroTypeFactory.getAvroProductType(productType)
        ));
    }

    @Override
    public URL getFileManagerUrl() {
        return this.fileManagerUrl;
    }

    @Override
    public void setFileManagerUrl(URL fileManagerUrl) {
        this.fileManagerUrl = fileManagerUrl;

    }

    @Override
    public DataTransfer getDataTransfer() {
        return this.dataTransfer;
    }

    @Override
    public void setDataTransfer(DataTransfer dataTransfer) {
        this.dataTransfer = dataTransfer;
    }

    public static void main(String[] args) {
        CmdLineUtility cmdLineUtility = new CmdLineUtility();
        cmdLineUtility.run(args);
    }
}
