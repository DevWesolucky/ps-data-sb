package pl.wesolucky.shop.domain;


public class ShopDTO 
{
	public long id;
	public String domain;
	
	public ShopDTO(Shop shop)
	{
		this.id = shop.getId();
		this.domain = shop.getDomain();
	}
	
}
