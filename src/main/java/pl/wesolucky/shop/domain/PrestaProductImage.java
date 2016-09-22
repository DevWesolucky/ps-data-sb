package pl.wesolucky.shop.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class PrestaProductImage 
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
    private int index;
	private String url;
	
	@ManyToOne
	@JsonIgnore
	private PrestaProduct prestaProduct;
	
	
	public PrestaProductImage()
	{
	}
	
	
	@Override
	public String toString()
	{
		String log = "id: " + this.id;
		log += " | url: " + this.url;
		return log;
	}
	
	
	// ::: GET / SET :::
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public PrestaProduct getPrestaProduct() {
		return prestaProduct;
	}

	public void setPrestaProduct(PrestaProduct prestaProduct) {
		this.prestaProduct = prestaProduct;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
