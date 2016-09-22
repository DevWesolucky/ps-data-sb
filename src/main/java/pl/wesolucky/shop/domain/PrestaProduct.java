package pl.wesolucky.shop.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class PrestaProduct 
{
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private int productId;
	private String code;
	
	private String supplierId;
	private String name;
	private String linkRewrite;
	private String url;
	
	private int attributeProductId;
	private int attributeProductDefaultOn;
	
	private int attributeId;
	private String attributeName = "";
	
	private String ean;
	private String producer = "";
	private double weight;
	
	private String visibility;
	private int availability;
	
	private double vatPercent;
	private double wholesaleNettoPrice;
	private double nettoPrice;
	
	private String dateAdd;
	private String dateUpdate;
	
	private long shopId;
	
	// ::: NO DB PROPERITES :::
	
	private boolean hasChildren = false;
	
	
	
	@OneToMany (mappedBy = "prestaProduct")
	private Set<PrestaProductImage> prestaProductImageSet;
	
	
	public PrestaProduct()
	{
		
	}
	


	@Override
	public String toString()
	{
		String log = "";
		log += "id: " + id;
		log += " | productId: " + productId;
		log += " | attrProdId: " + attributeProductId;
		log += " | code: " + code;
//		log += " | supplierId: " + supplierId;
		log += " | name: " + name;
		log += " | availability: " + availability;
//		if (attributeProducts != null) log += " | attributeProducts size: " + attributeProducts.size();
		
		if (attributeProductId > 0)
		{
			log += " | attributeId: " + attributeId;
			log += " | attributeName: " + attributeName;
		}
		return log;
	}

	
	// ::: GET SET :::
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSupplierId() {
		return supplierId;
	}


	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getLinkRewrite() {
		return linkRewrite;
	}


	public void setLinkRewrite(String linkRewrite) {
		this.linkRewrite = linkRewrite;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public int getAttributeProductId() {
		return attributeProductId;
	}


	public void setAttributeProductId(int attributeProductId) {
		this.attributeProductId = attributeProductId;
	}


	public int getAttributeProductDefaultOn() {
		return attributeProductDefaultOn;
	}


	public void setAttributeProductDefaultOn(int attributeProductDefaultOn) {
		this.attributeProductDefaultOn = attributeProductDefaultOn;
	}


	public int getAttributeId() {
		return attributeId;
	}


	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}


	public String getAttributeName() {
		return attributeName;
	}


	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}


	public String getEan() {
		return ean;
	}


	public void setEan(String ean) {
		this.ean = ean;
	}


	public String getProducer() {
		return producer;
	}


	public void setProducer(String producer) {
		this.producer = producer;
	}


	public double getWeight() {
		return weight;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}


	public String getVisibility() {
		return visibility;
	}


	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}


	public int getAvailability() {
		return availability;
	}


	public void setAvailability(int availability) {
		this.availability = availability;
	}


	public double getVatPercent() {
		return vatPercent;
	}


	public void setVatPercent(double vatPercent) {
		this.vatPercent = vatPercent;
	}


	public double getWholesaleNettoPrice() {
		return wholesaleNettoPrice;
	}


	public void setWholesaleNettoPrice(double wholesaleNettoPrice) {
		this.wholesaleNettoPrice = wholesaleNettoPrice;
	}


	public double getNettoPrice() {
		return nettoPrice;
	}


	public void setNettoPrice(double nettoPrice) {
		this.nettoPrice = nettoPrice;
	}


	public String getDateAdd() {
		return dateAdd;
	}


	public void setDateAdd(String dateAdd) {
		this.dateAdd = dateAdd;
	}


	public String getDateUpdate() {
		return dateUpdate;
	}


	public void setDateUpdate(String dateUpdate) {
		this.dateUpdate = dateUpdate;
	}


	public boolean isHasChildren() {
		return hasChildren;
	}


	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}


	public long getShopId() {
		return shopId;
	}


	public void setShopId(long shopId) {
		this.shopId = shopId;
	}

	public Set<PrestaProductImage> getPrestaProductImageSet() {
		return prestaProductImageSet;
	}


	public void setPrestaProductImageSet(Set<PrestaProductImage> prestaProductImageSet) {
		this.prestaProductImageSet = prestaProductImageSet;
	}

}
