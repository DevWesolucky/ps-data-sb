package pl.wesolucky.shop.presta;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import pl.wesolucky.shop.domain.PrestaProduct;


public interface PrestaProductRepository extends CrudRepository<PrestaProduct, Long>
{
	
	PrestaProduct findByProductIdAndAttributeProductId(long productId, int attributeProductId);
	List<PrestaProduct> findByProductId(int productId);
	List<PrestaProduct> findByShopId(long shopId);
	
}
