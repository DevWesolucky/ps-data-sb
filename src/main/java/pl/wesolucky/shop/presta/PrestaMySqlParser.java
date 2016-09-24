package pl.wesolucky.shop.presta;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.wesolucky.shop.domain.PrestaProduct;
import pl.wesolucky.shop.domain.PrestaProductImage;
import pl.wesolucky.shop.domain.Shop;


public class PrestaMySqlParser 
{
	private final String PHOTO_SIZE = "-large_default";
	
	private Map<Integer, String> psProductLangNameMap;
	private Map<Integer, String> psProductLangLinkRewriteNameMap;
	private Map<Integer, String> psSupplierMap;
	private Map<Integer, Double> psTaxMap;
	
	private Map<Integer, Integer> psAttributeMap;
	private Map<Integer, String> psAttributeLangMap;
	private Map<Integer, Integer> psProductAttributeCombinationMap;
	private Map<Integer, String> psLayeredIndexableAttributeGroupLangValueMap;
	private Map<Integer, Integer> psImageMap;
	
	private List<Integer[]> psProductAttributeImageList;
	
	private Map<Integer, PrestaProduct> mainProductsMap;
	private Map<Integer, PrestaProduct> attributeProductsMap;
	private Map<String, PrestaProduct> realProductsMap;
	
	private final Logger log = LoggerFactory.getLogger(PrestaMySqlParser.class);
	
	/**
	 * Resets all collections before parse fresh data.
	 * 
	 * @param shop Selected shop
	 */
	public void resetCollections(Shop shop)
	{
		shop.setMySqlProductsList(new ArrayList<>());
		
		mainProductsMap = new HashMap<>();
		attributeProductsMap = new HashMap<>();
		realProductsMap = new HashMap<>();
	}
	
	/**
	 * Parses result of query for particular table.
	 * 
	 * @param resultSet The query result
	 * @param table The table
	 * @param shop Selected shop
	 */
	public void parseResultSetForTable(ResultSet resultSet, String table, Shop shop)
	{
		
		switch (table) {
		
			case "ps_product_lang":
				parsePsProductLang(resultSet);
				break;
				
			case "ps_supplier":
				parsePsSupplier(resultSet);
				break;
				
			case "ps_tax":
				parsePsTax(resultSet);
				break;
				
			case "ps_attribute":
				parsePsAttribute(resultSet);
				break;
				
			case "ps_attribute_lang":
				parsePsAttributeLang(resultSet);
				break;
				
			case "ps_product_attribute_combination":
				parsePsProductAttributeCombination(resultSet);
				break;
				
			case "ps_layered_indexable_attribute_group_lang_value":
				parsePssLayeredIndexableAttributeGroupLangValue(resultSet);
				break;
				
			case "ps_product_attribute_image":
				parsePsProductAttributeImage(resultSet);
				break;
				
			case "ps_image":
				parsePsImage(resultSet);
				break;
				
			case "ps_product":
				parsePsProduct(resultSet, shop);
				break;
				
			case "ps_product_attribute":
				parsePsProductAttribute(resultSet, shop);
				setupAttributeProductImages(shop);
				// setup main image after set hasChildren property (don't add all images for main product with children)
				setupMainProductImages(shop);
				break;
				
			case "ps_stock_available":
				parsePsStockAvailable(resultSet, shop);
				validateCodesAndSetupRealProductsMap(shop);
				
				log.debug(":: mainProductsMap.size(): " + mainProductsMap.size());
				log.debug(":: attributeProductsMap.size(): " + attributeProductsMap.size());
				log.debug(":: realProductsMap.size(): " + realProductsMap.size());
				break;

		}
	}

	
	// ::: PRODUCTS :::

