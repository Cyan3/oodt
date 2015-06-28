package org.apache.oodt.cas.filemgr.system;


import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.*;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.*;


import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface FileManagerClient {

    public boolean refreshConfigAndPolicy();

    public boolean isAlive();

    public boolean transferringProduct(Product product)throws DataTransferException;

    public boolean removeProductTransferStatus(Product product)throws DataTransferException;

    public boolean isTransferComplete(Product product)throws DataTransferException;

    public boolean moveProduct(Product product, String newPath)throws DataTransferException;

    public boolean modifyProduct(Product product) throws CatalogException;

    public boolean removeProduct(Product product) throws CatalogException;

    @SuppressWarnings("unchecked")
    public FileTransferStatus getCurrentFileTransfer()throws DataTransferException;

    @SuppressWarnings("unchecked")
    public List<FileTransferStatus> getCurrentFileTransfers()throws DataTransferException;

    public double getProductPctTransferred(Product product)throws DataTransferException;

    public double getRefPctTransferred(Reference reference)throws DataTransferException;

    @SuppressWarnings("unchecked")
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum)throws CatalogException;

    @SuppressWarnings("unchecked")
    public ProductPage getFirstPage(ProductType type) throws CatalogException;

    @SuppressWarnings("unchecked")
    public ProductPage getLastPage(ProductType type) throws CatalogException;

    @SuppressWarnings("unchecked")
    public ProductPage getNextPage(ProductType type, ProductPage currPage)throws CatalogException;

    @SuppressWarnings("unchecked")
    public ProductPage getPrevPage(ProductType type, ProductPage currPage)throws CatalogException;

    public String addProductType(ProductType type)throws RepositoryManagerException;

    public boolean hasProduct(String productName) throws CatalogException;

    public int getNumProducts(ProductType type) throws CatalogException;

    @SuppressWarnings("unchecked")
    public List<Product> getTopNProducts(int n) throws CatalogException;

    @SuppressWarnings("unchecked")
    public List<Product> getTopNProducts(int n, ProductType type)throws CatalogException;

    public void setProductTransferStatus(Product product)throws CatalogException;

    public void addProductReferences(Product product) throws CatalogException;

    public void addMetadata(Product product, Metadata metadata)throws CatalogException;

    public boolean updateMetadata(Product product, Metadata met)throws CatalogException;

    public String catalogProduct(Product product) throws CatalogException;

    @SuppressWarnings("unchecked")
    public Metadata getMetadata(Product product) throws CatalogException;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Metadata getReducedMetadata(Product product, List<?> elements)throws CatalogException;

    public boolean removeFile(String filePath) throws DataTransferException;

    public byte[] retrieveFile(String filePath, int offset, int numBytes)throws DataTransferException;

    public void transferFile(String filePath, byte[] fileData, int offset,int numBytes) throws DataTransferException;

    @SuppressWarnings("unchecked")
    public List<Product> getProductsByProductType(ProductType type)throws CatalogException;

    @SuppressWarnings("unchecked")
    public List<Element> getElementsByProductType(ProductType type)throws ValidationLayerException;

    @SuppressWarnings("unchecked")
    public Element getElementById(String elementId)throws ValidationLayerException;

    @SuppressWarnings("unchecked")
    public Element getElementByName(String elementName)throws ValidationLayerException;

    @SuppressWarnings("unchecked")
    public Element getElementByName(String elementName, ProductType type)throws ValidationLayerException;

    public List<QueryResult> complexQuery(ComplexQuery complexQuery)throws CatalogException;

    @SuppressWarnings("unchecked")
    public List<Product> query(Query query, ProductType type)throws CatalogException;

    @SuppressWarnings("unchecked")
    public ProductType getProductTypeByName(String productTypeName)throws RepositoryManagerException;

    @SuppressWarnings("unchecked")
    public ProductType getProductTypeById(String productTypeId)throws RepositoryManagerException;

    @SuppressWarnings("unchecked")
    public List<ProductType> getProductTypes()throws RepositoryManagerException;

    @SuppressWarnings("unchecked")
    public List<Reference> getProductReferences(Product product)throws CatalogException;

    @SuppressWarnings("unchecked")
    public Product getProductById(String productId) throws CatalogException;

    @SuppressWarnings("unchecked")
    public Product getProductByName(String productName) throws CatalogException;

    public String ingestProduct(Product product, Metadata metadata,boolean clientTransfer) throws Exception;

    @SuppressWarnings("unchecked")
    public Metadata getCatalogValues(Metadata metadata, ProductType productType)throws XmlRpcException, IOException;

    @SuppressWarnings("unchecked")
    public Metadata getOrigValues(Metadata metadata, ProductType productType)throws XmlRpcException, IOException;

    @SuppressWarnings("unchecked")
    public Query getCatalogQuery(Query query, ProductType productType)throws XmlRpcException, IOException;

    public URL getFileManagerUrl();

    public void setFileManagerUrl(URL fileManagerUrl);

    public DataTransfer getDataTransfer();

    public void setDataTransfer(DataTransfer dataTransfer);

}