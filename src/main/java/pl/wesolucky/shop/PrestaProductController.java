package pl.wesolucky.shop;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pl.wesolucky.shop.domain.FailureMessage;
import pl.wesolucky.shop.domain.PrestaProduct;
import pl.wesolucky.shop.domain.PrestaProductImage;
import pl.wesolucky.shop.domain.Shop;
import pl.wesolucky.shop.presta.PrestaMySqlDAO;
import pl.wesolucky.shop.presta.PrestaProductImageRepository;
import pl.wesolucky.shop.presta.PrestaProductRepository;

@RestController
@RequestMapping("/api")
public class PrestaProductController 
{
	
	@Inject
	private ShopRepository shopRepository;
	
	@Inject
	private PrestaProductRepository prestaProductRepository;
	
	@Inject
	private PrestaProductImageRepository prestaProductImageRepository;
	
	private final PrestaMySqlDAO prestaMySqlDAO = new PrestaMySqlDAO();
	private final Logger log = LoggerFactory.getLogger(ShopController.class);
	
	
	/**
	 * Gets selected product by id from default source (H2 data base).
	 * 
	 * @param id The id of selected product.
	 * @return ResponseEntity with PrestaProduct in body, 
	 * or ResponseEntity with bed request status and failure message
	 */
    @RequestMapping(value = "products/default/{id}", 
			method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getDefaultProductById(@PathVariable Long id)
	{
        String failureMsg = "";

    	PrestaProduct product = prestaProductRepository.findOne(id);
    	if (product == null)
    	{
        	failureMsg = "Cannot find product with id: " + id;
            return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
    	}
    	
    	return new ResponseEntity<Object>(product, HttpStatus.OK);
	}
	
	/**
	 * Gets all selected shop products from default source (H2 data base).
	 * 
	 * @param id The id of selected shop
	 * @return List<PrestaProduct>
	 */
    @RequestMapping(value = "products/default/shop/{id}", 
			method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDefaultProductsByShopId(@PathVariable Long id)
    {
    	log.info("REST request to get products from H2 data base : {}", id);
    	List<PrestaProduct> productsList = prestaProductRepository.findByShopId(id);
    	return new ResponseEntity<Object>(productsList, HttpStatus.OK);
    }
    
    
    /**
     * Gets all products data from MySQL and checks all products exist
     * in data base of application (H2). If not save.
     * 
     * @param id The id of selected shop
     * @return List<PrestaProduct>
     */
    @RequestMapping(value = "products/presta/shop/{id}", 
    				method = RequestMethod.GET, 
    				produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPrestaProductsByShopId(@PathVariable Long id)
    {
    	log.info("REST request to get products from MySQL and save them in H2 data base : {}", id);
    	
    	Shop shop = shopRepository.findOne(id);
    	prestaMySqlDAO.readAllPrestaProductData(shop);
    	
        StopWatch watch = new StopWatch("ShopController.showShopProducts");
        watch.start("save new products");
    	
    	// sort by productId then by attributeProductId
    	Comparator<PrestaProduct> comparator = (p, o) -> p.getProductId() - o.getProductId();
    	comparator = comparator.thenComparing((p, o) -> p.getAttributeProductId() - o.getAttributeProductId());
    	shop.getMySqlProductsList().sort(comparator);
    	
    	int newProductCounter = 0;
    	int newImageCounter = 0;
    	
    	// get all saved products to check a product already exists in H2 data base
    	List<PrestaProduct> archPrestaProductList = prestaProductRepository.findByShopId(shop.getId());
    	
    	for (int i = 0; i < shop.getMySqlProductsList().size(); i++)
    	{
    		PrestaProduct pProduct = shop.getMySqlProductsList().get(i);
    		
    		// check by productId and attributeProductId
    		if (!isProductExistsInH2Db(pProduct, archPrestaProductList))
    		{
    			newProductCounter++;
    			pProduct.setShopId(id);
    			prestaProductRepository.save(pProduct);
    			if (pProduct.getPrestaProductImageSet() == null)
    			{
    				log.debug("NULL image set for productId: " + pProduct.getProductId() 
    									+ " and attributeProductId: " + pProduct.getAttributeProductId());
    				continue;
    			}
    			
    			int imgIndex = 0;
    			for (PrestaProductImage pImage : pProduct.getPrestaProductImageSet())
    			{
    				newImageCounter++;
    				pImage.setIndex(imgIndex);
    				pImage.setPrestaProduct(pProduct);
    				prestaProductImageRepository.save(pImage);
    				imgIndex++;
    			}
    			
    		}
    	}
    	
    	watch.stop();
    	log.debug("newProductCounter: " + newProductCounter + " | newImageCounter: " + newImageCounter);
    	log.debug(watch.prettyPrint());
    	
    	List<PrestaProduct> productsList = prestaProductRepository.findByShopId(id);
    	return new ResponseEntity<Object>(productsList, HttpStatus.OK);
    }
    

	private boolean isProductExistsInH2Db(PrestaProduct pProduct, List<PrestaProduct> archPrestaProductList) 
	{
		// unique product should has unique values pair for productId and attributeProductId
    	List<PrestaProduct> checkList = archPrestaProductList.stream()
			.filter(p -> p.getProductId() == pProduct.getProductId() && p.getAttributeProductId() == pProduct.getAttributeProductId())
			.collect(Collectors.toList());
    	
    	if (checkList.size() > 1) 
    	{
    		System.err.println("More than one product found with the same productId: " + pProduct.getProductId() 
								+ " and attributeProductId: " + pProduct.getAttributeProductId());
    	}
    	
    	return checkList.size() > 0;
    	
	}
	
}