	private void parsePsProduct(ResultSet resultSet, Shop shop) 
	{
		// {"id_product", "reference", "id_category_default", "ean13", "id_supplier", "id_tax_rules_group", 
		// "visibility", "wholesale_price", "price", "weight", "date_upd", "date_add"}
		
		try {
			while (resultSet.next()) 
			{
				PrestaProduct product = new PrestaProduct();
				product.setProductId(resultSet.getInt("id_product"));
				
				shop.getMySqlProductsList().add(product);
				mainProductsMap.put(product.getProductId(), product);
				
				product.setCode(resultSet.getString("reference"));
				product.setVisibility(resultSet.getString("visibility"));
				product.setName(psProductLangNameMap.get(product.getProductId()));
				product.setLinkRewrite(psProductLangLinkRewriteNameMap.get(product.getProductId()));
				
				String urlIdPart =  product.getProductId() + "-" +  product.getLinkRewrite(); // default in PresstaShop
				if (shop.isReverseIdInUrl()) urlIdPart = product.getLinkRewrite() + "-" + product.getProductId();
				product.setUrl(shop.getBaseUrl() + "/" + urlIdPart + ".html");
				
				// use supplier name as id (in presta db id_supplier is integer)
				product.setSupplierId(psSupplierMap.get(resultSet.getInt("id_supplier")));
				
				// product.setVatPercent(psTaxMap.get(resultSet.getInt("id_tax_rules_group"))); // TOFIX null exception
				
				product.setWholesaleNettoPrice(resultSet.getDouble("wholesale_price"));
				product.setNettoPrice(resultSet.getDouble("price"));
				
				product.setEan(resultSet.getString("ean13"));
				product.setWeight(resultSet.getDouble("weight"));
				
				product.setDateAdd(resultSet.getString("date_add").substring(0, 19));
				product.setDateUpdate(resultSet.getString("date_upd").substring(0, 19));
				
			}
			
			log.debug("--- main products num: " + mainProductsMap.size());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	private void parsePsProductAttribute(ResultSet resultSet, Shop shop) 
	{
		// {"id_product", "id_product_attribute", "reference", "wholesale_price", "price", "default_on"}
		
		try {
			while (resultSet.next()) 
			{
				PrestaProduct product = new PrestaProduct();
				product.setProductId(resultSet.getInt("id_product"));
				
				product.setAttributeProductId(resultSet.getInt("id_product_attribute"));
				attributeProductsMap.put(product.getAttributeProductId(), product);
				
				shop.getMySqlProductsList().add(product);

				PrestaProduct mainProduct = mainProductsMap.get(product.getProductId());
				if (mainProduct == null)
				{
					
					log.debug("Can't find main product for productId: " + product.getProductId() 
										+ " | attributeProductId: " + product.getAttributeProductId());
					continue;
				}
				mainProduct.setHasChildren(true);
				
				// copy properties from main product
				product.setVisibility(mainProduct.getVisibility());
				product.setName(mainProduct.getName());
				product.setUrl(mainProduct.getUrl());
				product.setLinkRewrite(mainProduct.getLinkRewrite());
				product.setSupplierId(mainProduct.getSupplierId());
				product.setVatPercent(mainProduct.getVatPercent());
				product.setDateAdd(mainProduct.getDateAdd());
				product.setDateUpdate(mainProduct.getDateUpdate());
				
				product.setCode(resultSet.getString("reference"));
				product.setWholesaleNettoPrice(resultSet.getDouble("wholesale_price"));
				product.setNettoPrice(resultSet.getDouble("price"));
				
				product.setEan(resultSet.getString("ean13"));
				product.setWeight(resultSet.getDouble("weight"));
				
				if (psProductAttributeCombinationMap.get(product.getAttributeProductId()) == null)
				{
					log.debug("NULL FOR attributeProductId: " + product.getAttributeProductId());
					continue;
				}
				
				product.setAttributeId(psProductAttributeCombinationMap.get(product.getAttributeProductId()));
				product.setAttributeName(psAttributeLangMap.get(product.getAttributeId()));
				
				int attributeGroupIdValue = psAttributeMap.get(product.getAttributeId());
				String attrUrlName = psLayeredIndexableAttributeGroupLangValueMap.get(attributeGroupIdValue);
				if (attrUrlName != null && !attrUrlName.equals("")) 
				{
					attrUrlName = attrUrlName.replace("-", "_");
					product.setUrl(product.getUrl() + "#/" + product.getAttributeId() + "-" + attrUrlName);
				}
				
				product.setAttributeProductDefaultOn(resultSet.getInt("default_on"));
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void validateCodesAndSetupRealProductsMap(Shop shop) 
	{
		// Main products with children should have empty code and hasChildren
		// property is set on the basis of attribute products data
		List<String> allCodesList = new ArrayList<>();
		List<String> duplicateCodesList = new ArrayList<>();

		for (PrestaProduct mainProduct : mainProductsMap.values()) 
		{
			// add to real products only independent main products with code (enabled to buy)
			if (!mainProduct.isHasChildren()) 
			{
				// go next if invalid code
				if (!validateProductCode(mainProduct.getCode(), allCodesList, duplicateCodesList)) continue;
				realProductsMap.put(mainProduct.getCode(), mainProduct);
				
			} else {
				if (!mainProduct.getCode().equals("") && mainProduct.getCode() != null) 
				{
					log.debug("Main product with children and id: " + mainProduct.getProductId() + " should has no code ");
				}
			}
		}

		for (PrestaProduct attrProduct : attributeProductsMap.values()) 
		{
			// go next if invalid code
			if (!validateProductCode(attrProduct.getCode(), allCodesList, duplicateCodesList)) continue;
			realProductsMap.put(attrProduct.getCode(), attrProduct);
		}
		
		for (String duplicateCode : duplicateCodesList)
		{
			realProductsMap.remove(duplicateCode);
		}

	}
	
	private boolean validateProductCode(String code, List<String> allCodesList, List<String> duplicateCodesList)
	{
		if (allCodesList.indexOf(code) != -1) 
		{
			duplicateCodesList.add(code);
			return false;
		}
		allCodesList.add(code);
		
		boolean hasEmptyCode = code == null || code.equals("");
		if (hasEmptyCode) return false;
		
		boolean hasWhiteSpace = !code.trim().equals(code);
		if (hasWhiteSpace) return false;
		
		return true;
	}
	
	
	// ::: IMAGES :::
	
	private void setupMainProductImages(Shop shop) 
	{
		for (PrestaProduct product : mainProductsMap.values())
		{
			product.setPrestaProductImageSet(new HashSet<>());
		}
		
		for (Map.Entry<Integer, Integer> entry : psImageMap.entrySet())
		{
			PrestaProduct product = mainProductsMap.get(entry.getValue());
			// don't add all photos to main with children
			if (product == null || product.isHasChildren()) continue;
			String imgUrl = shop.getBaseUrl() + "/" + entry.getKey() + PHOTO_SIZE + "/" + product.getLinkRewrite() + ".jpg";
			PrestaProductImage prestaProductImage = new PrestaProductImage();
			prestaProductImage.setUrl(imgUrl);
			product.getPrestaProductImageSet().add(prestaProductImage);
		}
		
	}
	
	private void setupAttributeProductImages(Shop shop) 
	{
		for (PrestaProduct product : attributeProductsMap.values())
		{
			product.setPrestaProductImageSet(new HashSet<>());
		}
		
		log.debug(":: psProductAttributeImageList.size(): " + psProductAttributeImageList.size());
		int nullCounter = 0;
		// List instead Map used because id_image in ps_product_attribute_image table is not unique 
		// (in ps_image table id_image is unique)
		for (int i = 0; i < psProductAttributeImageList.size(); i++) 
		{
			int attrProductId = psProductAttributeImageList.get(i)[0];
			PrestaProduct product = attributeProductsMap.get(attrProductId);
			if (product == null) 
			{
				nullCounter++;
				continue;
			}
			String imgUrl = shop.getBaseUrl() + "/" + psProductAttributeImageList.get(i)[1] + PHOTO_SIZE + "/" 
							+ product.getLinkRewrite() + ".jpg";
			PrestaProductImage prestaProductImage = new PrestaProductImage();
			prestaProductImage.setUrl(imgUrl);
			product.getPrestaProductImageSet().add(prestaProductImage);
		}
		log.debug(":: nullCounter: " + nullCounter);
		
	}
	
	
	// ::: AVAILABILITY :::
	
	private void parsePsStockAvailable(ResultSet resultSet, Shop shop) 
	{
		// {"id_product", "id_product_attribute", "quantity"}
		try {
			while (resultSet.next()) 
			{
				int idProduct = resultSet.getInt("id_product");
				int idProductAttribute = resultSet.getInt("id_product_attribute");
				int quantity = resultSet.getInt("quantity");
				
				if (idProductAttribute == 0) 
				{
					PrestaProduct mainProduct = mainProductsMap.get(idProduct);
					if (mainProduct != null) mainProduct.setAvailability(quantity);
				} else {
					PrestaProduct attrProduct = attributeProductsMap.get(idProductAttribute);
					if (attrProduct != null) 
					{
						attrProduct.setAvailability(quantity);
						attrProduct.setVisibility(attrProduct.getAvailability() > 0 ? "both" : "none");
					}
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	// ::: PROPERTIES :::

	private void parsePsProductLang(ResultSet resultSet) 
	{
		// {"id_product", "name", "link_rewrite"}
		psProductLangNameMap = new HashMap<>();
		psProductLangLinkRewriteNameMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psProductLangNameMap.put(resultSet.getInt("id_product"), resultSet.getString("name"));
				psProductLangLinkRewriteNameMap.put(resultSet.getInt("id_product"), resultSet.getString("link_rewrite"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsSupplier(ResultSet resultSet) 
	{
		// {"id_supplier", "name"}
		psSupplierMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psSupplierMap.put(resultSet.getInt("id_supplier"), resultSet.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsTax(ResultSet resultSet) 
	{
		// {"id_tax", "rate"}
		psTaxMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psTaxMap.put(resultSet.getInt("id_tax"), resultSet.getDouble("rate"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsAttribute(ResultSet resultSet) 
	{
		// {"id_attribute", "id_attribute_group"}
		psAttributeMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psAttributeMap.put(resultSet.getInt("id_attribute"), resultSet.getInt("id_attribute_group"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsAttributeLang(ResultSet resultSet) 
	{
		// {"id_attribute", "name"}
		psAttributeLangMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psAttributeLangMap.put(resultSet.getInt("id_attribute"), resultSet.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsProductAttributeCombination(ResultSet resultSet) 
	{
		// {"id_product_attribute", "id_attribute"}
		psProductAttributeCombinationMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psProductAttributeCombinationMap.put(resultSet.getInt("id_product_attribute"), resultSet.getInt("id_attribute"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePssLayeredIndexableAttributeGroupLangValue(ResultSet resultSet) 
	{
		// {"id_attribute_group", "url_name"}
		psLayeredIndexableAttributeGroupLangValueMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psLayeredIndexableAttributeGroupLangValueMap.put(resultSet.getInt("id_attribute_group"), 
																resultSet.getString("url_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void parsePsProductAttributeImage(ResultSet resultSet) 
	{
		// {"id_image", "id_product_attribute"}
		psProductAttributeImageList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				Integer[] anArray = new Integer[2];
				anArray[0] = resultSet.getInt("id_product_attribute");
				anArray[1] = resultSet.getInt("id_image");
				psProductAttributeImageList.add(anArray);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePsImage(ResultSet resultSet) 
	{
		// {"id_image", "id_product"}
		psImageMap = new HashMap<>();
		try {
			while (resultSet.next()) {
				psImageMap.put(resultSet.getInt("id_image"), resultSet.getInt("id_product"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}