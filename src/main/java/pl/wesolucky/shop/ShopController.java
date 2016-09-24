package pl.wesolucky.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pl.wesolucky.shop.domain.FailureMessage;
import pl.wesolucky.shop.domain.Shop;
import pl.wesolucky.shop.domain.ShopDTO;
import pl.wesolucky.shop.presta.PrestaMySqlDAO;


@RestController
@RequestMapping("/api")
public class ShopController 
{
	
	@Inject
	private ShopRepository shopRepository;
	
	private final PrestaMySqlDAO prestaMySqlDAO = new PrestaMySqlDAO();
	private final Logger log = LoggerFactory.getLogger(ShopController.class);
	
	
    /**
     * POST  /shops : Creates a new shop.
     *
     * @param shop The shop to create
     * @return The ResponseEntity with status 201 (Created) and body with the new shop, 
     * or with status 400 (Bad Request) if the shop has already an ID
     */
    @RequestMapping(value = "/shops",
    				method = RequestMethod.POST,
    				produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createShop(@RequestBody Shop shop) 
    {
        log.info("REST request to save Shop : {}", shop);
        String failureMsg = "";
        if (shop.getId() != 0) 
        {
        	failureMsg = "A new shop cannot have an ID that already exist > id: " + shop.getId();
            return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
        }
        
        String baseUrl = shop.isHttpsEnabled() ? "https://" + shop.getDomain() : "http://" + shop.getDomain();
        shop.setBaseUrl(baseUrl);
        
        // check MySQL connection by list tables
        failureMsg = prestaMySqlDAO.checkTables(shop);
        if (!failureMsg.equals(""))
        {
        	failureMsg = "MySQL connection failed. " + failureMsg;
        	return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
        }
        Shop result = shopRepository.save(shop);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }
    
    /**
     * PUT  /shops : Updates an existing shop.
     *
     * @param shop the shop to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated shop,
     * or with status 400 (Bad Request) if the shop is not valid,
     * or with status 500 (Internal Server Error) if the shop couldn't be updated
     */
    @RequestMapping(value = "/shops",
    				method = RequestMethod.PUT,
    				produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateShop(@RequestBody Shop shop)
    {
        log.info("REST request to update Shop : {}", shop);
        String failureMsg = "";
        
        // check whether the shop exists
        Shop archShop = shopRepository.findOne(shop.getId());
        if (archShop == null) 
        {
        	failureMsg = "Update failed. Can't find shop with id: " + shop.getId();
        	return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
        }
        
        String baseUrl = shop.isHttpsEnabled() ? "https://" + shop.getDomain() : "http://" + shop.getDomain();
        shop.setBaseUrl(baseUrl);
        
        // check MySQL connection by list of tables
        failureMsg = prestaMySqlDAO.checkTables(shop);
        if (!failureMsg.equals(""))
        {
        	failureMsg = "MySQL connection failed. " + failureMsg;
        	return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
        }
        
        Shop result = shopRepository.save(shop);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }
    
    /**
     * DELETE  /shops/:id : deletes shop by id.
     *
     * @param id The id of the shop to delete
     * @return The ResponseEntity with status 200 (OK) and with the deleted shop data in body, or failure message 400 (Bad request)
     */
    @RequestMapping(value = "/shops/{id}",
			method = RequestMethod.DELETE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deletShop(@PathVariable Long id)
	{
        log.info("REST request to delete Shop : {}", id);
        String failureMsg = "";
        Shop archShop = shopRepository.findOne(id);
        if (archShop == null) failureMsg = "Deletion failed. Can't find shop with id: " + id;
        if (!failureMsg.equals(""))
        {
        	 return new ResponseEntity<Object>(new FailureMessage(failureMsg), HttpStatus.BAD_REQUEST);
        }
        // TODO remove products and products images
        shopRepository.delete(id);
        return new ResponseEntity<Object>(archShop, HttpStatus.OK);
	}

    /**
     * GET  /shops : gets all the shops DTO with base data (don't include MySQL connection parameters without reason).
     *
     * @return List<ShopDTO> 
     */
    @RequestMapping(value = "/shops",
    				method = RequestMethod.GET,
    				produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShopDTO> getAllShops() 
    {
        log.info("REST request to get all Shops");
        List<Shop> shops = shopRepository.findAll();
        List<ShopDTO> shopDtoList = new ArrayList<>();
        for (int i = 0; i < shops.size(); i++) shopDtoList.add(new ShopDTO(shops.get(i)));
        return shopDtoList;
    }

    /**
     * GET  /shops/:id : gets the shop by id.
     *
     * @param id The id of the shop to retrieve
     * @return The ResponseEntity with status 200 (OK) and with the shop in body, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/shops/{id}",
    				method = RequestMethod.GET,
    				produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getShop(@PathVariable Long id) 
    {
        log.info("REST request to get Shop : {}", id);
        Shop shop = shopRepository.findOne(id);
        return Optional.ofNullable(shop)
            .map(result -> new ResponseEntity<Object>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<Object>(new FailureMessage("Can't find shop with id " + id), HttpStatus.NOT_FOUND));
    }
	
	
	
}
